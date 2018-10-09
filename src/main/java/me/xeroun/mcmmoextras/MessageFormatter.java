package me.xeroun.mcmmoextras;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.util.NumberConversions;

public class MessageFormatter {

    private final McMMOExtras plugin;

    private final Pattern COLOR_CHAR = Pattern.compile("&", Pattern.LITERAL);
    private final Pattern variablePattern;
    private final Map<String, Function<McMMOPlayerXpGainEvent, String>> replacers = new HashMap<>();

    public MessageFormatter(McMMOExtras plugin) {
        this.plugin = plugin;

        replacers.put("@skill", this::getLocalizedName);
        replacers.put("@exp", event -> formatNumber(xp(event)));
        replacers.put("@gainedExp", event -> formatNumber(NumberConversions.round(event.getRawXpGained())));
        replacers.put("@remainingExp", event -> formatNumber(remainingXp(event)));
        replacers.put("@level",event -> formatNumber(event.getSkillLevel()));
        replacers.put("@nextLevel", event -> formatNumber(event.getSkillLevel() + 1));
        replacers.put("@reqExp", event -> formatNumber(requiredXp(event)));
        replacers.put("@percent", event ->
                NumberFormat.getPercentInstance().format(plugin.calculatePercent(xp(event), requiredXp(event))));

        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = replacers.keySet().iterator();
        while (iterator.hasNext()) {
            String var = iterator.next();
            builder.append('(').append(Pattern.quote(var)).append(')');
            if (iterator.hasNext()) {
                builder.append('|');
            }
        }

        variablePattern = Pattern.compile(builder.toString());
    }

    private int requiredXp(McMMOPlayerXpGainEvent event) {
        return ExperienceAPI.getXPToNextLevel(event.getPlayer(), event.getSkill().name());
    }

    private int remainingXp(McMMOPlayerXpGainEvent event) {
        return ExperienceAPI.getXPRemaining(event.getPlayer(), event.getSkill().name());
    }

    private int xp(McMMOPlayerXpGainEvent event) {
        return ExperienceAPI.getXP(event.getPlayer(), event.getSkill().name());
    }

    /**
     * Format the number including with 10^3 delimiters.
     * @param number the number that should be formatted
     * @return formatted number
     */
    private String formatNumber(int number) {
        return NumberFormat.getNumberInstance().format(number);
    }

    private String getLocalizedName(McMMOPlayerXpGainEvent event) {
        return event.getSkill().getName();
    }

    public String format(McMMOPlayerXpGainEvent event) {
        ChatColor color = ChatColor.GOLD;
        String colorPath = "bar.color." + event.getSkill().name().toLowerCase();
        if (plugin.getConfig().isSet(colorPath)) {
            //specific color for a skill type
            String configColor = plugin.getConfig().getString(colorPath);

            //filter the color char; otherwise we won't detect the color
            color = ChatColor.getByChar(COLOR_CHAR.matcher(configColor).replaceAll(""));
        }

        //StringBuilder is only compatible with Java 9+
        StringBuffer buffer = new StringBuffer(color.toString());

        String format = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("bar.format"));
        Matcher matcher = variablePattern.matcher(format);
        while (matcher.find()) {
            matcher.appendReplacement(buffer, replacers.get(matcher.group()).apply(event));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
