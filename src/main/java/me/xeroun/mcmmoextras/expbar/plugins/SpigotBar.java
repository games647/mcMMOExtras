package me.xeroun.mcmmoextras.expbar.plugins;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SpigotBar implements BossAPI {

    private final Map<UUID, BossBar> bossbars = Maps.newHashMap();
    private BarStyle segments;
    private BarColor barColor;

    public SpigotBar(FileConfiguration config) {
        String confSeg = config.getString("segments");
        try {
            segments = BarStyle.valueOf(confSeg.toUpperCase());
        } catch (IllegalArgumentException argumentException) {
            segments = BarStyle.SOLID;
        }

        String confColor = config.getString("color");
        try {
            barColor = BarColor.valueOf(confColor.toUpperCase());
        } catch (IllegalArgumentException argumentException) {
            barColor = BarColor.BLUE;
        }
    }

    @Override
    public boolean hasBar(Player player) {
        return bossbars.containsKey(player.getUniqueId());
    }

    @Override
    public void removeBar(Player player) {
        BossBar bar = bossbars.remove(player.getUniqueId());
        if (bar != null) {
            bar.setVisible(false);
        }
    }

    @Override
    public void setMessage(Player player, String newMessage, float percent) {
        UUID uniqueId = player.getUniqueId();
        BossBar bar = bossbars.get(uniqueId);
        if (bar == null) {
            bar = Bukkit.createBossBar(newMessage, barColor, segments);
            bar.addPlayer(player);
            bossbars.put(uniqueId, bar);
        } else {
            bar.setTitle(newMessage);
            bar.setProgress(percent / 100);
        }
    }
}
