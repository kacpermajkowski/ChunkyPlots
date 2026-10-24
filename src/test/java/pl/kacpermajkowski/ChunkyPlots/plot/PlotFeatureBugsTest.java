package pl.kacpermajkowski.ChunkyPlots.plot;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.TestChunkFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PlotFeatureBugsTest {

    private Server mockServer;
    private World overworld;
    private World nether;

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
        overworld = Mockito.mock(World.class);
        nether = Mockito.mock(World.class);

        when(overworld.getName()).thenReturn("world");
        when(nether.getName()).thenReturn("world_nether");

        when(mockServer.getWorld("world")).thenReturn(overworld);
        when(mockServer.getWorld("world_nether")).thenReturn(nether);

        when(overworld.getChunkAt(any(Location.class))).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            return TestChunkFactory.createChunk(loc.getBlockX() >> 4, loc.getBlockZ() >> 4, overworld);
        });

        when(nether.getChunkAt(any(Location.class))).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            return TestChunkFactory.createChunk(loc.getBlockX() >> 4, loc.getBlockZ() >> 4, nether);
        });
    }

    @AfterEach
    void tearDown() {
        BukkitTestHelper.tearDownMockServer();
    }

    @Test
    @DisplayName("Fix Verification: Plot.isLocationInside() returns false for different worlds at the same chunk coordinates")
    void testCrossWorldLocationFalsePositive() {
        Plot overworldPlot = new Plot(UUID.randomUUID(), 10, 20, "world", UUID.randomUUID());

        // Location is in the NETHER at chunk coordinates (10, 20) -> block coordinates (160, 64, 320)
        Location netherLocation = new Location(nether, 160, 64, 320);

        // Verification: Must be false because worlds differ!
        assertFalse(overworldPlot.isLocationInside(netherLocation),
                "Location in Nether at chunk (10, 20) must NOT be inside Overworld plot!");

        // Same coordinates in OVERWORLD must be true
        Location overworldLocation = new Location(overworld, 160, 64, 320);
        assertTrue(overworldPlot.isLocationInside(overworldLocation),
                "Location in Overworld at chunk (10, 20) must be inside Overworld plot");
    }

    @Test
    @DisplayName("Fix Verification: Plot.getChunk() returns null safely when world is unloaded or missing")
    void testUnloadedWorldSafeHandling() {
        when(mockServer.getWorld("unloaded_world")).thenReturn(null);

        Plot unloadedWorldPlot = new Plot(UUID.randomUUID(), 5, 5, "unloaded_world", UUID.randomUUID());

        // Calling getChunk() should return null safely without throwing NullPointerException
        assertDoesNotThrow(() -> {
            Chunk chunk = unloadedWorldPlot.getChunk();
            assertNull(chunk, "getChunk() must safely return null when world is not loaded");
        });
    }

    @Test
    @DisplayName("Fix Verification: Plot.getOwnerName() returns safe fallback if OfflinePlayer has no cached username")
    void testOfflinePlayerNullName() {
        UUID ownerId = UUID.randomUUID();
        OfflinePlayer offlinePlayer = Mockito.mock(OfflinePlayer.class);
        when(offlinePlayer.getName()).thenReturn(null);
        when(mockServer.getOfflinePlayer(ownerId)).thenReturn(offlinePlayer);

        Plot plot = new Plot(ownerId, 0, 0, "world", UUID.randomUUID());

        String ownerName = plot.getOwnerName();
        assertNotNull(ownerName, "Owner name must never be null");
        assertEquals("Unknown (" + ownerId.toString().substring(0, 8) + ")", ownerName);
    }
}
