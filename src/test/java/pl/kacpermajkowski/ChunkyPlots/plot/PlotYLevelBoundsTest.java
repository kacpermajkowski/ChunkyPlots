package pl.kacpermajkowski.ChunkyPlots.plot;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.TestChunkFactory;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionNotifier;
import pl.kacpermajkowski.ChunkyPlots.protections.block.BlockBreakProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.block.BlockBurnProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.block.BlockPlaceListener;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerBucketEmptyListener;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerInteractListener;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.io.StringReader;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class PlotYLevelBoundsTest {

    private Server mockServer;
    private JavaPlugin mockPlugin;
    private World mockWorld;
    private UUID ownerUuid;
    private UUID strangerUuid;
    private Player owner;
    private Player stranger;
    private Plot plot;
    private PlotManager plotManager;
    private PlotPermissionChecker checker;

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
        mockPlugin = mock(JavaPlugin.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("ChunkyPlotsYTest"));
        when(mockPlugin.getServer()).thenReturn(mockServer);

        mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        when(mockWorld.getMinHeight()).thenReturn(-64);
        when(mockWorld.getMaxHeight()).thenReturn(320);
        when(mockServer.getWorld("world")).thenReturn(mockWorld);

        when(mockWorld.getChunkAt(anyInt(), anyInt())).thenAnswer(inv ->
                TestChunkFactory.createChunk(inv.getArgument(0), inv.getArgument(1), mockWorld));
        when(mockWorld.getChunkAt(any(Location.class))).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            return TestChunkFactory.createChunk(loc.getBlockX() >> 4, loc.getBlockZ() >> 4, mockWorld);
        });

        ownerUuid = UUID.randomUUID();
        strangerUuid = UUID.randomUUID();

        owner = mock(Player.class);
        when(owner.getUniqueId()).thenReturn(ownerUuid);
        when(owner.getName()).thenReturn("PlotOwner");

        stranger = mock(Player.class);
        when(stranger.getUniqueId()).thenReturn(strangerUuid);
        when(stranger.getName()).thenReturn("Stranger");

        // Chunk (2, 3) -> x: 32..47, z: 48..63
        plot = new Plot(ownerUuid, 2, 3, "world", UUID.randomUUID());

        PlotRepository mockRepo = mock(PlotRepository.class);
        when(mockRepo.loadAll()).thenReturn(List.of(plot));
        plotManager = new PlotManager(mockRepo, mock(ItemStack.class));

        UserManager userManager = mock(UserManager.class);
        checker = new PlotPermissionChecker(plotManager, userManager);

        ProtectionNotifier.clearAll();
        ProtectionNotifier.setCooldownMillis(0);
    }

    @AfterEach
    void tearDown() {
        Config.setInstance(null);
        BukkitTestHelper.tearDownMockServer();
        ProtectionNotifier.clearAll();
        ProtectionNotifier.resetCooldown();
    }

    private Config createConfig(String yamlContent) throws Exception {
        String base = """
                use-message-prefix: true
                prefix: "[ChunkyPlots]"
                prefix-spacer: " > "
                interaction-message-display: [CHAT]
                plot-item:
                  material: "NOTE_BLOCK"
                  name: "Plot Block"
                  lore: ["Claim chunk"]
                plot-limits:
                  default: 5
                worlds:
                  world: ""
                """ + yamlContent;

        YamlConfiguration configYaml = new YamlConfiguration();
        configYaml.load(new StringReader(base));

        YamlConfiguration messagesYaml = new YamlConfiguration();
        for (Message msg : Message.values()) {
            messagesYaml.set("messages." + msg.name().toLowerCase(), "Message: " + msg.name());
        }

        Config config = new Config(mockPlugin, configYaml, messagesYaml);
        plotManager.setConfig(config);
        return config;
    }

    private Block mockBlock(int x, int y, int z) {
        Block block = mock(Block.class);
        when(block.getX()).thenReturn(x);
        when(block.getY()).thenReturn(y);
        when(block.getZ()).thenReturn(z);
        when(block.getWorld()).thenReturn(mockWorld);
        when(block.getLocation()).thenReturn(new Location(mockWorld, x, y, z));
        Chunk chunk = TestChunkFactory.createChunk(x >> 4, z >> 4, mockWorld);
        when(block.getChunk()).thenReturn(chunk);
        return block;
    }

    @Test
    @DisplayName("Default Config: min-y is 0, max-y is null (disabled)")
    void testDefaultConfigYBounds() throws Exception {
        Config config = createConfig("");

        assertEquals(0, config.getPlotMinY());
        assertNull(config.getPlotMaxY());
        assertFalse(config.isPlotMaxYEnabled());

        // Anything below 0 is out of bounds
        assertFalse(config.isYWithinBounds(-1));
        assertFalse(config.isYWithinBounds(-64));

        // 0 and anything above is within bounds
        assertTrue(config.isYWithinBounds(0));
        assertTrue(config.isYWithinBounds(64));
        assertTrue(config.isYWithinBounds(256));
        assertTrue(config.isYWithinBounds(319));
    }

    @Test
    @DisplayName("Custom Config: Custom min-y and max-y parse properly")
    void testCustomConfigYBounds() throws Exception {
        String yaml = """
                plot-height:
                  min-y: -32
                  max-y: 128
                """;
        Config config = createConfig(yaml);

        assertEquals(-32, config.getPlotMinY());
        assertEquals(128, config.getPlotMaxY());
        assertTrue(config.isPlotMaxYEnabled());

        assertFalse(config.isYWithinBounds(-33));
        assertTrue(config.isYWithinBounds(-32));
        assertTrue(config.isYWithinBounds(0));
        assertTrue(config.isYWithinBounds(128));
        assertFalse(config.isYWithinBounds(129));
    }

    @Test
    @DisplayName("Config: max-y disable keywords ('disabled', 'null', 'none', '-1', 'unlimited') parse to null")
    void testMaxYDisableKeywords() throws Exception {
        for (String keyword : List.of("disabled", "null", "none", "-1", "unlimited", "FALSE", "")) {
            String yaml = "plot-height:\n  min-y: 0\n  max-y: " + keyword + "\n";
            Config config = createConfig(yaml);
            assertNull(config.getPlotMaxY(), "Keyword '" + keyword + "' should parse to null (disabled top limit)");
            assertFalse(config.isPlotMaxYEnabled());
        }
    }

    @Test
    @DisplayName("PlotManager: getPlot(Block) and getPlot(Location) return null for y < minY")
    void testPlotManagerYLevelFiltering() throws Exception {
        createConfig(""); // min-y = 0, max-y = null

        // Block inside plot chunk (2, 3) at y = 64
        Block aboveZeroBlock = mockBlock(35, 64, 50);
        Plot foundAbove = plotManager.getPlot(aboveZeroBlock);
        assertNotNull(foundAbove, "Block at y=64 should be inside the plot");
        assertEquals(plot.getUUID(), foundAbove.getUUID());

        // Block inside plot chunk (2, 3) at y = 0 (boundary)
        Block boundaryBlock = mockBlock(35, 0, 50);
        assertNotNull(plotManager.getPlot(boundaryBlock), "Block at boundary y=0 should be inside the plot");

        // Block inside plot chunk (2, 3) at y = -1 (underground cave)
        Block belowZeroBlock = mockBlock(35, -1, 50);
        assertNull(plotManager.getPlot(belowZeroBlock), "Block at y=-1 should be treated as unprotected wilderness");

        // Location query checks
        Location aboveLoc = new Location(mockWorld, 35.5, 64.0, 50.5);
        Location belowLoc = new Location(mockWorld, 35.5, -10.0, 50.5);
        assertNotNull(plotManager.getPlot(aboveLoc));
        assertNull(plotManager.getPlot(belowLoc));
    }

    @Test
    @DisplayName("PlotManager: getPlot respects configured top max-y limit")
    void testPlotManagerMaxYLimit() throws Exception {
        String yaml = """
                plot-height:
                  min-y: 0
                  max-y: 100
                """;
        createConfig(yaml);

        Block inBounds = mockBlock(35, 100, 50);
        Block outOfTop = mockBlock(35, 101, 50);

        assertNotNull(plotManager.getPlot(inBounds), "Block at y=100 should be inside plot");
        assertNull(plotManager.getPlot(outOfTop), "Block at y=101 should be unprotected wilderness when max-y=100");
    }

    @Test
    @DisplayName("Protections: Stranger cannot break block at y >= 0, but CAN break block at y < 0 (deepslate caves)")
    void testBlockBreakProtectionAtYLevels() throws Exception {
        createConfig(""); // min-y = 0
        BlockBreakProtection breakProtection = new BlockBreakProtection(checker);

        // 1. Break at y = 64 on plot -> Cancelled for stranger
        Block surfaceBlock = mockBlock(35, 64, 50);
        BlockBreakEvent surfaceEvent = new BlockBreakEvent(surfaceBlock, stranger);
        breakProtection.onBlockBreak(surfaceEvent);
        assertTrue(surfaceEvent.isCancelled(), "Stranger must not break blocks at y >= 0");

        // 2. Break at y = -15 on plot chunk -> Allowed for stranger (wilderness)
        Block deepslateCaveBlock = mockBlock(35, -15, 50);
        BlockBreakEvent caveEvent = new BlockBreakEvent(deepslateCaveBlock, stranger);
        breakProtection.onBlockBreak(caveEvent);
        assertFalse(caveEvent.isCancelled(), "Stranger must be allowed to mine at y < 0 (unprotected wilderness)");
    }

    @Test
    @DisplayName("Protections: Stranger cannot place block at y >= 0, but CAN place block at y < 0")
    void testBlockPlaceProtectionAtYLevels() throws Exception {
        createConfig(""); // min-y = 0
        BlockPlaceListener placeListener = new BlockPlaceListener(checker);

        // 1. Place at y = 64 -> Cancelled
        Block surfaceBlock = mockBlock(35, 64, 50);
        BlockPlaceEvent surfaceEvent = mock(BlockPlaceEvent.class);
        when(surfaceEvent.getPlayer()).thenReturn(stranger);
        when(surfaceEvent.getBlockPlaced()).thenReturn(surfaceBlock);
        placeListener.onBlockPlace(surfaceEvent);
        verify(surfaceEvent).setCancelled(true);

        // 2. Place at y = -5 -> Allowed
        Block caveBlock = mockBlock(35, -5, 50);
        BlockPlaceEvent caveEvent = mock(BlockPlaceEvent.class);
        when(caveEvent.getPlayer()).thenReturn(stranger);
        when(caveEvent.getBlockPlaced()).thenReturn(caveBlock);
        placeListener.onBlockPlace(caveEvent);
        verify(caveEvent, never()).setCancelled(true);
    }

    @Test
    @DisplayName("Protections: PlayerInteractListener permits clicking blocks at y < 0 but protects blocks at y >= 0")
    void testInteractProtectionAtYLevels() throws Exception {
        createConfig(""); // min-y = 0
        PlayerInteractListener interactListener = new PlayerInteractListener(checker, null);

        // 1. Click block at y = 64 -> Cancelled
        Block chestAbove = mockBlock(35, 64, 50);
        PlayerInteractEvent clickAbove = mock(PlayerInteractEvent.class);
        when(clickAbove.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(clickAbove.getClickedBlock()).thenReturn(chestAbove);
        when(clickAbove.getPlayer()).thenReturn(stranger);

        interactListener.onPlayerInteract(clickAbove);
        verify(clickAbove).setCancelled(true);

        // 2. Click block at y = -20 -> Allowed
        Block chestBelow = mockBlock(35, -20, 50);
        PlayerInteractEvent clickBelow = mock(PlayerInteractEvent.class);
        when(clickBelow.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(clickBelow.getClickedBlock()).thenReturn(chestBelow);
        when(clickBelow.getPlayer()).thenReturn(stranger);

        interactListener.onPlayerInteract(clickBelow);
        verify(clickBelow, never()).setCancelled(true);
    }

    @Test
    @DisplayName("Protections: PlayerBucketEmptyListener permits emptying buckets at y < 0")
    void testBucketEmptyAtYLevels() throws Exception {
        createConfig(""); // min-y = 0
        PlayerBucketEmptyListener bucketListener = new PlayerBucketEmptyListener(checker);

        // 1. Bucket at y = 64 -> Cancelled
        Block targetAbove = mockBlock(35, 64, 50);
        PlayerBucketEmptyEvent bucketAbove = mock(PlayerBucketEmptyEvent.class);
        when(bucketAbove.getPlayer()).thenReturn(stranger);
        when(bucketAbove.getBlockClicked()).thenReturn(targetAbove);

        bucketListener.onPlayerBucketEmpty(bucketAbove);
        verify(bucketAbove).setCancelled(true);

        // 2. Bucket at y = -10 -> Allowed
        Block targetBelow = mockBlock(35, -10, 50);
        PlayerBucketEmptyEvent bucketBelow = mock(PlayerBucketEmptyEvent.class);
        when(bucketBelow.getPlayer()).thenReturn(stranger);
        when(bucketBelow.getBlockClicked()).thenReturn(targetBelow);

        bucketListener.onPlayerBucketEmpty(bucketBelow);
        verify(bucketBelow, never()).setCancelled(true);
    }

    @Test
    @DisplayName("Protections: Fire burning at y < 0 is allowed in wilderness")
    void testBlockBurnAtYLevels() throws Exception {
        createConfig(""); // min-y = 0
        BlockBurnProtection burnProtection = new BlockBurnProtection(checker);

        Block fire = mockBlock(35, -5, 50);
        Block burned = mockBlock(35, -4, 50);

        BlockBurnEvent event = new BlockBurnEvent(burned, fire);
        burnProtection.onBlockBurn(event);

        assertFalse(event.isCancelled(), "Fire burning underground at y < 0 is wilderness, should be allowed");
    }

    @Test
    @DisplayName("Plot.isLocationInside: respects Y-level bounds")
    void testPlotIsLocationInsideWithYBounds() throws Exception {
        createConfig(""); // min-y = 0

        Location insideY = new Location(mockWorld, 35, 64, 50);
        Location boundaryY = new Location(mockWorld, 35, 0, 50);
        Location undergroundY = new Location(mockWorld, 35, -1, 50);

        assertTrue(plot.isLocationInside(insideY));
        assertTrue(plot.isLocationInside(boundaryY));
        assertFalse(plot.isLocationInside(undergroundY));
    }
}
