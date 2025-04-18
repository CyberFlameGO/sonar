/*
 * Copyright (C) 2025 Sonar Contributors
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

package xyz.jonesdev.sonar.velocity.fallback;

import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.protocol.packet.HandshakePacket;
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.sonar.common.fallback.FallbackInboundHandlerAdapter;

import java.net.InetSocketAddress;

import static xyz.jonesdev.sonar.common.fallback.protocol.packets.handshake.HandshakePacket.STATUS;

final class FallbackVelocityInboundHandler extends FallbackInboundHandlerAdapter {

  @Override
  public void channelRead(final @NotNull ChannelHandlerContext ctx, final Object msg) throws Exception {
    if (msg instanceof HandshakePacket handshake) {
      // We don't care about server pings; remove the handler
      if (handshake.getNextStatus() == STATUS) {
        ctx.pipeline().remove(this);
      } else {
        handleHandshake(ctx, handshake.getServerAddress(), handshake.getProtocolVersion().getProtocol());
      }
    } else if (msg instanceof ServerLoginPacket serverLogin) {
      // Deject this pipeline and let Sonar process the login packet
      ctx.pipeline().remove(this);
      // Make sure to use the potentially modified, original IP
      final MinecraftConnection minecraftConnection = ctx.pipeline().get(MinecraftConnection.class);
      final InetSocketAddress socketAddress = (InetSocketAddress) minecraftConnection.getRemoteAddress();
      handleLogin(ctx, () -> ctx.fireChannelRead(msg), serverLogin.getUsername(), socketAddress);
      return;
    }
    ctx.fireChannelRead(msg);
  }
}
