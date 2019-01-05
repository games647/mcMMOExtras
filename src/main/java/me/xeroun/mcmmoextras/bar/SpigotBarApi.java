package me.xeroun.mcmmoextras.bar;

import com.gmail.nossr50.datatypes.skills.PrimarySkill;
import com.google.common.base.Enums;
import com.google.common.base.Optional;

import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
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

    private final Map<UUID, EnumMap<PrimarySkill, BossBar>> bossBars = new HashMap<>();
    private final Deque<BossBar> oldBars = new LinkedList<>();
    private final int concurrentBars;

    private final Map<PrimarySkill, BarStyle> specificStyle = new EnumMap<>(PrimarySkill.class);
    private final Map<PrimarySkill, BarColor> specificColor = new EnumMap<>(PrimarySkill.class);

    public SpigotBarApi(ConfigurationSection config) {
        concurrentBars = config.getInt("concurrentBars");

        parseSpecificConfig(config);
    }

    @Override
    public void removeBar(Player player, PrimarySkill skill) {
        Map<PrimarySkill, BossBar> skillBars = bossBars.get(player.getUniqueId());
        if (skillBars != null) {
            BossBar bar = skillBars.get(skill);
            if (bar != null) {
                bar.setVisible(false);
            }
        }
    }

    @Override
    public void removeAllBars(Player player) {
        Map<PrimarySkill, BossBar> skillBars = bossBars.remove(player.getUniqueId());
        if (skillBars != null) {
            skillBars.values().forEach(bar -> bar.setVisible(false));
        }
    }

    @Override
    public void setMessage(Player player, PrimarySkill skill, String newMessage, double percent) {
        UUID uniqueId = player.getUniqueId();

        EnumMap<PrimarySkill, BossBar> skillBars = bossBars
                .computeIfAbsent(uniqueId, k -> new EnumMap<>(PrimarySkill.class));

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

        bar.setProgress(percent);
        oldBars.remove(bar);
        oldBars.addLast(bar);
        if (oldBars.size() > concurrentBars) {
            oldBars.removeFirst().setVisible(false);
        }
    }

    private void parseSpecificConfig(ConfigurationSection config) {
        BarStyle defaultStyle = parseStyle(config.getString("segments"), BarStyle.SOLID);
        BarColor defaultBarColor = parseColor(config.getString("color"), BarColor.WHITE);

        for (PrimarySkill skillType : PrimarySkill.values()) {
            String skillName = skillType.name().toLowerCase();
            BarColor color = parseColor(config.getString("bar.barColor." + skillName), defaultBarColor);
            specificColor.put(skillType, color);

            BarStyle style = parseStyle(config.getString("bar.segments." + skillName), defaultStyle);
            specificStyle.put(skillType, style);
        }
    }

    private BarColor parseColor(String name, BarColor def) {
        return parseEnum(name, BarColor.class, def);
    }

    private BarStyle parseStyle(String name, BarStyle def) {
        return parseEnum(name, BarStyle.class, def);
    }

    private <T extends Enum<T>> T parseEnum(String name, Class<T> enumClass, T def) {
        if (name == null || name.trim().isEmpty()) {
            return def;
        }

        Optional<T> style = Enums.getIfPresent(enumClass, name.trim().toUpperCase());
        if (style.isPresent()) {
            return style.get();
        }

        return def;
    }
}
