package me.xeroun.mcmmoextras.expbar;

import me.confuser.barapi.BarAPI;
import me.xeroun.mcmmoextras.McMExtras;
import me.xeroun.mcmmoextras.PlayerData;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ExpBarCommands implements CommandExecutor {

    private static final String PREFIX = ChatColor.YELLOW + "[" + ChatColor.GOLD
            + "%name%"
            + ChatColor.YELLOW + "] " + ChatColor.RESET;

    private final McMExtras plugin;

    private final String compiledPrefix;

    public ExpBarCommands(McMExtras plugin) {
        this.plugin = plugin;

        compiledPrefix = PREFIX.replace("%name%", plugin.getName());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ((sender instanceof Player)) {
            Player player = (Player) sender;

            final PlayerData data = plugin.getData(player.getName());
            if (data.isEnabled()) {
                //disable it
                player.sendMessage(compiledPrefix + ChatColor.AQUA + "The exp bar has been disabled.");
                BarAPI.removeBar(player);
                data.setEnabled(false);
            } else {
                //enable it
                player.sendMessage(compiledPrefix + ChatColor.AQUA + "The exp bar has been enabled.");
                data.setEnabled(true);
            }
        } else {
            //player only feature
            sender.sendMessage(ChatColor.DARK_RED + "The expbar is a ingame graphical feature. "
                    + "So just player could use it");
        }

        return true;
    }
}
