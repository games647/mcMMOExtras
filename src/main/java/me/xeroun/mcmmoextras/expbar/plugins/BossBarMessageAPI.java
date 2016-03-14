package me.xeroun.mcmmoextras.expbar.plugins;

import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.inventivetalent.bossbar.BossBarAPI;
import org.inventivetalent.bossbar.BossBarAPI.Color;
import org.inventivetalent.bossbar.BossBarAPI.Style;

public class BossBarMessageAPI implements BossAPI {

    private Style segments;
    private Color barColor;

    public BossBarMessageAPI(FileConfiguration config) {
        String confSeg = config.getString("segments");
        BarStyle bukkitSegments;
        try {
            bukkitSegments = BarStyle.valueOf(confSeg.toUpperCase());
        } catch (IllegalArgumentException argumentException) {
            bukkitSegments = BarStyle.SOLID;
        }

        switch (bukkitSegments) {
            case SEGMENTED_6:
                segments = Style.NOTCHED_6;
                break;
            case SEGMENTED_10:
                segments = Style.NOTCHED_10;
                break;
            case SEGMENTED_12:
                segments = Style.NOTCHED_12;
                break;
            case SEGMENTED_20:
                segments = Style.NOTCHED_20;
                break;
            default:
            case SOLID:
                segments = Style.PROGRESS;
                break;
        }

        String confColor = config.getString("color");
        try {
            barColor = Color.valueOf(confColor.toUpperCase());
        } catch (IllegalArgumentException argumentException) {
            barColor = Color.BLUE;
        }
    }

    @Override
    public boolean hasBar(Player player) {
        return !BossBarAPI.getBossBars(player).isEmpty();
    }

    @Override
    public void removeBar(Player player) {
        BossBarAPI.removeAllBars(player);
    }

    @Override
    public void setMessage(Player player, String newMessage, float percent) {
        //remove the old dragon. Otherwise the old message persists
        BossBarAPI.removeAllBars(player);
        if (BossBarAPI.is1_9) {
            BossBarAPI.addBar(player, new TextComponent(newMessage), barColor, segments, percent);
        } else {
            BossBarAPI.setMessage(player, newMessage, percent);
        }
    }
}
