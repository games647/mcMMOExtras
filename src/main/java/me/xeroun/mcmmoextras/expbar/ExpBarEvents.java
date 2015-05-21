package me.xeroun.mcmmoextras.expbar;

import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import me.xeroun.mcmmoextras.McMExtras;
import me.xeroun.mcmmoextras.PlayerData;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ExpBarEvents implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onExpGain(McMMOPlayerXpGainEvent xpGainEvent) {
        String playerName = xpGainEvent.getPlayer().getName();
        PlayerData playerData = McMExtras.getInstance().getData(playerName);

        playerData.setLastUsedSkill(xpGainEvent.getSkill().name());
        playerData.updateExpBar();
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent quitEvent) {
        McMExtras.getInstance().clearData(quitEvent.getPlayer().getName());
    }
}
