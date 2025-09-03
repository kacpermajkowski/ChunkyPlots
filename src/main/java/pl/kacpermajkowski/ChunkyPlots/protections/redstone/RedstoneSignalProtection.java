package pl.kacpermajkowski.ChunkyPlots.protections.redstone;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

import java.util.HashSet;
import java.util.Set;

public class RedstoneSignalProtection implements Listener {
    private static final BlockFace[] CARDINAL_FACES = {
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.UP,
            BlockFace.DOWN
    };

//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onRedstoneSignal(final BlockRedstoneEvent event) {
//        final Block block = event.getBlock();
//        final Plot plot = PlotManager.getInstance().getPlot(block);
//        if(plot == null) return;
//
//        boolean cancelled = false;
//        for(BlockFace face : CARDINAL_FACES) {
//            final Block neighbor = block.getRelative(face);
//            if (neighbor.getBlockPower() == 0) continue;
//
//            final Plot neighborPlot = PlotManager.getInstance().getPlot(neighbor);
//            if(ProtectionUtil.canPlotAffectPlot(neighborPlot, plot)){
//                cancelled = true;
//                break;
//            }
//        }
//
//        if(cancelled) {
//            event.setNewCurrent(event.getOldCurrent());
//        }
//    }

//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onBlockPowered(final BlockPhysicsEvent event) {
//        final Block block = event.getBlock();
//        if(!block.is)
//        final Plot plot = PlotManager.getInstance().getPlot(block);
//        if(plot == null) return;
//
//        boolean cancelled = false;
//        for(BlockFace face : CARDINAL_FACES) {
//            final Block neighbor = block.getRelative(face);
//            if (neighbor.getBlockPower() == 0) continue;
//
//            final Plot neighborPlot = PlotManager.getInstance().getPlot(neighbor);
//            if(ProtectionUtil.canPlotAffectPlot(neighborPlot, plot)){
//                cancelled = true;
//                break;
//            }
//        }
//
//        if(cancelled) {
//            event.setNewCurrent(event.getOldCurrent());
//
//        }
//    }
}
