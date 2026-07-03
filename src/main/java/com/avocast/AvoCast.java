package com.avocast;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ThreadLocalRandom;

public final class AvoCast extends JavaPlugin implements CommandExecutor {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private BukkitTask broadcastTask;
    private int messageIndex = 0;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("buy").setExecutor(this);
        getCommand("avocast").setExecutor(this);

        startBroadcaster();

        getLogger().info("AvoCast has been enabled! Broadcasting every "
                + getConfig().getInt("settings.broadcast-interval-minutes", 5) + " minute(s).");
    }

    @Override
    public void onDisable() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
        }
        getLogger().info("AvoCast has been disabled.");
    }

    // ---------------------------------------------------------
    //  Broadcaster
    // ---------------------------------------------------------

    private void startBroadcaster() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
        }

        if (!getConfig().getBoolean("settings.enabled", true)) {
            return;
        }

        int minutes = Math.max(1, getConfig().getInt("settings.broadcast-interval-minutes", 5));
        long intervalTicks = minutes * 60L * 20L; // minutes -> ticks

        messageIndex = 0;

        broadcastTask = Bukkit.getScheduler().runTaskTimer(this, this::sendNextBroadcast, intervalTicks, intervalTicks);
    }

    private void sendNextBroadcast() {
        List<String> messages = getConfig().getStringList("broadcast-messages");
        if (messages.isEmpty()) {
            return;
        }

        String raw;
        if (getConfig().getBoolean("settings.random-order", false)) {
            raw = messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
        } else {
            if (messageIndex >= messages.size()) {
                messageIndex = 0;
            }
            raw = messages.get(messageIndex);
            messageIndex++;
        }

        String parsed = applyPlaceholders(raw);
        Bukkit.broadcastMessage(colorize(parsed));
    }

    private String applyPlaceholders(String message) {
        FileConfiguration cfg = getConfig();
        String discord = cfg.getString("links.discord", "");
        String vote = cfg.getString("links.vote", "");
        String store = cfg.getString("links.store", "");

        return message
                .replace("{discord}", discord)
                .replace("{vote}", vote)
                .replace("{store}", store);
    }

    // ---------------------------------------------------------
    //  Commands
    // ---------------------------------------------------------

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "buy":
                return handleBuy(sender);
            case "avocast":
                return handleAvoCast(sender, args);
            default:
                return false;
        }
    }

    private boolean handleBuy(CommandSender sender) {
        if (!sender.hasPermission("avocast.buy")) {
            sender.sendMessage(colorize(getConfig().getString("messages.no-permission", "&cNo permission.")));
            return true;
        }

        List<String> lines = getConfig().getStringList("buy-command.message");
        for (String line : lines) {
            sender.sendMessage(colorize(applyPlaceholders(line)));
        }
        return true;
    }

    private boolean handleAvoCast(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("avocast.admin")) {
                    sender.sendMessage(colorize(getConfig().getString("messages.no-permission", "&cNo permission.")));
                    return true;
                }
                reloadConfig();
                startBroadcaster(); // restart timer with new interval/messages
                sender.sendMessage(colorize(getConfig().getString("messages.reload-success", "&aReloaded!")));
                return true;

            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        List<String> help = getConfig().getStringList("messages.help");
        for (String line : help) {
            sender.sendMessage(colorize(line));
        }
    }

    // ---------------------------------------------------------
    //  Color utility - supports classic (&a) AND hex (&#RRGGBB)
    // ---------------------------------------------------------

    public static String colorize(String message) {
        if (message == null) return "";

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group(1);
            try {
                matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
            } catch (Exception ignored) {
                // If server version doesn't support ChatColor.of, just skip hex and leave as-is
                matcher.appendReplacement(buffer, "");
            }
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
