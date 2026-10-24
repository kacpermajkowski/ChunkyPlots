package pl.kacpermajkowski.ChunkyPlots.plot.limit;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.TestChunkFactory;
import pl.kacpermajkowski.ChunkyPlots.api.ChunkyPlotsAPI;
import pl.kacpermajkowski.ChunkyPlots.api.ChunkyPlotsAPIImpl;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.hook.vault.VaultHook;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotBlockPlaceListener;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotRepository;
import pl.kacpermajkowski.ChunkyPlots.plot.border.PlotBorderVisualizer;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PlotLimitManagerTest {
    private Server mockServer;
    private World mockWorld;
    private PlotRepository mockRepository;
    private ItemStack mockPlotItem;
    private PlotManager plotManager;
    private Config mockConfig;
    private VaultHook mockVaultHook;
    private PlotLimitManager limitManager;
    private PlotBorderVisualizer mockVisualizer;
    private ChunkyPlotsAPI api;

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
        mockWorld = Mockito.mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        when(mockServer.getWorld("world")).thenReturn(mockWorld);

        mockRepository = Mockito.mock(PlotRepository.class);
        when(mockRepository.count()).thenReturn(0);
        when(mockRepository.loadAll()).thenReturn(List.of());

        mockPlotItem = Mockito.mock(ItemStack.class);
        when(mockPlotItem.isSimilar(any())).thenReturn(true);

        plotManager = new PlotManager(mockRepository, mockPlotItem);

        mockConfig = Mockito.mock(Config.class);
        when(mockConfig.getDefaultPlotLimit()).thenReturn(1);
        Map<String, Integer> groupLimits = new HashMap<>();
        groupLimits.put("vip", 3);
        groupLimits.put("mvp", 10);
        when(mockConfig.getGroupPlotLimits()).thenReturn(groupLimits);

        mockVaultHook = Mockito.mock(VaultHook.class);
        when(mockVaultHook.isHooked()).thenReturn(true);
        when(mockVaultHook.getPlayerGroups(any())).thenReturn(new String[0]);

        limitManager = new PlotLimitManager(mockConfig, plotManager, mockVaultHook);
        plotManager.setLimitManager(limitManager);

        mockVisualizer = Mockito.mock(PlotBorderVisualizer.class);
        api = new ChunkyPlotsAPIImpl(plotManager, limitManager, mockVisualizer, mockVaultHook);
    }

    @AfterEach
    void tearDown() {
        BukkitTestHelper.tearDownMockServer();
    }

    private Player createMockPlayer(UUID uuid, String name, String... permissions) {
        Player player = Mockito.mock(Player.class);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn(name);

        Set<PermissionAttachmentInfo> effectivePerms = new HashSet<>();
        Set<String> permSet = new HashSet<>(Arrays.asList(permissions));

        for (String perm : permissions) {
            PermissionAttachmentInfo info = Mockito.mock(PermissionAttachmentInfo.class);
            when(info.getPermission()).thenReturn(perm);
            when(info.getValue()).thenReturn(true);
            effectivePerms.add(info);
        }

        when(player.getEffectivePermissions()).thenReturn(effectivePerms);
        when(player.hasPermission(anyString())).thenAnswer(inv -> permSet.contains(inv.getArgument(0)));
        when(player.isOp()).thenReturn(false);

        return player;
    }

    @Test
    @DisplayName("Default plot limit applies when no permissions or groups are set")
    void testDefaultPlotLimit() {
        Player player = createMockPlayer(UUID.randomUUID(), "Alice");
        when(mockVaultHook.getPrimaryGroup(player)).thenReturn(null);
        when(mockVaultHook.getPlayerGroups(player)).thenReturn(new String[0]);

        assertEquals(1, limitManager.getPlotLimit(player));
        assertEquals(1, limitManager.getRemainingPlots(player));
        assertFalse(limitManager.hasReachedPlotLimit(player));
        assertTrue(limitManager.canClaimMorePlots(player));
    }

    @Test
    @DisplayName("chunkyplots.limit.<number> resolves to exact permission value")
    void testPermissionPlotLimit() {
        Player player = createMockPlayer(UUID.randomUUID(), "Bob", "chunkyplots.limit.5");
        when(mockVaultHook.getPrimaryGroup(player)).thenReturn(null);
        when(mockVaultHook.getPlayerGroups(player)).thenReturn(new String[0]);

        assertEquals(5, limitManager.getPlotLimit(player));
        assertEquals(5, limitManager.getRemainingPlots(player));
    }

    @Test
    @DisplayName("Multiple permission limits resolve to highest number")
    void testMultiplePermissionLimitsResolveToHighest() {
        Player player = createMockPlayer(UUID.randomUUID(), "Charlie", "chunkyplots.limit.2", "chunkyplots.limit.8", "chunkyplots.limit.4");
        assertEquals(8, limitManager.getPlotLimit(player));
    }

    @Test
    @DisplayName("Admin, wildcard, and OP bypasses return Integer.MAX_VALUE")
    void testUnlimitedPermissions() {
        Player opPlayer = createMockPlayer(UUID.randomUUID(), "OpPlayer");
        when(opPlayer.isOp()).thenReturn(true);
        assertEquals(Integer.MAX_VALUE, limitManager.getPlotLimit(opPlayer));
        assertFalse(limitManager.hasReachedPlotLimit(opPlayer));

        Player adminPlayer = createMockPlayer(UUID.randomUUID(), "AdminPlayer", "chunkyplots.admin");
        assertEquals(Integer.MAX_VALUE, limitManager.getPlotLimit(adminPlayer));

        Player bypassPlayer = createMockPlayer(UUID.randomUUID(), "BypassPlayer", "chunkyplots.plotadmin");
        assertEquals(Integer.MAX_VALUE, limitManager.getPlotLimit(bypassPlayer));

        Player unlimitedPlayer = createMockPlayer(UUID.randomUUID(), "UnlimitedPlayer", "chunkyplots.limit.unlimited");
        assertEquals(Integer.MAX_VALUE, limitManager.getPlotLimit(unlimitedPlayer));

        Player wildcardPlayer = createMockPlayer(UUID.randomUUID(), "WildcardPlayer", "chunkyplots.limit.*");
        assertEquals(Integer.MAX_VALUE, limitManager.getPlotLimit(wildcardPlayer));
    }

    @Test
    @DisplayName("Vault groups resolve plot limit properly")
    void testVaultGroupPlotLimit() {
        Player vipPlayer = createMockPlayer(UUID.randomUUID(), "VipPlayer");
        when(mockVaultHook.getPrimaryGroup(vipPlayer)).thenReturn("vip");
        when(mockVaultHook.getPlayerGroups(vipPlayer)).thenReturn(new String[]{"default", "vip"});

        assertEquals(3, limitManager.getPlotLimit(vipPlayer));

        Player mvpPlayer = createMockPlayer(UUID.randomUUID(), "MvpPlayer");
        when(mockVaultHook.getPrimaryGroup(mvpPlayer)).thenReturn("mvp");
        when(mockVaultHook.getPlayerGroups(mvpPlayer)).thenReturn(new String[]{"mvp"});

        assertEquals(10, limitManager.getPlotLimit(mvpPlayer));
    }

    @Test
    @DisplayName("Plot limit tracking with owned plots and claim rejection")
    void testPlotLimitReached() {
        UUID playerId = UUID.randomUUID();
        Player player = createMockPlayer(playerId, "Claimer", "chunkyplots.limit.2");

        assertEquals(2, limitManager.getPlotLimit(player));
        assertEquals(0, limitManager.getPlotCount(player));
        assertEquals(2, limitManager.getRemainingPlots(player));
        assertFalse(limitManager.hasReachedPlotLimit(player));

        // Claim plot 1
        Block block1 = Mockito.mock(Block.class);
        Chunk chunk1 = TestChunkFactory.createChunk(0, 0, mockWorld);
        when(block1.getChunk()).thenReturn(chunk1);
        boolean claimed1 = plotManager.claimPlot(player, block1);
        assertTrue(claimed1, "First plot should be claimed successfully");

        assertEquals(1, limitManager.getPlotCount(player));
        assertEquals(1, limitManager.getRemainingPlots(player));
        assertFalse(limitManager.hasReachedPlotLimit(player));
        assertTrue(limitManager.canClaimMorePlots(player));

        // Claim plot 2 -> reach limit
        Block block2 = Mockito.mock(Block.class);
        Chunk chunk2 = TestChunkFactory.createChunk(1, 0, mockWorld);
        when(block2.getChunk()).thenReturn(chunk2);
        boolean claimed2 = plotManager.claimPlot(player, block2);
        assertTrue(claimed2, "Second plot should be claimed successfully");

        assertEquals(2, limitManager.getPlotCount(player));
        assertEquals(0, limitManager.getRemainingPlots(player));
        assertTrue(limitManager.hasReachedPlotLimit(player));
        assertFalse(limitManager.canClaimMorePlots(player));

        // Attempt to claim plot 3 -> rejected by limit!
        Block block3 = Mockito.mock(Block.class);
        Chunk chunk3 = TestChunkFactory.createChunk(2, 0, mockWorld);
        when(block3.getChunk()).thenReturn(chunk3);
        boolean claimed3 = plotManager.claimPlot(player, block3);
        assertFalse(claimed3, "Claiming beyond plot limit must be rejected");
        assertEquals(2, limitManager.getPlotCount(player));
    }

    @Test
    @DisplayName("ChunkyPlotsAPI reads plot limit and location info accurately")
    void testChunkyPlotsAPI() {
        UUID ownerId = UUID.randomUUID();
        Player player = createMockPlayer(ownerId, "OwnerPlayer", "chunkyplots.limit.3");
        Location loc = new Location(mockWorld, 32, 64, 48); // chunk (2, 3)
        when(player.getLocation()).thenReturn(loc);

        OfflinePlayer offlineOwner = Mockito.mock(OfflinePlayer.class);
        when(offlineOwner.getName()).thenReturn("OwnerPlayer");
        when(mockServer.getOfflinePlayer(ownerId)).thenReturn(offlineOwner);

        // Wilderness at start
        assertTrue(api.isWilderness(loc));
        assertTrue(api.isWilderness(player));
        assertEquals("Wilderness", api.getPlotOwnerName(player));
        assertNull(api.getPlotOwnerUUID(player));

        // Invalidate wilderness cache for chunk (2, 3) and mock repository load
        plotManager.evictChunk(2, 3, "world");
        Chunk chunk = TestChunkFactory.createChunk(2, 3, mockWorld);
        Plot plot = new Plot(ownerId, 2, 3, "world", UUID.randomUUID());
        when(mockRepository.loadByChunk(2, 3, "world")).thenReturn(plot);

        // API checks
        assertEquals(plot, api.getPlotAt(loc));
        assertEquals(plot, api.getPlotAt(player));
        assertFalse(api.isWilderness(loc));
        assertFalse(api.isWilderness(player));
        assertEquals("OwnerPlayer", api.getPlotOwnerName(player));
        assertEquals(ownerId, api.getPlotOwnerUUID(player));
        assertEquals("OwnerPlayer", api.getPlotOwnerNameAt(loc));
        assertEquals(ownerId, api.getPlotOwnerUUIDAt(loc));

        // Limit API checks
        assertEquals(3, api.getPlotLimit(player));
        assertTrue(api.canClaimPlot(player));
        assertFalse(api.hasReachedPlotLimit(player));
        assertEquals(mockVaultHook, api.getVaultHook());
        assertEquals(limitManager, api.getLimitManager());
    }

    @Test
    @DisplayName("PlotBlockPlaceListener cancels block placement when player reaches plot limit")
    void testPlotBlockPlaceListenerCancelsWhenLimitReached() {
        PlotBlockPlaceListener listener = new PlotBlockPlaceListener(plotManager);

        UUID playerId = UUID.randomUUID();
        Player player = createMockPlayer(playerId, "BlockPlacer", "chunkyplots.limit.1");

        // 1st block placed -> succeeds
        Block block1 = Mockito.mock(Block.class);
        Chunk chunk1 = TestChunkFactory.createChunk(10, 10, mockWorld);
        when(block1.getChunk()).thenReturn(chunk1);
        BlockPlaceEvent event1 = Mockito.mock(BlockPlaceEvent.class);
        when(event1.getItemInHand()).thenReturn(plotManager.getPlotItem());
        when(event1.getBlockPlaced()).thenReturn(block1);
        when(event1.getPlayer()).thenReturn(player);

        listener.onBlockPlace(event1);
        verify(event1, never()).setCancelled(true);
        verify(block1).setType(Material.AIR);

        // 2nd block placed -> rejected due to limit -> cancelled!
        Block block2 = Mockito.mock(Block.class);
        Chunk chunk2 = TestChunkFactory.createChunk(11, 10, mockWorld);
        when(block2.getChunk()).thenReturn(chunk2);
        BlockPlaceEvent event2 = Mockito.mock(BlockPlaceEvent.class);
        when(event2.getItemInHand()).thenReturn(plotManager.getPlotItem());
        when(event2.getBlockPlaced()).thenReturn(block2);
        when(event2.getPlayer()).thenReturn(player);

        listener.onBlockPlace(event2);
        verify(event2).setCancelled(true);
        verify(block2, never()).setType(Material.AIR);
    }
}
