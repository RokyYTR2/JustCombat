package dev.meyba.justCombat.listeners;

import dev.meyba.justCombat.JustCombat;
import dev.meyba.justCombat.managers.CombatManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.entity.EnderPearl;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.List;

public class CombatListener implements Listener {
    private final JustCombat plugin;
    private final CombatManager combatManager;

    public CombatListener(JustCombat plugin, CombatManager combatManager) {
        this.plugin = plugin;
        this.combatManager = combatManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = null;

        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile projectile) {
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            }
        }

        if (attacker != null && !attacker.equals(victim)) {
            if (victim.hasPermission("combat.bypass") || attacker.hasPermission("combat.bypass")) {
                return;
            }
            combatManager.tagPlayer(victim, attacker);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        combatManager.handleCombatLog(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        event.setDeathMessage(null);

        String message;
        if (killer != null) {
            message = plugin.getConfig().getString("messages.combat.death-by-player", "&7ᴘʟᴀʏᴇʀ &c%victim% &7ᴡᴀꜱ ᴋɪʟʟᴇᴅ ʙʏ &a%killer%")
                    .replace("%victim%", victim.getName())
                    .replace("%killer%", killer.getName());
        } else {
            message = plugin.getConfig().getString("messages.combat.death-other", "&7ᴘʟᴀʏᴇʀ &c%victim% &7ᴅɪᴇᴅ")
                    .replace("%victim%", victim.getName());
        }

        String deathMessage = colorize(combatManager.getPrefix() + message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(deathMessage);
        }

        combatManager.removeCombat(victim);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!combatManager.isInCombat(player)) {
            return;
        }

        if (!plugin.getConfig().getBoolean("blocked-commands.enabled", true)) {
            return;
        }

        String message = event.getMessage().toLowerCase();
        String command = message.split(" ")[0].substring(1);

        if (command.contains(":")) {
            command = command.split(":")[1];
        }

        List<String> blockedCommands = plugin.getConfig().getStringList("blocked-commands.commands");
        boolean shouldBlock = blockedCommands.contains("*") || blockedCommands.contains(command);

        if (shouldBlock) {
            event.setCancelled(true);

            String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix", ""));
            String blockedMessage = plugin.getConfig().getString("messages.combat.command-blocked", "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴜꜱᴇ ᴛʜɪꜱ ᴄᴏᴍᴍᴀɴᴅ ᴡʜɪʟᴇ ɪɴ ᴄᴏᴍʙᴀᴛ!");
            player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', blockedMessage));

            String soundName = plugin.getConfig().getString("sounds.command-blocked");
            if (soundName != null && !soundName.isEmpty()) {
                try {
                    Sound sound = Sound.valueOf(soundName);
                    player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (!combatManager.isInCombat(player)) {
            return;
        }

        if (!combatManager.isDisableFly()) {
            return;
        }

        if (event.isFlying()) {
            event.setCancelled(true);
            String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix", ""));
            String flyMessage = plugin.getConfig().getString("messages.combat.fly-disabled", "&cғʟʏ ᴍᴏᴅᴇ ᴅɪꜱᴀʙʟᴇᴅ ᴅᴜᴇ ᴛᴏ ᴄᴏᴍʙᴀᴛ!");
            player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', flyMessage));

            String soundName = plugin.getConfig().getString("sounds.command-blocked");
            if (soundName != null && !soundName.isEmpty()) {
                try {
                    Sound sound = Sound.valueOf(soundName);
                    player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) {
            return;
        }

        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        if (!combatManager.isInCombat(player)) {
            return;
        }

        if (!combatManager.isBlockEnderPearls()) {
            return;
        }

        event.setCancelled(true);

        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix", ""));
        String pearlMessage = plugin.getConfig().getString("messages.combat.ender-pearl-blocked", "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ᴜꜱᴇ ᴇɴᴅᴇʀ ᴘᴇᴀʀʟꜱ ᴡʜɪʟᴇ ɪɴ ᴄᴏᴍʙᴀᴛ!");
        player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', pearlMessage));

        String soundName = plugin.getConfig().getString("sounds.command-blocked");
        if (soundName != null && !soundName.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(soundName);
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}