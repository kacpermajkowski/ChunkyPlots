package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;

public class AnimalTemptingProtection implements Listener {
    private final PlotPermissionChecker checker;

    public AnimalTemptingProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if(itemInMainHand.getType() == Material.AIR) return;

        Plot plot = checker.getPlot(event.getRightClicked());
        if(plot == null) return;

        if (plot.getFlag(PlotFlag.ANIMAL_TEMPT) || plot.getFlag(PlotFlag.ANIMAL_INTERACT)) return;

        if (!checker.canPlayerAffect(player, plot)) {
            event.setCancelled(true);
            ProtectionNotifier.notify(player, Message.PROTECTION_ENTITY_INTERACT, plot);
        }
    }
}
