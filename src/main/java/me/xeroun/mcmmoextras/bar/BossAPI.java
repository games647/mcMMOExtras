package me.xeroun.mcmmoextras.bar;

import com.gmail.nossr50.datatypes.skills.PrimarySkill;

import org.bukkit.entity.Player;

public interface BossAPI {

    void removeBar(Player player, PrimarySkill lastUsedSkill);

    void removeAllBars(Player player);

    void setMessage(Player player, PrimarySkill skill, String newMessage, double percent);
}
