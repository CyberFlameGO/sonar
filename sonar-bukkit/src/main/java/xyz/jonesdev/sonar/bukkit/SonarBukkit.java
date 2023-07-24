/*
 * Copyright (C) 2023 Sonar Contributors
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

package xyz.jonesdev.sonar.bukkit;

import lombok.Getter;
import org.bstats.bukkit.Metrics;
import xyz.jonesdev.sonar.api.Sonar;
import xyz.jonesdev.sonar.api.SonarPlatform;
import xyz.jonesdev.sonar.api.SonarSupplier;
import xyz.jonesdev.sonar.api.config.SonarConfiguration;
import xyz.jonesdev.sonar.api.database.DatabaseType;
import xyz.jonesdev.sonar.api.logger.Logger;
import xyz.jonesdev.sonar.bukkit.command.SonarCommand;
import xyz.jonesdev.sonar.bukkit.verbose.ActionBarVerbose;
import xyz.jonesdev.sonar.common.SonarBootstrap;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.logging.Level;

import static xyz.jonesdev.sonar.api.database.Database.IP_COLUMN;
import static xyz.jonesdev.sonar.api.database.Database.VERIFIED_TABLE;

public enum SonarBukkit implements Sonar, SonarBootstrap<SonarBukkitPlugin> {

  INSTANCE;

  @Getter
  private SonarBukkitPlugin plugin;

  @Getter
  private ActionBarVerbose actionBarVerbose;

  @Getter
  private SonarConfiguration config;

  @Getter
  private File pluginDataFolder;

  @Getter
  private final Logger logger = new Logger() {

    @Override
    public void info(final String message, final Object... args) {
      plugin.getLogger().log(Level.INFO, message, args);
    }

    @Override
    public void warn(final String message, final Object... args) {
      plugin.getLogger().log(Level.WARNING, message, args);
    }

    @Override
    public void error(final String message, final Object... args) {
      plugin.getLogger().log(Level.SEVERE, message, args);
    }
  };

  @Getter
  private final DecimalFormat formatter = new DecimalFormat("#,###");

  @Override
  public SonarPlatform getPlatform() {
    return SonarPlatform.BUKKIT;
  }

  @Override
  public void enable(final SonarBukkitPlugin plugin) {
    this.plugin = plugin;

    final long start = System.currentTimeMillis();

    // Set the API to this class
    SonarSupplier.set(this);

    logger.info("Initializing Sonar...");

    pluginDataFolder = plugin.getDataFolder();

    // Initialize configuration
    config = new SonarConfiguration(plugin.getDataFolder());
    reload();

    // Initialize bStats.org metrics
    new Metrics(plugin, getServiceId());

    // Register Sonar command
    Objects.requireNonNull(plugin.getCommand("sonar")).setExecutor(new SonarCommand());

    // Initialize action bar verbose
    actionBarVerbose = new ActionBarVerbose(plugin.getServer());

    // Register action bar verbose task
    plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, actionBarVerbose::update,
      100L, 100L);

    // Done
    final long startDelay = System.currentTimeMillis() - start;

    logger.info("Done (" + String.format("%.3f", startDelay / 1000D) + "s)!");
  }

  @Override
  public void disable() {
    getLogger().info("Starting shutdown process...");

    if (getConfig().DATABASE != DatabaseType.NONE) {
      getLogger().info("[database] Saving entries to database...");

      // We need to clear the table because we don't want any IPs that aren't present
      // or have been manually removed to still be present in the database
      getDatabase().clear(VERIFIED_TABLE);
      getDatabase().addListToTable(VERIFIED_TABLE, IP_COLUMN, getFallback().getVerified());

      // Dispose the database instance
      getDatabase().dispose();

      getLogger().info("[database] Done.");
    }

    getLogger().info("Successfully shut down.");
  }

  @Override
  public void reload() {
    getConfig().load();

    if (getConfig().DATABASE != DatabaseType.NONE) {
      getLogger().info("[database] Initializing database...");
      getDatabase().initialize(getConfig());

      // Clear all verified IPs from memory to avoid issues with the database
      if (!getFallback().getVerified().isEmpty()) {
        getLogger().info("[database] Cleaning verified IPs from memory...");
        getFallback().getVerified().clear();
      }

      // Load all blacklisted and verified IPs from the database
      getLogger().info("[database] Loading verified IPs from the database...");
      getFallback().getVerified().addAll(getDatabase().getListFromTable(VERIFIED_TABLE, IP_COLUMN));
    }
  }
}
