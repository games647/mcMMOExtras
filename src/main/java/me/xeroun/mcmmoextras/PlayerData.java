package me.xeroun.mcmmoextras;

import com.gmail.nossr50.api.ExperienceAPI;

import me.confuser.barapi.BarAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerData {

    private final String playerName;

    private boolean enabled = true;
    private int time = 15;

    private String lastUsedSkill;

    public PlayerData(final String playerName) {
        this.playerName = playerName;

        //disappear timer
        new BukkitRunnable() {

            @Override
            public void run() {
                if (!enabled) {
                    cancel();
                    return;
                }

                time--;
                if (time <= 0) {
                    final Player player = Bukkit.getPlayer(playerName);
                    if (player == null) {
                        //Player went offline
                        cancel();
                        return;
                    }

                    BarAPI.removeBar(player);
                }
            }
        }.runTaskTimer(McMMOExtras.getInstance(), 0, 20L);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setLastUsedSkill(String skill) {
        lastUsedSkill = skill;
    }

    public void updateExpBar() {
        if (!enabled || lastUsedSkill == null) {
            return;
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            //player went offline
            return;
        }

        int exp = ExperienceAPI.getXP(player, lastUsedSkill);
        int requiredExp = ExperienceAPI.getXPToNextLevel(player, lastUsedSkill);
        float percent = calculatePercent(exp, requiredExp);

        String newMessage = formatMessage(player, exp, requiredExp, percent);
        String oldMessage = BarAPI.getMessage(player);
        if (!newMessage.equals(oldMessage)) {
            //if the player level ups the message would be different. BarAPI doesn't update the message if the player
            //already has a bar
            BarAPI.removeBar(player);
        }

        BarAPI.setMessage(player, newMessage, percent);

        time = McMMOExtras.getInstance().getConfig().getInt("bar.disappear");
    }

    private String formatMessage(Player player, int exp, int requiredExp, float percent) {
        //default value
        ChatColor color = ChatColor.GOLD;
        String colorPath = "bar.color." + lastUsedSkill.toLowerCase();
        if (McMMOExtras.getInstance().getConfig().isString(colorPath)) {
            //specific color for a skill type
            String configColor = McMMOExtras.getInstance().getConfig().getString(colorPath);
            //filter the color char; otherwise we won't detect the color
            color = ChatColor.getByChar(configColor.replace("&", ""));
        }

        String level = Integer.toString(ExperienceAPI.getLevel(player, lastUsedSkill));
        //custom variable replacement
        String format = McMMOExtras.getInstance().getConfig().getString("bar.format")
                .replace("@skill", lastUsedSkill)
                .replace("@level", level)
                .replace("@exp", Integer.toString(exp))
                .replace("@reqExp", Integer.toString(requiredExp))
                .replace("@percent", Float.toString(percent));

        return color + format;
    }

    private float calculatePercent(int exp, int requiredExp) {
        //progress for the next level
        float percent = exp * 100F / requiredExp;
        //filter invalid values from mcMMO
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }

        return percent;
    }
}
