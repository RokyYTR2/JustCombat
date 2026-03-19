package dev.meyba.justCombat.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardHook {
    private final boolean enabled;

    public WorldGuardHook() {
        this.enabled = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    public boolean isPvpAllowed(Player player) {
        if (!enabled) {
            return true;
        }

        Location loc = player.getLocation();
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        StateFlag.State state = query.queryState(BukkitAdapter.adapt(loc), WorldGuardPlugin.inst().wrapPlayer(player), Flags.PVP);
        
        return state != StateFlag.State.DENY;
    }

    public boolean isEnabled() {
        return enabled;
    }
}