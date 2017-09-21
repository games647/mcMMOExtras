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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SpigotBarApi implements BossAPI {

    private final Map<UUID, EnumMap<SkillType, BossBar>> bossbars = Maps.newHashMap();
    private final LinkedList<BossBar> oldBars = Lists.newLinkedList();
    private final int concurrentBars;

    private final Map<SkillType, BarStyle> specificStyle = Maps.newEnumMap(SkillType.class);
    private final Map<SkillType, BarColor> specificColor = Maps.newEnumMap(SkillType.class);

    private BarStyle defaultStyle;
    private BarColor defaultBarColor;

    public SpigotBarApi(ConfigurationSection config) {
        concurrentBars = config.getInt("concurrentBars");

        parseSpecificConfig(config);
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
                skillBars.values().forEach(bar -> bar.setVisible(false));
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

        EnumMap<SkillType, BossBar> skillBars = bossbars
                .computeIfAbsent(uniqueId, k -> Maps.newEnumMap(SkillType.class));

        BossBar bar = skillBars.get(skill);
        if (bar == null) {
            BarStyle style = specificStyle.get(skill);
            if (style == null) {
                style = defaultStyle;
            }

            BarColor color = specificColor.get(skill);
            if (color == null) {
                color = defaultBarColor;
            }

            bar = Bukkit.createBossBar(newMessage, color, style);
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

    private void parseSpecificConfig(ConfigurationSection config) {
        for (SkillType skillType : SkillType.values()) {
            String skillName = skillType.getName().toLowerCase();
            BarColor color = parseColor(config.getString("bar.barColor." + skillName));
            if (color != null) {
                specificColor.put(skillType, color);
            }

            BarStyle style = parseStyle(config.getString("bar.segments." + skillName));
            if (style != null) {
                specificStyle.put(skillType, style);
            }
        }

        defaultStyle = parseStyle(config.getString("segments"));
        if (defaultStyle == null) {
            defaultStyle = BarStyle.SOLID;
        }

        defaultBarColor = parseColor(config.getString("color"));
        if (defaultBarColor == null) {
            defaultBarColor = BarColor.BLUE;
        }
    }

    private BarColor parseColor(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        try {
            return BarColor.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException argumentException) {
            return null;
        }
    }

    private BarStyle parseStyle(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        try {
            return BarStyle.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException argumentException) {
            return null;
        }
    }
}
