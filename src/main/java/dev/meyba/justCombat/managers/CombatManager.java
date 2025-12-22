package dev.meyba.justCombat.managers;

import dev.meyba.justCombat.JustCombat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {
    private final JustCombat plugin;
    private final Map<UUID, Long> combatLog = new HashMap<>();
    private final int combatDuration;

    public CombatManager(JustCombat plugin) {
        this.plugin = plugin;
        this.combatDuration = plugin.getConfig().getInt("combat-duration", 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                combatLog.entrySet().removeIf(entry -> {
                    if (now > entry.getValue()) {
                        Player player = Bukkit.getPlayer(entry.getKey());
                        if (player != null) {
                            player.sendMessage(plugin.getPrefix() + plugin.getMessage("combat.combat-ended"));
                        }
                        return true;
                    }
                    return false;
                });
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void tagPlayer(Player player) {
        boolean wasInCombat = isInCombat(player);
        combatLog.put(player.getUniqueId(), System.currentTimeMillis() + (combatDuration * 1000L));

        if (!wasInCombat) {}
    }

    public boolean isInCombat(Player player) {
        Long expiry = combatLog.get(player.getUniqueId());
        return expiry != null && System.currentTimeMillis() < expiry;
    }

    public void removePlayer(Player player) {
        combatLog.remove(player.getUniqueId());
    }
}