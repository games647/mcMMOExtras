package me.xeroun.mcmmoextras.bar;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.PrimarySkill;
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
    public void onExpGain(McMMOPlayerXpGainEvent xpGainEvent) {
        Player player = xpGainEvent.getPlayer();
        PrimarySkill skill = xpGainEvent.getSkill();
        if (!player.hasPermission(permission) || plugin.isForbiddenRegion(player, skill)) {
            return;
        }

        String skillName = skill.name();
        int level = ExperienceAPI.getLevel(player, skillName);

        //permission based max levels
        if (level <= plugin.getMaxLevel(player, skill)) {
            PlayerData playerData = plugin.getData(player);
            playerData.updateExpBar(xpGainEvent);
        }
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent quitEvent) {
        plugin.clearData(quitEvent.getPlayer());
    }
}
