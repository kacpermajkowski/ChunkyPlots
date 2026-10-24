package pl.kacpermajkowski.ChunkyPlots.protections.redstone;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PistonProtection implements Listener {
    private final PlotPermissionChecker checker;

    public PistonProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonExtend(final BlockPistonExtendEvent event) {
        if (!canPistonAffectBlocks(event.getBlock(), event.getDirection(), event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonRetract(final BlockPistonRetractEvent event) {
        if (!canPistonAffectBlocks(event.getBlock(), event.getDirection(), event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    private boolean canPistonAffectBlocks(Block piston, BlockFace direction, List<Block> blocks) {
        Plot pistonPlot = checker.getPlot(piston);
        UUID pistonOwnerUuid = pistonPlot != null ? pistonPlot.getOwnerUUID() : null;

        Plot pistonHeadPlot = checker.getPlot(piston.getRelative(direction));
        List<Plot> blockPlots = getPlotsOfAllBlocks(blocks);
        List<Plot> affectedPlots = getPlotsAffectedByPiston(direction, blocks);

        if (pistonHeadPlot != null && !pistonHeadPlot.getFlag(PlotFlag.PISTON) && !checker.canOwnerAffect(pistonOwnerUuid, pistonHeadPlot)) return false;
        for (Plot p : affectedPlots) {
            if (!p.getFlag(PlotFlag.PISTON) && !checker.canOwnerAffect(pistonOwnerUuid, p)) return false;
        }
        for (Plot p : blockPlots) {
            if (!p.getFlag(PlotFlag.PISTON) && !checker.canOwnerAffect(pistonOwnerUuid, p)) return false;
        }

        return true;
    }

    private List<Plot> getPlotsOfAllBlocks(List<Block> blocks) {
        List<Plot> plots = new ArrayList<>();
        for (Block b : blocks) {
            Plot plot = checker.getPlot(b);
            if (plot != null && !plots.contains(plot)) {
                plots.add(plot);
            }
        }
        return plots;
    }

    private List<Plot> getPlotsAffectedByPiston(BlockFace direction, List<Block> blocks) {
        List<Plot> plots = new ArrayList<>();
        for (Block b : blocks) {
            Plot affectedPlot = checker.getPlot(b.getRelative(direction));
            if (affectedPlot != null && !plots.contains(affectedPlot)) {
                plots.add(affectedPlot);
            }
        }
        return plots;
    }
}
