package me.xeroun.mcmmoextras;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;

import java.util.EnumMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

public class PlayerData {

    private final McMMOExtras plugin;

    private final UUID playerUUID;
    private final EnumMap<SkillType, Integer> disappearTimers = new EnumMap<>(SkillType.class);

    private boolean enabled = true;

    public PlayerData(McMMOExtras plugin, UUID playerUUID) {
        this.plugin = plugin;
        this.playerUUID = playerUUID;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void updateExpBar(SkillType usedSkill, float gainedExp) {
        if (!enabled) {
            return;
        }

        Player player = Bukkit.getPlayer(playerUUID);
        String skillName = usedSkill.getName();
        int level = ExperienceAPI.getLevel(player, skillName);

        int exp = ExperienceAPI.getXP(player, skillName);
        int requiredExp = ExperienceAPI.getXPToNextLevel(player, skillName);
        int percent = calculatePercent(exp, requiredExp);

        String newMessage = formatMessage(skillName, level, exp, requiredExp, gainedExp, percent);
        updateBar(player, usedSkill, newMessage, percent);
    }

    private void updateBar(Player player, final SkillType skill, String message, float percent) {
        plugin.getBossAPI().setMessage(player, skill, message, percent);

        Bukkit.getScheduler().cancelTask(disappearTimers.getOrDefault(skill, -1));

        //disappear timer
        if (!plugin.getConfig().getBoolean("alwaysShow")) {
            int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                Player onlinePlayer = Bukkit.getPlayer(playerUUID);
                if (onlinePlayer != null) {
                    plugin.getBossAPI().removeBar(onlinePlayer, skill);
                }
            }, plugin.getConfig().getInt("bar.disappear") * 20L);

            disappearTimers.put(skill, taskId);
        }
    }

    private String formatMessage(String lastSkill, int level, int exp, int requiredExp, float gained, int percent) {
        ChatColor color = ChatColor.GOLD;
        String colorPath = "bar.color." + lastSkill.toLowerCase();
        if (plugin.getConfig().isSet(colorPath)) {
            //specific color for a skill type
            String configColor = plugin.getConfig().getString(colorPath);

            //filter the color char; otherwise we won't detect the color
            color = ChatColor.getByChar(configColor.replace("&", ""));
        }

        //custom variable replacement
        String format = plugin.getConfig().getString("bar.format")
                .replace("@skill", lastSkill)
                .replace("@level", Integer.toString(level))
                .replace("@nextLevel", Integer.toString(level + 1))
                .replace("@exp", Integer.toString(exp))
                .replace("@remainingExp", Integer.toString(requiredExp - exp))
                .replace("@gainedExp", Integer.toString(NumberConversions.round(gained)))
                .replace("@reqExp", Integer.toString(requiredExp))
                .replace("@percent", Integer.toString(percent));

        return color + format;
    }

    private int calculatePercent(int exp, int requiredExp) {
        //progress for the next level
        int percent = exp * 100 / requiredExp;
        //filter invalid values from mcMMO
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }

        return percent;
    }
}
