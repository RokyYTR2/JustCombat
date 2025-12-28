package dev.meyba.justCombat;

import dev.meyba.justCombat.commands.CombatCommands;
import dev.meyba.justCombat.listeners.CombatListener;
import dev.meyba.justCombat.managers.CombatManager;
import dev.meyba.justCombat.utils.VersionChecker;
import org.bukkit.plugin.java.JavaPlugin;

public final class JustCombat extends JavaPlugin {
    private CombatManager combatManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        combatManager = new CombatManager(this);

        getCommand("combat").setExecutor(new CombatCommands(this, combatManager));

        getServer().getPluginManager().registerEvents(new CombatListener(this, combatManager), this);

        new VersionChecker(this, "RokyYTR2", "JustCombat").checkForUpdates();

        getLogger().info("JustCombat has been enabled!");
    }

    @Override
    public void onDisable() {
        if (combatManager != null) {
            combatManager.cleanup();
        }
        getLogger().info("JustCombat has been disabled!");
    }
}