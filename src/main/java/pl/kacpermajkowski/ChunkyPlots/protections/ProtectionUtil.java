package pl.kacpermajkowski.ChunkyPlots.protections;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;

import java.util.List;

@Deprecated
public abstract class ProtectionUtil {
    private static PlotPermissionChecker checker;

    public static void init(PlotPermissionChecker permissionChecker) {
        checker = permissionChecker;
    }

    public static boolean canPlayerAffect(Player player, Plot plot){
        return checker != null && checker.canPlayerAffect(player, plot);
    }

    public static boolean canPlayerAffect(Player player, Block block){
        return checker != null && checker.canPlayerAffect(player, block);
    }

    public static boolean canPlayerAffect(Player player, BlockState state){
        return checker != null && checker.canPlayerAffect(player, state);
    }

    public static boolean canPlayerAffect(Player player, Entity entity){
        return checker != null && checker.canPlayerAffect(player, entity);
    }

    public static boolean canPlayerAffectPlots(Player player, List<Plot> plots){
        return checker != null && checker.canPlayerAffectPlots(player, plots);
    }

    public static boolean canPlayerAffectBlocks(Player player, List<Block> blocks){
        return checker != null && checker.canPlayerAffectBlocks(player, blocks);
    }

    public static boolean canPlayerAffectEntities(Player player, List<Entity> entities){
        return checker != null && checker.canPlayerAffectEntities(player, entities);
    }

    public static boolean canBlockAffect(Block block, Plot affectedPlot){
        return checker != null && checker.canBlockAffect(block, affectedPlot);
    }

    public static boolean canBlockAffect(Block block, Block affectedBlock){
        return checker != null && checker.canBlockAffect(block, affectedBlock);
    }

    public static boolean canBlockAffect(Block block, BlockState affectedBlock){
        return checker != null && checker.canBlockAffect(block, affectedBlock);
    }

    public static boolean canBlockAffectEntity(Block block, Entity entity){
        return checker != null && checker.canBlockAffectEntity(block, entity);
    }

    public static boolean canEntityAffect(Entity entity, Entity affectedEntity){
        return checker != null && checker.canEntityAffect(entity, affectedEntity);
    }

    public static boolean canPlotAffectPlot(Plot source, Plot destination){
        return checker != null && checker.canPlotAffectPlot(source, destination);
    }

    public static boolean canPlotAffectPlots(Plot plot, List<Plot> plots){
        return checker != null && checker.canPlotAffectPlots(plot, plots);
    }

    public static Location getSafeBorderPoint(Entity entity, Chunk chunk) {
        return ChunkBorderUtil.getSafeBorderPoint(entity, chunk);
    }
}
