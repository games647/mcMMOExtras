package me.xeroun.mcmmoextras.expbar;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import java.util.UUID;

import me.xeroun.mcmmoextras.McMMOExtras;
import me.xeroun.mcmmoextras.PlayerData;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ExpBarEvents implements Listener {

    private final McMMOExtras plugin;
    private final String permission;

    public ExpBarEvents(McMMOExtras plugin) {
        this.plugin = plugin;
        this.permission = plugin.getName().toLowerCase() + ".expbar";
    }

    @EventHandler(ignoreCancelled = true)
    public void onExpGain(final McMMOPlayerXpGainEvent xpGainEvent) {
        Player player = xpGainEvent.getPlayer();
        if (!player.hasPermission(permission)) {
            return;
        }

        UUID uniqueId = player.getUniqueId();
        SkillType skill = xpGainEvent.getSkill();
        float xpGained = xpGainEvent.getRawXpGained();
        //set the new value not the old one
        Bukkit.getScheduler().runTask(plugin, () -> onNewExp(uniqueId, skill, xpGained));
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent quitEvent) {
        plugin.clearData(quitEvent.getPlayer());
    }

    private void onNewExp(UUID playerUUID, SkillType skillType, float xpGain) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) {
            //player went offline in that one tick
            return;
        }

        String skillName = skillType.getName();
        int level = ExperienceAPI.getLevel(player, skillName);

        //permission based max levels
        if (level <= plugin.getMaxLevel(player, skillType) && !plugin.isForbiddenRegion(player, skillType)) {
            PlayerData playerData = plugin.getData(player);
            playerData.updateExpBar(skillType, xpGain);
        }
    }
}
