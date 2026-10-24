package pl.kacpermajkowski.ChunkyPlots.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.SqlitePlotRepository;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.user.User;
import pl.kacpermajkowski.ChunkyPlots.user.SqliteUserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class SqlitePersistenceTest {

    private File tempFolder;
    private DatabaseManager databaseManager;
    private SqlitePlotRepository plotRepository;
    private SqliteUserRepository userRepository;
    private Logger logger;

    @BeforeEach
    void setUp() throws IOException {
        tempFolder = Files.createTempDirectory("chunkyplots-test-db").toFile();
        logger = Logger.getLogger("ChunkyPlotsTest");

        databaseManager = new DatabaseManager(tempFolder, logger);
        plotRepository = new SqlitePlotRepository(databaseManager, tempFolder, logger);
        userRepository = new SqliteUserRepository(databaseManager, tempFolder, logger);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (tempFolder != null && tempFolder.exists()) {
            Files.walk(tempFolder.toPath())
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    @Test
    @DisplayName("SQLite Plot Persistence: Save, Load, and Delete plot with Whitelist/Blacklist")
    void testPlotSaveLoadDelete() {
        UUID plotId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID whitelistedUser = UUID.randomUUID();
        UUID blacklistedUser = UUID.randomUUID();

        Plot plot = new Plot(ownerId, 15, -25, "world_nether", plotId);
        plot.whitelistPlayer(whitelistedUser);
        plot.blacklistPlayer(blacklistedUser);

        // 1. Save plot synchronously
        plotRepository.saveSync(plot);

        // 2. Load plots
        List<Plot> loadedPlots = plotRepository.loadAll();
        assertEquals(1, loadedPlots.size());

        Plot loadedPlot = loadedPlots.get(0);
        assertEquals(plotId, loadedPlot.getUUID());
        assertEquals(ownerId, loadedPlot.getOwnerUUID());
        assertEquals(15, loadedPlot.getChunkX());
        assertEquals(-25, loadedPlot.getChunkZ());
        assertEquals("world_nether", loadedPlot.getWorldName());

        assertTrue(loadedPlot.isPlayerWhitelisted(whitelistedUser));
        assertTrue(loadedPlot.isPlayerBlacklisted(blacklistedUser));

        // 3. Update plot (add another whitelist member)
        UUID secondWhitelisted = UUID.randomUUID();
        loadedPlot.whitelistPlayer(secondWhitelisted);
        plotRepository.saveSync(loadedPlot);

        List<Plot> updatedPlots = plotRepository.loadAll();
        assertEquals(1, updatedPlots.size());
        assertTrue(updatedPlots.get(0).isPlayerWhitelisted(secondWhitelisted));

        // 4. Delete plot
        plotRepository.deleteSync(loadedPlot);
        assertTrue(plotRepository.loadAll().isEmpty(), "Plot must be removed after deletion");
    }

    @Test
    @DisplayName("SQLite Plot Flags Persistence: Save, Load, Update, Reset and Delete flags")
    void testPlotFlagPersistence() {
        UUID plotId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Plot plot = new Plot(ownerId, 10, 20, "world", plotId);
        // Default: PVP is false, ENTRY is true
        assertFalse(plot.getFlag(PlotFlag.PVP));
        assertTrue(plot.getFlag(PlotFlag.ENTRY));

        // Set custom flags
        plot.setFlag(PlotFlag.PVP, true);
        plot.setFlag(PlotFlag.ENTRY, false);
        plot.setFlag(PlotFlag.EXPLOSIONS, true);

        // Save plot with flags
        plotRepository.saveSync(plot);

        // Load plot
        List<Plot> loadedPlots = plotRepository.loadAll();
        assertEquals(1, loadedPlots.size());
        Plot loaded = loadedPlots.get(0);

        assertTrue(loaded.getFlag(PlotFlag.PVP));
        assertFalse(loaded.getFlag(PlotFlag.ENTRY));
        assertTrue(loaded.getFlag(PlotFlag.EXPLOSIONS));
        assertFalse(loaded.getFlag(PlotFlag.FIRE_SPREAD)); // default untouched

        // Reset PVP flag and update
        loaded.resetFlag(PlotFlag.PVP);
        plotRepository.saveSync(loaded);

        List<Plot> reloadedPlots = plotRepository.loadAll();
        assertEquals(1, reloadedPlots.size());
        Plot reloaded = reloadedPlots.get(0);

        assertFalse(reloaded.getFlag(PlotFlag.PVP));
        assertFalse(reloaded.getFlag(PlotFlag.ENTRY));
        assertTrue(reloaded.getFlag(PlotFlag.EXPLOSIONS));
        assertEquals(2, reloaded.getCustomFlags().size());

        // Delete plot
        plotRepository.deleteSync(reloaded);
        assertTrue(plotRepository.loadAll().isEmpty());
    }

    @Test
    @DisplayName("SQLite User Persistence: Save, Load, and Batch Save")
    void testUserSaveLoadBatch() {
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        User user1 = new User(user1Id);
        user1.setTeleportOnCooldown(true);

        User user2 = new User(user2Id);
        user2.setTeleportOnCooldown(false);

        // Batch save synchronously
        userRepository.saveAllSync(List.of(user1, user2));

        // Load all
        Set<User> loadedUsers = userRepository.loadAll();
        assertEquals(2, loadedUsers.size());

        User loadedUser1 = loadedUsers.stream()
                .filter(u -> u.getPlayerUUID().equals(user1Id))
                .findFirst()
                .orElse(null);
        assertNotNull(loadedUser1);
        assertTrue(loadedUser1.isTeleportOnCooldown());

        User loadedUser2 = loadedUsers.stream()
                .filter(u -> u.getPlayerUUID().equals(user2Id))
                .findFirst()
                .orElse(null);
        assertNotNull(loadedUser2);
        assertFalse(loadedUser2.isTeleportOnCooldown());
    }

    @Test
    @DisplayName("SQLite Async Queue: Asynchronous save flushes on databaseManager.close()")
    void testAsyncPersistenceFlush() {
        Plot plot = new Plot(UUID.randomUUID(), 3, 3, "world", UUID.randomUUID());
        plotRepository.save(plot);

        // Closing databaseManager blocks until async queue terminates
        databaseManager.close();

        // Re-open DB to verify persisted data
        DatabaseManager reOpenedDb = new DatabaseManager(tempFolder, logger);
        SqlitePlotRepository reOpenedRepo = new SqlitePlotRepository(reOpenedDb, tempFolder, logger);
        List<Plot> loaded = reOpenedRepo.loadAll();
        assertEquals(1, loaded.size());
        assertEquals(plot.getUUID(), loaded.get(0).getUUID());
        reOpenedDb.close();
    }
}
