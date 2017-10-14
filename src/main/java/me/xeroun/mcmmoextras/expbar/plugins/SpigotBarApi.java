package me.xeroun.mcmmoextras.expbar.plugins;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.google.common.base.Enums;
import com.google.common.base.Optional;
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

    private final Map<UUID, EnumMap<SkillType, BossBar>> bossBars = Maps.newHashMap();
    private final LinkedList<BossBar> oldBars = Lists.newLinkedList();
    private final int concurrentBars;

    private final Map<SkillType, BarStyle> specificStyle = Maps.newEnumMap(SkillType.class);
    private final Map<SkillType, BarColor> specificColor = Maps.newEnumMap(SkillType.class);

    public SpigotBarApi(ConfigurationSection config) {
        concurrentBars = config.getInt("concurrentBars");

        parseSpecificConfig(config);
    }

    @Override
    public void removeBar(Player player, SkillType skill) {
        EnumMap<SkillType, BossBar> skillBars = bossBars.get(player.getUniqueId());
        if (skillBars != null) {
            BossBar bar = skillBars.get(skill);
            if (bar != null) {
                bar.setVisible(false);
            }
        }
    }

    @Override
    public void removeAllBars(Player player) {
        EnumMap<SkillType, BossBar> skillBars = bossBars.remove(player.getUniqueId());
        if (skillBars != null) {
            skillBars.values().forEach(bar -> bar.setVisible(false));
        }
    }

    @Override
    public void setMessage(Player player, SkillType skill, String newMessage, float percent) {
        UUID uniqueId = player.getUniqueId();

        EnumMap<SkillType, BossBar> skillBars = bossBars
                .computeIfAbsent(uniqueId, k -> Maps.newEnumMap(SkillType.class));

        BossBar bar = skillBars.computeIfAbsent(skill, skillType -> {
            BarStyle style = specificStyle.get(skillType);
            BarColor color = specificColor.get(skillType);
            if (style == null || color == null) {
                return null;
            }

            BossBar tempBar = Bukkit.createBossBar(newMessage, color, style);
            tempBar.addPlayer(player);
            return tempBar;
        });

        bar.setTitle(newMessage);
        bar.setVisible(true);

        bar.setProgress(percent / 100);
        oldBars.remove(bar);
        oldBars.addLast(bar);
        if (oldBars.size() > concurrentBars) {
            oldBars.removeFirst().setVisible(false);
        }
    }

    private void parseSpecificConfig(ConfigurationSection config) {
        BarStyle defaultStyle = parseStyle(config.getString("segments"), BarStyle.SOLID);
        BarColor defaultBarColor = parseColor(config.getString("color"), BarColor.WHITE);

        for (SkillType skillType : SkillType.values()) {
            String skillName = skillType.getName().toLowerCase();
            BarColor color = parseColor(config.getString("bar.barColor." + skillName), defaultBarColor);
            specificColor.put(skillType, color);

            BarStyle style = parseStyle(config.getString("bar.segments." + skillName), defaultStyle);
            specificStyle.put(skillType, style);
        }
    }

    private BarColor parseColor(String name, BarColor def) {
        return parseEnum(name, def);
    }

    private BarStyle parseStyle(String name, BarStyle def) {
        return parseEnum(name, def);
    }

    private <T extends Enum<T>> T parseEnum(String name, T def) {
        if (name == null || name.trim().isEmpty()) {
            return def;
        }

        Optional<T> style = Enums.getIfPresent(def.getClass(), name.trim().toUpperCase());
        if (style.isPresent()) {
            return style.get();
        }

        return def;
    }
}
