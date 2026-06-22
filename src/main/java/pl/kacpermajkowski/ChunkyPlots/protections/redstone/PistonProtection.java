package pl.kacpermajkowski.ChunkyPlots.protections.redstone;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

import java.util.ArrayList;
import java.util.List;

public class PistonProtection implements Listener {

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
        PlotManager plotManager = PlotManager.getInstance();
        Plot pistonPlot = plotManager.getPlot(piston);

        Player pistonOwner = null;
        if (pistonPlot != null) {
            pistonOwner = Bukkit.getPlayer(pistonPlot.getOwnerUUID());
        }

        Plot pistonHeadPlot = plotManager.getPlot(piston.getRelative(direction));
        List<Plot> blockPlots = getPlotsOfAllBlocks(blocks);
        List<Plot> affectedPlots = getPlotsAffectedByPiston(direction, blocks);

        if (!ProtectionUtil.canPlayerAffect(pistonOwner, pistonHeadPlot)) return false;
        if (!ProtectionUtil.canPlayerAffectPlots(pistonOwner, affectedPlots)) return false;
        if (!ProtectionUtil.canPlayerAffectPlots(pistonOwner, blockPlots)) return false;

        return true;
    }

    private List<Plot> getPlotsOfAllBlocks(List<Block> blocks) {
        List<Plot> plots = new ArrayList<>();
        for (Block b : blocks) {
            Plot plot = PlotManager.getInstance().getPlot(b.getChunk());
            if (plot != null && !plots.contains(plot))
                plots.add(plot);
        }
        return plots;
    }

    private List<Plot> getPlotsAffectedByPiston(BlockFace direction, List<Block> blocks) {
        List<Plot> plots = new ArrayList<>();
        for (Block b : blocks) {
            Plot affectedPlot = PlotManager.getInstance().getPlot(b.getRelative(direction).getChunk());
            if (affectedPlot != null && !plots.contains(affectedPlot))
                plots.add(affectedPlot);
        }
        return plots;
    }
}