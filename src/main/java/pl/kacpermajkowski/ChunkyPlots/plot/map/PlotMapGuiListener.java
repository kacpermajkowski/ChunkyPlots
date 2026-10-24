package pl.kacpermajkowski.ChunkyPlots.plot.map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.border.PlotRelation;

import java.util.List;

/**
 * Handles interactions inside the PlotMapHolder chest GUI.
 */
public class PlotMapGuiListener implements Listener {
    private static final String SOUND_CLICK = "ui.button.click";
    private static final String SOUND_PICKUP = "entity.experience_orb.pickup";

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PlotMapHolder holder)) {
            return;
        }

        // Cancel all clicks inside or involving this GUI to prevent item theft/moving
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Only handle clicks inside the top GUI inventory
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getInventory()) {
            return;
        }

        int slot = event.getRawSlot();
        ClickType click = event.getClick();
        boolean isFastPan = click.isShiftClick() || click.isRightClick();
        int step = isFastPan ? 5 : 1;

        // Viewport Chunks (Slots 0 to 44: 5 rows x 9 columns)
        if (slot >= 0 && slot < 45) {
            int row = slot / 9;
            int col = slot % 9;
            int targetChunkX = holder.getCenterX() + (col - 4);
            int targetChunkZ = holder.getCenterZ() + (row - 2);

            if (click.isRightClick()) {
                // Right click: send details to chat
                sendChunkDetails(player, holder, targetChunkX, targetChunkZ);
                playSound(player, SOUND_CLICK, 1.0f);
            } else {
                // Left click: center map on clicked chunk
                holder.setCenter(targetChunkX, targetChunkZ);
                playSound(player, SOUND_CLICK, 1.25f);
            }
            return;
        }

        // Navigation & Action Controls in Row 5 (Slots 45 to 53)
        switch (slot) {
            case 45 -> { // West (X - step)
                holder.pan(-step, 0);
                playSound(player, SOUND_CLICK, 1.1f);
            }
            case 46 -> { // North (Z - step)
                holder.pan(0, -step);
                playSound(player, SOUND_CLICK, 1.1f);
            }
            case 47 -> { // Recenter on player
                holder.recenter();
                playSound(player, SOUND_PICKUP, 1.2f);
            }
            case 48 -> { // South (Z + step)
                holder.pan(0, step);
                playSound(player, SOUND_CLICK, 1.1f);
            }
            case 49 -> { // East (X + step)
                holder.pan(step, 0);
                playSound(player, SOUND_CLICK, 1.1f);
            }
            case 50 -> { // Plot cycler (LMB = next, RMB = prev, Shift = list on chat)
                if (click.isShiftClick()) {
                    PlotManager pm = holder.getPlotManager();
                    List<Plot> accessible = pm != null ? pm.getAccessiblePlots(player) : List.of();
                    if (accessible.isEmpty()) {
                        player.sendMessage(TextUtil.toComponent("§6[ChunkyPlots] §cNie posiadasz ani nie należysz do żadnej działki."));
                    } else {
                        player.sendMessage(TextUtil.toComponent("§6=== §eTwoje Dostępne Działki (" + accessible.size() + ") §6==="));
                        for (Plot p : accessible) {
                            boolean isOwner = p.isPlayerOwner(player);
                            String prefix = isOwner ? "§a[Własna] " : "§b[Członek] ";
                            player.sendMessage(TextUtil.toComponent(prefix + "§e" + p.getMnemonicId() + " §7[" + p.getChunkX() + ", " + p.getChunkZ() + "; " + p.getWorldName() + "] §8- Właściciel: §f" + p.getOwnerName()));
                        }
                    }
                    playSound(player, SOUND_CLICK, 1.0f);
                } else if (click.isRightClick()) {
                    Plot prev = holder.jumpToPrevPlot();
                    if (prev != null) {
                        boolean isOwner = prev.isPlayerOwner(player);
                        player.sendMessage(TextUtil.toComponent("§6[ChunkyPlots] §7Przełączono na działkę: " + (isOwner ? "§a" : "§b") + prev.getMnemonicId() + " §7[" + prev.getChunkX() + ", " + prev.getChunkZ() + "] §8(" + prev.getWorldName() + ")"));
                        playSound(player, SOUND_CLICK, 1.2f);
                    } else {
                        player.sendMessage(TextUtil.toComponent("§6[ChunkyPlots] §cNie masz żadnych dostępnych działek do przeglądania."));
                        playSound(player, SOUND_CLICK, 0.6f);
                    }
                } else {
                    Plot next = holder.jumpToNextPlot();
                    if (next != null) {
                        boolean isOwner = next.isPlayerOwner(player);
                        player.sendMessage(TextUtil.toComponent("§6[ChunkyPlots] §7Przełączono na działkę: " + (isOwner ? "§a" : "§b") + next.getMnemonicId() + " §7[" + next.getChunkX() + ", " + next.getChunkZ() + "] §8(" + next.getWorldName() + ")"));
                        playSound(player, SOUND_CLICK, 1.2f);
                    } else {
                        player.sendMessage(TextUtil.toComponent("§6[ChunkyPlots] §cNie masz żadnych dostępnych działek do przeglądania."));
                        playSound(player, SOUND_CLICK, 0.6f);
                    }
                }
            }
            case 51 -> { // Position & Legend
                double dist = holder.getDistanceToPlayer();
                player.sendMessage(TextUtil.toComponent("§6=== §ePozycja i Legenda Mapy §6==="));
                player.sendMessage(TextUtil.toComponent("§7Środek widoku: §eChunk [" + holder.getCenterX() + ", " + holder.getCenterZ() + "] §7w §f" + (holder.getWorld() != null ? holder.getWorld().getName() : "brak")));
                if (dist >= 0) {
                    player.sendMessage(TextUtil.toComponent("§7Odległość od Ciebie: §f" + String.format("%.1f", dist) + " chunków (" + (int) (dist * 16) + " bloków)"));
                } else {
                    player.sendMessage(TextUtil.toComponent("§7Odległość: §8(inny świat niż Twoja aktualna pozycja)"));
                }
                player.sendMessage(TextUtil.toComponent("§a■ Twoja działka §7- pełne prawa budowania i zarządzania"));
                player.sendMessage(TextUtil.toComponent("§b■ Działka członka §7- jesteś dodany na białą listę"));
                player.sendMessage(TextUtil.toComponent("§c■ Obca działka §7- działka innego gracza (chroniona)"));
                player.sendMessage(TextUtil.toComponent("§e■ Dzicz §7- wolny chunk, możesz założyć działkę!"));
                playSound(player, SOUND_CLICK, 1.0f);
            }
            case 52 -> { // Refresh
                holder.render();
                playSound(player, SOUND_CLICK, 1.4f);
            }
            case 53 -> { // Close
                playSound(player, SOUND_CLICK, 0.8f);
                player.closeInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof PlotMapHolder) {
            event.setCancelled(true);
        }
    }

    private void sendChunkDetails(Player player, PlotMapHolder holder, int chunkX, int chunkZ) {
        PlotManager pm = holder.getPlotManager();
        Plot targetPlot = (pm != null && holder.getWorld() != null)
                ? pm.getPlot(chunkX, chunkZ, holder.getWorld().getName())
                : null;

        int minBlockX = chunkX << 4;
        int maxBlockX = minBlockX + 15;
        int minBlockZ = chunkZ << 4;
        int maxBlockZ = minBlockZ + 15;

        player.sendMessage(TextUtil.toComponent("§8----------------------------------------------------"));
        player.sendMessage(TextUtil.toComponent("§6[ChunkyPlots] §eInformacje o chunku: §f[" + chunkX + ", " + chunkZ + "]"));
        player.sendMessage(TextUtil.toComponent("§7Współrzędne bloków: §8X: " + minBlockX + ".." + maxBlockX + ", Z: " + minBlockZ + ".." + maxBlockZ));
        if (targetPlot == null) {
            player.sendMessage(TextUtil.toComponent("§7Status: §eDzicz (brak działki) - wolny teren"));
        } else {
            PlotRelation relation = holder.determineRelation(targetPlot);
            player.sendMessage(TextUtil.toComponent("§7Status: " + relation.getChatColor() + relation.getPlName()));
            player.sendMessage(TextUtil.toComponent("§7ID Działki: §f" + targetPlot.getID()));
            player.sendMessage(TextUtil.toComponent("§7Kod: §e" + targetPlot.getMnemonicId()));
            player.sendMessage(TextUtil.toComponent("§7Właściciel: §f" + targetPlot.getOwnerName()));
            if (targetPlot.getWhitelist() != null && !targetPlot.getWhitelist().isEmpty()) {
                player.sendMessage(TextUtil.toComponent("§7Członkowie: §b" + targetPlot.getWhitelist().size() + " graczy"));
            }
        }
        player.sendMessage(TextUtil.toComponent("§8----------------------------------------------------"));
    }

    private void playSound(Player player, String soundKey, float pitch) {
        if (player == null || soundKey == null) return;
        try {
            player.playSound(player.getLocation(), soundKey, 0.6f, pitch);
        } catch (Throwable ignored) {}
    }
}
