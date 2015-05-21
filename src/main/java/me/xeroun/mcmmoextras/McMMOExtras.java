package me.xeroun.mcmmoextras;

import me.xeroun.mcmmoextras.expbar.ExpBarCommands;
import me.xeroun.mcmmoextras.expbar.ExpBarEvents;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

public class McMMOExtras extends JavaPlugin {

    private static McMMOExtras instance;

    public static McMMOExtras getInstance() {
        return instance;
    }

    private final Map<String, PlayerData> data = Maps.newHashMap();

    public PlayerData getData(String player) {
        PlayerData playerData = data.get(player);
        //get the data and check if the data exists at once
        if (playerData == null) {
            //lazy loading
            playerData = new PlayerData(player);
            data.put(player, playerData);
        }

        return playerData;
    }

    public void clearData(String player) {
        data.remove(player);
    }

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().isPluginEnabled("mcMMO")
                && getServer().getPluginManager().isPluginEnabled("BarAPI")) {
            //create a config only if there is none
            saveDefaultConfig();

            instance = this;

            getServer().getPluginManager().registerEvents(new ExpBarEvents(), this);
            getCommand("expbar").setExecutor(new ExpBarCommands(this));
        } else {
            //inform the users
            getLogger().log(Level.INFO, "{0} requires mcMMO and BarAPI to function.", getName());
        }
    }

    @Override
    public void onDisable() {
        //cleanup after reloads
        data.clear();

        //Prevent memory leaks; see this http://bukkit.org/threads/how-to-make-your-plugin-better.77899/
        instance = null;
    }
}
