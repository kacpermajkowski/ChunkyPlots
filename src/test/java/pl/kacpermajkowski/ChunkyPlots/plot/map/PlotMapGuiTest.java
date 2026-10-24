package pl.kacpermajkowski.ChunkyPlots.plot.map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.map.PlotMapCommand;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.border.PlotRelation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class PlotMapGuiTest {
    private Server mockServer;
    private PlotManager mockPlotManager;
    private Player mockPlayer;
    private World mockWorld;
    private Chunk mockChunk;
    private Inventory mockInventory;
    private Map<Integer, ItemStack> inventorySlots;

    private final UUID playerUuid = UUID.randomUUID();
    private final UUID strangerUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
        mockPlotManager = mock(PlotManager.class);
        mockPlayer = mock(Player.class);
        mockWorld = mock(World.class);
        mockChunk = mock(Chunk.class);

        inventorySlots = new HashMap<>();
        mockInventory = mock(Inventory.class);
        doAnswer(invocation -> {
            int slot = invocation.getArgument(0);
            ItemStack item = invocation.getArgument(1);
            inventorySlots.put(slot, item);
            return null;
        }).when(mockInventory).setItem(anyInt(), any());
        when(mockInventory.getItem(anyInt())).thenAnswer(invocation -> inventorySlots.get(invocation.getArgument(0)));
        when(mockInventory.getSize()).thenReturn(54);

        when(mockWorld.getName()).thenReturn("world");

        // Player standing at chunk [10, 20]
        when(mockChunk.getX()).thenReturn(10);
        when(mockChunk.getZ()).thenReturn(20);
        when(mockChunk.getWorld()).thenReturn(mockWorld);

        Location loc = mock(Location.class);
        when(loc.getChunk()).thenReturn(mockChunk);
        when(loc.getWorld()).thenReturn(mockWorld);

        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayer.getName()).thenReturn("Tester");
        when(mockPlayer.getLocation()).thenReturn(loc);
        when(mockPlayer.getWorld()).thenReturn(mockWorld);

        // Plug-in mock ItemStack creation to avoid headless registry bootstrapping in tests
        PlotMapHolder.setItemFactory(mat -> {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.getType()).thenReturn(mat);
            ItemMeta meta = mat == Material.PLAYER_HEAD ? mock(SkullMeta.class) : mock(ItemMeta.class);
            when(mockItem.getItemMeta()).thenReturn(meta);
            return mockItem;
        });
    }

    @AfterEach
    void tearDown() {
        PlotMapHolder.resetItemFactory();
        BukkitTestHelper.tearDownMockServer();
    }

    private InventoryClickEvent mockClick(int slot, ClickType clickType) {
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        when(event.getInventory()).thenReturn(mockInventory);
        when(event.getClickedInventory()).thenReturn(mockInventory);
        when(event.getWhoClicked()).thenReturn(mockPlayer);
        when(event.getRawSlot()).thenReturn(slot);
        when(event.getClick()).thenReturn(clickType);
        final boolean[] cancelled = {false};
        doAnswer(inv -> {
            cancelled[0] = inv.getArgument(0);
            return null;
        }).when(event).setCancelled(anyBoolean());
        when(event.isCancelled()).thenAnswer(inv -> cancelled[0]);
        return event;
    }

    @Test
    @DisplayName("PlotMapHolder pan and recenter logic works accurately")
    void testHolderPanningAndRecentering() {
        PlotMapHolder holder = new PlotMapHolder(mockPlayer, mockWorld, mockPlotManager, 10, 20);
        holder.setInventory(mockInventory);
        when(mockInventory.getHolder()).thenReturn(holder);

        assertEquals(10, holder.getCenterX());
        assertEquals(20, holder.getCenterZ());

        // Pan East (X + 1)
        holder.pan(1, 0);
        assertEquals(11, holder.getCenterX());
        assertEquals(20, holder.getCenterZ());

        // Pan North (Z - 1)
        holder.pan(0, -1);
        assertEquals(11, holder.getCenterX());
        assertEquals(19, holder.getCenterZ());

        // Pan West by 5
        holder.pan(-5, 0);
        assertEquals(6, holder.getCenterX());
        assertEquals(19, holder.getCenterZ());

        // Recenter to player [10, 20]
        holder.recenter();
        assertEquals(10, holder.getCenterX());
        assertEquals(20, holder.getCenterZ());

        // Set direct center
        holder.setCenter(50, -100);
        assertEquals(50, holder.getCenterX());
        assertEquals(-100, holder.getCenterZ());
    }

    @Test
    @DisplayName("PlotRelation is correctly determined for own, member, third-party and wilderness")
    void testDeterminePlotRelation() {
        PlotMapHolder holder = new PlotMapHolder(mockPlayer, mockWorld, mockPlotManager, 10, 20);

        // Wilderness (null plot)
        assertEquals(PlotRelation.WILDERNESS, holder.determineRelation(null));

        // Own plot
        Plot ownPlot = mock(Plot.class);
        when(ownPlot.isPlayerOwner(mockPlayer)).thenReturn(true);
        assertEquals(PlotRelation.OWN_PLOT, holder.determineRelation(ownPlot));

        // Member plot
        Plot memberPlot = mock(Plot.class);
        when(memberPlot.isPlayerOwner(mockPlayer)).thenReturn(false);
        when(memberPlot.isPlayerWhitelisted(mockPlayer)).thenReturn(true);
        assertEquals(PlotRelation.MEMBER_PLOT, holder.determineRelation(memberPlot));

        // Third-party plot
        Plot strangerPlot = mock(Plot.class);
        when(strangerPlot.isPlayerOwner(mockPlayer)).thenReturn(false);
        when(strangerPlot.isPlayerWhitelisted(mockPlayer)).thenReturn(false);
        assertEquals(PlotRelation.THIRD_PARTY_PLOT, holder.determineRelation(strangerPlot));
    }

    @Test
    @DisplayName("render() populates all 54 slots with chunk items and navigation controls")
    void testRenderPopulatesAllSlots() {
        // Prepare plots around player
        Plot ownPlot = mock(Plot.class);
        when(ownPlot.isPlayerOwner(mockPlayer)).thenReturn(true);
        when(ownPlot.getID()).thenReturn("own-plot");
        when(ownPlot.getOwnerName()).thenReturn("Tester");

        Plot strangerPlot = mock(Plot.class);
        when(strangerPlot.isPlayerOwner(mockPlayer)).thenReturn(false);
        when(strangerPlot.isPlayerWhitelisted(mockPlayer)).thenReturn(false);
        when(strangerPlot.getID()).thenReturn("stranger-plot");
        when(strangerPlot.getOwnerName()).thenReturn("Stranger");

        // Chunk [10, 20] is player location (own plot)
        when(mockPlotManager.getPlot(10, 20, "world")).thenReturn(ownPlot);
        // Chunk [11, 20] is stranger plot
        when(mockPlotManager.getPlot(11, 20, "world")).thenReturn(strangerPlot);
        // Other chunks return null (wilderness)

        PlotMapHolder holder = new PlotMapHolder(mockPlayer, mockWorld, mockPlotManager, 10, 20);
        holder.setInventory(mockInventory);
        when(mockInventory.getHolder()).thenReturn(holder);
        holder.render();

        // 45 viewport slots should be set
        for (int i = 0; i < 45; i++) {
            assertNotNull(inventorySlots.get(i), "Slot " + i + " should have a chunk item");
        }

        // Center slot is row 2, col 4 -> slot = 2 * 9 + 4 = 22
        // Since player is at [10, 20], slot 22 must be PLAYER_HEAD
        ItemStack playerStandingItem = inventorySlots.get(22);
        assertNotNull(playerStandingItem);
        assertEquals(Material.PLAYER_HEAD, playerStandingItem.getType());

        // Slot 23 is chunk [11, 20] -> stranger plot -> RED_STAINED_GLASS_PANE
        ItemStack strangerItem = inventorySlots.get(23);
        assertNotNull(strangerItem);
        assertEquals(Material.RED_STAINED_GLASS_PANE, strangerItem.getType());

        // Slot 21 is chunk [9, 20] -> wilderness -> YELLOW_STAINED_GLASS_PANE
        ItemStack wildernessItem = inventorySlots.get(21);
        assertNotNull(wildernessItem);
        assertEquals(Material.YELLOW_STAINED_GLASS_PANE, wildernessItem.getType());

        // Controls in row 5 (slots 45..53)
        assertEquals(Material.ARROW, inventorySlots.get(45).getType()); // West
        assertEquals(Material.ARROW, inventorySlots.get(46).getType()); // North
        assertEquals(Material.COMPASS, inventorySlots.get(47).getType()); // Recenter
        assertEquals(Material.ARROW, inventorySlots.get(48).getType()); // South
        assertEquals(Material.ARROW, inventorySlots.get(49).getType()); // East
        assertEquals(Material.FILLED_MAP, inventorySlots.get(50).getType()); // Info
        assertEquals(Material.BOOK, inventorySlots.get(51).getType()); // Legend
        assertEquals(Material.SUNFLOWER, inventorySlots.get(52).getType()); // Refresh
        assertEquals(Material.BARRIER, inventorySlots.get(53).getType()); // Close
    }

    @Test
    @DisplayName("PlotMapGuiListener cancels clicks and handles navigation buttons")
    void testGuiClickHandling() {
        PlotMapHolder holder = new PlotMapHolder(mockPlayer, mockWorld, mockPlotManager, 10, 20);
        holder.setInventory(mockInventory);
        when(mockInventory.getHolder()).thenReturn(holder);
        holder.render();

        PlotMapGuiListener listener = new PlotMapGuiListener();

        // Click slot 45 (West) -> centerX becomes 9
        InventoryClickEvent westClick = mockClick(45, ClickType.LEFT);
        listener.onInventoryClick(westClick);
        assertTrue(westClick.isCancelled());
        assertEquals(9, holder.getCenterX());

        // Shift click slot 49 (East) -> fast pan +5 -> centerX becomes 14
        InventoryClickEvent eastShiftClick = mockClick(49, ClickType.SHIFT_LEFT);
        listener.onInventoryClick(eastShiftClick);
        assertTrue(eastShiftClick.isCancelled());
        assertEquals(14, holder.getCenterX());

        // Click slot 47 (Recenter) -> resets to player's [10, 20]
        InventoryClickEvent recenterClick = mockClick(47, ClickType.LEFT);
        listener.onInventoryClick(recenterClick);
        assertTrue(recenterClick.isCancelled());
        assertEquals(10, holder.getCenterX());
        assertEquals(20, holder.getCenterZ());

        // Click viewport slot 0 (Row 0, Col 0: chunk [10 - 4 = 6, 20 - 2 = 18])
        InventoryClickEvent chunkClick = mockClick(0, ClickType.LEFT);
        listener.onInventoryClick(chunkClick);
        assertTrue(chunkClick.isCancelled());
        assertEquals(6, holder.getCenterX());
        assertEquals(18, holder.getCenterZ());

        // Click slot 53 (Close)
        InventoryClickEvent closeClick = mockClick(53, ClickType.LEFT);
        listener.onInventoryClick(closeClick);
        assertTrue(closeClick.isCancelled());
        verify(mockPlayer).closeInventory();
    }

    @Test
    @DisplayName("PlotMapGuiListener cancels inventory drag")
    void testInventoryDragCancelled() {
        PlotMapHolder holder = new PlotMapHolder(mockPlayer, mockWorld, mockPlotManager, 10, 20);
        holder.setInventory(mockInventory);
        when(mockInventory.getHolder()).thenReturn(holder);

        PlotMapGuiListener listener = new PlotMapGuiListener();
        InventoryDragEvent dragEvent = mock(InventoryDragEvent.class);
        when(dragEvent.getInventory()).thenReturn(mockInventory);

        listener.onInventoryDrag(dragEvent);
        verify(dragEvent).setCancelled(true);
    }

    @Test
    @DisplayName("PlotMapCommand executes and tab completes correctly")
    void testPlotMapCommand() {
        PlotMapManager mockMapManager = mock(PlotMapManager.class);
        PlotMapCommand cmd = new PlotMapCommand(mockMapManager);

        assertEquals("map", cmd.getName());
        assertEquals("/plot map [chunkX] [chunkZ]", cmd.getSyntax());
        assertEquals("chunkyplots.player", cmd.getPermission());

        // Execute without args opens map at player
        cmd.execute(mockPlayer, new String[0]);
        verify(mockMapManager).openMap(mockPlayer);

        // Execute with coordinates opens map at coords
        cmd.execute(mockPlayer, new String[]{"15", "-30"});
        verify(mockMapManager).openMap(mockPlayer, 15, -30);

        // Tab completion
        List<String> tab1 = cmd.getTabCompletion(mockPlayer, new String[]{""});
        assertEquals(List.of("10"), tab1);

        List<String> tab2 = cmd.getTabCompletion(mockPlayer, new String[]{"10", ""});
        assertEquals(List.of("20"), tab2);
    }
}
