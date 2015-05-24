package me.xeroun.mcmmoextras;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.mewin.util.Util;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.SetFlag;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardFlagSupport {

    private final WGCustomFlagsPlugin customFlagsPlugin;
    private final WorldGuardPlugin worldGuard;

    private SetFlag<SkillType> skillListFlag;

    public WorldGuardFlagSupport(Plugin worldGuardPlugin, Plugin customFlagsPlugin) {
        this.customFlagsPlugin = (WGCustomFlagsPlugin) customFlagsPlugin;
        this.worldGuard = (WorldGuardPlugin) worldGuardPlugin;
    }

    public boolean isForbiddenSkillInRegion(Player player, String skill) {
        if (customFlagsPlugin != null && worldGuard != null) {
            Set<SkillType> flag = Util.getFlagValue(worldGuard, player.getLocation(), skillListFlag);
            if (flag != null) {
                //if found return fast
                return flag.contains(SkillType.getSkill(skill));
            }
        }

        return false;
    }

    public void registerWorldGuardFlag() {
        if (customFlagsPlugin != null) {
            EnumFlag<SkillType> skillFlag = new EnumFlag<SkillType>("skill-show", SkillType.class);
            skillListFlag = new SetFlag<SkillType>("skill-show", skillFlag);
            customFlagsPlugin.addCustomFlag(skillListFlag);
        }
    }
}
