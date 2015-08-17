package me.xeroun.mcmmoextras.expbar;

import me.confuser.barapi.BarAPI;

import org.bukkit.entity.Player;
import org.inventivetalent.bossbar.BossBarAPI;

public class ExpbarBridge {

    private final boolean useBossBar;

    public ExpbarBridge(boolean useBossBar) {
        this.useBossBar = useBossBar;
    }

    public void removeBar(Player player) {
        if (useBossBar) {
            BossBarAPI.removeBar(player);
        } else {
            BarAPI.removeBar(player);
        }
    }

    public boolean hasBar(Player player) {
        if (useBossBar) {
            return BossBarAPI.hasBar(player);
        }

        return BarAPI.hasBar(player);
    }

    public void setMessage(Player player, String newMessage, float percent) {
        if (useBossBar) {
            BossBarAPI.setMessage(player, newMessage, percent);
        } else {
            String oldMessage = BarAPI.getMessage(player);
            if (!newMessage.equals(oldMessage)) {
                //if the player level ups the message would be different.
                //BarAPI doesn't update the message if the player already has a bar
                BarAPI.removeBar(player);
            }

            BarAPI.setMessage(player, newMessage, percent);
        }
    }
}
