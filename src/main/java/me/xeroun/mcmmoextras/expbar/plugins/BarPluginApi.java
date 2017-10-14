package me.xeroun.mcmmoextras.expbar.plugins;

import com.gmail.nossr50.datatypes.skills.SkillType;

import me.confuser.barapi.BarAPI;

import org.bukkit.entity.Player;

public class BarPluginApi implements BossAPI {

    private SkillType lastSkillType;

    @Override
    public void removeBar(Player player, SkillType skill) {
        if (lastSkillType == skill) {
            BarAPI.removeBar(player);
        }
    }

    @Override
    public void removeAllBars(Player player) {
        BarAPI.removeBar(player);
    }

    @Override
    public void setMessage(Player player, SkillType skill, String newMessage, float percent) {
        String oldMessage = BarAPI.getMessage(player);
        if (!newMessage.equals(oldMessage)) {
            //if the player level ups the message would be different.
            //BarPluginApi doesn't update the message if the player already has a bar
            BarAPI.removeBar(player);
        }

        BarAPI.setMessage(player, newMessage, percent);
        lastSkillType = skill;
    }
}
