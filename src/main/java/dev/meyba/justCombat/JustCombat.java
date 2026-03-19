package dev.meyba.justCombat;

import dev.meyba.justCombat.command.CombatCommand;
import dev.meyba.justCombat.listeners.CombatListener;
import dev.meyba.justCombat.managers.CombatManager;
import dev.meyba.justCombat.utils.VersionUtil;
import dev.meyba.justCombat.hooks.WorldGuardHook;
import org.bukkit.plugin.java.JavaPlugin;

public final class JustCombat extends JavaPlugin {
    private CombatManager combatManager;
    private WorldGuardHook worldGuardHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        combatManager = new CombatManager(this);
        worldGuardHook = new WorldGuardHook();

        getCommand("combat").setExecutor(new CombatCommand(this, combatManager));

        getServer().getPluginManager().registerEvents(new CombatListener(this, combatManager, worldGuardHook), this);

        new VersionUtil(this, "RokyYTR2", "JustCombat").checkForUpdates();

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