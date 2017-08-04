package me.xeroun.mcmmoextras;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardFlagSupport {

    private final McMMOExtras plugin;
    private final WorldGuardPlugin worldGuard;

    private EnumFlag<SkillType> skillListFlag = new EnumFlag<SkillType>("skill-show", SkillType.class);

    public WorldGuardFlagSupport(McMMOExtras plugin, Plugin worldGuard) {
        this.plugin = plugin;
        this.worldGuard = (WorldGuardPlugin) worldGuard;
    }

    public boolean isForbiddenSkillInRegion(Player player, SkillType skill) {
        if (worldGuard != null) {
            Location location = player.getLocation();
            ApplicableRegionSet regions = worldGuard.getRegionContainer().get(player.getWorld()).getApplicableRegions(location);

            LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
            return regions.queryAllValues(localPlayer, skillListFlag).contains(skill);
        }

        return false;
    }

    public void registerWorldGuardFlag() {
        if (worldGuard != null) {
            FlagRegistry flagRegistry = worldGuard.getFlagRegistry();
            try {
                // register our flag with the registry
                flagRegistry.register(skillListFlag);
            } catch (FlagConflictException e) {
                // some other plugin registered a flag by the same name already.
                // you may want to re-register with a different name, but this
                // could cause issues with saved flags in region files. it's better
                // to print a message to let the server admin know of the conflict
            }
        }
    }
}
