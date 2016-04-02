package me.xeroun.mcmmoextras;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.google.common.collect.Maps;

import java.util.EnumMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

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

    public void updateExpBar(SkillType lastUsedSkill) {
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
        float percent = calculatePercent(exp, requiredExp);

        String newMessage = formatMessage(player, skillName, exp, requiredExp, percent);
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

            Runnable disappearTimer = new Runnable() {

                @Override
                public void run() {
                    Player player = Bukkit.getPlayerExact(playerName);
                    if (player != null) {
                        plugin.getBossAPI().removeBar(player, skill);
                    }
                }
            };

            //disappear timer
            int disappearTime = plugin.getConfig().getInt("bar.disappear");
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, disappearTimer, disappearTime * 20);
            disappearTimers.put(skill, task.getTaskId());
        }
    }

    private String formatMessage(Player player, String lastUsedSkill, int exp, int requiredExp, float percent) {
        //default value
        ChatColor color = ChatColor.GOLD;
        String colorPath = "bar.color." + lastUsedSkill.toLowerCase();
        if (McMMOExtras.getInstance().getConfig().isSet(colorPath)) {
            //specific color for a skill type
            String configColor = McMMOExtras.getInstance().getConfig().getString(colorPath);
            //filter the color char; otherwise we won't detect the color
            color = ChatColor.getByChar(configColor.replace("&", ""));
        }

        String level = Integer.toString(ExperienceAPI.getLevel(player, lastUsedSkill));
        //custom variable replacement
        String format = McMMOExtras.getInstance().getConfig().getString("bar.format")
                .replace("@skill", lastUsedSkill)
                .replace("@level", level)
                .replace("@exp", Integer.toString(exp))
                .replace("@reqExp", Integer.toString(requiredExp))
                .replace("@percent", Float.toString(percent));

        return color + format;
    }

    private float calculatePercent(int exp, int requiredExp) {
        //progress for the next level
        float percent = exp * 100F / requiredExp;
        //filter invalid values from mcMMO
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }

        return percent;
    }
}
