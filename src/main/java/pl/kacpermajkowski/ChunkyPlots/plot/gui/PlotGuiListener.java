package pl.kacpermajkowski.ChunkyPlots.plot.gui;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.border.PlotBorderVisualizer;
import pl.kacpermajkowski.ChunkyPlots.plot.map.PlotMapManager;

import java.util.UUID;

/**
 * Handles inventory clicks and actions in all Plot Management GUI menus.
 */
public class PlotGuiListener implements Listener {
    private static final String SOUND_CLICK = "ui.button.click";
    private static final String SOUND_ORB = "entity.experience_orb.pickup";
    private static final String SOUND_ANVIL = "block.anvil.use";
    private static final String SOUND_BREAK = "entity.item.break";

    private final PlotGuiManager guiManager;

    public PlotGuiListener(PlotGuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PlotGuiHolder holder)) {
            return;
        }

        // Cancel all clicks in ChunkyPlots GUI menus
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getInventory()) {
            return;
        }

        int slot = event.getRawSlot();
        ClickType click = event.getClick();
        PlotGuiType type = holder.getType();
        Plot plot = holder.getPlot();

        switch (type) {
            case DASHBOARD -> handleDashboardClick(player, holder, plot, slot);
            case WHITELIST -> handleWhitelistClick(player, holder, plot, slot);
            case BLACKLIST -> handleBlacklistClick(player, holder, plot, slot);
            case PLAYER_SELECT -> handlePlayerSelectClick(player, holder, plot, slot);
            case PLOT_LIST -> handlePlotListClick(player, holder, slot, click);
            case CONFIRM_DISPOSE -> handleConfirmDisposeClick(player, holder, plot, slot);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof PlotGuiHolder) {
            event.setCancelled(true);
        }
    }

    private void handleDashboardClick(Player player, PlotGuiHolder holder, Plot plot, int slot) {
        Object action = holder.getSlotData(slot);
        if (action == null) return;

        String act = action.toString();
        switch (act) {
            case "WHITELIST" -> {
                if (plot == null) {
                    new MessageBuilder(Message.NULL_PLOT).sendChat(player);
                    return;
                }
                if (!plot.isPlayerOwner(player)) {
                    new MessageBuilder(Message.NOT_OWNER).sendChat(player);
                    return;
                }
                playSound(player, SOUND_CLICK, 1.2f);
                guiManager.openWhitelistGui(player, plot, 0);
            }
            case "BLACKLIST" -> {
                if (plot == null) {
                    new MessageBuilder(Message.NULL_PLOT).sendChat(player);
                    return;
                }
                if (!plot.isPlayerOwner(player)) {
                    new MessageBuilder(Message.NOT_OWNER).sendChat(player);
                    return;
                }
                playSound(player, SOUND_CLICK, 1.2f);
                guiManager.openBlacklistGui(player, plot, 0);
            }
            case "BORDER" -> {
                PlotBorderVisualizer visualizer = guiManager.getBorderVisualizer();
                if (visualizer != null) {
                    visualizer.toggle(player);
                }
                playSound(player, SOUND_CLICK, 1.3f);
                guiManager.openPlotDashboard(player, plot);
            }
            case "MAP" -> {
                PlotMapManager mapManager = guiManager.getMapManager();
                if (mapManager != null) {
                    playSound(player, SOUND_CLICK, 1.2f);
                    int cx = plot != null ? plot.getChunkX() : player.getLocation().getChunk().getX();
                    int cz = plot != null ? plot.getChunkZ() : player.getLocation().getChunk().getZ();
                    mapManager.openMap(player, cx, cz);
                }
            }
            case "PLOT_LIST" -> {
                playSound(player, SOUND_CLICK, 1.1f);
                guiManager.openPlotListGui(player, 0);
            }
            case "DISPOSE" -> {
                if (plot == null) return;
                if (!plot.isPlayerOwner(player)) {
                    new MessageBuilder(Message.NOT_OWNER).sendChat(player);
                    return;
                }
                playSound(player, SOUND_ANVIL, 1.0f);
                guiManager.openConfirmDisposeGui(player, plot);
            }
            case "CLOSE" -> {
                playSound(player, SOUND_CLICK, 0.8f);
                player.closeInventory();
            }
        }
    }

    private void handleWhitelistClick(Player player, PlotGuiHolder holder, Plot plot, int slot) {
        Object data = holder.getSlotData(slot);
        if (data instanceof String action) {
            switch (action) {
                case "BACK" -> {
                    playSound(player, SOUND_CLICK, 1.0f);
                    guiManager.openPlotDashboard(player, plot);
                }
                case "ADD" -> {
                    playSound(player, SOUND_CLICK, 1.2f);
                    guiManager.openPlayerSelectGui(player, plot, PlayerSelectAction.ADD_WHITELIST, 0);
                }
                case "PREV_PAGE" -> {
                    playSound(player, SOUND_CLICK, 1.1f);
                    guiManager.openWhitelistGui(player, plot, holder.getPage() - 1);
                }
                case "NEXT_PAGE" -> {
                    playSound(player, SOUND_CLICK, 1.1f);
                    guiManager.openWhitelistGui(player, plot, holder.getPage() + 1);
                }
                case "CLOSE" -> {
                    playSound(player, SOUND_CLICK, 0.8f);
                    player.closeInventory();
                }
            }
            return;
        }

        // Member removal click
        UUID targetUuid = holder.getSlotData(slot, UUID.class);
        if (targetUuid != null && plot != null) {
            String name = resolveName(targetUuid);
            plot.unwhitelistPlayer(targetUuid);
            playSound(player, SOUND_BREAK, 1.0f);
            new MessageBuilder(Message.REMOVED_MEMBER_FROM_PLOT).username(name).sendChat(player);
            guiManager.openWhitelistGui(player, plot, holder.getPage());
        }
    }

    private void handleBlacklistClick(Player player, PlotGuiHolder holder, Plot plot, int slot) {
        Object data = holder.getSlotData(slot);
        if (data instanceof String action) {
            switch (action) {
                case "BACK" -> {
                    playSound(player, SOUND_CLICK, 1.0f);
                    guiManager.openPlotDashboard(player, plot);
                }
                case "ADD" -> {
                    playSound(player, SOUND_CLICK, 1.2f);
                    guiManager.openPlayerSelectGui(player, plot, PlayerSelectAction.ADD_BLACKLIST, 0);
                }
                case "PREV_PAGE" -> {
                    playSound(player, SOUND_CLICK, 1.1f);
                    guiManager.openBlacklistGui(player, plot, holder.getPage() - 1);
                }
                case "NEXT_PAGE" -> {
                    playSound(player, SOUND_CLICK, 1.1f);
                    guiManager.openBlacklistGui(player, plot, holder.getPage() + 1);
                }
                case "CLOSE" -> {
                    playSound(player, SOUND_CLICK, 0.8f);
                    player.closeInventory();
                }
            }
            return;
        }

        // Blacklist removal (unban) click
        UUID targetUuid = holder.getSlotData(slot, UUID.class);
        if (targetUuid != null && plot != null) {
            String name = resolveName(targetUuid);
            plot.unblacklistPlayer(targetUuid);
            playSound(player, SOUND_CLICK, 1.2f);
            new MessageBuilder(Message.BLACKLIST_REMOVED_FROM_PLOT).username(name).sendChat(player);
            guiManager.openBlacklistGui(player, plot, holder.getPage());
        }
    }

    private void handlePlayerSelectClick(Player player, PlotGuiHolder holder, Plot plot, int slot) {
        Object data = holder.getSlotData(slot);
        if (data instanceof String action) {
            switch (action) {
                case "BACK" -> {
                    playSound(player, SOUND_CLICK, 1.0f);
                    if (holder.getSelectAction() == PlayerSelectAction.ADD_WHITELIST) {
                        guiManager.openWhitelistGui(player, plot, 0);
                    } else {
                        guiManager.openBlacklistGui(player, plot, 0);
                    }
                }
                case "PREV_PAGE" -> {
                    playSound(player, SOUND_CLICK, 1.1f);
                    guiManager.openPlayerSelectGui(player, plot, holder.getSelectAction(), holder.getPage() - 1);
                }
                case "NEXT_PAGE" -> {
                    playSound(player, SOUND_CLICK, 1.1f);
                    guiManager.openPlayerSelectGui(player, plot, holder.getSelectAction(), holder.getPage() + 1);
                }
                case "CLOSE" -> {
                    playSound(player, SOUND_CLICK, 0.8f);
                    player.closeInventory();
                }
            }
            return;
        }

        UUID targetUuid = holder.getSlotData(slot, UUID.class);
        if (targetUuid != null && plot != null) {
            String targetName = resolveName(targetUuid);
            if (holder.getSelectAction() == PlayerSelectAction.ADD_WHITELIST) {
                try {
                    plot.whitelistPlayer(targetUuid);
                    playSound(player, SOUND_ORB, 1.2f);
                    new MessageBuilder(Message.ADDED_MEMBER_TO_PLOT).username(targetName).sendChat(player);
                } catch (IllegalArgumentException e) {
                    new MessageBuilder(Message.CANNOT_ADD_BLACKLISTED_PLAYER_AS_MEMBER).sendChat(player);
                }
                guiManager.openWhitelistGui(player, plot, 0);
            } else {
                try {
                    plot.blacklistPlayer(targetUuid);
                    playSound(player, SOUND_ANVIL, 1.0f);
                    new MessageBuilder(Message.BLACKLIST_ADDED_TO_PLOT).username(targetName).sendChat(player);
                } catch (IllegalArgumentException e) {
                    new MessageBuilder(Message.ERROR_UNSPECIFIED).sendChat(player);
                }
                guiManager.openBlacklistGui(player, plot, 0);
            }
        }
    }

    private void handlePlotListClick(Player player, PlotGuiHolder holder, int slot, ClickType click) {
        Object data = holder.getSlotData(slot);
        if (data instanceof String action) {
            switch (action) {
                case "BACK" -> {
                    playSound(player, SOUND_CLICK, 1.0f);
                    guiManager.openMainGui(player);
                }
                case "PREV_PAGE" -> {
                    playSound(player, SOUND_CLICK, 1.1f);
                    guiManager.openPlotListGui(player, holder.getPage() - 1);
                }
                case "NEXT_PAGE" -> {
                    playSound(player, SOUND_CLICK, 1.1f);
                    guiManager.openPlotListGui(player, holder.getPage() + 1);
                }
                case "CLOSE" -> {
                    playSound(player, SOUND_CLICK, 0.8f);
                    player.closeInventory();
                }
            }
            return;
        }

        Plot selectedPlot = holder.getSlotData(slot, Plot.class);
        if (selectedPlot != null) {
            if (click.isRightClick()) {
                // Right click: open chunk map centered on that plot
                PlotMapManager mapManager = guiManager.getMapManager();
                if (mapManager != null) {
                    playSound(player, SOUND_CLICK, 1.2f);
                    mapManager.openMap(player, selectedPlot.getChunkX(), selectedPlot.getChunkZ());
                }
            } else {
                // Left click: open dashboard for this plot
                playSound(player, SOUND_CLICK, 1.2f);
                guiManager.openPlotDashboard(player, selectedPlot);
            }
        }
    }

    private void handleConfirmDisposeClick(Player player, PlotGuiHolder holder, Plot plot, int slot) {
        Object action = holder.getSlotData(slot);
        if (action == null) return;

        if ("CONFIRM".equals(action.toString())) {
            playSound(player, SOUND_ANVIL, 0.8f);
            player.closeInventory();
            guiManager.disposePlot(player, plot);
        } else if ("CANCEL".equals(action.toString())) {
            playSound(player, SOUND_CLICK, 1.0f);
            guiManager.openPlotDashboard(player, plot);
        }
    }

    private String resolveName(UUID uuid) {
        if (uuid == null) return "Unknown";
        if (Bukkit.getServer() != null) {
            try {
                OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                if (op != null && op.getName() != null) return op.getName();
            } catch (Throwable ignored) {}
        }
        return uuid.toString().substring(0, 8);
    }

    private void playSound(Player player, String soundKey, float pitch) {
        if (player == null || soundKey == null) return;
        try {
            player.playSound(player.getLocation(), soundKey, 0.6f, pitch);
        } catch (Throwable ignored) {}
    }
}
