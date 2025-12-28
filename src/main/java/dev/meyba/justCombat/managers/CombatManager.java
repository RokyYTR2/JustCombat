package dev.meyba.justCombat.managers;

import dev.meyba.justCombat.JustCombat;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {
    private final JustCombat plugin;
    private final Map<UUID, Long> combatPlayers = new HashMap<>();
    private final Map<UUID, BukkitTask> combatTasks = new HashMap<>();

    private int combatDuration;
    private boolean killOnLogout;
    private boolean broadcastLogout;
    private boolean showActionbar;
    private int actionbarUpdateInterval;

    public CombatManager(JustCombat plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        this.combatDuration = plugin.getConfig().getInt("settings.combat-duration", 15);
        this.killOnLogout = plugin.getConfig().getBoolean("settings.kill-on-logout", true);
        this.broadcastLogout = plugin.getConfig().getBoolean("settings.broadcast-logout", true);
        this.showActionbar = plugin.getConfig().getBoolean("settings.show-actionbar", true);
        this.actionbarUpdateInterval = plugin.getConfig().getInt("settings.actionbar-update-interval", 2);
    }

    public void reloadConfig() {
        loadConfig();
    }

    public void tagPlayer(Player player, Player attacker) {
        boolean playerWasInCombat = isInCombat(player);
        boolean attackerWasInCombat = isInCombat(attacker);

        tagSinglePlayer(player, !playerWasInCombat);
        tagSinglePlayer(attacker, !attackerWasInCombat);
    }

    private void tagSinglePlayer(Player player, boolean sendMessage) {
        UUID uuid = player.getUniqueId();

        if (combatTasks.containsKey(uuid)) {
            combatTasks.get(uuid).cancel();
        }

        combatPlayers.put(uuid, System.currentTimeMillis() + (combatDuration * 1000L));

        if (sendMessage) {
            String message = plugin.getConfig().getString("messages.combat.tagged", "&cᴅᴏꜱᴛᴀʟ ᴊꜱɪ ꜱᴇ ᴅᴏ ᴄᴏᴍʙᴀᴛᴜ!");
            player.sendMessage(colorize(getPrefix() + message));

            String soundName = plugin.getConfig().getString("sounds.combat-start");
            if (soundName != null && !soundName.isEmpty()) {
                try {
                    Sound sound = Sound.valueOf(soundName);
                    player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    removeCombat(player);
                    cancel();
                    return;
                }

                long remaining = combatPlayers.getOrDefault(uuid, 0L) - System.currentTimeMillis();

                if (remaining <= 0) {
                    removeCombat(player);
                    String endMessage = plugin.getConfig().getString("messages.combat.combat-ended", "&aᴠʏᴘʀšᴇʟ ᴛɪ ᴄᴏᴍʙᴀᴛ!");
                    player.sendMessage(colorize(getPrefix() + endMessage));

                    String soundName = plugin.getConfig().getString("sounds.combat-end");
                    if (soundName != null && !soundName.isEmpty()) {
                        try {
                            Sound sound = Sound.valueOf(soundName);
                            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                        } catch (IllegalArgumentException ignored) {}
                    }
                    cancel();
                    return;
                }

                if (showActionbar) {
                    double seconds = remaining / 1000.0;
                    String format = plugin.getConfig().getString("actionbar.time-format", "%.1f");
                    String timeStr = String.format(format, seconds);
                    String actionBarText = plugin.getConfig().getString("actionbar.text", "&c&lᴄᴏᴍʙᴀᴛ &8» &f%time%ꜱ")
                            .replace("%time%", timeStr);
                    sendActionBar(player, actionBarText);
                }
            }
        }.runTaskTimer(plugin, 0L, actionbarUpdateInterval);

        combatTasks.put(uuid, task);
    }

    public boolean isInCombat(Player player) {
        if (!combatPlayers.containsKey(player.getUniqueId())) {
            return false;
        }
        return combatPlayers.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public void removeCombat(Player player) {
        UUID uuid = player.getUniqueId();
        combatPlayers.remove(uuid);
        if (combatTasks.containsKey(uuid)) {
            combatTasks.get(uuid).cancel();
            combatTasks.remove(uuid);
        }
    }

    public long getRemainingCombatTime(Player player) {
        if (!isInCombat(player)) {
            return 0;
        }
        return combatPlayers.get(player.getUniqueId()) - System.currentTimeMillis();
    }

    public void handleCombatLog(Player player) {
        if (isInCombat(player)) {
            if (killOnLogout) {
                player.setHealth(0);
            }

            if (broadcastLogout) {
                String message = plugin.getConfig().getString("messages.combat.combat-logout", "&c%player% &7ꜱᴇ ᴏᴅᴘᴏᴊɪʟ ᴠ ᴄᴏᴍʙᴀᴛᴜ!")
                        .replace("%player%", player.getName());
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendMessage(colorize(getPrefix() + message));
                }
            }
        }
        removeCombat(player);
    }

    public void cleanup() {
        for (BukkitTask task : combatTasks.values()) {
            task.cancel();
        }
        combatTasks.clear();
        combatPlayers.clear();
    }

    public String getPrefix() {
        return plugin.getConfig().getString("prefix", "");
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(colorize(message)));
    }
}