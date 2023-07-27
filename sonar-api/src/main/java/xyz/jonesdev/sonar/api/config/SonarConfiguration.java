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

package xyz.jonesdev.sonar.api.config;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.sonar.api.database.DatabaseType;
import xyz.jonesdev.sonar.api.yaml.SimpleYamlConfig;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SonarConfiguration {
  @Getter
  private final SimpleYamlConfig generalConfig;
  private final SimpleYamlConfig messagesConfig;

  public SonarConfiguration(final @NotNull File folder) {
    generalConfig = new SimpleYamlConfig(folder, "config");
    messagesConfig = new SimpleYamlConfig(folder, "messages");
  }

  public String PREFIX;
  public String SUPPORT_URL;

  public String ACTION_BAR_LAYOUT;
  public Collection<String> ANIMATION;

  public boolean ENABLE_VERIFICATION;
  public boolean CHECK_GRAVITY;
  public boolean CHECK_COLLISIONS;
  public boolean LOG_CONNECTIONS;
  public Pattern VALID_NAME_REGEX;
  public Pattern VALID_BRAND_REGEX;
  public int MAXIMUM_BRAND_LENGTH;
  public int MAXIMUM_MOVEMENT_TICKS;
  public int MINIMUM_PLAYERS_FOR_ATTACK;
  public int MAXIMUM_VERIFYING_PLAYERS;
  public int MAXIMUM_ONLINE_PER_IP;
  public int MAXIMUM_QUEUED_PLAYERS;
  public int MAXIMUM_QUEUE_POLLS;
  public int MAXIMUM_LOGIN_PACKETS;
  public int VERIFICATION_TIMEOUT;
  public int VERIFICATION_READ_TIMEOUT;
  public int VERIFICATION_DELAY;

  public boolean LOG_DURING_ATTACK;

  public String HEADER, FOOTER;
  public String TOO_MANY_PLAYERS;
  public String TOO_FAST_RECONNECT;
  public String TOO_MANY_ONLINE_PER_IP;
  public String INVALID_USERNAME;
  public String VERIFICATION_SUCCESS;
  public String VERIFICATION_FAILED;
  public String ALREADY_VERIFYING;
  public String ALREADY_QUEUED;
  public String BLACKLISTED;
  public String UNEXPECTED_ERROR;

  public String INCORRECT_COMMAND_USAGE;
  public String INCORRECT_IP_ADDRESS;
  public String SUB_COMMAND_NO_PERM;
  public String ILLEGAL_IP_ADDRESS;
  public String PLAYERS_ONLY;
  public String CONSOLE_ONLY;
  public String COMMAND_COOL_DOWN;
  public String COMMAND_COOL_DOWN_LEFT;

  public String BLACKLIST_EMPTY;
  public String BLACKLIST_ADD;
  public String BLACKLIST_DUPLICATE;
  public String BLACKLIST_NOT_FOUND;
  public String BLACKLIST_REMOVE;
  public String BLACKLIST_CLEARED;
  public String BLACKLIST_SIZE;

  public String WHITELIST_ADD;
  public String WHITELIST_DUPLICATE;
  public String WHITELIST_NOT_FOUND;
  public String WHITELIST_REMOVE;
  public String WHITELIST_SIZE;

  public String VERBOSE_SUBSCRIBED;
  public String VERBOSE_UNSUBSCRIBED;
  public String VERBOSE_SUBSCRIBED_OTHER;
  public String VERBOSE_UNSUBSCRIBED_OTHER;
  public String RELOADING;
  public String RELOADED;

  public boolean LOCKDOWN_ENABLED;
  public boolean LOCKDOWN_ENABLE_NOTIFY;
  public boolean LOCKDOWN_LOG_ATTEMPTS;
  public String LOCKDOWN_DISCONNECT;
  public String LOCKDOWN_ACTIVATED;
  public String LOCKDOWN_DEACTIVATED;
  public String LOCKDOWN_NOTIFICATION;
  public String LOCKDOWN_CONSOLE_LOG;

  public DatabaseType DATABASE;
  public boolean ALLOW_PURGING;
  public String DATABASE_FILE_NAME;
  public String DATABASE_URL;
  public String DATABASE_NAME;
  public String DATABASE_USERNAME;
  public String DATABASE_PASSWORD;
  public int DATABASE_PORT;
  public int DATABASE_QUERY_LIMIT;

  public String DATABASE_PURGE_DISALLOWED;
  public String DATABASE_PURGE_CONFIRM;
  public String DATABASE_PURGE;
  public String DATABASE_PURGE_ALREADY;
  public String DATABASE_NOT_SELECTED;
  public String DATABASE_RELOADING;
  public String DATABASE_RELOADED;

  public void load() {
    Objects.requireNonNull(generalConfig);

    generalConfig.load();
    messagesConfig.load();

    // Message settings
    messagesConfig.getYaml().setComment("messages.prefix",
      "Placeholder for every '%prefix%' in this configuration file"
    );
    PREFIX = formatString(messagesConfig.getString("messages.prefix", "&e&lSonar &7» &f"));

    messagesConfig.getYaml().setComment("messages.support-url",
      "Placeholder for every '%support-url%' in this configuration file"
    );
    SUPPORT_URL = messagesConfig.getString("messages.support-url", "https://jonesdev.xyz/discord/");

    // General options
    generalConfig.getYaml().setComment("general.max-online-per-ip",
      "Maximum number of players online with the same IP address"
    );
    MAXIMUM_ONLINE_PER_IP = clamp(generalConfig.getInt("general.max-online-per-ip", 3), 1, Byte.MAX_VALUE);

    generalConfig.getYaml().setComment("general.min-players-for-attack",
      "Minimum number of new players in order for an attack to be detected"
    );
    MINIMUM_PLAYERS_FOR_ATTACK = clamp(generalConfig.getInt("general.min-players-for-attack", 5), 2, 1024);

    // Lockdown
    generalConfig.getYaml().setComment("general.lockdown.enabled",
      "Should Sonar prevent players from joining the server?"
    );
    LOCKDOWN_ENABLED = generalConfig.getBoolean("general.lockdown.enabled", false);

    generalConfig.getYaml().setComment("general.lockdown.log-attempts",
      "Should Sonar should log login attempts during lockdown?"
    );
    LOCKDOWN_LOG_ATTEMPTS = generalConfig.getBoolean("general.lockdown.log-attempts", true);

    generalConfig.getYaml().setComment("general.lockdown.notify-admins",
      "Should Sonar notify admins when they join the server during lockdown?"
    );
    LOCKDOWN_ENABLE_NOTIFY = generalConfig.getBoolean("general.lockdown.notify-admins", true);

    // Database
    generalConfig.getYaml().setComment("general.database.type",
      "The database can either be NONE, MYSQL or YAML"
    );
    DATABASE = DatabaseType.valueOf(generalConfig.getString("general.database.type", "NONE").toUpperCase());

    generalConfig.getYaml().setComment("general.database.allow-purging",
      "Should Sonar allow database purges?"
    );
    ALLOW_PURGING = generalConfig.getBoolean("general.database.allow-purging", true);

    // YAML
    generalConfig.getYaml().setComment("general.database.yaml.file-name",
      "YAML database file name"
    );
    DATABASE_FILE_NAME = generalConfig.getString("general.database.yaml.file-name", "database");

    // MySQL
    generalConfig.getYaml().setComment("general.database.mysql.name",
      "MySQL database name"
    );
    DATABASE_NAME = generalConfig.getString("general.database.mysql.name", "sonar");

    generalConfig.getYaml().setComment("general.database.mysql.url",
      "MySQL database URL"
    );
    DATABASE_URL = generalConfig.getString("general.database.mysql.url", "localhost");

    generalConfig.getYaml().setComment("general.database.mysql.port",
      "MySQL database port"
    );
    DATABASE_PORT = clamp(generalConfig.getInt("general.database.mysql.port", 3306), 0, 65535);

    generalConfig.getYaml().setComment("general.database.mysql.username",
      "MySQL database username"
    );
    DATABASE_USERNAME = generalConfig.getString("general.database.mysql.username", "root");

    generalConfig.getYaml().setComment("general.database.mysql.password",
      "MySQL database password"
    );
    DATABASE_PASSWORD = generalConfig.getString("general.database.mysql.password", "");

    generalConfig.getYaml().setComment("general.database.mysql.query-limit",
      "Maximum number of database entries"
    );
    DATABASE_QUERY_LIMIT = clamp(generalConfig.getInt("general.database.mysql.query-limit", 100000), 1000,
      Integer.MAX_VALUE);

    // Queue
    generalConfig.getYaml().setComment("general.queue.max-players",
      "Maximum number of players on the queue"
    );
    MAXIMUM_QUEUED_PLAYERS = clamp(generalConfig.getInt("general.queue.max-players", 8192), 128, Short.MAX_VALUE);

    generalConfig.getYaml().setComment("general.queue.max-polls",
      "Maximum number of queue polls per 500 milliseconds"
    );
    MAXIMUM_QUEUE_POLLS = clamp(generalConfig.getInt("general.queue.max-polls", 10), 1, 1000);

    // Verification
    generalConfig.getYaml().setComment("general.verification.enabled",
      "Should Sonar verify new players? (Recommended)"
    );
    ENABLE_VERIFICATION = generalConfig.getBoolean("general.verification.enabled", true);

    generalConfig.getYaml().setComment("general.verification.check-gravity",
      "Should Sonar check for valid client gravity? (Recommended)"
    );
    CHECK_GRAVITY = generalConfig.getBoolean("general.verification.check-gravity", true);

    generalConfig.getYaml().setComment("general.verification.check-collisions",
      "Should Sonar check for valid client collisions? (Recommended)"
    );
    CHECK_COLLISIONS = generalConfig.getBoolean("general.verification.check-collisions", true);

    generalConfig.getYaml().setComment("general.verification.log-connections",
      "Should Sonar log new connections?"
    );
    LOG_CONNECTIONS = generalConfig.getBoolean("general.verification.log-connections", true);

    generalConfig.getYaml().setComment("general.verification.log-during-attack",
      "Should Sonar log new connections during an attack?"
    );
    LOG_DURING_ATTACK = generalConfig.getBoolean("general.verification.log-during-attack", false);

    generalConfig.getYaml().setComment("general.verification.valid-name-regex",
      "Regex for validating usernames during verification"
    );
    VALID_NAME_REGEX = Pattern.compile(generalConfig.getString(
      "general.verification.valid-name-regex", "^[a-zA-Z0-9_.*!]+$"
    ));

    generalConfig.getYaml().setComment("general.verification.valid-brand-regex",
      "Regex for validating client brands during verification"
    );
    VALID_BRAND_REGEX = Pattern.compile(generalConfig.getString(
      "general.verification.valid-brand-regex", "^[a-zA-Z0-9-/.,:;_()\\[\\]{}!?' *]+$"
    ));

    generalConfig.getYaml().setComment("general.verification.max-brand-length",
      "Maximum client brand length during verification"
    );
    MAXIMUM_BRAND_LENGTH = generalConfig.getInt("general.verification.max-brand-length", 64);

    generalConfig.getYaml().setComment("general.verification.timeout",
      "Amount of time that has to pass before a player is disconnected"
    );
    VERIFICATION_TIMEOUT = clamp(generalConfig.getInt("general.verification.timeout", 10000), 1500, 30000);

    generalConfig.getYaml().setComment("general.verification.read-timeout",
      "Amount of time that has to pass before a player times out"
    );
    VERIFICATION_READ_TIMEOUT = clamp(generalConfig.getInt("general.verification.read-timeout", 4000), 500, 30000);

    generalConfig.getYaml().setComment("general.verification.max-login-packets",
      "Maximum number of login packets the player has to send in order to be kicked"
    );
    MAXIMUM_LOGIN_PACKETS = clamp(generalConfig.getInt("general.verification.max-login-packets", 256), 128, 8192);

    generalConfig.getYaml().setComment("general.verification.max-movement-ticks",
      "Maximum number of movement packets the player has to send in order to be verified"
    );
    MAXIMUM_MOVEMENT_TICKS = clamp(generalConfig.getInt("general.verification.max-movement-ticks", 8), 2, 100);

    generalConfig.getYaml().setComment("general.verification.max-players",
      "Maximum number of players verifying at the same time"
    );
    MAXIMUM_VERIFYING_PLAYERS = clamp(generalConfig.getInt("general.verification.max-players", 1024), 1,
      Short.MAX_VALUE);

    generalConfig.getYaml().setComment("general.verification.rejoin-delay",
      "Minimum number of rejoin delay during verification"
    );
    VERIFICATION_DELAY = clamp(generalConfig.getInt("general.verification.rejoin-delay", 8000), 0, 100000);

    // load this here otherwise it could cause issues
    messagesConfig.getYaml().setComment("messages.header",
      "Placeholder for every '%header%' in this configuration file"
    );
    HEADER = fromList(messagesConfig.getStringList("messages.header",
      Arrays.asList(
        "&e&lSonar"
      )));

    messagesConfig.getYaml().setComment("messages.footer",
      "Placeholder for every '%footer%' in this configuration file"
    );
    FOOTER = fromList(messagesConfig.getStringList("messages.footer",
      Arrays.asList(
        "&7If you believe that this is an error, contact an administrator."
      )));

    messagesConfig.getYaml().setComment("messages.lockdown.enabled",
      "Message that is shown when a player enables server lockdown"
    );
    LOCKDOWN_ACTIVATED = formatString(messagesConfig.getString("messages.lockdown.enabled",
      "%prefix%The server is now in lockdown mode."
    ));

    messagesConfig.getYaml().setComment("messages.lockdown.disabled",
      "Message that is shown when a player disables server lockdown"
    );
    LOCKDOWN_DEACTIVATED = formatString(messagesConfig.getString("messages.lockdown.disabled",
      "%prefix%The server is no longer in lockdown mode."
    ));

    messagesConfig.getYaml().setComment("messages.lockdown.notification",
      "Message that is shown when an admin joins the server during lockdown"
    );
    LOCKDOWN_NOTIFICATION = formatString(messagesConfig.getString("messages.lockdown.notification",
      "%prefix%&aHey, the server is currently in lockdown mode. If you want to disable the lockdown mode, " +
        "type " +
        "&f/sonar" +
        " lockdown&a."
    ));

    messagesConfig.getYaml().setComment("messages.lockdown.console-log",
      "Message that is shown to console when a normal player tries joining the server during lockdown"
    );
    LOCKDOWN_CONSOLE_LOG = messagesConfig.getString("messages.lockdown.console-log",
      "%player% (%ip%, %protocol%) tried to join during lockdown mode."
    );

    messagesConfig.getYaml().setComment("messages.lockdown.disconnect-message",
      "Message that is shown to a normal player when they try joining the server during lockdown"
    );
    LOCKDOWN_DISCONNECT = fromList(messagesConfig.getStringList("messages.lockdown.disconnect-message",
      Arrays.asList(
        "%header%",
        "&cThe server is currently locked down, please try again later.",
        "%footer%"
      )));

    messagesConfig.getYaml().setComment("messages.reload.start",
      "Message that is shown when someone starts reloading Sonar"
    );
    RELOADING = formatString(messagesConfig.getString("messages.reload.start",
      "%prefix%Reloading Sonar..."
    ));

    messagesConfig.getYaml().setComment("messages.reload.finish",
      "Message that is shown when Sonar has finished reloading"
    );
    RELOADED = formatString(messagesConfig.getString("messages.reload.finish",
      "%prefix%&aSuccessfully reloaded &7(%taken%ms)"
    ));

    messagesConfig.getYaml().setComment("messages.verbose.subscribed",
      "Message that is shown when a player subscribes to Sonar verbose"
    );
    VERBOSE_SUBSCRIBED = formatString(messagesConfig.getString("messages.verbose.subscribed",
      "%prefix%You are now viewing Sonar verbose."
    ));

    messagesConfig.getYaml().setComment("messages.verbose.unsubscribed",
      "Message that is shown when a player unsubscribes from Sonar verbose"
    );
    VERBOSE_UNSUBSCRIBED = formatString(messagesConfig.getString("messages.verbose.unsubscribed",
      "%prefix%You are no longer viewing Sonar verbose."
    ));

    messagesConfig.getYaml().setComment("messages.verbose.subscribed-other",
      "Message that is shown when a player makes another player subscribe to Sonar verbose"
    );
    VERBOSE_SUBSCRIBED_OTHER = formatString(messagesConfig.getString("messages.verbose.subscribed-other",
      "%prefix%%player% is now viewing Sonar verbose."
    ));

    messagesConfig.getYaml().setComment("messages.verbose.unsubscribed-other",
      "Message that is shown when a player makes another player unsubscribe from Sonar verbose"
    );
    VERBOSE_UNSUBSCRIBED_OTHER = formatString(messagesConfig.getString("messages.verbose.unsubscribed-other",
      "%prefix%%player% is no longer viewing Sonar verbose."
    ));

    messagesConfig.getYaml().setComment("messages.database.disallowed",
      "Message that is shown when someone tries purging the database when it is disallowed"
    );
    DATABASE_PURGE_DISALLOWED = formatString(messagesConfig.getString("messages.database.disallowed",
      "%prefix%&cPurging the database is currently disallowed. Therefore, your action has been cancelled."
    ));

    messagesConfig.getYaml().setComment("messages.database.purge-confirm",
      "Message that is shown when someone tries purging the database and has to confirm their action"
    );
    DATABASE_PURGE_CONFIRM = formatString(messagesConfig.getString("messages.database.purge-confirm",
      "%prefix%&cPlease confirm that you want to delete all database entries by typing &7/sonar database " +
        "purge " +
        "confirm&c."
    ));

    messagesConfig.getYaml().setComment("messages.database.purged",
      "Message that is shown when the database was successfully purged"
    );
    DATABASE_PURGE = formatString(messagesConfig.getString("messages.database.purged",
      "%prefix%&aSuccessfully purged all database entries."
    ));

    messagesConfig.getYaml().setComment("messages.database.already-purging",
      "Message that is shown when the database is already being purged"
    );
    DATABASE_PURGE_ALREADY = formatString(messagesConfig.getString("messages.database.already-purging",
      "%prefix%&cThere is already a purge currently running."
    ));

    messagesConfig.getYaml().setComment("messages.database.not-selected",
      "Message that is shown when no database is configured"
    );
    DATABASE_NOT_SELECTED = formatString(messagesConfig.getString("messages.database.not-selected",
      "%prefix%&cYou have not selected any data storage type."
    ));

    messagesConfig.getYaml().setComment("messages.database.reload.start",
      "Message that is shown when someone starts reloading the database"
    );
    DATABASE_RELOADING = formatString(messagesConfig.getString("messages.database.reload.start",
      "%prefix%Reloading all databases..."
    ));

    messagesConfig.getYaml().setComment("messages.database.reload.finish",
      "Message that is shown when the database has finished reloading"
    );
    DATABASE_RELOADED = formatString(messagesConfig.getString("messages.database.reload.finish",
      "%prefix%&aSuccessfully reloaded &7(%taken%ms)"
    ));

    messagesConfig.getYaml().setComment("messages.incorrect-command-usage",
      "Message that is shown when someone uses a command incorrectly"
    );
    INCORRECT_COMMAND_USAGE = formatString(messagesConfig.getString("messages.incorrect-command-usage",
      "%prefix%&cUsage: /sonar %usage%"
    ));

    messagesConfig.getYaml().setComment("messages.invalid-ip-address",
      "Message that is shown when someone provides an invalid IP address (Invalid characters)"
    );
    INCORRECT_IP_ADDRESS = formatString(messagesConfig.getString("messages.invalid-ip-address",
      "%prefix%The IP address you provided seems to be invalid."
    ));

    messagesConfig.getYaml().setComment("messages.illegal-ip-address",
      "Message that is shown when someone provides an illegal IP address (Local IP)"
    );
    ILLEGAL_IP_ADDRESS = formatString(messagesConfig.getString("messages.illegal-ip-address",
      "%prefix%The IP address you provided seems to be either a local or loopback IP."
    ));

    messagesConfig.getYaml().setComment("messages.player-only",
      "Message that is shown when the console runs a command that is player-only"
    );
    PLAYERS_ONLY = formatString(messagesConfig.getString("messages.player-only",
      "%prefix%&cYou can only execute this command as a player."
    ));

    messagesConfig.getYaml().setComment("messages.console-only",
      "Message that is shown when a player runs a command that is console-only"
    );
    CONSOLE_ONLY = formatString(messagesConfig.getString("messages.console-only",
      "%prefix%&cFor security reasons, you can only execute this command through console."
    ));

    messagesConfig.getYaml().setComment("messages.command-cool-down",
      "Message that is shown when a player executes Sonar commands too quickly"
    );
    COMMAND_COOL_DOWN = formatString(messagesConfig.getString("messages.command-cool-down",
      "%prefix%&cYou can only execute this command every 0.5 seconds."
    ));
    COMMAND_COOL_DOWN_LEFT = formatString(messagesConfig.getString("messages.command-cool-down-left",
      "%prefix%&cPlease wait another &l%time-left%s&r&c."
    ));

    messagesConfig.getYaml().setComment("messages.sub-command-no-permission",
      "Message that is shown when a player does not have permission to execute a certain subcommand"
    );
    SUB_COMMAND_NO_PERM = formatString(messagesConfig.getString("messages.sub-command-no-permission",
      "%prefix%&cYou do not have permission to execute this subcommand. &7(%permission%)"
    ));

    messagesConfig.getYaml().setComment("messages.blacklist.empty",
      "Message that is shown when someone tries clearing the blacklist but is is empty"
    );
    BLACKLIST_EMPTY = formatString(messagesConfig.getString("messages.blacklist.empty",
      "%prefix%The blacklist is currently empty. Therefore, no IP addresses were removed from the blacklist."
    ));

    messagesConfig.getYaml().setComment("messages.blacklist.cleared",
      "Message that is shown when someone clears the blacklist"
    );
    BLACKLIST_CLEARED = formatString(messagesConfig.getString("messages.blacklist.cleared",
      "%prefix%You successfully removed a total of %removed% IP address(es) from the blacklist."
    ));

    messagesConfig.getYaml().setComment("messages.blacklist.size",
      "Message that is shown when someone checks the size of the blacklist"
    );
    BLACKLIST_SIZE = formatString(messagesConfig.getString("messages.blacklist.size",
      "%prefix%The blacklist currently contains %amount% IP address(es)."
    ));

    messagesConfig.getYaml().setComment("messages.blacklist.added",
      "Message that is shown when someone adds an IP address to the blacklist"
    );
    BLACKLIST_ADD = formatString(messagesConfig.getString("messages.blacklist.added",
      "%prefix%Successfully added %ip% to the blacklist."
    ));

    messagesConfig.getYaml().setComment("messages.blacklist.removed",
      "Message that is shown when someone removes an IP address from the blacklist"
    );
    BLACKLIST_REMOVE = formatString(messagesConfig.getString("messages.blacklist.removed",
      "%prefix%Successfully removed %ip% from the blacklist."
    ));

    messagesConfig.getYaml().setComment("messages.blacklist.duplicate-ip",
      "Message that is shown when someone adds an IP address to the blacklist but it is already blacklisted"
    );
    BLACKLIST_DUPLICATE = formatString(messagesConfig.getString("messages.blacklist.duplicate-ip",
      "%prefix%The IP address you provided is already blacklisted."
    ));

    messagesConfig.getYaml().setComment("messages.blacklist.ip-not-found",
      "Message that is shown when someone removes an IP address from the blacklist but it is not blacklisted"
    );
    BLACKLIST_NOT_FOUND = formatString(messagesConfig.getString("messages.blacklist.ip-not-found",
      "%prefix%The IP address you provided is not blacklisted."
    ));

    messagesConfig.getYaml().setComment("messages.whitelist.size",
      "Message that is shown when someone checks the size of the whitelist"
    );
    WHITELIST_SIZE = formatString(messagesConfig.getString("messages.whitelist.size",
      "%prefix%The whitelist currently contains %amount% IP address(es)."
    ));

    messagesConfig.getYaml().setComment("messages.whitelist.added",
      "Message that is shown when someone adds an IP address to the whitelist"
    );
    WHITELIST_ADD = formatString(messagesConfig.getString("messages.whitelist.added",
      "%prefix%Successfully added %ip% to the whitelist."
    ));

    messagesConfig.getYaml().setComment("messages.whitelist.removed",
      "Message that is shown when someone removes an IP address from the whitelist"
    );
    WHITELIST_REMOVE = formatString(messagesConfig.getString("messages.whitelist.removed",
      "%prefix%Successfully removed %ip% from the whitelist."
    ));

    messagesConfig.getYaml().setComment("messages.whitelist.duplicate-ip",
      "Message that is shown when someone adds an IP address to the whitelist but it is already whitelisted"
    );
    WHITELIST_DUPLICATE = formatString(messagesConfig.getString("messages.whitelist.duplicate-ip",
      "%prefix%The IP address you provided is already whitelisted."
    ));

    messagesConfig.getYaml().setComment("messages.whitelist.ip-not-found",
      "Message that is shown when someone removes an IP address from the whitelist but it is not whitelisted"
    );
    WHITELIST_NOT_FOUND = formatString(messagesConfig.getString("messages.whitelist.ip-not-found",
      "%prefix%The IP address you provided is not whitelisted."
    ));

    messagesConfig.getYaml().setComment("messages.verification.too-many-players",
      "Disconnect message that is shown when too many players are verifying at the same time"
    );
    TOO_MANY_PLAYERS = fromList(messagesConfig.getStringList("messages.verification.too-many-players",
      Arrays.asList(
        "%header%",
        "&6Too many players are currently trying to log in, try again later.",
        "&7Please wait a few seconds before trying to join again.",
        "%footer%"
      )));

    messagesConfig.getYaml().setComment("messages.verification.too-fast-reconnect",
      "Disconnect message that is shown when someone rejoins too fast during verification"
    );
    TOO_FAST_RECONNECT = fromList(messagesConfig.getStringList("messages.verification.too-fast-reconnect",
      Arrays.asList(
        "%header%",
        "&6You reconnected too fast, try again later.",
        "&7Please wait a few seconds before trying to verify again.",
        "%footer%"
      )));

    messagesConfig.getYaml().setComment("messages.verification.already-verifying",
      "Disconnect message that is shown when someone joins but is already verifying"
    );
    ALREADY_VERIFYING = fromList(messagesConfig.getStringList("messages.verification.already-verifying",
      Arrays.asList(
        "%header%",
        "&cYour IP address is currently being verified.",
        "&cPlease wait a few seconds before trying to verify again.",
        "%footer%"
      )));

    messagesConfig.getYaml().setComment("messages.verification.already-queued",
      "Disconnect message that is shown when someone joins but is already queued for verification"
    );
    ALREADY_QUEUED = fromList(messagesConfig.getStringList("messages.verification.already-queued",
      Arrays.asList(
        "%header%",
        "&cYour IP address is currently queued for verification.",
        "&cPlease wait a few minutes before trying to verify again.",
        "%footer%"
      )));

    messagesConfig.getYaml().setComment("messages.verification.blacklisted",
      "Disconnect message that is shown when someone joins but is temporarily blacklisted"
    );
    BLACKLISTED = fromList(messagesConfig.getStringList("messages.verification.blacklisted",
      Arrays.asList(
        "%header%",
        "&cYou are currently denied from entering the server.",
        "&cPlease wait a few minutes to be able to join the server again.",
        "&6False positive? &7%support-url%",
        "%footer%"
      )));

    messagesConfig.getYaml().setComment("messages.verification.unexpected-error",
      "Disconnect message that is shown when someone tries verifying but something goes wrong (Usually lag)"
    );
    UNEXPECTED_ERROR = fromList(messagesConfig.getStringList("messages.verification.unexpected-error",
      Arrays.asList(
        "%header%",
        "&6An unexpected error occurred when trying to process your connection.",
        "&7Please wait a few seconds before trying to verify again.",
        "&6Need help? &7%support-url%",
        "%footer%"
      )));

    messagesConfig.getYaml().setComment("messages.verification.invalid-username",
      "Disconnect message that is shown when someone joins with an invalid username"
    );
    INVALID_USERNAME = fromList(messagesConfig.getStringList("messages.verification.invalid-username",
      Arrays.asList(
        "%header%",
        "&cYour username contains invalid characters.",
        "%footer%"
      )));

    messagesConfig.getYaml().setComment("messages.verification.success",
      "Disconnect message that is shown when someone verifies successfully"
    );
    VERIFICATION_SUCCESS = fromList(messagesConfig.getStringList("messages.verification.success",
      Arrays.asList(
        "%header%",
        "&aYou have successfully passed the verification.",
        "&fYou are now able to play on the server when you reconnect."
      )));

    messagesConfig.getYaml().setComment("messages.verification.failed",
      "Disconnect message that is shown when someone fails verification"
    );
    VERIFICATION_FAILED = fromList(messagesConfig.getStringList("messages.verification.failed",
      Arrays.asList(
        "%header%",
        "&cYou have failed the verification.",
        "&7Please wait a few seconds before trying to verify again.",
        "&6Need help? &7%support-url%"
      )));

    messagesConfig.getYaml().setComment("messages.verification.too-many-online-per-ip",
      "Disconnect message that is shown when someone joins but there are too many online players with their IP address"
    );
    TOO_MANY_ONLINE_PER_IP = fromList(messagesConfig.getStringList("messages.too-many-online-per-ip",
      Arrays.asList(
        "%header%",
        "&cThere are too many players online with your IP address.",
        "%footer%"
      )));

    messagesConfig.getYaml().setComment("messages.action-bar.layout",
      "General layout for the verbose action-bar"
    );
    ACTION_BAR_LAYOUT = formatString(messagesConfig.getString(
      "messages.action-bar.layout",
      "%prefix%&fQueued &7%queued%" +
        "  &fVerifying &7%verifying%" +
        "  &fBlacklisted &7%blacklisted%" +
        "  &fTraffic &7%total%" +
        "  &fMemory &7≅ %used-memory%" +
        "  &a&l%animation%"
    ));
    ANIMATION = messagesConfig.getStringList("messages.action-bar.animation",
      Arrays.asList("◜", "◝", "◞", "◟") // ▙ ▛ ▜ ▟
    );

    generalConfig.save();
    messagesConfig.save();
  }

  private static int clamp(final int v, final int max, final int min) {
    return Math.max(Math.min(v, min), max);
  }

  private String fromList(final Collection<String> list) {
    return formatString(String.join(System.lineSeparator(), list));
  }

  private String formatString(final String string) {
    return translateAlternateColorCodes(formatHex(Objects.requireNonNull(string)))
      .replace("%prefix%", PREFIX == null ? "" : PREFIX)
      .replace("%support-url%", SUPPORT_URL == null ? "" : SUPPORT_URL)
      .replace("%header%", HEADER == null ? "" : HEADER)
      .replace("%footer%", FOOTER == null ? "" : FOOTER);
  }

  private static final Pattern HEX_PATTERN = Pattern.compile("&#[a-fA-F0-9]{6}");

  // TODO: implement
  private static String formatHex(String data) {
    Matcher matcher = HEX_PATTERN.matcher(data);

    while (matcher.find()) {
      final String color = data.substring(matcher.start(), matcher.end());
      /*int rgb;
      try {
        rgb = Integer.parseInt(data.substring(1), 16);
      } catch (NumberFormatException exception) {
        throw new IllegalArgumentException("Illegal hex string " + data);
      }*/

      final StringBuilder magic = new StringBuilder("§x");
      for (final char c : data.substring(1).toCharArray()) {
        magic.append('§').append(c);
      }
      data = data.replace(color, magic);
      matcher = HEX_PATTERN.matcher(data);
    }
    return data;
  }

  private static String translateAlternateColorCodes(final String textToTranslate) {
    final char[] b = textToTranslate.toCharArray();

    for (int i = 0; i < b.length - 1; i++) {
      if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
        b[i] = '§';
        b[i + 1] = Character.toLowerCase(b[i + 1]);
      }
    }

    return new String(b);
  }
}
