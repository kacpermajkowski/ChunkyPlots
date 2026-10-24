package pl.kacpermajkowski.ChunkyPlots.hook.worldguard;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.TestChunkFactory;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotRepository;

import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorldGuardHookTest {

    private org.bukkit.Server mockServer;
    private PluginManager mockPluginManager;

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
        mockPluginManager = mockServer.getPluginManager();
    }

    @AfterEach
    void tearDown() {
        BukkitTestHelper.tearDownMockServer();
    }

    @Test
    void testWorldGuardNotInstalledDisablesHook() {
        when(mockPluginManager.getPlugin("WorldGuard")).thenReturn(null);

        Plugin mockPlugin = mock(Plugin.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("TestPlugin"));

        WorldGuardHook hook = new WorldGuardHook(mockPlugin);
        assertFalse(hook.isHooked());

        World mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        assertFalse(hook.hasOverlappingRegion(mockWorld, 0, 0));

        Chunk mockChunk = TestChunkFactory.createChunk(0, 0, mockWorld);
        assertFalse(hook.hasOverlappingRegion(mockChunk));
    }

    @Test
    void testCustomOverlapChecker() {
        Plugin mockPlugin = mock(Plugin.class);
        WorldGuardHook hook = new WorldGuardHook(mockPlugin);

        World mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        Chunk chunk1 = TestChunkFactory.createChunk(5, 10, mockWorld);
        Chunk chunk2 = TestChunkFactory.createChunk(6, 10, mockWorld);

        hook.setOverlapChecker((world, chunkX, chunkZ) -> chunkX == 5 && chunkZ == 10);
        assertTrue(hook.isHooked());

        assertTrue(hook.hasOverlappingRegion(chunk1));
        assertTrue(hook.hasOverlappingRegion(mockWorld, 5, 10));
        assertFalse(hook.hasOverlappingRegion(chunk2));
        assertFalse(hook.hasOverlappingRegion(mockWorld, 6, 10));
    }

    @Test
    void testClaimPlotBlockedWhenWorldGuardRegionOverlapsAndDisallowed() {
        Config config = mock(Config.class);
        when(config.isWorldAllowed(any())).thenReturn(true);
        when(config.isYWithinBounds(anyInt())).thenReturn(true);
        when(config.isWorldGuardAllowClaimOverlappingRegions()).thenReturn(false);
        MessageBuilder.init(config);

        PlotRepository repo = mock(PlotRepository.class);
        when(repo.count()).thenReturn(0);
        when(repo.loadAll()).thenReturn(Collections.emptyList());

        PlotManager plotManager = new PlotManager(repo, mock(ItemStack.class));
        plotManager.setConfig(config);

        WorldGuardHook hook = new WorldGuardHook(mock(Plugin.class));
        hook.setOverlapChecker((world, chunkX, chunkZ) -> true); // overlapping region exists
        plotManager.setWorldGuardHook(hook);

        World mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");

        Chunk mockChunk = TestChunkFactory.createChunk(1, 2, mockWorld);
        Block mockBlock = mock(Block.class);
        when(mockBlock.getChunk()).thenReturn(mockChunk);
        when(mockBlock.getWorld()).thenReturn(mockWorld);
        when(mockBlock.getX()).thenReturn(16);
        when(mockBlock.getZ()).thenReturn(32);
        when(mockBlock.getY()).thenReturn(64);

        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockPlayer.getName()).thenReturn("TestPlayer");

        boolean claimed = plotManager.claimPlot(mockPlayer, mockBlock);

        assertFalse(claimed, "Claiming should be prevented when WorldGuard region overlaps and config disallows it");
        verify(repo, never()).save(any());
        verify(mockPlayer).sendMessage(any(net.kyori.adventure.text.Component.class));
    }

    @Test
    void testClaimPlotAllowedWhenWorldGuardOverlapAllowedInConfig() {
        Config config = mock(Config.class);
        when(config.isWorldAllowed(any())).thenReturn(true);
        when(config.isYWithinBounds(anyInt())).thenReturn(true);
        when(config.isWorldGuardAllowClaimOverlappingRegions()).thenReturn(true);
        MessageBuilder.init(config);

        PlotRepository repo = mock(PlotRepository.class);
        when(repo.count()).thenReturn(0);
        when(repo.loadAll()).thenReturn(Collections.emptyList());

        PlotManager plotManager = new PlotManager(repo, mock(ItemStack.class));
        plotManager.setConfig(config);

        WorldGuardHook hook = new WorldGuardHook(mock(Plugin.class));
        hook.setOverlapChecker((world, chunkX, chunkZ) -> true); // overlapping region exists
        plotManager.setWorldGuardHook(hook);

        World mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");

        Chunk mockChunk = TestChunkFactory.createChunk(1, 2, mockWorld);
        Block mockBlock = mock(Block.class);
        when(mockBlock.getChunk()).thenReturn(mockChunk);
        when(mockBlock.getWorld()).thenReturn(mockWorld);
        when(mockBlock.getX()).thenReturn(16);
        when(mockBlock.getZ()).thenReturn(32);
        when(mockBlock.getY()).thenReturn(64);

        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockPlayer.getName()).thenReturn("TestPlayer");

        boolean claimed = plotManager.claimPlot(mockPlayer, mockBlock);

        assertTrue(claimed, "Claiming should be permitted when allow-claim-overlapping-regions is true");
        verify(repo, times(1)).save(any());
    }

    @Test
    void testClaimPlotAllowedWhenNoWorldGuardRegionOverlap() {
        Config config = mock(Config.class);
        when(config.isWorldAllowed(any())).thenReturn(true);
        when(config.isYWithinBounds(anyInt())).thenReturn(true);
        when(config.isWorldGuardAllowClaimOverlappingRegions()).thenReturn(false);
        MessageBuilder.init(config);

        PlotRepository repo = mock(PlotRepository.class);
        when(repo.count()).thenReturn(0);
        when(repo.loadAll()).thenReturn(Collections.emptyList());

        PlotManager plotManager = new PlotManager(repo, mock(ItemStack.class));
        plotManager.setConfig(config);

        WorldGuardHook hook = new WorldGuardHook(mock(Plugin.class));
        hook.setOverlapChecker((world, chunkX, chunkZ) -> false); // no overlap
        plotManager.setWorldGuardHook(hook);

        World mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");

        Chunk mockChunk = TestChunkFactory.createChunk(1, 2, mockWorld);
        Block mockBlock = mock(Block.class);
        when(mockBlock.getChunk()).thenReturn(mockChunk);
        when(mockBlock.getWorld()).thenReturn(mockWorld);
        when(mockBlock.getX()).thenReturn(16);
        when(mockBlock.getZ()).thenReturn(32);
        when(mockBlock.getY()).thenReturn(64);

        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockPlayer.getName()).thenReturn("TestPlayer");

        boolean claimed = plotManager.claimPlot(mockPlayer, mockBlock);

        assertTrue(claimed, "Claiming should succeed when no WorldGuard region overlaps");
        verify(repo, times(1)).save(any());
    }
}
