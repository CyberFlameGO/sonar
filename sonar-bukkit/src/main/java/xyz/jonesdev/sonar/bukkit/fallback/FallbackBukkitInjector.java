/*
 * Copyright (C) 2023-2024 Sonar Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.jonesdev.sonar.bukkit.fallback;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.sonar.api.ReflectiveOperationException;
import xyz.jonesdev.sonar.common.fallback.netty.FallbackInjectedChannelInitializer;

import java.lang.reflect.Field;
import java.util.List;

import static xyz.jonesdev.sonar.api.fallback.FallbackPipelines.FALLBACK_PACKET_DECODER;

// Check out these links if you want to see some more magic
// https://github.com/retrooper/packetevents/blob/2.0/spigot/src/main/java/io/github/retrooper/packetevents/util/SpigotReflectionUtil.java
// https://github.com/dmulloy2/ProtocolLib/blob/master/TinyProtocol/src/main/java/com/comphenix/tinyprotocol/TinyProtocol.java
// https://github.com/ViaVersion/ViaVersion/blob/master/bukkit/src/main/java/com/viaversion/viaversion/bukkit/handlers/BukkitChannelInitializer.java
@UtilityClass
public class FallbackBukkitInjector {
  private final String LEGACY_NMS_PACKAGE;
  private final String OBC_PACKAGE;

  private final BukkitServerVersion SERVER_VERSION;
  private final boolean USES_LEGACY_PACKAGING, OBFUSCATED;

  private final Class<?> MINECRAFT_SERVER_CLASS;
  private final Class<?> CRAFTBUKKIT_SERVER_CLASS;
  private final Class<?> SERVER_CONNECTION_CLASS;

  private final Object MINECRAFT_SERVER_CONNECTION_INSTANCE;

  static {
    SERVER_VERSION = resolveServerVersion();
    USES_LEGACY_PACKAGING = SERVER_VERSION.compareTo(BukkitServerVersion.MINECRAFT_1_17) < 0;

    try {
      final String bukkitPackage = Bukkit.getServer().getClass().getPackage().getName();

      String modifiedPackageName;
      if (SERVER_VERSION.compareTo(BukkitServerVersion.MINECRAFT_1_20_5) >= 0) {
        // Example: org.bukkit.craftbukkit
        modifiedPackageName = bukkitPackage.split("\\.")[2];
      } else {
        // Example: org.bukkit.craftbukkit.v1_16_R3
        modifiedPackageName = bukkitPackage.split("\\.")[3];
      }

      LEGACY_NMS_PACKAGE = "net.minecraft.server." + modifiedPackageName + ".";
      OBC_PACKAGE = bukkitPackage + ".";

      OBFUSCATED = isObfuscated();
      // Minecraft classes
      MINECRAFT_SERVER_CLASS = getNMSClass("server.MinecraftServer", "MinecraftServer");
      SERVER_CONNECTION_CLASS = getNMSClass(OBFUSCATED ? "server.network.ServerConnection" : "server.network.ServerConnectionListener", "ServerConnection");
      // CraftBukkit classes
      CRAFTBUKKIT_SERVER_CLASS = getOBCClass("CraftServer");

      Object minecraftServerInstance;
      try {
        // 1.20.5+
        minecraftServerInstance = getFieldAt(MINECRAFT_SERVER_CLASS, MINECRAFT_SERVER_CLASS, 0).get(null);
      } catch (Exception exception) {
        minecraftServerInstance = getFieldAt(CRAFTBUKKIT_SERVER_CLASS, MINECRAFT_SERVER_CLASS, 0).get(Bukkit.getServer());
      }

      MINECRAFT_SERVER_CONNECTION_INSTANCE = getFieldAt(MINECRAFT_SERVER_CLASS, SERVER_CONNECTION_CLASS, 0).get(minecraftServerInstance);
    } catch (Exception exception) {
      throw new ReflectiveOperationException(exception);
    }
  }

  private @NotNull Field getFieldAt(final @NotNull Class<?> clazz, final @NotNull Class<?> type, final int index) {
    int currentIndex = 0;
    for (final Field field : clazz.getDeclaredFields()) {
      field.setAccessible(true);

      if (type.isAssignableFrom(field.getType())) {
        if (currentIndex++ == index) {
          return field;
        }
      }
    }

    if (clazz.getSuperclass() != null) {
      return getFieldAt(clazz.getSuperclass(), type, index);
    }
    throw new IllegalStateException("Could not find field #" + index + " in " + clazz.getName());
  }

  private @NotNull BukkitServerVersion resolveServerVersion() {
    final String bukkitVersion = Bukkit.getBukkitVersion();

    for (final BukkitServerVersion serverVersion : BukkitServerVersion.REVERSED_VALUES) {
      if (bukkitVersion.contains(serverVersion.getRelease())) {
        return serverVersion;
      }
    }

    // Throw an error if we cannot find the server version
    throw new IllegalStateException("Could not find server version " + bukkitVersion);
  }

  private boolean isObfuscated() {
    try {
      Class.forName("net.minecraft.server.network.PlayerConnection");
      return true;
    } catch (ClassNotFoundException exception) {
      return false;
    }
  }

  private @NotNull Class<?> getNMSClass(final String modern, final String legacy) throws ClassNotFoundException {
    return Class.forName(USES_LEGACY_PACKAGING ? LEGACY_NMS_PACKAGE + legacy : "net.minecraft." + modern);
  }

  private @NotNull Class<?> getOBCClass(final String clazz) throws ClassNotFoundException {
    return Class.forName(OBC_PACKAGE + clazz);
  }

  @SuppressWarnings("unchecked")
  public void inject() {
    try {
      for (int i = 0; i < 2; i++) {
        final Field field = getFieldAt(SERVER_CONNECTION_CLASS, List.class, i);

        // Check if the field has the correct generic type;
        // We need this field to be List<ChannelFuture>
        if (!field.getGenericType().getTypeName().contains(ChannelFuture.class.getName())) {
          continue;
        }

        final var list = (List<?>) field.get(MINECRAFT_SERVER_CONNECTION_INSTANCE);

        for (final Object object : list) {
          final ChannelFuture channelFuture = (ChannelFuture) object;
          final List<String> names = channelFuture.channel().pipeline().names();

          ChannelHandler bootstrap = null;

          for (final String name : names) {
            try {
              final ChannelHandler handler = channelFuture.channel().pipeline().get(name);
              // Inline get method
              final Field childHandlerField = handler.getClass().getDeclaredField("childHandler");
              childHandlerField.setAccessible(true);
              final var childHandler = (ChannelInitializer<?>) childHandlerField.get(handler);

              if (childHandler != null) {
                bootstrap = handler;
              }
              break;
            } catch (Exception exception) {
              // Ignore this one
            }
          }

          if (bootstrap == null) {
            bootstrap = channelFuture.channel().pipeline().first();
          }

          final Field childHandlerField = bootstrap.getClass().getDeclaredField("childHandler");
          childHandlerField.setAccessible(true);
          final var originalInitializer = (ChannelInitializer<Channel>) childHandlerField.get(bootstrap);

          childHandlerField.set(bootstrap, new FallbackInjectedChannelInitializer(originalInitializer,
            pipeline -> pipeline.addAfter("splitter", FALLBACK_PACKET_DECODER, new FallbackBukkitInboundHandler())));
        }
        return;
      }
    } catch (Exception exception) {
      throw new ReflectiveOperationException(exception);
    }
  }
}
