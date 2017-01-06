package me.xeroun.mcmmoextras;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.google.common.collect.Maps;

import java.util.EnumMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;

public class PlayerData {

    private final String playerName;

    private boolean enabled = true;
    private final EnumMap<SkillType, Integer> disappearTimers = Maps.newEnumMap(SkillType.class);

    public PlayerData(String playerName) {
        this.playerName = playerName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void updateExpBar(SkillType lastUsedSkill, float gainedExp) {
        if (!enabled || lastUsedSkill == null) {
            return;
        }

        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) {
            //player went offline
            return;
        }

        String skillName = lastUsedSkill.getName();

        int exp = ExperienceAPI.getXP(player, skillName);
        int requiredExp = ExperienceAPI.getXPToNextLevel(player, skillName);
        int percent = calculatePercent(exp, requiredExp);

        String newMessage = formatMessage(player, skillName, exp, requiredExp, gainedExp, percent);
        updateBar(player, lastUsedSkill, newMessage, percent);
    }

    private void updateBar(Player player, final SkillType skill, String message, float percent) {
        final McMMOExtras plugin = McMMOExtras.getInstance();

        plugin.getBossAPI().setMessage(player, skill, message, percent);
        if (!plugin.getConfig().getBoolean("alwaysShow")) {
            Integer taskId = disappearTimers.get(skill);
            if (taskId != null) {
                Bukkit.getScheduler().cancelTask(taskId);
            }

            //disappear timer
            int disappearTime = plugin.getConfig().getInt("bar.disappear");
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player onlinePlayer = Bukkit.getPlayerExact(playerName);
                if (onlinePlayer != null) {
                    plugin.getBossAPI().removeBar(onlinePlayer, skill);
                }
            }, disappearTime * 20);
            disappearTimers.put(skill, task.getTaskId());
        }
    }

    private String formatMessage(Player player, String lastSkill, int exp, int requiredExp, float gained, int percent) {
        //default value
        ChatColor color = ChatColor.GOLD;
        String colorPath = "bar.color." + lastSkill.toLowerCase();
        if (McMMOExtras.getInstance().getConfig().isSet(colorPath)) {
            //specific color for a skill type
            String configColor = McMMOExtras.getInstance().getConfig().getString(colorPath);
            //filter the color char; otherwise we won't detect the color
            color = ChatColor.getByChar(configColor.replace("&", ""));
        }

        int level = ExperienceAPI.getLevel(player, lastSkill);
        //custom variable replacement
        String format = McMMOExtras.getInstance().getConfig().getString("bar.format")
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

    public int calculatePercent(int exp, int requiredExp) {
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
