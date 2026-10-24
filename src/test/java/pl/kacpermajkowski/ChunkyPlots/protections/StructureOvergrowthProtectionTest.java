package pl.kacpermajkowski.ChunkyPlots.protections;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.world.StructureGrowEvent;
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
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotRepository;
import pl.kacpermajkowski.ChunkyPlots.protections.block.StructureOvergrowProtection;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class StructureOvergrowthProtectionTest {
    private Server mockServer;
    private World mockWorld;
    private PlotManager plotManager;
    private PlotPermissionChecker checker;
    private Config mockConfig;

    private Plot myPlot;
    private Plot neighborPlot;
    private UUID myUuid;
    private UUID neighborUuid;
    private Player player;

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
        mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        when(mockServer.getWorld("world")).thenReturn(mockWorld);

        when(mockWorld.getChunkAt(anyInt(), anyInt())).thenAnswer(inv ->
                TestChunkFactory.createChunk(inv.getArgument(0), inv.getArgument(1), mockWorld));
        when(mockWorld.getChunkAt(any(Location.class))).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            return TestChunkFactory.createChunk(loc.getBlockX() >> 4, loc.getBlockZ() >> 4, mockWorld);
        });

        myUuid = UUID.randomUUID();
        neighborUuid = UUID.randomUUID();

        player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(myUuid);
        when(player.getName()).thenReturn("MyPlayer");

        // Chunk (0, 0) is my plot, Chunk (1, 0) is neighbor's plot
        myPlot = new Plot(myUuid, 0, 0, "world", UUID.randomUUID());
        neighborPlot = new Plot(neighborUuid, 1, 0, "world", UUID.randomUUID());

        PlotRepository mockRepo = mock(PlotRepository.class);
        when(mockRepo.loadAll()).thenReturn(List.of(myPlot, neighborPlot));
        plotManager = new PlotManager(mockRepo, mock(ItemStack.class));

        UserManager mockUserManager = mock(UserManager.class);
        checker = new PlotPermissionChecker(plotManager, mockUserManager);

        mockConfig = mock(Config.class);
        when(mockConfig.isDontGenerateOvergrowingBlocks()).thenReturn(false);
        when(mockConfig.isDropSaplingOnFail()).thenReturn(true);

        StructureOvergrowProtection.setItemDropHandler((world, loc, mat) -> {
            ItemStack mockItem = mock(ItemStack.class);
            when(mockItem.getType()).thenReturn(mat);
            world.dropItem(loc, mockItem);
        });

        ProtectionNotifier.clearAll();
        ProtectionNotifier.setCooldownMillis(0);
    }

    @AfterEach
    void tearDown() {
        StructureOvergrowProtection.resetItemDropHandler();
        BukkitTestHelper.tearDownMockServer();
        ProtectionNotifier.clearAll();
        ProtectionNotifier.resetCooldown();
    }

    private BlockState mockBlockState(int x, int z) {
        BlockState state = mock(BlockState.class);
        Location loc = new Location(mockWorld, x, 65, z);
        when(state.getLocation()).thenReturn(loc);
        when(state.getWorld()).thenReturn(mockWorld);
        when(state.getX()).thenReturn(x);
        when(state.getZ()).thenReturn(z);
        return state;
    }

    private Block mockSaplingBlock(Location saplingLoc) {
        Block sapling = mock(Block.class);
        when(sapling.getType()).thenReturn(Material.OAK_SAPLING);
        when(sapling.getWorld()).thenReturn(mockWorld);
        when(sapling.getLocation()).thenReturn(saplingLoc);
        when(mockWorld.getBlockAt(saplingLoc)).thenReturn(sapling);
        when(mockWorld.getBlockAt(saplingLoc.getBlockX(), saplingLoc.getBlockY(), saplingLoc.getBlockZ())).thenReturn(sapling);
        return sapling;
    }

    @Test
    @DisplayName("Default mode (cancel & drop sapling): cancels event, drops sapling, and clears block")
    void testCancelAndDropSapling() {
        when(mockConfig.isDontGenerateOvergrowingBlocks()).thenReturn(false);
        when(mockConfig.isDropSaplingOnFail()).thenReturn(true);

        StructureOvergrowProtection listener = new StructureOvergrowProtection(checker, mockConfig);

        Location saplingLoc = new Location(mockWorld, 15, 64, 8); // chunk (0, 0) -> myPlot
        Block saplingBlock = mockSaplingBlock(saplingLoc);

        // Blocks: one inside myPlot (x=15), one extending into neighborPlot (x=16 -> chunk 1, 0)
        BlockState inBoundsBlock = mockBlockState(15, 8);
        BlockState overgrowingBlock = mockBlockState(16, 8);
        List<BlockState> blocks = new ArrayList<>(List.of(inBoundsBlock, overgrowingBlock));

        StructureGrowEvent event = new StructureGrowEvent(saplingLoc, null, false, player, blocks);
        listener.onStructureGrow(event);

        assertTrue(event.isCancelled(), "Event should be cancelled on overgrowth");
        verify(mockWorld).dropItem(eq(saplingLoc), any(ItemStack.class));
        verify(saplingBlock).setType(Material.AIR);
        assertEquals(Message.PROTECTION_STRUCTURE_GROW, ProtectionNotifier.getLastSentMessage());
    }

    @Test
    @DisplayName("Cancel mode without drop sapling: cancels event, leaves sapling block intact")
    void testCancelWithoutDropSapling() {
        when(mockConfig.isDontGenerateOvergrowingBlocks()).thenReturn(false);
        when(mockConfig.isDropSaplingOnFail()).thenReturn(false);

        StructureOvergrowProtection listener = new StructureOvergrowProtection(checker, mockConfig);

        Location saplingLoc = new Location(mockWorld, 15, 64, 8);
        Block saplingBlock = mockSaplingBlock(saplingLoc);

        BlockState inBoundsBlock = mockBlockState(15, 8);
        BlockState overgrowingBlock = mockBlockState(16, 8);
        List<BlockState> blocks = new ArrayList<>(List.of(inBoundsBlock, overgrowingBlock));

        StructureGrowEvent event = new StructureGrowEvent(saplingLoc, null, false, player, blocks);
        listener.onStructureGrow(event);

        assertTrue(event.isCancelled(), "Event should be cancelled on overgrowth");
        verify(mockWorld, never()).dropItem(any(), any());
        verify(saplingBlock, never()).setType(Material.AIR);
        assertEquals(Message.PROTECTION_STRUCTURE_GROW, ProtectionNotifier.getLastSentMessage());
    }

    @Test
    @DisplayName("dontGenerateOvergrowingBlocks = true: removes out-of-bounds blocks and lets tree grow")
    void testDontGenerateOvergrowingBlocks() {
        when(mockConfig.isDontGenerateOvergrowingBlocks()).thenReturn(true);
        when(mockConfig.isDropSaplingOnFail()).thenReturn(false);

        StructureOvergrowProtection listener = new StructureOvergrowProtection(checker, mockConfig);

        Location saplingLoc = new Location(mockWorld, 15, 64, 8); // chunk (0, 0) -> myPlot
        Block saplingBlock = mockSaplingBlock(saplingLoc);

        BlockState inBoundsBlock1 = mockBlockState(14, 8); // chunk 0, 0
        BlockState inBoundsBlock2 = mockBlockState(15, 8); // chunk 0, 0
        BlockState overgrowingBlock = mockBlockState(16, 8); // chunk 1, 0 -> neighborPlot
        List<BlockState> blocks = new ArrayList<>(List.of(inBoundsBlock1, inBoundsBlock2, overgrowingBlock));

        StructureGrowEvent event = new StructureGrowEvent(saplingLoc, null, false, player, blocks);
        listener.onStructureGrow(event);

        assertFalse(event.isCancelled(), "Event should NOT be cancelled when trimming overgrowing blocks");
        assertEquals(2, event.getBlocks().size(), "Only in-bounds blocks should remain");
        assertTrue(event.getBlocks().contains(inBoundsBlock1));
        assertTrue(event.getBlocks().contains(inBoundsBlock2));
        assertFalse(event.getBlocks().contains(overgrowingBlock));

        verify(mockWorld, never()).dropItem(any(), any());
        verify(saplingBlock, never()).setType(Material.AIR);
    }

    @Test
    @DisplayName("dontGenerateOvergrowingBlocks = true when ALL blocks are out-of-bounds: cancels event")
    void testDontGenerateOvergrowingBlocksAllRemoved() {
        when(mockConfig.isDontGenerateOvergrowingBlocks()).thenReturn(true);
        when(mockConfig.isDropSaplingOnFail()).thenReturn(true);

        StructureOvergrowProtection listener = new StructureOvergrowProtection(checker, mockConfig);

        Location saplingLoc = new Location(mockWorld, 15, 64, 8);
        Block saplingBlock = mockSaplingBlock(saplingLoc);

        BlockState overgrowingBlock1 = mockBlockState(16, 8); // neighborPlot
        BlockState overgrowingBlock2 = mockBlockState(17, 8); // neighborPlot
        List<BlockState> blocks = new ArrayList<>(List.of(overgrowingBlock1, overgrowingBlock2));

        StructureGrowEvent event = new StructureGrowEvent(saplingLoc, null, false, player, blocks);
        listener.onStructureGrow(event);

        assertTrue(event.isCancelled(), "Event should be cancelled when no blocks remain in valid area");
        verify(mockWorld).dropItem(eq(saplingLoc), any(ItemStack.class));
        verify(saplingBlock).setType(Material.AIR);
        assertEquals(Message.PROTECTION_STRUCTURE_GROW, ProtectionNotifier.getLastSentMessage());
    }

    @Test
    @DisplayName("Tree completely inside own plot: grows without changes or cancellations")
    void testStructureCompletelyInBounds() {
        when(mockConfig.isDontGenerateOvergrowingBlocks()).thenReturn(true);

        StructureOvergrowProtection listener = new StructureOvergrowProtection(checker, mockConfig);

        Location saplingLoc = new Location(mockWorld, 5, 64, 5); // chunk 0, 0
        Block saplingBlock = mockSaplingBlock(saplingLoc);

        BlockState b1 = mockBlockState(5, 5);
        BlockState b2 = mockBlockState(6, 5);
        List<BlockState> blocks = new ArrayList<>(List.of(b1, b2));

        StructureGrowEvent event = new StructureGrowEvent(saplingLoc, null, false, player, blocks);
        listener.onStructureGrow(event);

        assertFalse(event.isCancelled());
        assertEquals(2, event.getBlocks().size());
        verify(mockWorld, never()).dropItem(any(), any());
    }

    @Test
    @DisplayName("Config parsing loads structure-overgrowth settings properly")
    void testConfigStructureOvergrowthParsing() throws Exception {
        JavaPlugin mockPlugin = mock(JavaPlugin.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("ChunkyPlotsTest"));
        when(mockPlugin.getServer()).thenReturn(mockServer);

        String yaml = """
                use-message-prefix: true
                prefix: "[ChunkyPlots]"
                prefix-spacer: " > "
                interaction-message-display: [CHAT]
                plot-item:
                  material: "STONE"
                  name: "Plot Block"
                  lore: ["Claim chunk"]
                plot-limits:
                  default: 5
                structure-overgrowth:
                  dont-generate-overgrowing-blocks: true
                  drop-sapling-on-fail: false
                """;
        YamlConfiguration configYaml = new YamlConfiguration();
        configYaml.load(new StringReader(yaml));

        YamlConfiguration messagesYaml = new YamlConfiguration();
        for (Message msg : Message.values()) {
            messagesYaml.set("messages." + msg.name().toLowerCase(), "Msg " + msg.name());
        }

        Config config = new Config(mockPlugin, configYaml, messagesYaml);
        assertTrue(config.isDontGenerateOvergrowingBlocks());
        assertFalse(config.isDropSaplingOnFail());
    }
}
