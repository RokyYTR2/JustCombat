package dev.meyba.justCombat;

import dev.meyba.justCombat.listeners.CombatListener;
import dev.meyba.justCombat.managers.CombatManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class JustCombat extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        CombatManager combatManager = new CombatManager(this);

        getServer().getPluginManager().registerEvents(new CombatListener(this, combatManager), this);

        getLogger().info("JustCombat has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("JustCombat has been disabled!");
    }

    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix", "&cCombat > &f"));
    }

    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages." + path, "&cMessage not found: " + path));
    }
}