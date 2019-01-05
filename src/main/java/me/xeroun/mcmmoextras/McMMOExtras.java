package me.xeroun.mcmmoextras;

import com.gmail.nossr50.datatypes.skills.PrimarySkill;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.xeroun.mcmmoextras.bar.BossAPI;
import me.xeroun.mcmmoextras.bar.ExpBarCommands;
import me.xeroun.mcmmoextras.bar.ExpBarEvents;
import me.xeroun.mcmmoextras.bar.SpigotBarApi;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

public class McMMOExtras extends JavaPlugin {

    private final Map<UUID, PlayerData> data = new HashMap<>();
    private final MessageFormatter formatter = new MessageFormatter(this);

    //optional dependencies
    private Permission permission;
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
        bossAPI = new SpigotBarApi(getConfig());

        //create a config only if there is none
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new ExpBarEvents(this), this);
        getCommand("expbar").setExecutor(new ExpBarCommands(this));

        setupPermissions();
        registerWorldGuardFlag();
    }

    @Override
    public void onDisable() {
        //cleanup after reloads
        data.clear();

        Bukkit.getOnlinePlayers().forEach(bossAPI::removeAllBars);
    }

    public MessageFormatter getFormatter() {
        return formatter;
    }

    public BossAPI getBossAPI() {
        return bossAPI;
    }

    public boolean isForbiddenRegion(Player player, PrimarySkill skill) {
        return regionsWhitelist != null && regionsWhitelist.isForbidden(player, skill);
    }

    public int getMaxLevel(Player player, PrimarySkill skill) {
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
                regionsWhitelist = new WorldGuardFlagSupport();
                regionsWhitelist.registerWorldGuardFlag();
            }
        }
    }

    public double calculatePercent(int exp, int requiredExp) {
        //progress for the next level
        double percent = exp / (double) requiredExp;

        //filter invalid values from mcMMO
        percent = Math.max(0.0D, percent);
        percent = Math.min(percent, 1.0D);
        return percent;
    }
}
