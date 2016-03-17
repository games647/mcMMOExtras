package me.xeroun.mcmmoextras.expbar.plugins;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.inventivetalent.bossbar.BossBar;
import org.inventivetalent.bossbar.BossBarAPI;
import org.inventivetalent.bossbar.BossBarAPI.Color;
import org.inventivetalent.bossbar.BossBarAPI.Style;

public class BossBarMessageAPI implements BossAPI {

    private final Map<UUID, EnumMap<SkillType, BossBar>> bossbars = Maps.newHashMap();
    private final LinkedList<BossBar> oldBars = Lists.newLinkedList();
    private final int concurrentBars;

    private SkillType lastUsedSkill;

    private Style segments;
    private Color barColor;

    public BossBarMessageAPI(FileConfiguration config) {
        if (BossBarAPI.is1_9) {
            concurrentBars = config.getInt("concurrentBars");

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
        } else {
            concurrentBars = 1;
        }
    }

    @Override
    public boolean hasBar(Player player) {
        return !BossBarAPI.getBossBars(player).isEmpty();
    }

    @Override
    public void removeBar(Player player, SkillType skill) {
        if (!BossBarAPI.is1_9) {
            if (skill == null || lastUsedSkill == skill) {
                BossBarAPI.removeAllBars(player);
            }
        } else if (skill == null) {
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
        if (BossBarAPI.is1_9) {
            setMessageNew(player, skill, newMessage, percent);
        } else {
            setMessageOld(player, skill, newMessage, percent);
        }
    }

    private void setMessageNew(Player player, SkillType skill, String newMessage, float percent) {
        UUID uniqueId = player.getUniqueId();

        EnumMap<SkillType, BossBar> skillBars = bossbars.get(uniqueId);
        if (skillBars == null) {
            skillBars = Maps.newEnumMap(SkillType.class);
            bossbars.put(uniqueId, skillBars);
        }

        BossBar bar = skillBars.get(skill);
        if (bar == null) {
            bar = BossBarAPI.addBar(player, new TextComponent(newMessage), barColor, segments, percent);
            bar.addPlayer(player);
            skillBars.put(skill, bar);
        } else {
            bar.setMessage(newMessage);
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

    private void setMessageOld(Player player, SkillType skill, String newMessage, float percent) {
        BossBarAPI.setMessage(player, newMessage, percent);
        lastUsedSkill = skill;
    }
}
