package me.xeroun.mcmmoextras;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.google.common.collect.Maps;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import java.util.Map;
import java.util.logging.Level;

import me.xeroun.mcmmoextras.expbar.ExpBarCommands;
import me.xeroun.mcmmoextras.expbar.ExpBarEvents;
import me.xeroun.mcmmoextras.expbar.plugins.BarAPI;
import me.xeroun.mcmmoextras.expbar.plugins.BossAPI;
import me.xeroun.mcmmoextras.expbar.plugins.BossBarMessageAPI;
import me.xeroun.mcmmoextras.expbar.plugins.SpigotBarApi;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

public class McMMOExtras extends JavaPlugin {

    private static McMMOExtras instance;

    public static McMMOExtras getInstance() {
        return instance;
    }

    private final Map<String, PlayerData> data = Maps.newHashMap();

    //optional dependencies
    private Permission permission = null;
    private WorldGuardFlagSupport regionsWhitelist;
    private BossAPI bossAPI;

    public PlayerData getData(String playerName) {
        return data.computeIfAbsent(playerName, PlayerData::new);
    }

    public void clearData(Player player) {
        data.remove(player.getName());

        instance.getBossAPI().removeBar(player, null);
    }

    @Override
    public void onEnable() {
        //check the dependencies
        if (initializeBarAPI()) {
            //create a config only if there is none
            saveDefaultConfig();

            instance = this;

            getServer().getPluginManager().registerEvents(new ExpBarEvents(), this);
            getCommand("expbar").setExecutor(new ExpBarCommands(this));

            setupPermissions();
            registerWorldGuardFlag();
        } else {
            //inform the users
            getLogger().log(Level.INFO, "{0} requires BarAPI, BossBarAPI or Spigot 1.9+ to work.", getName());
        }
    }

    @Override
    public void onDisable() {
        //cleanup after reloads
        data.clear();

        //Prevent memory leaks; see this https://bukkit.org/threads/how-to-make-your-plugin-better.77899/
        instance = null;

        Bukkit.getOnlinePlayers().forEach(player -> bossAPI.removeBar(player, null));
    }

    public BossAPI getBossAPI() {
        return bossAPI;
    }

    public boolean isForbiddenSkillInRegion(Player player, SkillType skill) {
        return regionsWhitelist != null && regionsWhitelist.isForbiddenSkillInRegion(player, skill);

    }

    public int getMaxSkillLevel(Player player, String skill) {
        if (permission == null || !permission.hasGroupSupport()) {
            //vault hasn't found
            return Integer.MAX_VALUE;
        }

        String primaryGroup = permission.getPrimaryGroup(player);

        String configPath = "permissions." + primaryGroup + '.' + skill;
        return getConfig().getInt(configPath, Integer.MAX_VALUE);
    }

    private void setupPermissions() {
        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            ServicesManager serviceManager = getServer().getServicesManager();
            RegisteredServiceProvider<Permission> permissionProvider = serviceManager.getRegistration(Permission.class);
            if (permissionProvider != null) {
                permission = permissionProvider.getProvider();
            }
        }
    }

    private void registerWorldGuardFlag() {
        if (getConfig().getBoolean("useWorldGuardFlags")) {
            if (getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
                getLogger().warning("Using world guards flags requires the plugin WorldGuard");
            } else {
                regionsWhitelist = new WorldGuardFlagSupport(WorldGuardPlugin.inst());
                regionsWhitelist.registerWorldGuardFlag();
            }
        }
    }

    private boolean initializeBarAPI() {
        //load priority. If this plugin is found use it in order to fix the not see bug
        try {
            Class.forName("org.bukkit.boss.BossBar");
            bossAPI = new SpigotBarApi(getConfig());
            return true;
        } catch (ClassNotFoundException notFoundEx) {
            if (getServer().getPluginManager().isPluginEnabled("BossBarAPI")) {
                bossAPI = new BossBarMessageAPI(getConfig());
                return true;
            } else if (getServer().getPluginManager().isPluginEnabled("BarAPI")) {
                bossAPI = new BarAPI();
                return true;
            }
        }

        return false;
    }
}
