package me.xeroun.mcmmoextras;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.google.common.collect.Maps;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import me.xeroun.mcmmoextras.expbar.ExpBarCommands;
import me.xeroun.mcmmoextras.expbar.ExpBarEvents;
import me.xeroun.mcmmoextras.expbar.plugins.BarPluginApi;
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

    private final Map<UUID, PlayerData> data = Maps.newHashMap();

    //optional dependencies
    private Permission permission = null;
    private WorldGuardFlagSupport regionsWhitelist;
    private BossAPI bossAPI;

    public PlayerData getData(Player player) {
        UUID uuid = player.getUniqueId();
        return data.computeIfAbsent(uuid, playerUUID -> new PlayerData(this, playerUUID));
    }

    public void clearData(Player player) {
        data.remove(player.getUniqueId());
        bossAPI.removeAllBars(player);
    }

    @Override
    public void onEnable() {
        //check the dependencies
        if (initializeBarAPI()) {
            //create a config only if there is none
            saveDefaultConfig();

            getServer().getPluginManager().registerEvents(new ExpBarEvents(this), this);
            getCommand("expbar").setExecutor(new ExpBarCommands(this));

            setupPermissions();
            registerWorldGuardFlag();
        } else {
            //inform the users
            getLogger().log(Level.INFO, "{0} requires BarPluginApi, BossBarAPI or Spigot 1.9+ to work.", getName());
        }
    }

    @Override
    public void onDisable() {
        //cleanup after reloads
        data.clear();

        Bukkit.getOnlinePlayers().forEach(player -> bossAPI.removeAllBars(player));
    }

    public BossAPI getBossAPI() {
        return bossAPI;
    }

    public boolean isForbiddenRegion(Player player, SkillType skill) {
        return regionsWhitelist != null && regionsWhitelist.isForbiddenSkillInRegion(player, skill);
    }

    public int getMaxLevel(Player player, SkillType skill) {
        if (permission == null || !permission.hasGroupSupport()) {
            //vault hasn't found
            return Integer.MAX_VALUE;
        }

        String primaryGroup = permission.getPrimaryGroup(player);
        String configPath = "permissions." + primaryGroup + '.' + skill.getName();
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
            //only available in 1.9
            Class.forName("org.bukkit.boss.BossBar");
            bossAPI = new SpigotBarApi(getConfig());
            return true;
        } catch (ClassNotFoundException notFoundEx) {
            if (getServer().getPluginManager().isPluginEnabled("BossBarAPI")) {
                bossAPI = new BossBarMessageAPI();
                return true;
            } else if (getServer().getPluginManager().isPluginEnabled("BarPluginApi")) {
                bossAPI = new BarPluginApi();
                return true;
            }
        }

        return false;
    }
}
