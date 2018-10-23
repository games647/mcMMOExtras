package me.xeroun.mcmmoextras;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardFlagSupport {

    private final WorldGuard worldGuard = WorldGuard.getInstance();
    private final SetFlag<SkillType> skillListFlag = new SetFlag<>("skill-ignore",
            new EnumFlag<>("skill", SkillType.class));

    public boolean isForbidden(Player player, SkillType skill) {
        if (worldGuard != null) {
            Location location = player.getLocation();

            World world = new BukkitWorld(player.getWorld());
            RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(world);
            if (regionManager == null) {
                return false;
            }

            ApplicableRegionSet regions = regionManager.getApplicableRegions(toVector(location));
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            Set<SkillType> result = regions.queryValue(localPlayer, skillListFlag);

            if (result != null) {
                return result.contains(skill);
            }
        }

        return false;
    }

    private Vector toVector(Location loc) {
        return new Vector(loc.getX(), loc.getY(), loc.getZ());
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
