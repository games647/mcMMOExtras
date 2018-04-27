package me.xeroun.mcmmoextras.bar;

import com.gmail.nossr50.datatypes.skills.SkillType;

import org.bukkit.entity.Player;

public interface BossAPI {

    void removeBar(Player player, SkillType lastUsedSkill);

    void removeAllBars(Player player);

    void setMessage(Player player, SkillType skill, String newMessage, float percent);
}
