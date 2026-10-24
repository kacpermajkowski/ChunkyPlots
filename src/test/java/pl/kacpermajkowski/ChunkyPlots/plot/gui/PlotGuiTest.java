package pl.kacpermajkowski.ChunkyPlots.plot.gui;

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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.gui.PlotGuiCommand;
import pl.kacpermajkowski.ChunkyPlots.hook.vault.VaultHook;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.border.PlotBorderVisualizer;
import pl.kacpermajkowski.ChunkyPlots.plot.group.Group;
import pl.kacpermajkowski.ChunkyPlots.plot.limit.PlotLimitManager;
import pl.kacpermajkowski.ChunkyPlots.plot.map.PlotMapManager;
import pl.kacpermajkowski.ChunkyPlots.user.User;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class PlotGuiTest {
    private Server mockServer;
    private PlotManager mockPlotManager;
    private UserManager mockUserManager;
    private PlotBorderVisualizer mockBorderVisualizer;
    private PlotLimitManager mockLimitManager;
    private VaultHook mockVaultHook;
    private PlotMapManager mockMapManager;
    private PlotGuiManager guiManager;

    private Player mockPlayer;
    private PlayerInventory mockPlayerInv;
    private World mockWorld;
    private Chunk mockChunk;
    private Plot mockPlot;

    private final UUID playerUuid = UUID.randomUUID();
    private final UUID friendUuid = UUID.randomUUID();
    private final UUID enemyUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
        mockPlotManager = mock(PlotManager.class);
        mockUserManager = mock(UserManager.class);
        mockBorderVisualizer = mock(PlotBorderVisualizer.class);
        mockLimitManager = mock(PlotLimitManager.class);
        mockVaultHook = mock(VaultHook.class);
        mockMapManager = mock(PlotMapManager.class);

        guiManager = new PlotGuiManager(
                null, mockPlotManager, mockUserManager,
                mockBorderVisualizer, mockLimitManager, mockVaultHook, mockMapManager
        );

        mockPlayer = mock(Player.class);
        mockPlayerInv = mock(PlayerInventory.class);
        mockWorld = mock(World.class);
        mockChunk = mock(Chunk.class);

        when(mockWorld.getName()).thenReturn("world");
        when(mockChunk.getX()).thenReturn(5);
        when(mockChunk.getZ()).thenReturn(10);
        when(mockChunk.getWorld()).thenReturn(mockWorld);

        Location loc = mock(Location.class);
        when(loc.getChunk()).thenReturn(mockChunk);
        when(loc.getWorld()).thenReturn(mockWorld);

        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayer.getName()).thenReturn("PlotOwner");
        when(mockPlayer.getLocation()).thenReturn(loc);
        when(mockPlayer.getWorld()).thenReturn(mockWorld);
        when(mockPlayer.getInventory()).thenReturn(mockPlayerInv);

        mockPlot = new Plot(playerUuid, 5, 10, "world", UUID.randomUUID());

        PlotGuiHolder.setItemFactory(mat -> {
            ItemStack item = mock(ItemStack.class);
            when(item.getType()).thenReturn(mat);
            ItemMeta meta = mat == Material.PLAYER_HEAD ? mock(SkullMeta.class) : mock(ItemMeta.class);
            when(item.getItemMeta()).thenReturn(meta);
            return item;
        });

        // Mock Bukkit.createInventory to return a working mock inventory
        when(mockServer.createInventory(any(), anyInt(), any(net.kyori.adventure.text.Component.class)))
                .thenAnswer(inv -> {
                    PlotGuiHolder h = inv.getArgument(0);
                    int size = inv.getArgument(1);
                    Inventory mockInv = mock(Inventory.class);
                    when(mockInv.getHolder()).thenReturn(h);
                    when(mockInv.getSize()).thenReturn(size);
                    Map<Integer, ItemStack> slots = new HashMap<>();
                    doAnswer(set -> {
                        slots.put(set.getArgument(0), set.getArgument(1));
                        return null;
                    }).when(mockInv).setItem(anyInt(), any());
                    when(mockInv.getItem(anyInt())).thenAnswer(get -> slots.get(get.getArgument(0)));
                    return mockInv;
                });
    }

    @AfterEach
    void tearDown() {
        PlotGuiHolder.resetItemFactory();
        BukkitTestHelper.tearDownMockServer();
    }

    private InventoryClickEvent mockClick(PlotGuiHolder holder, int slot, ClickType clickType) {
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        when(event.getInventory()).thenReturn(holder.getInventory());
        when(event.getClickedInventory()).thenReturn(holder.getInventory());
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
    @DisplayName("openPlotDashboard opens dashboard with all controls")
    void testOpenPlotDashboard() {
        guiManager.openPlotDashboard(mockPlayer, mockPlot);

        verify(mockPlayer).openInventory(any(Inventory.class));
    }

    @Test
    @DisplayName("Dashboard clicks dispatch to whitelist, blacklist, border, map, and dispose")
    void testDashboardClickNavigation() {
        PlotGuiListener listener = new PlotGuiListener(guiManager);

        PlotGuiHolder holder = new PlotGuiHolder(PlotGuiType.DASHBOARD, mockPlayer, mockPlot);
        Inventory inv = mock(Inventory.class);
        when(inv.getHolder()).thenReturn(holder);
        holder.setInventory(inv);

        // Slot 19: Whitelist
        holder.setSlotData(19, "WHITELIST");
        InventoryClickEvent wlClick = mockClick(holder, 19, ClickType.LEFT);
        listener.onInventoryClick(wlClick);
        assertTrue(wlClick.isCancelled());
        verify(mockPlayer, atLeastOnce()).openInventory(any(Inventory.class));

        // Slot 21: Blacklist
        holder.setSlotData(21, "BLACKLIST");
        InventoryClickEvent blClick = mockClick(holder, 21, ClickType.LEFT);
        listener.onInventoryClick(blClick);
        assertTrue(blClick.isCancelled());

        // Slot 23: Border
        holder.setSlotData(23, "BORDER");
        InventoryClickEvent borderClick = mockClick(holder, 23, ClickType.LEFT);
        listener.onInventoryClick(borderClick);
        assertTrue(borderClick.isCancelled());
        verify(mockBorderVisualizer).toggle(mockPlayer);

        // Slot 25: Map
        holder.setSlotData(25, "MAP");
        InventoryClickEvent mapClick = mockClick(holder, 25, ClickType.LEFT);
        listener.onInventoryClick(mapClick);
        assertTrue(mapClick.isCancelled());
        verify(mockMapManager).openMap(mockPlayer, 5, 10);

        // Slot 29: Plot list
        holder.setSlotData(29, "PLOT_LIST");
        InventoryClickEvent listClick = mockClick(holder, 29, ClickType.LEFT);
        listener.onInventoryClick(listClick);
        assertTrue(listClick.isCancelled());

        // Slot 33: Dispose
        holder.setSlotData(33, "DISPOSE");
        InventoryClickEvent disposeClick = mockClick(holder, 33, ClickType.LEFT);
        listener.onInventoryClick(disposeClick);
        assertTrue(disposeClick.isCancelled());
    }

    @Test
    @DisplayName("Whitelist GUI clicking member skull removes them from plot whitelist")
    void testWhitelistRemoval() {
        mockPlot.whitelistPlayer(friendUuid);
        assertTrue(mockPlot.isPlayerWhitelisted(friendUuid));

        PlotGuiListener listener = new PlotGuiListener(guiManager);
        PlotGuiHolder holder = new PlotGuiHolder(PlotGuiType.WHITELIST, mockPlayer, mockPlot);
        Inventory inv = mock(Inventory.class);
        when(inv.getHolder()).thenReturn(holder);
        holder.setInventory(inv);

        holder.setSlotData(0, friendUuid);
        InventoryClickEvent click = mockClick(holder, 0, ClickType.LEFT);
        listener.onInventoryClick(click);

        assertTrue(click.isCancelled());
        assertFalse(mockPlot.isPlayerWhitelisted(friendUuid), "Friend should be unwhitelisted");
    }

    @Test
    @DisplayName("Blacklist GUI clicking banned skull removes them from plot blacklist")
    void testBlacklistRemoval() {
        mockPlot.blacklistPlayer(enemyUuid);
        assertTrue(mockPlot.isPlayerBlacklisted(enemyUuid));

        PlotGuiListener listener = new PlotGuiListener(guiManager);
        PlotGuiHolder holder = new PlotGuiHolder(PlotGuiType.BLACKLIST, mockPlayer, mockPlot);
        Inventory inv = mock(Inventory.class);
        when(inv.getHolder()).thenReturn(holder);
        holder.setInventory(inv);

        holder.setSlotData(0, enemyUuid);
        InventoryClickEvent click = mockClick(holder, 0, ClickType.LEFT);
        listener.onInventoryClick(click);

        assertTrue(click.isCancelled());
        assertFalse(mockPlot.isPlayerBlacklisted(enemyUuid), "Enemy should be unblacklisted");
    }

    @Test
    @DisplayName("PlayerSelect GUI clicking target adds them to whitelist or blacklist")
    void testPlayerSelectAddsTarget() {
        PlotGuiListener listener = new PlotGuiListener(guiManager);

        // Add to whitelist
        PlotGuiHolder wlHolder = new PlotGuiHolder(PlotGuiType.PLAYER_SELECT, mockPlayer, mockPlot, 0, PlayerSelectAction.ADD_WHITELIST);
        Inventory inv1 = mock(Inventory.class);
        when(inv1.getHolder()).thenReturn(wlHolder);
        wlHolder.setInventory(inv1);
        wlHolder.setSlotData(0, friendUuid);

        InventoryClickEvent wlClick = mockClick(wlHolder, 0, ClickType.LEFT);
        listener.onInventoryClick(wlClick);
        assertTrue(wlClick.isCancelled());
        assertTrue(mockPlot.isPlayerWhitelisted(friendUuid), "Friend should now be whitelisted");

        // Add to blacklist
        PlotGuiHolder blHolder = new PlotGuiHolder(PlotGuiType.PLAYER_SELECT, mockPlayer, mockPlot, 0, PlayerSelectAction.ADD_BLACKLIST);
        Inventory inv2 = mock(Inventory.class);
        when(inv2.getHolder()).thenReturn(blHolder);
        blHolder.setInventory(inv2);
        blHolder.setSlotData(0, enemyUuid);

        InventoryClickEvent blClick = mockClick(blHolder, 0, ClickType.LEFT);
        listener.onInventoryClick(blClick);
        assertTrue(blClick.isCancelled());
        assertTrue(mockPlot.isPlayerBlacklisted(enemyUuid), "Enemy should now be blacklisted");
    }

    @Test
    @DisplayName("Confirm dispose deletes plot, refunds plot item, and removes from groups")
    void testConfirmDisposeExecution() {
        PlotGuiListener listener = new PlotGuiListener(guiManager);

        when(mockPlotManager.disposePlot(mockPlot, mockPlayer)).thenReturn(true);
        ItemStack plotBlockItem = mock(ItemStack.class);
        when(mockPlotManager.getPlotItem()).thenReturn(plotBlockItem);

        User mockUser = mock(User.class);
        Group mockGroup = mock(Group.class);
        when(mockGroup.isDefault()).thenReturn(true);
        when(mockUser.getGroups()).thenReturn(List.of(mockGroup));
        when(mockUserManager.getUser(mockPlayer)).thenReturn(mockUser);

        PlotGuiHolder holder = new PlotGuiHolder(PlotGuiType.CONFIRM_DISPOSE, mockPlayer, mockPlot);
        Inventory inv = mock(Inventory.class);
        when(inv.getHolder()).thenReturn(holder);
        holder.setInventory(inv);

        holder.setSlotData(11, "CONFIRM");
        InventoryClickEvent confirmClick = mockClick(holder, 11, ClickType.LEFT);
        listener.onInventoryClick(confirmClick);

        assertTrue(confirmClick.isCancelled());
        verify(mockPlotManager).disposePlot(mockPlot, mockPlayer);
        verify(mockPlayerInv).addItem(plotBlockItem);
        verify(mockGroup).remove(mockPlot);
        verify(mockPlayer).closeInventory();
    }

    @Test
    @DisplayName("PlotList GUI clicking plot opens dashboard or map")
    void testPlotListInteractions() {
        PlotGuiListener listener = new PlotGuiListener(guiManager);

        PlotGuiHolder holder = new PlotGuiHolder(PlotGuiType.PLOT_LIST, mockPlayer, null);
        Inventory inv = mock(Inventory.class);
        when(inv.getHolder()).thenReturn(holder);
        holder.setInventory(inv);

        holder.setSlotData(0, mockPlot);

        // Left click opens dashboard
        InventoryClickEvent leftClick = mockClick(holder, 0, ClickType.LEFT);
        listener.onInventoryClick(leftClick);
        assertTrue(leftClick.isCancelled());
        verify(mockPlayer, atLeastOnce()).openInventory(any(Inventory.class));

        // Right click opens map
        InventoryClickEvent rightClick = mockClick(holder, 0, ClickType.RIGHT);
        listener.onInventoryClick(rightClick);
        assertTrue(rightClick.isCancelled());
        verify(mockMapManager).openMap(mockPlayer, 5, 10);
    }

    @Test
    @DisplayName("Inventory drag event is cancelled in all plot GUIs")
    void testInventoryDragCancelled() {
        PlotGuiListener listener = new PlotGuiListener(guiManager);
        PlotGuiHolder holder = new PlotGuiHolder(PlotGuiType.DASHBOARD, mockPlayer, mockPlot);
        Inventory inv = mock(Inventory.class);
        when(inv.getHolder()).thenReturn(holder);

        InventoryDragEvent dragEvent = mock(InventoryDragEvent.class);
        when(dragEvent.getInventory()).thenReturn(inv);

        listener.onInventoryDrag(dragEvent);
        verify(dragEvent).setCancelled(true);
    }

    @Test
    @DisplayName("PlotGuiCommand executes and tab completes accurately")
    void testPlotGuiCommand() {
        PlotGuiCommand cmd = new PlotGuiCommand(guiManager, mockPlotManager);

        assertEquals("gui", cmd.getName());
        assertEquals("/plot gui [id]", cmd.getSyntax());
        assertEquals("chunkyplots.player", cmd.getPermission());

        // No args -> opens main GUI
        when(mockPlotManager.getPlot(any(Location.class))).thenReturn(mockPlot);
        cmd.execute(mockPlayer, new String[0]);
        verify(mockPlayer, atLeastOnce()).openInventory(any(Inventory.class));

        // With plot ID arg -> opens dashboard for that plot
        when(mockPlotManager.getPlot("5;10")).thenReturn(mockPlot);
        cmd.execute(mockPlayer, new String[]{"5;10"});
        verify(mockPlotManager).getPlot("5;10");

        // Tab completion
        when(mockPlotManager.getPlotsByOwner(playerUuid)).thenReturn(List.of(mockPlot));
        List<String> completions = cmd.getTabCompletion(mockPlayer, new String[]{""});
        assertTrue(completions.contains("5;10"));
        assertTrue(completions.contains(mockPlot.getMnemonicId()));
    }
}
