package me.xeroun.mcmmoextras.expbar.plugins;

import org.bukkit.entity.Player;

public interface BossAPI {

    boolean hasBar(Player player);

    void removeBar(Player player);

    void setMessage(Player player, String newMessage, float percent);
}
