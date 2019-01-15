package me.xeroun.mcmmoextras.bar;

import com.gmail.nossr50.datatypes.skills.PrimarySkillType;

import org.bukkit.entity.Player;

public interface BossAPI {

    void removeBar(Player player, PrimarySkillType lastUsedSkill);

    void removeAllBars(Player player);

    void setMessage(Player player, PrimarySkillType skill, String newMessage, double percent);
}
