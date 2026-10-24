package pl.kacpermajkowski.ChunkyPlots.protections.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

import java.util.Iterator;

public class StructureOvergrowProtection implements Listener {

    @FunctionalInterface
    public interface ItemDropHandler {
        void dropItem(World world, Location location, Material material);
    }

    private static ItemDropHandler itemDropHandler = (world, location, material) -> {
        world.dropItem(location, new ItemStack(material, 1));
    };

    public static void setItemDropHandler(ItemDropHandler handler) {
        itemDropHandler = handler != null ? handler : (w, l, m) -> w.dropItem(l, new ItemStack(m, 1));
    }

    public static void resetItemDropHandler() {
        itemDropHandler = (w, l, m) -> w.dropItem(l, new ItemStack(m, 1));
    }

    private final PlotPermissionChecker checker;
    private final Config config;

    public StructureOvergrowProtection(PlotPermissionChecker checker) {
        this(checker, Config.getInstance());
    }

    public StructureOvergrowProtection(PlotPermissionChecker checker, Config config) {
        this.checker = checker;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStructureGrow(StructureGrowEvent event) {
        if (event.isCancelled()) return;

        Plot triggerPlot = checker.getPlot(event.getLocation());
        boolean dontGenerateOvergrowth = config != null ? config.isDontGenerateOvergrowingBlocks() : false;
        boolean dropSaplingOnFail = config != null ? config.isDropSaplingOnFail() : true;

        if (dontGenerateOvergrowth) {
            // Trim mode: remove overgrowing blocks so only in-bounds blocks generate
            Plot firstViolatedPlot = null;
            Iterator<BlockState> it = event.getBlocks().iterator();
            while (it.hasNext()) {
                BlockState blockState = it.next();
                Plot affectedPlot = checker.getPlot(blockState.getLocation());
                if (isOvergrowing(triggerPlot, affectedPlot)) {
                    it.remove();
                    if (firstViolatedPlot == null) {
                        firstViolatedPlot = affectedPlot;
                    }
                }
            }

            // If no blocks remain at all, fail the entire growth
            if (event.getBlocks().isEmpty()) {
                failGrowth(event, dropSaplingOnFail, firstViolatedPlot);
            }
        } else {
            // Cancel mode: cancel entire structure growth if any block extends beyond bounds
            for (BlockState blockState : event.getBlocks()) {
                Plot affectedPlot = checker.getPlot(blockState.getLocation());
                if (isOvergrowing(triggerPlot, affectedPlot)) {
                    failGrowth(event, dropSaplingOnFail, affectedPlot);
                    return;
                }
            }
        }
    }

    public boolean isOvergrowing(Plot triggerPlot, Plot affectedPlot) {
        if (affectedPlot != null && affectedPlot.getFlag(PlotFlag.STRUCTURE_GROW)) {
            return false;
        }
        return affectedPlot != null && (triggerPlot == null || !affectedPlot.hasTheSameOwnerAs(triggerPlot));
    }

    private void failGrowth(StructureGrowEvent event, boolean dropSapling, Plot violatedPlot) {
        event.setCancelled(true);

        if (dropSapling && event.getLocation() != null && event.getLocation().getWorld() != null) {
            Block sapling = event.getLocation().getBlock();
            if (sapling != null && sapling.getType() != Material.AIR) {
                try {
                    itemDropHandler.dropItem(sapling.getWorld(), event.getLocation(), sapling.getType());
                } catch (Throwable ignored) {}
                sapling.setType(Material.AIR);
            }
        }

        Player player = event.getPlayer();
        if (player != null) {
            ProtectionNotifier.notify(player, Message.PROTECTION_STRUCTURE_GROW, violatedPlot);
        }
    }
}
