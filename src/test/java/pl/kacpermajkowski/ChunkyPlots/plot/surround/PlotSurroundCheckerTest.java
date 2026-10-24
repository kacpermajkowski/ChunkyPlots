package pl.kacpermajkowski.ChunkyPlots.plot.surround;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.TestChunkFactory;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class PlotSurroundCheckerTest {

    private org.bukkit.Server mockServer;
    private PluginManager mockPluginManager;
    private Config mockConfig;
    private World mockWorld;

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
        mockPluginManager = mockServer.getPluginManager();

        mockConfig = mock(Config.class);
        when(mockConfig.isWorldAllowed(any())).thenReturn(true);
        when(mockConfig.isYWithinBounds(anyInt())).thenReturn(true);
        when(mockConfig.isSurroundProtectionEnabled()).thenReturn(true);
        when(mockConfig.getSurroundProtectionSearchDepth()).thenReturn(5);
        MessageBuilder.init(mockConfig);

        mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
    }

    @AfterEach
    void tearDown() {
        BukkitTestHelper.tearDownMockServer();
    }

    private Player createMockPlayer(UUID uuid, String name) {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn(name);
        return player;
    }

    private Block createMockBlock(int chunkX, int chunkZ) {
        Chunk chunk = TestChunkFactory.createChunk(chunkX, chunkZ, mockWorld);
        Block block = mock(Block.class);
        when(block.getChunk()).thenReturn(chunk);
        when(block.getWorld()).thenReturn(mockWorld);
        when(block.getX()).thenReturn(chunkX << 4);
        when(block.getZ()).thenReturn(chunkZ << 4);
        when(block.getY()).thenReturn(64);
        return block;
    }

    private static class InMemoryPlotRepo implements PlotRepository {
        final List<Plot> plots = new ArrayList<>();

        @Override
        public void save(Plot plot) {
            plots.removeIf(p -> p.getUUID().equals(plot.getUUID()));
            plots.add(plot);
        }

        @Override
        public void delete(Plot plot) {
            plots.removeIf(p -> p.getUUID().equals(plot.getUUID()));
        }

        @Override
        public Plot loadByUuid(UUID uuid) {
            return plots.stream().filter(p -> p.getUUID().equals(uuid)).findFirst().orElse(null);
        }

        @Override
        public Plot loadByChunk(int chunkX, int chunkZ, String worldName) {
            return plots.stream().filter(p -> p.getChunkX() == chunkX && p.getChunkZ() == chunkZ && p.getWorldName().equals(worldName)).findFirst().orElse(null);
        }

        @Override
        public List<Plot> loadByOwner(UUID ownerUuid) {
            return plots.stream().filter(p -> p.getOwnerUUID().equals(ownerUuid)).toList();
        }

        @Override
        public List<Plot> loadByMember(UUID memberUuid) {
            return plots.stream().filter(p -> p.isPlayerWhitelisted(memberUuid)).toList();
        }

        @Override
        public List<Plot> loadAll() {
            return new ArrayList<>(plots);
        }

        @Override
        public int count() {
            return plots.size();
        }
    }

    @Test
    void testPlayerCanSurroundHisOwnPlot() {
        InMemoryPlotRepo repo = new InMemoryPlotRepo();
        PlotManager plotManager = new PlotManager(repo, mock(ItemStack.class));
        plotManager.setConfig(mockConfig);

        UUID playerAUuid = UUID.randomUUID();
        Player playerA = createMockPlayer(playerAUuid, "PlayerA");

        // Player A claims center plot (0, 0)
        assertTrue(plotManager.claimPlot(playerA, createMockBlock(0, 0)));

        // Player A claims 4 orthogonal neighbors around (0, 0)
        assertTrue(plotManager.claimPlot(playerA, createMockBlock(0, 1)));
        assertTrue(plotManager.claimPlot(playerA, createMockBlock(0, -1)));
        assertTrue(plotManager.claimPlot(playerA, createMockBlock(1, 0)));
        // 4th orthogonal neighbor completes the encirclement around (0, 0)
        assertTrue(plotManager.claimPlot(playerA, createMockBlock(-1, 0)),
                "Player A should be allowed to surround their own plot!");

        // Center plot is surrounded by Player A's own plots, which connect to wilderness outside
        assertNotNull(plotManager.getPlot(0, 0, "world"));
        assertNotNull(plotManager.getPlot(-1, 0, "world"));
    }

    @Test
    void testEnemyCannotEncirclePlayerPlot() {
        InMemoryPlotRepo repo = new InMemoryPlotRepo();
        PlotManager plotManager = new PlotManager(repo, mock(ItemStack.class));
        plotManager.setConfig(mockConfig);

        UUID playerAUuid = UUID.randomUUID();
        Player playerA = createMockPlayer(playerAUuid, "PlayerA");

        UUID playerBUuid = UUID.randomUUID();
        Player playerB = createMockPlayer(playerBUuid, "PlayerB");

        // Player A owns (0, 0)
        assertTrue(plotManager.claimPlot(playerA, createMockBlock(0, 0)));

        // Player B claims 3 sides around Player A
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(0, 1)));
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(0, -1)));
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(1, 0)));

        // Player B tries to claim the 4th side (-1, 0) to completely surround Player A
        boolean bClaimed = plotManager.claimPlot(playerB, createMockBlock(-1, 0));
        assertFalse(bClaimed, "Player B must not be allowed to completely surround Player A's plot!");
        assertNull(plotManager.getPlot(-1, 0, "world"), "The blocking chunk should not be claimed");
        verify(playerB, atLeastOnce()).sendMessage(any(net.kyori.adventure.text.Component.class));
    }

    @Test
    void testEnclosedPlayerCanClaimHisWayOut() {
        InMemoryPlotRepo repo = new InMemoryPlotRepo();
        PlotManager plotManager = new PlotManager(repo, mock(ItemStack.class));
        plotManager.setConfig(mockConfig);

        UUID playerAUuid = UUID.randomUUID();
        Player playerA = createMockPlayer(playerAUuid, "PlayerA");

        UUID playerBUuid = UUID.randomUUID();
        Player playerB = createMockPlayer(playerBUuid, "PlayerB");

        // Player A owns (0, 0)
        assertTrue(plotManager.claimPlot(playerA, createMockBlock(0, 0)));

        // Player B claims 3 sides
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(0, 1)));
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(0, -1)));
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(1, 0)));

        // Player A claims the exit chunk (-1, 0) to expand and reach wilderness
        boolean aClaimed = plotManager.claimPlot(playerA, createMockBlock(-1, 0));
        assertTrue(aClaimed, "Player A should be allowed to claim the exit chunk to escape!");
        assertNotNull(plotManager.getPlot(-1, 0, "world"));
    }

    @Test
    void testCannotClaimChunkCompletelySurroundedByOtherPlayers() {
        InMemoryPlotRepo repo = new InMemoryPlotRepo();
        PlotManager plotManager = new PlotManager(repo, mock(ItemStack.class));
        plotManager.setConfig(mockConfig);

        UUID playerBUuid = UUID.randomUUID();
        Player playerB = createMockPlayer(playerBUuid, "PlayerB");

        // Player B creates a ring around (5, 5)
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(5, 6)));
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(5, 4)));
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(6, 5)));
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(4, 5)));

        // Now Player A walks into the enclosed 1x1 hole at (5, 5) and tries to claim it
        UUID playerAUuid = UUID.randomUUID();
        Player playerA = createMockPlayer(playerAUuid, "PlayerA");

        boolean aClaimed = plotManager.claimPlot(playerA, createMockBlock(5, 5));
        assertFalse(aClaimed, "Player A should not be able to claim a plot completely surrounded by another player!");
        assertNull(plotManager.getPlot(5, 5, "world"));
    }

    @Test
    void testOwnerCanClaimChunkInsideHisOwnRing() {
        InMemoryPlotRepo repo = new InMemoryPlotRepo();
        PlotManager plotManager = new PlotManager(repo, mock(ItemStack.class));
        plotManager.setConfig(mockConfig);

        UUID playerAUuid = UUID.randomUUID();
        Player playerA = createMockPlayer(playerAUuid, "PlayerA");

        // Player A creates a ring around (5, 5)
        assertTrue(plotManager.claimPlot(playerA, createMockBlock(5, 6)));
        assertTrue(plotManager.claimPlot(playerA, createMockBlock(5, 4)));
        assertTrue(plotManager.claimPlot(playerA, createMockBlock(6, 5)));
        assertTrue(plotManager.claimPlot(playerA, createMockBlock(4, 5)));

        // Player A fills the hole at (5, 5)
        boolean aClaimed = plotManager.claimPlot(playerA, createMockBlock(5, 5));
        assertTrue(aClaimed, "Player A should be allowed to claim inside their own surrounding ring!");
        assertNotNull(plotManager.getPlot(5, 5, "world"));
    }

    @Test
    void testHollowPerimeterEncirclementAtRadius2Prevented() {
        InMemoryPlotRepo repo = new InMemoryPlotRepo();
        PlotManager plotManager = new PlotManager(repo, mock(ItemStack.class));
        plotManager.setConfig(mockConfig);

        UUID playerAUuid = UUID.randomUUID();
        Player playerA = createMockPlayer(playerAUuid, "PlayerA");

        UUID playerBUuid = UUID.randomUUID();
        Player playerB = createMockPlayer(playerBUuid, "PlayerB");

        // Player A is at (0, 0)
        assertTrue(plotManager.claimPlot(playerA, createMockBlock(0, 0)));

        // Player B builds a 5x5 perimeter (radius 2) around Player A with wilderness inside
        List<int[]> perimeterChunks = new ArrayList<>();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.max(Math.abs(x), Math.abs(z)) == 2) {
                    perimeterChunks.add(new int[]{x, z});
                }
            }
        }
        assertEquals(16, perimeterChunks.size());

        // Claim first 14 perimeter chunks (indices 0 to 13)
        for (int i = 0; i < 14; i++) {
            int[] c = perimeterChunks.get(i);
            boolean claimed = plotManager.claimPlot(playerB, createMockBlock(c[0], c[1]));
            assertTrue(claimed, "Perimeter chunk " + i + " should succeed before ring is closed");
        }

        // The 15th chunk (index 14) is (2, 1), which closes the orthogonal perimeter around Player A
        int[] closingChunk = perimeterChunks.get(14);
        boolean closedRing = plotManager.claimPlot(playerB, createMockBlock(closingChunk[0], closingChunk[1]));
        assertFalse(closedRing, "Player B should not be allowed to close a hollow perimeter encircling Player A!");
    }

    @Test
    void testMultiPlayerCoalitionSurroundPrevented() {
        InMemoryPlotRepo repo = new InMemoryPlotRepo();
        PlotManager plotManager = new PlotManager(repo, mock(ItemStack.class));
        plotManager.setConfig(mockConfig);

        UUID targetUuid = UUID.randomUUID();
        Player targetPlayer = createMockPlayer(targetUuid, "TargetPlayer");

        UUID p1Uuid = UUID.randomUUID();
        Player p1 = createMockPlayer(p1Uuid, "Player1");

        UUID p2Uuid = UUID.randomUUID();
        Player p2 = createMockPlayer(p2Uuid, "Player2");

        UUID p3Uuid = UUID.randomUUID();
        Player p3 = createMockPlayer(p3Uuid, "Player3");

        UUID p4Uuid = UUID.randomUUID();
        Player p4 = createMockPlayer(p4Uuid, "Player4");

        // Target owns (0, 0)
        assertTrue(plotManager.claimPlot(targetPlayer, createMockBlock(0, 0)));

        // 3 different players claim 3 sides
        assertTrue(plotManager.claimPlot(p1, createMockBlock(0, 1)));
        assertTrue(plotManager.claimPlot(p2, createMockBlock(0, -1)));
        assertTrue(plotManager.claimPlot(p3, createMockBlock(1, 0)));

        // 4th player tries to close the trap
        boolean p4Claimed = plotManager.claimPlot(p4, createMockBlock(-1, 0));
        assertFalse(p4Claimed, "Coalition of multiple other players should not be able to surround target!");
    }

    @Test
    void testSurroundProtectionDisabledInConfigAllowsClaim() {
        when(mockConfig.isSurroundProtectionEnabled()).thenReturn(false);

        InMemoryPlotRepo repo = new InMemoryPlotRepo();
        PlotManager plotManager = new PlotManager(repo, mock(ItemStack.class));
        plotManager.setConfig(mockConfig);

        UUID playerAUuid = UUID.randomUUID();
        Player playerA = createMockPlayer(playerAUuid, "PlayerA");

        UUID playerBUuid = UUID.randomUUID();
        Player playerB = createMockPlayer(playerBUuid, "PlayerB");

        assertTrue(plotManager.claimPlot(playerA, createMockBlock(0, 0)));
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(0, 1)));
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(0, -1)));
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(1, 0)));

        // When surround protection is disabled, the 4th side CAN be claimed
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(-1, 0)),
                "Should allow surrounding when surround-protection is disabled in config");
    }

    @Test
    void testSearchDepthZeroTreatsAsDisabled() {
        when(mockConfig.getSurroundProtectionSearchDepth()).thenReturn(0);

        InMemoryPlotRepo repo = new InMemoryPlotRepo();
        PlotManager plotManager = new PlotManager(repo, mock(ItemStack.class));
        plotManager.setConfig(mockConfig);

        UUID playerAUuid = UUID.randomUUID();
        Player playerA = createMockPlayer(playerAUuid, "PlayerA");

        UUID playerBUuid = UUID.randomUUID();
        Player playerB = createMockPlayer(playerBUuid, "PlayerB");

        assertTrue(plotManager.claimPlot(playerA, createMockBlock(0, 0)));
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(0, 1)));
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(0, -1)));
        assertTrue(plotManager.claimPlot(playerB, createMockBlock(1, 0)));

        assertTrue(plotManager.claimPlot(playerB, createMockBlock(-1, 0)),
                "Should allow claim when search depth <= 0");
    }
}
