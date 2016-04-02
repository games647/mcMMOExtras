package me.xeroun.mcmmoextras.expbar.plugins;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

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

    private final Map<SkillType, Style> specificStyle = Maps.newEnumMap(SkillType.class);
    private final Map<SkillType, Color> specificColor = Maps.newEnumMap(SkillType.class);

    private Style defaultStyle;
    private Color defaultBarColor;

    public BossBarMessageAPI(FileConfiguration config) {
        if (BossBarAPI.is1_9) {
            concurrentBars = config.getInt("concurrentBars");

            parseSpecificConfig(config);
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
            Style style = specificStyle.get(skill);
            if (style == null) {
                style = defaultStyle;
            }

            Color color = specificColor.get(skill);
            if (style == null) {
                color = defaultBarColor;
            }

            bar = BossBarAPI.addBar(player, new TextComponent(newMessage), color, style, percent);
            bar.addPlayer(player);
            skillBars.put(skill, bar);
        } else {
            bar.setMessage(ComponentSerializer.toString(new TextComponent(newMessage)));
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

    private void parseSpecificConfig(FileConfiguration config) {
        for (SkillType skillType : SkillType.values()) {
            String skillName = skillType.getName().toLowerCase();
            Color color = parseColor(config.getString("bar.barColor." + skillName));
            if (color != null) {
                specificColor.put(skillType, color);
            }

            Style style = parseStyle(config.getString("bar.segments." + skillName));
            if (style != null) {
                specificStyle.put(skillType, style);
            }
        }

        defaultStyle = parseStyle(config.getString("segments"));
        if (defaultStyle == null) {
            defaultStyle = Style.PROGRESS;
        }

        defaultBarColor = parseColor(config.getString("color"));
        if (defaultBarColor == null) {
            defaultBarColor = Color.BLUE;
        }
    }

    private Color parseColor(String name) {
        if (name.trim().isEmpty()) {
            return null;
        }

        try {
            return Color.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException argumentException) {
            return null;
        }
    }

    private Style parseStyle(String name) {
        if (name.trim().isEmpty()) {
            return null;
        }

        try {
            BarStyle bukkitStyle =  BarStyle.valueOf(name.toUpperCase());
            Style style;
            switch (bukkitStyle) {
                case SEGMENTED_6:
                    style = Style.NOTCHED_6;
                    break;
                case SEGMENTED_10:
                    style = Style.NOTCHED_10;
                    break;
                case SEGMENTED_12:
                    style = Style.NOTCHED_12;
                    break;
                case SEGMENTED_20:
                    style = Style.NOTCHED_20;
                    break;
                default:
                case SOLID:
                    style = Style.PROGRESS;
                    break;
            }

            return style;
        } catch (IllegalArgumentException argumentException) {
            return null;
        }
    }
}
