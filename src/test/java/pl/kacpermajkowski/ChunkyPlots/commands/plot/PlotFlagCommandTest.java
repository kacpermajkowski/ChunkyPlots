package pl.kacpermajkowski.ChunkyPlots.commands.plot;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.flag.PlotFlagCommand;
import pl.kacpermajkowski.ChunkyPlots.database.DatabaseManager;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.SqlitePlotRepository;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlotFlagCommandTest {
    private Server mockServer;
    private File tempFolder;
    private DatabaseManager databaseManager;
    private SqlitePlotRepository plotRepository;
    private PlotManager plotManager;
    private PlotFlagCommand flagCommand;
    private Player mockOwner;
    private Player mockStranger;
    private World mockWorld;
    private final UUID ownerUuid = UUID.randomUUID();
    private final UUID strangerUuid = UUID.randomUUID();
    private Plot plot;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = BukkitTestHelper.setUpMockServer();
        tempFolder = Files.createTempDirectory("chunkyplots-flag-test").toFile();
        Logger logger = Logger.getLogger("ChunkyPlotsFlagTest");

        mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        when(mockServer.getWorld("world")).thenReturn(mockWorld);

        databaseManager = new DatabaseManager(tempFolder, logger);
        plotRepository = new SqlitePlotRepository(databaseManager, tempFolder, logger);

        Plot initialPlot = new Plot(ownerUuid, 5, 5, "world", UUID.randomUUID());
        plotRepository.saveSync(initialPlot);
        plotManager = new PlotManager(plotRepository, null);
        plot = plotManager.getPlot(initialPlot.getUUID());
        flagCommand = new PlotFlagCommand(plotManager);

        mockOwner = mock(Player.class);
        when(mockOwner.getUniqueId()).thenReturn(ownerUuid);
        when(mockOwner.getName()).thenReturn("PlotOwner");

        mockStranger = mock(Player.class);
        when(mockStranger.getUniqueId()).thenReturn(strangerUuid);
        when(mockStranger.getName()).thenReturn("Stranger");

        when(mockOwner.getLocation()).thenAnswer(inv -> new Location(mockWorld, 88.0, 64.0, 88.0));
        when(mockStranger.getLocation()).thenAnswer(inv -> new Location(mockWorld, 88.0, 64.0, 88.0));
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
    @DisplayName("Command metadata: name, aliases, permission, syntax")
    void testCommandMetadata() {
        assertEquals("flag", flagCommand.getName());
        assertTrue(flagCommand.getAliases().contains("flags"));
        assertTrue(flagCommand.getAliases().contains("flaga"));
        assertEquals("chunkyplots.player", flagCommand.getPermission());
        assertNotNull(flagCommand.getDescription());
        assertNotNull(flagCommand.getSyntax());
    }

    @Test
    @DisplayName("Non-owner executing flag command is rejected")
    void testNonOwnerRejected() {
        flagCommand.execute(mockStranger, new String[]{"list"});
        verify(mockStranger, atLeastOnce()).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("List subcommand sends flag status to owner")
    void testListFlags() {
        flagCommand.execute(mockOwner, new String[]{"list"});
        verify(mockOwner, atLeast(2)).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("Set subcommand updates flag and persists to database")
    void testSetFlagAndPersist() {
        assertFalse(plot.getFlag(PlotFlag.PVP));

        flagCommand.execute(mockOwner, new String[]{"set", "pvp", "true"});

        Plot updatedPlot = plotManager.getPlot(plot.getUUID());
        assertTrue(updatedPlot.getFlag(PlotFlag.PVP));

        // Reload directly from database repository
        List<Plot> fromDb = plotRepository.loadAll();
        assertTrue(fromDb.get(0).getFlag(PlotFlag.PVP));
    }

    @Test
    @DisplayName("Direct flag subcommand /plot flag pvp true and toggle")
    void testDirectFlagSubcommand() {
        assertFalse(plot.getFlag(PlotFlag.PVP));

        // Set true
        flagCommand.execute(mockOwner, new String[]{"pvp", "true"});
        assertTrue(plotManager.getPlot(plot.getUUID()).getFlag(PlotFlag.PVP));

        // Toggle to false
        flagCommand.execute(mockOwner, new String[]{"pvp", "toggle"});
        assertFalse(plotManager.getPlot(plot.getUUID()).getFlag(PlotFlag.PVP));

        // Direct info /plot flag pvp
        flagCommand.execute(mockOwner, new String[]{"pvp"});
        verify(mockOwner, atLeastOnce()).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("Info subcommand /plot flag info pvp")
    void testInfoSubcommand() {
        flagCommand.execute(mockOwner, new String[]{"info", "pvp"});
        verify(mockOwner, atLeastOnce()).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("Reset subcommand resets single flag and all flags")
    void testResetSubcommand() {
        plot.setFlag(PlotFlag.PVP, true);
        plot.setFlag(PlotFlag.ENTRY, false);
        plotRepository.saveSync(plot);

        // Reset pvp
        flagCommand.execute(mockOwner, new String[]{"reset", "pvp"});
        assertFalse(plotManager.getPlot(plot.getUUID()).getFlag(PlotFlag.PVP));
        assertFalse(plotManager.getPlot(plot.getUUID()).getFlag(PlotFlag.ENTRY));

        // Reset all
        flagCommand.execute(mockOwner, new String[]{"reset", "all"});
        assertTrue(plotManager.getPlot(plot.getUUID()).getFlag(PlotFlag.ENTRY)); // ENTRY default is true
        assertTrue(plotManager.getPlot(plot.getUUID()).getCustomFlags().isEmpty());
    }

    @Test
    @DisplayName("Tab completion suggests flags and booleans correctly")
    void testTabCompletion() {
        // arg 0
        List<String> arg0 = flagCommand.getTabCompletion(mockOwner, new String[]{""});
        assertTrue(arg0.contains("list"));
        assertTrue(arg0.contains("set"));
        assertTrue(arg0.contains("info"));
        assertTrue(arg0.contains("reset"));
        assertTrue(arg0.contains("pvp"));
        assertTrue(arg0.contains("explosions"));

        // arg 1 for set
        List<String> setFlags = flagCommand.getTabCompletion(mockOwner, new String[]{"set", ""});
        assertTrue(setFlags.contains("pvp"));
        assertTrue(setFlags.contains("explosions"));

        // arg 1 for reset
        List<String> resetFlags = flagCommand.getTabCompletion(mockOwner, new String[]{"reset", ""});
        assertTrue(resetFlags.contains("all"));
        assertTrue(resetFlags.contains("pvp"));

        // arg 1 for direct flag
        List<String> directValues = flagCommand.getTabCompletion(mockOwner, new String[]{"pvp", ""});
        assertTrue(directValues.contains("true"));
        assertTrue(directValues.contains("false"));
        assertTrue(directValues.contains("toggle"));

        // arg 2 for set
        List<String> setValues = flagCommand.getTabCompletion(mockOwner, new String[]{"set", "pvp", ""});
        assertTrue(setValues.contains("true"));
        assertTrue(setValues.contains("false"));
        assertTrue(setValues.contains("toggle"));
    }
}
