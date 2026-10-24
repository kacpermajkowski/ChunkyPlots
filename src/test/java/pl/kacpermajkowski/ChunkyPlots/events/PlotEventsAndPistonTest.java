package pl.kacpermajkowski.ChunkyPlots.events;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.TestChunkFactory;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotRepository;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.PistonProtection;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PlotEventsAndPistonTest {

    private Server mockServer;
    private World mockWorld;

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
        mockWorld = Mockito.mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        when(mockServer.getWorld("world")).thenReturn(mockWorld);
    }

    @AfterEach
    void tearDown() {
        BukkitTestHelper.tearDownMockServer();
    }

    @Test
    @DisplayName("Domain Events: PlotPreClaimEvent can cancel plot claiming")
    void testPreClaimEventCancellation() {
        PlotRepository mockRepo = Mockito.mock(PlotRepository.class);
        PlotManager plotManager = new PlotManager(mockRepo, Mockito.mock(ItemStack.class));

        Chunk chunk = TestChunkFactory.createChunk(5, 5, mockWorld);
        Block block = Mockito.mock(Block.class);
        when(block.getChunk()).thenReturn(chunk);

        Player player = Mockito.mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());

        // PluginManager listener that cancels PlotPreClaimEvent
        PluginManager pm = mockServer.getPluginManager();
        doAnswer(inv -> {
            if (inv.getArgument(0) instanceof PlotPreClaimEvent preClaim) {
                preClaim.setCancelled(true);
            }
            return null;
        }).when(pm).callEvent(any());

        boolean claimed = plotManager.claimPlot(player, block);

        assertFalse(claimed, "claimPlot should return false when PlotPreClaimEvent is cancelled");
        assertNull(plotManager.getPlot(chunk), "Plot should not be indexed when pre-claim event is cancelled");
        verify(mockRepo, never()).save(any());
    }

    @Test
    @DisplayName("Domain Events: PlotPreDisposeEvent can cancel plot disposal")
    void testPreDisposeEventCancellation() {
        UUID ownerId = UUID.randomUUID();
        Plot plot = new Plot(ownerId, 8, 8, "world", UUID.randomUUID());

        PlotRepository mockRepo = Mockito.mock(PlotRepository.class);
        when(mockRepo.loadAll()).thenReturn(List.of(plot));
        PlotManager plotManager = new PlotManager(mockRepo, Mockito.mock(ItemStack.class));

        Player player = Mockito.mock(Player.class);
        when(player.getUniqueId()).thenReturn(ownerId);

        // Cancel disposal in pre-event
        PluginManager pm = mockServer.getPluginManager();
        doAnswer(inv -> {
            if (inv.getArgument(0) instanceof PlotPreDisposeEvent preDispose) {
                preDispose.setCancelled(true);
            }
            return null;
        }).when(pm).callEvent(any());

        boolean disposed = plotManager.disposePlot(plot, player);

        assertFalse(disposed, "disposePlot should return false when PlotPreDisposeEvent is cancelled");
        assertNotNull(plotManager.getPlot(8, 8, "world"), "Plot must remain indexed when disposal is cancelled");
        verify(mockRepo, never()).delete(any());
    }

    @Test
    @DisplayName("Piston Protection: Allows wilderness pistons and offline owner redstone, blocks border griefing")
    void testPistonProtections() {
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();

        Plot plotA = new Plot(playerA, 0, 0, "world", UUID.randomUUID());
        Plot plotB = new Plot(playerB, 1, 0, "world", UUID.randomUUID());

        PlotRepository mockRepo = Mockito.mock(PlotRepository.class);
        when(mockRepo.loadAll()).thenReturn(List.of(plotA, plotB));
        PlotManager plotManager = new PlotManager(mockRepo, Mockito.mock(ItemStack.class));

        UserManager mockUserManager = Mockito.mock(UserManager.class);
        PlotPermissionChecker checker = new PlotPermissionChecker(plotManager, mockUserManager);
        PistonProtection pistonProtection = new PistonProtection(checker);

        // Case 1: Wilderness piston pushing wilderness block -> Allowed
        Block wildernessPiston = mockBlock(100, 100);
        Block wildernessBlock = mockBlock(101, 100);
        BlockPistonExtendEvent wildEvent = new BlockPistonExtendEvent(wildernessPiston, List.of(wildernessBlock), BlockFace.EAST);
        pistonProtection.onBlockPistonExtend(wildEvent);
        assertFalse(wildEvent.isCancelled(), "Wilderness pistons pushing wilderness blocks must NOT be cancelled");

        // Case 2: Claimed plot piston owned by player A (owner is offline!) pushing blocks inside plot A -> Allowed
        Block plotAPiston = mockBlock(5, 5); // inside chunk (0, 0)
        Block plotABlock = mockBlock(6, 5);  // inside chunk (0, 0)
        BlockPistonExtendEvent insideEvent = new BlockPistonExtendEvent(plotAPiston, List.of(plotABlock), BlockFace.EAST);
        pistonProtection.onBlockPistonExtend(insideEvent);
        assertFalse(insideEvent.isCancelled(), "Pistons inside claimed plot must work even when owner is offline");

        // Case 3: Piston on border of plot A attempting to push block into plot B (different owner) -> Cancelled!
        Block borderPiston = mockBlock(15, 5); // chunk (0, 0)
        Block targetInPlotB = mockBlock(16, 5); // chunk (1, 0) - plot B
        BlockPistonExtendEvent griefEvent = new BlockPistonExtendEvent(borderPiston, List.of(borderPiston), BlockFace.EAST);
        when(borderPiston.getRelative(BlockFace.EAST)).thenReturn(targetInPlotB);

        pistonProtection.onBlockPistonExtend(griefEvent);
        assertTrue(griefEvent.isCancelled(), "Piston pushing across border into another player's plot MUST be cancelled!");
    }

    private Block mockBlock(int x, int z) {
        Block block = Mockito.mock(Block.class);
        when(block.getX()).thenReturn(x);
        when(block.getZ()).thenReturn(z);
        when(block.getWorld()).thenReturn(mockWorld);
        Chunk chunk = TestChunkFactory.createChunk(x >> 4, z >> 4, mockWorld);
        when(block.getChunk()).thenReturn(chunk);
        return block;
    }
}
