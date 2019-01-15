package me.xeroun.mcmmoextras;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import java.util.EnumMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerData {

    private final McMMOExtras plugin;

    private final UUID playerUUID;
    private final EnumMap<PrimarySkillType, Integer> disappearTimers = new EnumMap<>(PrimarySkillType.class);

    private boolean enabled = true;

    protected PlayerData(McMMOExtras plugin, UUID playerUUID) {
        this.plugin = plugin;
        this.playerUUID = playerUUID;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void updateExpBar(McMMOPlayerXpGainEvent event) {
        if (!enabled) {
            return;
        }

        Player player = Bukkit.getPlayer(playerUUID);
        PrimarySkillType usedSkill = event.getSkill();
        String skillName = usedSkill.name();

        int exp = ExperienceAPI.getXP(player, skillName);
        int requiredExp = ExperienceAPI.getXPToNextLevel(player, skillName);
        double percent = plugin.calculatePercent(exp, requiredExp);

        String newMessage = plugin.getFormatter().format(event);
        updateBar(player, usedSkill, newMessage, percent);
    }

    private void updateBar(Player player, final PrimarySkillType skill, String message, double percent) {
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
}
