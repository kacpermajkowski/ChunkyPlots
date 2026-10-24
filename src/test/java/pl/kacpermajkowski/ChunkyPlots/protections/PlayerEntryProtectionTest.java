package pl.kacpermajkowski.ChunkyPlots.protections;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerEntryProtection;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PlayerEntryProtectionTest {

    private PlotManager plotManager;
    private UserManager userManager;
    private PlotPermissionChecker checker;
    private World mockWorld;
    private Player deniedPlayer;
    private UUID deniedUuid;

    private Plot plotA;
    private Plot plotB;
    private Chunk chunkA;
    private Chunk chunkB;

    @BeforeEach
    void setUp() {
        plotManager = mock(PlotManager.class);
        userManager = mock(UserManager.class);
        checker = new PlotPermissionChecker(plotManager, userManager);

        mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");

        deniedUuid = UUID.randomUUID();
        deniedPlayer = mock(Player.class);
        when(deniedPlayer.getUniqueId()).thenReturn(deniedUuid);
        when(deniedPlayer.getWorld()).thenReturn(mockWorld);

        chunkA = mock(Chunk.class);
        when(chunkA.getX()).thenReturn(0);
        when(chunkA.getZ()).thenReturn(0);
        when(chunkA.getWorld()).thenReturn(mockWorld);

        chunkB = mock(Chunk.class);
        when(chunkB.getX()).thenReturn(1);
        when(chunkB.getZ()).thenReturn(0);
        when(chunkB.getWorld()).thenReturn(mockWorld);

        UUID ownerA = UUID.randomUUID();
        UUID ownerB = UUID.randomUUID();

        plotA = new Plot(ownerA, 0, 0, "world", UUID.randomUUID());
        plotA.blacklistPlayer(deniedUuid);

        plotB = new Plot(ownerB, 1, 0, "world", UUID.randomUUID());
        plotB.blacklistPlayer(deniedUuid);

        when(plotManager.getPlot(any(Location.class))).thenAnswer(invocation -> {
            Location loc = invocation.getArgument(0);
            if (loc == null || loc.getWorld() == null) return null;
            int cx = loc.getBlockX() >> 4;
            int cz = loc.getBlockZ() >> 4;
            if (cx == 0 && cz == 0) return plotA;
            if (cx == 1 && cz == 0) return plotB;
            return null;
        });

        when(plotManager.getPlot(any(Chunk.class))).thenAnswer(invocation -> {
            Chunk c = invocation.getArgument(0);
            if (c == null) return null;
            if (c.getX() == 0 && c.getZ() == 0) return plotA;
            if (c.getX() == 1 && c.getZ() == 0) return plotB;
            return null;
        });
    }

    @Test
    @DisplayName("Moving from one denied plot to adjacent denied plot teleports player out to safe wilderness")
    void testDeniedPlayerBetweenTwoPlotsTeleportedOut() {
        PlayerEntryProtection listener = new PlayerEntryProtection(checker);

        Location from = new Location(mockWorld, 15.5, 64, 8); // Inside Plot A
        Location to = new Location(mockWorld, 16.5, 64, 8);   // Inside Plot B
        when(deniedPlayer.getLocation()).thenReturn(from);

        PlayerMoveEvent event = new PlayerMoveEvent(deniedPlayer, from, to);
        listener.onPlayerMove(event);

        ArgumentCaptor<Location> teleportCaptor = ArgumentCaptor.forClass(Location.class);
        verify(deniedPlayer, atLeastOnce()).teleport(teleportCaptor.capture());

        Location destination = teleportCaptor.getValue();
        assertNotNull(destination);

        // Verify destination is neither in Plot A nor in Plot B
        int destChunkX = destination.getBlockX() >> 4;
        int destChunkZ = destination.getBlockZ() >> 4;

        boolean inPlotA = (destChunkX == 0 && destChunkZ == 0);
        boolean inPlotB = (destChunkX == 1 && destChunkZ == 0);

        assertFalse(inPlotA, "Destination should not be inside denied Plot A!");
        assertFalse(inPlotB, "Destination should not be inside denied Plot B!");
    }

    @Test
    @DisplayName("ChunkBorderUtil selects wilderness border point instead of adjacent denied plot")
    void testChunkBorderUtilExcludesAdjacentDeniedPlot() {
        // Player is at eastern edge of Plot A (X=15.5), adjacent to Plot B (X=16)
        Location locInPlotA = new Location(mockWorld, 15.5, 64, 8);
        when(deniedPlayer.getLocation()).thenReturn(locInPlotA);

        Location safePoint = ChunkBorderUtil.getSafeBorderPoint(deniedPlayer, chunkA, checker);
        assertNotNull(safePoint);

        int chunkX = safePoint.getBlockX() >> 4;
        int chunkZ = safePoint.getBlockZ() >> 4;

        assertFalse(chunkX == 0 && chunkZ == 0, "Safe point cannot be inside Plot A");
        assertFalse(chunkX == 1 && chunkZ == 0, "Safe point cannot be inside adjacent denied Plot B");
    }

    @Test
    @DisplayName("Player entering denied plot from safe wilderness is teleported back to safe from location")
    void testDeniedPlayerFromWildernessTeleportedBack() {
        PlayerEntryProtection listener = new PlayerEntryProtection(checker);

        Location from = new Location(mockWorld, 32.5, 64, 8); // Wilderness (chunk 2, 0)
        Location to = new Location(mockWorld, 31.5, 64, 8);   // Inside Plot B (chunk 1, 0)
        when(deniedPlayer.getLocation()).thenReturn(from);

        PlayerMoveEvent event = new PlayerMoveEvent(deniedPlayer, from, to);
        listener.onPlayerMove(event);

        ArgumentCaptor<Location> teleportCaptor = ArgumentCaptor.forClass(Location.class);
        verify(deniedPlayer).teleport(teleportCaptor.capture());

        Location destination = teleportCaptor.getValue();
        assertEquals(from.getX(), destination.getX(), 0.001);
        assertEquals(from.getY(), destination.getY(), 0.001);
        assertEquals(from.getZ(), destination.getZ(), 0.001);
    }
}
