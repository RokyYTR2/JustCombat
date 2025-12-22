package dev.meyba.justCombat.listeners;

import dev.meyba.justCombat.JustCombat;
import dev.meyba.justCombat.managers.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatListener implements Listener {
    private final JustCombat plugin;
    private final CombatManager combatManager;

    public CombatListener(JustCombat plugin, CombatManager combatManager) {
        this.plugin = plugin;
        this.combatManager = combatManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombat(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = null;
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile projectile) {
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            }
        }

        if (attacker != null && attacker != victim) {
            if (!combatManager.isInCombat(attacker)) {
                attacker.sendMessage(plugin.getPrefix() + plugin.getMessage("combat.tagged-attacker")
                        .replace("%player%", victim.getName()));
            }
            if (!combatManager.isInCombat(victim)) {
                victim.sendMessage(plugin.getPrefix() + plugin.getMessage("combat.tagged-victim")
                        .replace("%player%", attacker.getName()));
            }

            combatManager.tagPlayer(attacker);
            combatManager.tagPlayer(victim);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (combatManager.isInCombat(player)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("combat.command-blocked"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (combatManager.isInCombat(player)) {
            org.bukkit.Bukkit.broadcastMessage(plugin.getPrefix() + plugin.getMessage("combat.combat-logout")
                    .replace("%player%", player.getName()));
            combatManager.removePlayer(player);
        }
    }
}