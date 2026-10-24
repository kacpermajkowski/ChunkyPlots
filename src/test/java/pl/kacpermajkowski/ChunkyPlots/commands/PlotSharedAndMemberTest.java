package pl.kacpermajkowski.ChunkyPlots.commands;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.list.PlotListCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.shared.PlotSharedCommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.database.DatabaseManager;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.SqlitePlotRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlotSharedAndMemberTest {
    private Server mockServer;
    private File tempFolder;
    private DatabaseManager databaseManager;
    private SqlitePlotRepository plotRepository;
    private PlotManager plotManager;
    private Player mockPlayer;
    private final UUID playerUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() throws IOException {
        mockServer = BukkitTestHelper.setUpMockServer();
        tempFolder = Files.createTempDirectory("chunkyplots-test-member").toFile();
        Logger logger = Logger.getLogger("ChunkyPlotsTest");

        databaseManager = new DatabaseManager(tempFolder, logger);
        plotRepository = new SqlitePlotRepository(databaseManager, tempFolder, logger);
        plotManager = new PlotManager(plotRepository, null);

        mockPlayer = mock(Player.class);
        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayer.getName()).thenReturn("TestMember");
    }

    @AfterEach
    void tearDown() throws IOException {
        BukkitTestHelper.tearDownMockServer();
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (tempFolder != null && tempFolder.exists()) {
            Files.walk(tempFolder.toPath())
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {}
                    });
        }
    }

    @Test
    @DisplayName("SqlitePlotRepository.loadByMember loads only plots where player is whitelisted")
    void testSqliteLoadByMember() {
        UUID owner1 = UUID.randomUUID();
        UUID owner2 = UUID.randomUUID();

        Plot plot1 = new Plot(owner1, 10, 20, "world", UUID.randomUUID());
        plot1.whitelistPlayer(playerUuid);
        plotRepository.saveSync(plot1);

        Plot plot2 = new Plot(owner2, 30, 40, "world", UUID.randomUUID());
        plot2.whitelistPlayer(UUID.randomUUID()); // other member
        plotRepository.saveSync(plot2);

        Plot plot3 = new Plot(owner2, 50, 60, "world", UUID.randomUUID());
        plot3.blacklistPlayer(playerUuid); // blacklisted, not whitelisted
        plotRepository.saveSync(plot3);

        List<Plot> memberPlots = plotRepository.loadByMember(playerUuid);
        assertEquals(1, memberPlots.size());
        assertEquals(plot1.getUUID(), memberPlots.get(0).getUUID());
    }

    @Test
    @DisplayName("PlotManager getPlotsByMember and getAccessiblePlots return owned and member plots")
    void testPlotManagerMemberAndAccessiblePlots() {
        UUID strangerUuid = UUID.randomUUID();

        // Owned plot
        Plot ownedPlot = new Plot(playerUuid, 1, 1, "world", UUID.randomUUID());
        // Member plot
        Plot memberPlot = new Plot(strangerUuid, 2, 2, "world", UUID.randomUUID());
        memberPlot.whitelistPlayer(playerUuid);
        // Third-party plot
        Plot otherPlot = new Plot(strangerUuid, 3, 3, "world", UUID.randomUUID());

        plotRepository.saveSync(ownedPlot);
        plotRepository.saveSync(memberPlot);
        plotRepository.saveSync(otherPlot);

        plotManager = new PlotManager(plotRepository, null);

        List<Plot> owned = plotManager.getPlotsByOwner(playerUuid);
        assertEquals(1, owned.size());
        assertEquals(ownedPlot.getUUID(), owned.get(0).getUUID());

        List<Plot> member = plotManager.getPlotsByMember(playerUuid);
        assertEquals(1, member.size());
        assertEquals(memberPlot.getUUID(), member.get(0).getUUID());

        List<Plot> accessible = plotManager.getAccessiblePlots(playerUuid);
        assertEquals(2, accessible.size());
        assertTrue(accessible.stream().anyMatch(p -> p.getUUID().equals(ownedPlot.getUUID())));
        assertTrue(accessible.stream().anyMatch(p -> p.getUUID().equals(memberPlot.getUUID())));
    }

    @Test
    @DisplayName("PlotSharedCommand has aliases member, memberplots, sharedplots")
    void testPlotSharedCommandAliases() {
        PlotSharedCommand cmd = new PlotSharedCommand(plotManager);
        assertEquals("shared", cmd.getName());
        assertEquals(List.of("member", "memberplots", "sharedplots"), cmd.getAliases());
    }

    @Test
    @DisplayName("PlotListCommand delegates 'member' and 'shared' to PlotSharedCommand")
    void testPlotListCommandDelegation() {
        PlotSharedCommand mockShared = mock(PlotSharedCommand.class);
        PlotListCommand listCmd = new PlotListCommand(plotManager, mockShared);

        listCmd.execute(mockPlayer, new String[]{"member"});
        verify(mockShared).execute(mockPlayer, new String[0]);

        listCmd.execute(mockPlayer, new String[]{"shared"});
        verify(mockShared, times(2)).execute(mockPlayer, new String[0]);

        List<String> tabs = listCmd.getTabCompletion(mockPlayer, new String[]{""});
        assertTrue(tabs.contains("member"));
        assertTrue(tabs.contains("shared"));
    }
}
