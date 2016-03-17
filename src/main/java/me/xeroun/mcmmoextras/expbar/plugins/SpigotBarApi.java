package me.xeroun.mcmmoextras.expbar.plugins;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SpigotBarApi implements BossAPI {

    private final Map<UUID, EnumMap<SkillType, BossBar>> bossbars = Maps.newHashMap();
    private final LinkedList<BossBar> oldBars = Lists.newLinkedList();
    private final int concurrentBars;
    private BarStyle segments;
    private BarColor barColor;

    public SpigotBarApi(FileConfiguration config) {
        concurrentBars = config.getInt("concurrentBars");

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
    public void removeBar(Player player, SkillType skill) {
        if (skill == null) {
            EnumMap<SkillType, BossBar> skillBars = bossbars.remove(player.getUniqueId());
            if (skillBars != null) {
                for (BossBar bar : skillBars.values()) {
                    bar.setVisible(false);
                }
            }
        } else {
            EnumMap<SkillType, BossBar> skillBars = bossbars.get(player.getUniqueId());
            if (skillBars != null) {
                BossBar bar = skillBars.get(skill);
                if (bar != null) {
                    bar.setVisible(false);
                }
            }
        }
    }

    @Override
    public void setMessage(Player player, SkillType skill, String newMessage, float percent) {
        UUID uniqueId = player.getUniqueId();

        EnumMap<SkillType, BossBar> skillBars = bossbars.get(uniqueId);
        if (skillBars == null) {
            skillBars = Maps.newEnumMap(SkillType.class);
            bossbars.put(uniqueId, skillBars);
        }

        BossBar bar = skillBars.get(skill);
        if (bar == null) {
            bar = Bukkit.createBossBar(newMessage, barColor, segments);
            bar.addPlayer(player);
            skillBars.put(skill, bar);
        } else {
            bar.setTitle(newMessage);
            bar.setVisible(true);
        }

        bar.setProgress(percent / 100);
        oldBars.remove(bar);
        oldBars.addLast(bar);
        if (oldBars.size() > concurrentBars) {
            BossBar removed = oldBars.removeFirst();
            removed.setVisible(false);
        }
    }
}
