package pl.kacpermajkowski.ChunkyPlots.plot.map;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.border.PlotRelation;

import java.util.ArrayList;
import java.util.List;

/**
 * InventoryHolder representing the interactive 54-slot Chunk Map GUI.
 * Displays a 9x5 viewport of chunks surrounding the center coordinates,
 * along with navigation controls in the bottom row.
 */
public class PlotMapHolder implements InventoryHolder {
    public static final int INVENTORY_SIZE = 54;
    public static final int VIEWPORT_ROWS = 5;
    public static final int VIEWPORT_COLS = 9;

    @FunctionalInterface
    public interface ItemFactory {
        ItemStack create(Material material);
    }

    private static ItemFactory itemFactory = ItemStack::new;

    public static void setItemFactory(ItemFactory factory) {
        itemFactory = factory != null ? factory : ItemStack::new;
    }

    public static void resetItemFactory() {
        itemFactory = ItemStack::new;
    }

    private final Player player;
    private final World world;
    private final PlotManager plotManager;

    private int centerX;
    private int centerZ;
    private Inventory inventory;

    public PlotMapHolder(Player player, World world, PlotManager plotManager, int initialCenterX, int initialCenterZ) {
        this.player = player;
        this.world = world;
        this.plotManager = plotManager;
        this.centerX = initialCenterX;
        this.centerZ = initialCenterZ;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public World getWorld() {
        return world;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public void pan(int deltaX, int deltaZ) {
        this.centerX += deltaX;
        this.centerZ += deltaZ;
        render();
    }

    public void recenter() {
        if (player != null && player.getLocation() != null && player.getLocation().getChunk() != null) {
            this.centerX = player.getLocation().getChunk().getX();
            this.centerZ = player.getLocation().getChunk().getZ();
        }
        render();
    }

    public void setCenter(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
        render();
    }

    /**
     * Renders all 45 chunk items in rows 0-4 and 9 control items in row 5.
     */
    public void render() {
        if (inventory == null || player == null || world == null) return;

        int playerChunkX = player.getLocation() != null && player.getLocation().getChunk() != null
                ? player.getLocation().getChunk().getX() : centerX;
        int playerChunkZ = player.getLocation() != null && player.getLocation().getChunk() != null
                ? player.getLocation().getChunk().getZ() : centerZ;
        boolean isSameWorld = player.getWorld() != null && player.getWorld().equals(world);

        // Update inventory title if supported
        try {
            if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() == inventory) {
                player.getOpenInventory().setTitle("§8Mapa działek: §6[" + centerX + ", " + centerZ + "]");
            }
        } catch (Throwable ignored) {}

        // Render 5x9 Viewport (Slots 0 to 44)
        for (int row = 0; row < VIEWPORT_ROWS; row++) {
            for (int col = 0; col < VIEWPORT_COLS; col++) {
                int slot = row * 9 + col;
                int chunkX = centerX + (col - 4);
                int chunkZ = centerZ + (row - 2);

                boolean isPlayerHere = isSameWorld && (playerChunkX == chunkX && playerChunkZ == chunkZ);
                boolean isCenterChunk = (centerX == chunkX && centerZ == chunkZ);

                Plot plot = plotManager != null ? plotManager.getPlot(chunkX, chunkZ, world.getName()) : null;
                ItemStack chunkItem = createChunkItem(chunkX, chunkZ, plot, isPlayerHere, isCenterChunk);
                inventory.setItem(slot, chunkItem);
            }
        }

        // Render Controls in Row 5 (Slots 45 to 53)
        renderControls(playerChunkX, playerChunkZ);
    }

    private ItemStack createChunkItem(int chunkX, int chunkZ, Plot plot, boolean isPlayerHere, boolean isCenterChunk) {
        PlotRelation relation = determineRelation(plot);

        ItemStack item;
        if (isPlayerHere) {
            item = itemFactory.create(Material.PLAYER_HEAD);
            ItemMeta meta = item != null ? item.getItemMeta() : null;
            if (meta instanceof SkullMeta skullMeta) {
                try {
                    skullMeta.setOwningPlayer(player);
                } catch (Throwable ignored) {}
            }
            if (meta != null) {
                meta.displayName(TextUtil.toComponent("§6§l✦ JESTEŚ TUTAJ ✦ §7[" + chunkX + ", " + chunkZ + "]"));
                List<Component> lore = new ArrayList<>();
                lore.add(TextUtil.toComponent("§eTwoja aktualna lokalizacja w grze"));
                lore.add(TextUtil.toComponent("§7Chunk: §f[" + chunkX + ", " + chunkZ + "]"));
                lore.add(TextUtil.toComponent("§7Bloki: §8X: " + (chunkX << 4) + ".." + ((chunkX << 4) + 15) + ", Z: " + (chunkZ << 4) + ".." + ((chunkZ << 4) + 15)));
                lore.add(Component.empty());

                if (plot == null) {
                    lore.add(TextUtil.toComponent("§7Status: §eDzicz (brak działki)"));
                    lore.add(TextUtil.toComponent("§aMożesz założyć tu własną działkę!"));
                } else {
                    lore.add(TextUtil.toComponent("§7Status: " + relation.getChatColor() + relation.getPlName()));
                    lore.add(TextUtil.toComponent("§7Właściciel: §f" + plot.getOwnerName()));
                    lore.add(TextUtil.toComponent("§7ID Działki: §f" + plot.getID()));
                    if (plot.getWhitelist() != null && !plot.getWhitelist().isEmpty()) {
                        lore.add(TextUtil.toComponent("§7Członkowie: §b" + plot.getWhitelist().size()));
                    }
                }

                lore.add(Component.empty());
                lore.add(TextUtil.toComponent("§8-------------------------"));
                lore.add(TextUtil.toComponent("§eLPM: §fWyśrodkuj mapę na tym chunku"));
                lore.add(TextUtil.toComponent("§6PPM: §fWyświetl szczegóły na czacie"));
                meta.lore(lore);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                item.setItemMeta(meta);
            }
            return item;
        }

        // Color coded stained glass pane based on relation
        Material material = switch (relation) {
            case OWN_PLOT -> Material.LIME_STAINED_GLASS_PANE;
            case MEMBER_PLOT -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
            case THIRD_PARTY_PLOT -> Material.RED_STAINED_GLASS_PANE;
            case WILDERNESS -> Material.YELLOW_STAINED_GLASS_PANE;
        };

        item = itemFactory.create(material);
        ItemMeta meta = item != null ? item.getItemMeta() : null;
        if (meta != null) {
            String title = switch (relation) {
                case OWN_PLOT -> "§a§lTwoja działka §7[" + chunkX + ", " + chunkZ + "]";
                case MEMBER_PLOT -> "§b§lDziałka członka §7[" + chunkX + ", " + chunkZ + "]";
                case THIRD_PARTY_PLOT -> "§c§lObca działka §7[" + chunkX + ", " + chunkZ + "]";
                case WILDERNESS -> "§e§lDzicz §7[" + chunkX + ", " + chunkZ + "]";
            };
            meta.displayName(TextUtil.toComponent(title));

            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.toComponent("§7Chunk: §f[" + chunkX + ", " + chunkZ + "]"));
            lore.add(TextUtil.toComponent("§7Bloki: §8X: " + (chunkX << 4) + ".." + ((chunkX << 4) + 15) + ", Z: " + (chunkZ << 4) + ".." + ((chunkZ << 4) + 15)));

            if (isCenterChunk) {
                lore.add(TextUtil.toComponent("§6✦ Celownik / Środek widoku ✦"));
                try {
                    meta.setEnchantmentGlintOverride(true);
                } catch (Throwable ignored) {}
            }

            lore.add(Component.empty());
            if (plot != null) {
                lore.add(TextUtil.toComponent("§7Właściciel: §f" + plot.getOwnerName()));
                lore.add(TextUtil.toComponent("§7ID Działki: §8" + plot.getID()));
                if (plot.getWhitelist() != null && !plot.getWhitelist().isEmpty()) {
                    lore.add(TextUtil.toComponent("§7Członkowie: §b" + plot.getWhitelist().size() + " graczy"));
                }
            } else {
                lore.add(TextUtil.toComponent("§aWolny chunk! Możesz go zająć."));
            }

            lore.add(Component.empty());
            lore.add(TextUtil.toComponent("§8-------------------------"));
            lore.add(TextUtil.toComponent("§eLPM: §fWyśrodkuj widok"));
            lore.add(TextUtil.toComponent("§6PPM: §fWyświetl szczegóły na czacie"));

            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void renderControls(int playerChunkX, int playerChunkZ) {
        // Slot 45: West
        inventory.setItem(45, createControlItem(
                Material.ARROW,
                "§e§l← Przesuń na Zachód (West)",
                List.of(
                        "§7LPM: §fPrzesuń mapę o 1 chunk na zachód (X - 1)",
                        "§7Shift + LPM / PPM: §fPrzesuń o 5 chunków (X - 5)"
                )
        ));

        // Slot 46: North
        inventory.setItem(46, createControlItem(
                Material.ARROW,
                "§e§l↑ Przesuń na Północ (North)",
                List.of(
                        "§7LPM: §fPrzesuń mapę o 1 chunk na północ (Z - 1)",
                        "§7Shift + LPM / PPM: §fPrzesuń o 5 chunków (Z - 5)"
                )
        ));

        // Slot 47: Center on player
        inventory.setItem(47, createControlItem(
                Material.COMPASS,
                "§a§l🎯 Wyśrodkuj na graczu",
                List.of(
                        "§7LPM: §fPowróć do chunku, na którym stoisz",
                        "§7Twój chunk: §e[" + playerChunkX + ", " + playerChunkZ + "]"
                )
        ));

        // Slot 48: South
        inventory.setItem(48, createControlItem(
                Material.ARROW,
                "§e§l↓ Przesuń na Południe (South)",
                List.of(
                        "§7LPM: §fPrzesuń mapę o 1 chunk na południe (Z + 1)",
                        "§7Shift + LPM / PPM: §fPrzesuń o 5 chunków (Z + 5)"
                )
        ));

        // Slot 49: East
        inventory.setItem(49, createControlItem(
                Material.ARROW,
                "§e§l→ Przesuń na Wschód (East)",
                List.of(
                        "§7LPM: §fPrzesuń mapę o 1 chunk na wschód (X + 1)",
                        "§7Shift + LPM / PPM: §fPrzesuń o 5 chunków (X + 5)"
                )
        ));

        // Slot 50: Coordinates / Position info
        double distChunks = Math.hypot(centerX - playerChunkX, centerZ - playerChunkZ);
        inventory.setItem(50, createControlItem(
                Material.FILLED_MAP,
                "§6§l🗺 Pozycja mapy",
                List.of(
                        "§7Centrum widoku: §eChunk [" + centerX + ", " + centerZ + "]",
                        "§7Świat: §f" + world.getName(),
                        "§7Odległość od Ciebie: §f" + String.format("%.1f", distChunks) + " chunków (" + (int) (distChunks * 16) + " bloków)",
                        "§7Obszar widoku: §89x5 chunków (45 chunków)",
                        "",
                        "§eWskazówka: §7Użyj §f/plot map <x> <z>§7, aby",
                        "§7skoczyć bezpośrednio do współrzędnych."
                )
        ));

        // Slot 51: Legend
        inventory.setItem(51, createControlItem(
                Material.BOOK,
                "§b§lℹ Legenda mapy",
                List.of(
                        "§a■ Twoja działka §7(kolor zielony)",
                        "§b■ Działka członka §7(kolor błękitny)",
                        "§c■ Obca działka §7(kolor czerwony)",
                        "§e■ Dzicz / Wolny chunk §7(kolor żółty)",
                        "§6■ Głowa gracza §7- Twoja aktualna pozycja",
                        "§8-------------------------",
                        "§7Kliknij dowolny chunk w widoku,",
                        "§7aby natychmiast wyśrodkować na nim mapę!"
                )
        ));

        // Slot 52: Refresh
        inventory.setItem(52, createControlItem(
                Material.SUNFLOWER,
                "§e§l🔄 Odśwież mapę",
                List.of(
                        "§7Kliknij, aby ponownie załadować status",
                        "§7wszystkich widocznych chunków."
                )
        ));

        // Slot 53: Close
        inventory.setItem(53, createControlItem(
                Material.BARRIER,
                "§c§l❌ Zamknij",
                List.of(
                        "§7Kliknij, aby zamknąć mapę."
                )
        ));
    }

    private ItemStack createControlItem(Material material, String name, List<String> lore) {
        ItemStack item = itemFactory.create(material);
        ItemMeta meta = item != null ? item.getItemMeta() : null;
        if (meta != null) {
            meta.displayName(TextUtil.toComponent(name));
            List<Component> componentLore = new ArrayList<>();
            for (String line : lore) {
                componentLore.add(line.isEmpty() ? Component.empty() : TextUtil.toComponent(line));
            }
            meta.lore(componentLore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    public PlotRelation determineRelation(Plot plot) {
        if (plot == null) {
            return PlotRelation.WILDERNESS;
        }
        if (player != null) {
            if (plot.isPlayerOwner(player)) {
                return PlotRelation.OWN_PLOT;
            }
            if (plot.isPlayerWhitelisted(player)) {
                return PlotRelation.MEMBER_PLOT;
            }
        }
        return PlotRelation.THIRD_PARTY_PLOT;
    }
}
