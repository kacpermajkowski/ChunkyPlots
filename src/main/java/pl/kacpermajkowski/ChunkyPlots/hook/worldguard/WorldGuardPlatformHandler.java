package pl.kacpermajkowski.ChunkyPlots.hook.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.World;

class WorldGuardPlatformHandler {

    public static boolean hasOverlappingRegion(World world, int chunkX, int chunkZ) {
        if (world == null) return false;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        if (container == null) {
            return false;
        }

        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        if (regionManager == null) {
            return false;
        }

        int minX = chunkX << 4;
        int minZ = chunkZ << 4;
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        int minY = -64;
        int maxY = 320;
        try {
            minY = world.getMinHeight();
            maxY = world.getMaxHeight();
        } catch (Throwable ignored) {}
        if (minY >= maxY) {
            minY = -64;
            maxY = 320;
        }

        BlockVector3 min = BlockVector3.at(minX, minY, minZ);
        BlockVector3 max = BlockVector3.at(maxX, maxY, maxZ);

        ProtectedRegion chunkRegion = new ProtectedCuboidRegion("chunkyplots_claim_check", min, max);
        ApplicableRegionSet applicable = regionManager.getApplicableRegions(chunkRegion);

        for (ProtectedRegion region : applicable) {
            if (region == null) continue;
            if ("__global__".equalsIgnoreCase(region.getId())) continue;
            if (region.getType() != null && region.getType().name().equalsIgnoreCase("GLOBAL")) continue;
            return true;
        }

        return false;
    }
}
