package me.xeroun.mcmmoextras.expbar.plugins;

import org.bukkit.entity.Player;
import org.inventivetalent.bossbar.BossBarAPI;

public class BossBarMessageAPI implements BossAPI {

    @Override
    public boolean hasBar(Player player) {
        return BossBarAPI.hasBar(player);
    }

    @Override
    public void removeBar(Player player) {
        BossBarAPI.removeBar(player);
    }

    @Override
    public void setMessage(Player player, String newMessage, float percent) {
        //remove the old dragon. Otherwise the old message persists
        BossBarAPI.removeBar(player);
        BossBarAPI.setMessage(player, newMessage, percent);
    }
}
