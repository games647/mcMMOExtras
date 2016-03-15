package me.xeroun.mcmmoextras.expbar.plugins;

import com.gmail.nossr50.datatypes.skills.SkillType;
import org.bukkit.entity.Player;

public interface BossAPI {

    boolean hasBar(Player player);

    void removeBar(Player player, SkillType lastUsedSkill);

    void setMessage(Player player, SkillType skill, String newMessage, float percent);
}
