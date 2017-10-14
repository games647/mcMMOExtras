package me.xeroun.mcmmoextras.expbar;

import me.xeroun.mcmmoextras.McMMOExtras;
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

    private final McMMOExtras plugin;
    private final String compiledPrefix;

    public ExpBarCommands(McMMOExtras plugin) {
        this.plugin = plugin;

        compiledPrefix = PREFIX.replace("%name%", plugin.getName());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ((sender instanceof Player)) {
            onCommandPlayer((Player) sender);
        } else {
            //player only feature
            sender.sendMessage(ChatColor.DARK_RED + "The expbar is a in-game graphical feature. "
                    + "So just players could use it");
        }

        return true;
    }

    private void onCommandPlayer(Player player) {
        PlayerData data = plugin.getData(player);
        if (data.isEnabled()) {
            //disable it
            player.sendMessage(compiledPrefix + ChatColor.AQUA + "The exp bar has been disabled.");
            plugin.getBossAPI().removeBar(player, null);
            data.setEnabled(false);
        } else {
            //enable it
            player.sendMessage(compiledPrefix + ChatColor.AQUA + "The exp bar has been enabled.");
            data.setEnabled(true);
        }
    }
}
