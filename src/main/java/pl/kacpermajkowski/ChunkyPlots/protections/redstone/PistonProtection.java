package pl.kacpermajkowski.ChunkyPlots.protections.redstone;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.Plot;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;

import java.util.ArrayList;
import java.util.List;


public class PistonProtection implements Listener {

    @EventHandler
    public void onBlockPistonExtend(final BlockPistonExtendEvent event){
        if(!canPistonAffectBlocks(event.getBlock(), event.getDirection(), event.getBlocks())){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onBlockPistonRetract(final BlockPistonRetractEvent event){
        if(!canPistonAffectBlocks(event.getBlock(), event.getDirection(), event.getBlocks())){
            event.setCancelled(true);
        }
    }

    private boolean canPistonAffectBlocks(Block piston, BlockFace direction, List<Block> blocks) {
        PlotManager plotManager = ChunkyPlots.getInstance().plotManager;

        Plot pistonPlot = plotManager.getPlotByBlock(piston);
        Plot pistonHeadPlot = plotManager.getPlotByBlock(piston.getRelative(direction));
        List<Plot> blockPlots = getPlotsOfAllBlocks(blocks);
        List<Plot> affectedPlots = getPlotsAffectedByPiston(direction, blocks);

        if(pistonPlot == null){
            if(pistonHeadPlot != null)
                return false;
            else if (blockPlots.isEmpty())
                return affectedPlots.isEmpty();
            else
                return false;
        } else {
            if(!pistonPlot.hasTheSameOwnerAs(pistonHeadPlot) && pistonHeadPlot != null)
                return false;
            else if(doPlotsMatchTheOwnerOrHaveNoOwner(affectedPlots, pistonPlot.getOwnerNickname()))
                return doPlotsMatchTheOwnerOrHaveNoOwner(blockPlots, pistonPlot.getOwnerNickname());
            else
                return false;
        }
    }

    private boolean doPlotsMatchTheOwnerOrHaveNoOwner(List<Plot> affectedPlots, String ownerNickname) {
        for(Plot p : affectedPlots){
            if(p != null) {
                if (!p.getOwnerNickname().equals(ownerNickname)) {
                    return false;
                }
            }
        }
        return true;
    }

    private List<Plot> getPlotsOfAllBlocks(List<Block> blocks) {
        ArrayList<Plot> plots = new ArrayList<>();
        for(Block b: blocks){
            PlotManager plotManager = ChunkyPlots.getInstance().plotManager;

            Plot plot = plotManager.getPlotByChunk(b.getChunk());

            if(!plots.contains(plot) && plot != null)
                plots.add(plot);
        }
        return plots;
    }

    private List<Plot> getPlotsAffectedByPiston(BlockFace direction, List<Block> blocks) {
        ArrayList<Plot> plots = new ArrayList<>();
        for(Block b: blocks){
            PlotManager plotManager = ChunkyPlots.getInstance().plotManager;

            Block affectedBlock = b.getRelative(direction);
            Plot affectedPlot = plotManager.getPlotByChunk(affectedBlock.getChunk());

            if(!plots.contains(affectedPlot) && affectedPlot != null)
                plots.add(affectedPlot);
        }
        return plots;
    }
}
