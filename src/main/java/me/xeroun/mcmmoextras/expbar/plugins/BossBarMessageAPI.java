package me.xeroun.mcmmoextras.expbar.plugins;

import com.gmail.nossr50.datatypes.skills.SkillType;

import org.bukkit.entity.Player;
import org.inventivetalent.bossbar.BossBarAPI;

public class BossBarMessageAPI implements BossAPI {

    private SkillType lastUsedSkill;

    @Override
    public void removeBar(Player player, SkillType skill) {
        if (lastUsedSkill == skill) {
            BossBarAPI.removeAllBars(player);
        }
    }

    @Override
    public void removeAllBars(Player player) {
        BossBarAPI.removeAllBars(player);
    }

    @Override
    public void setMessage(Player player, SkillType skill, String newMessage, float percent) {
        BossBarAPI.setMessage(player, newMessage, percent);
        lastUsedSkill = skill;
    }
}
