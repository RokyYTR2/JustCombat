package dev.meyba.justCombat.command;

import dev.meyba.justCombat.JustCombat;
import dev.meyba.justCombat.managers.CombatManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CombatCommand implements CommandExecutor, TabCompleter {
    private final JustCombat plugin;
    private final CombatManager combatManager;

    public CombatCommand(JustCombat plugin, CombatManager combatManager) {
        this.plugin = plugin;
        this.combatManager = combatManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix", ""));

        if (args.length == 0) {
            String usageMsg = plugin.getConfig().getString("messages.commands.usage", "&cᴜꜱᴀɢᴇ: /combat <reload|info>");
            sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', usageMsg));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("combat.reload")) {
                    String noPermissionMsg = plugin.getConfig().getString("messages.commands.no-permission", "&cʏᴏᴜ ᴅᴏɴ'ᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪꜱꜱɪᴏɴ ᴛᴏ ᴜꜱᴇ ᴛʜɪꜱ ᴄᴏᴍᴍᴀɴᴅ.");
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', noPermissionMsg));
                    return true;
                }
                plugin.reloadConfig();
                combatManager.reloadConfig();
                String reloadSuccessMsg = plugin.getConfig().getString("messages.commands.reload-success", "&aᴄᴏɴꜰɪɢᴜʀᴀᴛɪᴏɴ ʀᴇʟᴏᴀᴅᴇᴅ!");
                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', reloadSuccessMsg));
                return true;

            case "info":
                if (!(sender instanceof Player player)) {
                    String notAPlayerMsg = plugin.getConfig().getString("messages.commands.not-a-player", "&cᴏɴʟʏ ᴘʟᴀʏᴇʀꜱ ᴄᴀɴ ᴜꜱᴇ ᴛʜɪꜱ ᴄᴏᴍᴍᴀɴᴅ.");
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', notAPlayerMsg));
                    return true;
                }
                if (combatManager.isInCombat(player)) {
                    long remaining = combatManager.getRemainingCombatTime(player);
                    double seconds = remaining / 1000.0;
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', String.format("&cʏᴏᴜ ᴀʀᴇ ɪɴ ᴄᴏᴍʙᴀᴛ ꜰᴏʀ &e%.1f &cꜱᴇᴄᴏɴᴅꜱ.", seconds)));
                } else {
                    sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', "&aʏᴏᴜ ᴀʀᴇ ɴᴏᴛ ɪɴ ᴄᴏᴍʙᴀᴛ."));
                }
                return true;

            default:
                String usageMsg = plugin.getConfig().getString("messages.commands.usage", "&cᴜꜱᴀɢᴇ: /combat <reload|info>");
                sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', usageMsg));
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("combat.reload")) {
                completions.add("reload");
            }
            completions.add("info");
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}