package me.xeroun.mcmmoextras.expbar;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import me.xeroun.mcmmoextras.McMMOExtras;
import me.xeroun.mcmmoextras.PlayerData;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ExpBarEvents implements Listener {

    private final McMMOExtras plugin;
    private final String permission;

    public ExpBarEvents(McMMOExtras plugin) {
        this.plugin = plugin;
        this.permission = plugin.getName().toLowerCase() + ".expbar";
    }

    //set the new value not the old one
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onExpGain(final McMMOPlayerXpGainEvent xpGainEvent) {
        Player player = xpGainEvent.getPlayer();
        if (!player.hasPermission(permission)) {
            return;
        }

        SkillType skill = xpGainEvent.getSkill();
        String skillName = skill.name();
        int level = ExperienceAPI.getLevel(player, skillName);

        //permission based max levels
        if (level <= plugin.getMaxLevel(player, skill) && !plugin.isForbiddenRegion(player, skill)) {
            PlayerData playerData = plugin.getData(player);
            playerData.updateExpBar(xpGainEvent);
        }
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent quitEvent) {
        plugin.clearData(quitEvent.getPlayer());
    }
}
