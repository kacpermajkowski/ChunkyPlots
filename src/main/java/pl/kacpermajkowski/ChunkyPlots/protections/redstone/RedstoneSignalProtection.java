package pl.kacpermajkowski.ChunkyPlots.protections.redstone;

import org.bukkit.block.BlockFace;
import org.bukkit.event.Listener;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

public class RedstoneSignalProtection implements Listener {
    private static final BlockFace[] CARDINAL_FACES = {
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.UP,
            BlockFace.DOWN
    };

    private final PlotPermissionChecker checker;

    public RedstoneSignalProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onRedstoneSignal(final BlockRedstoneEvent event) {
//        final Block block = event.getBlock();
//        final Plot plot = checker.getPlot(block);
//        if(plot == null) return;
//
//        boolean cancelled = false;
//        for(BlockFace face : CARDINAL_FACES) {
//            final Block neighbor = block.getRelative(face);
//            if (neighbor.getBlockPower() == 0) continue;
//
//            final Plot neighborPlot = checker.getPlot(neighbor);
//            if(checker.canPlotAffectPlot(neighborPlot, plot)){
//                cancelled = true;
//                break;
//            }
//        }
//
//        if(cancelled) {
//            event.setNewCurrent(event.getOldCurrent());
//        }
//    }
}
