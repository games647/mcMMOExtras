package me.xeroun.mcmmoextras.expbar.plugins;

import com.gmail.nossr50.datatypes.skills.SkillType;

import org.bukkit.entity.Player;

public class BarAPI implements BossAPI {

    private SkillType lastSkillType;

    @Override
    public boolean hasBar(Player player) {
        return me.confuser.barapi.BarAPI.hasBar(player);
    }

    @Override
    public void removeBar(Player player, SkillType skill) {
        if (lastSkillType == null || lastSkillType == skill) {
            me.confuser.barapi.BarAPI.removeBar(player);
        }
    }

    @Override
    public void setMessage(Player player, SkillType skill, String newMessage, float percent) {
        String oldMessage = me.confuser.barapi.BarAPI.getMessage(player);
        if (!newMessage.equals(oldMessage)) {
            //if the player level ups the message would be different.
            //BarAPI doesn't update the message if the player already has a bar
            me.confuser.barapi.BarAPI.removeBar(player);
        }

        me.confuser.barapi.BarAPI.setMessage(player, newMessage, percent);
        lastSkillType = skill;
    }
}
