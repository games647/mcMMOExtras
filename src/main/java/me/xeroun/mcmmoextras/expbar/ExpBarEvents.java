package me.xeroun.mcmmoextras.expbar;

import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import me.xeroun.mcmmoextras.McMMOExtras;
import me.xeroun.mcmmoextras.PlayerData;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ExpBarEvents implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onExpGain(final McMMOPlayerXpGainEvent xpGainEvent) {
        //set the new value not the old one
        Bukkit.getScheduler().runTaskLater(McMMOExtras.getInstance(), new Runnable() {

            @Override
            public void run() {
                String playerName = xpGainEvent.getPlayer().getName();
                PlayerData playerData = McMMOExtras.getInstance().getData(playerName);

                playerData.setLastUsedSkill(xpGainEvent.getSkill().name());
                playerData.updateExpBar();
            }
        }, 1L);
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent quitEvent) {
        McMMOExtras.getInstance().clearData(quitEvent.getPlayer().getName());
    }
}
