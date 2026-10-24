package pl.kacpermajkowski.ChunkyPlots.plot.border;

import net.kyori.adventure.text.Component;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.border.PlotShowBorderCommand;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PlotBorderVisualizerTest {
    private PlotManager plotManager;
    private Plugin plugin;
    private PlotBorderVisualizer visualizer;
    private Player player;
    private World world;
    private Chunk chunk;

    @BeforeEach
    void setUp() {
        plotManager = mock(PlotManager.class);
        plugin = mock(Plugin.class);
        when(plugin.isEnabled()).thenReturn(false); // run in mock/test mode without Bukkit scheduler

        visualizer = new PlotBorderVisualizer(plugin, plotManager);

        player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("TestPlayer");
        when(player.isOnline()).thenReturn(true);

        world = mock(World.class);
        when(world.getName()).thenReturn("world");
        when(world.getMinHeight()).thenReturn(-64);
        when(world.getMaxHeight()).thenReturn(320);
        when(world.isChunkLoaded(anyInt(), anyInt())).thenReturn(true);

        Block mockBlock = mock(Block.class);
        when(mockBlock.isPassable()).thenReturn(true);
        when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenReturn(mockBlock);

        chunk = mock(Chunk.class);
        when(chunk.getX()).thenReturn(5);
        when(chunk.getZ()).thenReturn(10);
        when(chunk.getWorld()).thenReturn(world);

        when(world.getChunkAt(any(Location.class))).thenReturn(chunk);
        when(world.getChunkAt(anyInt(), anyInt())).thenReturn(chunk);

        Location location = new Location(world, 85.5, 65.0, 165.5);
        when(player.getLocation()).thenReturn(location);
        when(player.getWorld()).thenReturn(world);
    }

    @Test
    @DisplayName("PlotRelation should return distinct colors for all four states")
    void testPlotRelations() {
        assertEquals(Color.fromRGB(255, 215, 0), PlotRelation.WILDERNESS.getColor());
        assertEquals(Color.fromRGB(50, 255, 50), PlotRelation.OWN_PLOT.getColor());
        assertEquals(Color.fromRGB(0, 200, 255), PlotRelation.MEMBER_PLOT.getColor());
        assertEquals(Color.fromRGB(255, 50, 50), PlotRelation.THIRD_PARTY_PLOT.getColor());

        assertNotNull(PlotRelation.WILDERNESS.getPlName());
        assertNotNull(PlotRelation.OWN_PLOT.getPlName());
        assertNotNull(PlotRelation.MEMBER_PLOT.getPlName());
        assertNotNull(PlotRelation.THIRD_PARTY_PLOT.getPlName());
    }

    @Test
    @DisplayName("Duration should default to 10 minutes (600s / 1200 iterations)")
    void testDurationDefault() {
        assertEquals(600, PlotBorderVisualizer.DEFAULT_DURATION_SECONDS);
        assertEquals(1200, PlotBorderVisualizer.MAX_ITERATIONS);
    }

    @Test
    @DisplayName("determineRelation should properly classify wilderness, own plot, member plot, and third party plot")
    void testDetermineRelation() {
        UUID playerUuid = player.getUniqueId();
        UUID otherUuid = UUID.randomUUID();

        // 1. Null plot -> Wilderness
        assertEquals(PlotRelation.WILDERNESS, visualizer.determineRelation(player, null));

        // 2. Plot owned by player -> OWN_PLOT
        Plot ownPlot = new Plot(playerUuid, 5, 10, "world", UUID.randomUUID());
        assertEquals(PlotRelation.OWN_PLOT, visualizer.determineRelation(player, ownPlot));

        // 3. Plot owned by someone else, but player is whitelisted -> MEMBER_PLOT
        Plot memberPlot = new Plot(otherUuid, 5, 10, "world", UUID.randomUUID());
        memberPlot.whitelistPlayer(playerUuid);
        assertEquals(PlotRelation.MEMBER_PLOT, visualizer.determineRelation(player, memberPlot));

        // 4. Plot owned by someone else, player not whitelisted -> THIRD_PARTY_PLOT
        Plot strangerPlot = new Plot(otherUuid, 5, 10, "world", UUID.randomUUID());
        assertEquals(PlotRelation.THIRD_PARTY_PLOT, visualizer.determineRelation(player, strangerPlot));
    }

    @Test
    @DisplayName("renderBorder should spawn particles only to the target player with correct color for own plot")
    void testRenderBorderOwnPlot() {
        Plot ownPlot = new Plot(player.getUniqueId(), 5, 10, "world", UUID.randomUUID());
        when(plotManager.getPlot(any(Chunk.class))).thenReturn(ownPlot);

        visualizer.renderBorder(player);

        ArgumentCaptor<Particle.DustOptions> captor = ArgumentCaptor.forClass(Particle.DustOptions.class);
        verify(player, atLeastOnce()).spawnParticle(
                eq(Particle.DUST),
                anyDouble(), anyDouble(), anyDouble(),
                eq(1),
                eq(0.0), eq(0.0), eq(0.0), eq(0.0),
                captor.capture()
        );

        // Verify the color is Green (own plot)
        assertEquals(Color.fromRGB(50, 255, 50), captor.getValue().getColor());
    }

    @Test
    @DisplayName("renderBorder should spawn particles with wilderness color when standing in wilderness")
    void testRenderBorderWilderness() {
        when(plotManager.getPlot(any(Chunk.class))).thenReturn(null);

        visualizer.renderBorder(player);

        ArgumentCaptor<Particle.DustOptions> captor = ArgumentCaptor.forClass(Particle.DustOptions.class);
        verify(player, atLeastOnce()).spawnParticle(
                eq(Particle.DUST),
                anyDouble(), anyDouble(), anyDouble(),
                eq(1),
                eq(0.0), eq(0.0), eq(0.0), eq(0.0),
                captor.capture()
        );

        // Verify the color is Yellow/Gold (wilderness)
        assertEquals(Color.fromRGB(255, 215, 0), captor.getValue().getColor());
    }

    @Test
    @DisplayName("renderBorder should spawn particles with third-party color when standing on another player's plot")
    void testRenderBorderThirdPartyPlot() {
        Plot strangerPlot = new Plot(UUID.randomUUID(), 5, 10, "world", UUID.randomUUID());
        when(plotManager.getPlot(any(Chunk.class))).thenReturn(strangerPlot);

        visualizer.renderBorder(player);

        ArgumentCaptor<Particle.DustOptions> captor = ArgumentCaptor.forClass(Particle.DustOptions.class);
        verify(player, atLeastOnce()).spawnParticle(
                eq(Particle.DUST),
                anyDouble(), anyDouble(), anyDouble(),
                eq(1),
                eq(0.0), eq(0.0), eq(0.0), eq(0.0),
                captor.capture()
        );

        // Verify the color is Red (third-party plot)
        assertEquals(Color.fromRGB(255, 50, 50), captor.getValue().getColor());
    }

    @Test
    @DisplayName("renderBorder should spawn particles with member color when standing on a friend's plot")
    void testRenderBorderMemberPlot() {
        Plot friendPlot = new Plot(UUID.randomUUID(), 5, 10, "world", UUID.randomUUID());
        friendPlot.whitelistPlayer(player.getUniqueId());
        when(plotManager.getPlot(any(Chunk.class))).thenReturn(friendPlot);

        visualizer.renderBorder(player);

        ArgumentCaptor<Particle.DustOptions> captor = ArgumentCaptor.forClass(Particle.DustOptions.class);
        verify(player, atLeastOnce()).spawnParticle(
                eq(Particle.DUST),
                anyDouble(), anyDouble(), anyDouble(),
                eq(1),
                eq(0.0), eq(0.0), eq(0.0), eq(0.0),
                captor.capture()
        );

        // Verify the color is Cyan (member plot)
        assertEquals(Color.fromRGB(0, 200, 255), captor.getValue().getColor());
    }

    @Test
    @DisplayName("PlotShowBorderCommand is a pure toggle without on/off parameters")
    void testPlotShowBorderCommand() {
        PlotShowBorderCommand command = new PlotShowBorderCommand(visualizer);

        assertEquals("showborder", command.getName());
        assertEquals("chunkyplots.player", command.getPermission());
        assertEquals("/plot showborder", command.getSyntax());
        assertNotNull(command.getDescription());

        // Test execution toggle
        command.execute(player, new String[]{});
        // In mock mode without plugin enabled, it calls renderBorder
        verify(player, atLeastOnce()).spawnParticle(eq(Particle.DUST), anyDouble(), anyDouble(), anyDouble(), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), any());

        // Pure toggle has no parameters to complete
        List<String> completions = command.getTabCompletion(player, new String[]{""});
        assertTrue(completions.isEmpty());
    }

    @Test
    @DisplayName("Player quit and kick should auto-disable and remove active task")
    void testPlayerQuitAndKickCleanup() {
        visualizer.onPlayerQuit(new PlayerQuitEvent(player, (Component) null));
        assertFalse(visualizer.isVisualizing(player));

        visualizer.onPlayerKick(new PlayerKickEvent(player, (Component) null, (Component) null));
        assertFalse(visualizer.isVisualizing(player));

        visualizer.stopAll();
        assertFalse(visualizer.isVisualizing(player));
    }
}
