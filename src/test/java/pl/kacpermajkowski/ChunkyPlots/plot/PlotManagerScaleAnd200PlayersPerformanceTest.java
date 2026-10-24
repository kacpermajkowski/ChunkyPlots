package pl.kacpermajkowski.ChunkyPlots.plot;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.TestChunkFactory;
import pl.kacpermajkowski.ChunkyPlots.database.DatabaseManager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class PlotManagerScaleAnd200PlayersPerformanceTest {

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

    /**
     * Virtual high-scale repository simulating a database of 1,000,000,000 (1 Billion) plots.
     * Generates plots deterministically for coordinates within a 1-billion plot universe.
     */
    static class OneBillionPlotRepository implements PlotRepository {
        public static final int TOTAL_PLOTS = 1_000_000_000;
        public static final int GRID_SIZE = 31622; // ~31,622 x 31,622 grid = 1,000,000,000 plots

        final AtomicInteger dbQueryCount = new AtomicInteger(0);

        @Override
        public int count() {
            return TOTAL_PLOTS;
        }

        @Override
        public List<Plot> loadAll() {
            throw new UnsupportedOperationException("Attempted to load 1 billion plots into RAM! Tiered cache must prevent this.");
        }

        @Override
        public Plot loadByChunk(int chunkX, int chunkZ, String worldName) {
            dbQueryCount.incrementAndGet();
            if (chunkX >= 0 && chunkX < GRID_SIZE && chunkZ >= 0 && chunkZ < GRID_SIZE) {
                UUID plotUuid = new UUID(((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL), 0x1B1110000000L);
                UUID ownerUuid = new UUID((long) (chunkX % 1000) << 32, chunkZ % 1000);
                return new Plot(ownerUuid, chunkX, chunkZ, worldName != null ? worldName : "world", plotUuid);
            }
            return null; // Wilderness
        }

        @Override
        public Plot loadByUuid(UUID uuid) {
            dbQueryCount.incrementAndGet();
            if (uuid != null && uuid.getLeastSignificantBits() == 0x1B1110000000L) {
                long msb = uuid.getMostSignificantBits();
                int chunkX = (int) (msb >> 32);
                int chunkZ = (int) (msb & 0xFFFFFFFFL);
                return loadByChunk(chunkX, chunkZ, "world");
            }
            return null;
        }

        @Override
        public List<Plot> loadByOwner(UUID ownerUuid) {
            dbQueryCount.incrementAndGet();
            List<Plot> owned = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                owned.add(new Plot(ownerUuid, i, i, "world", UUID.randomUUID()));
            }
            return owned;
        }

        @Override
        public void save(Plot plot) {}

        @Override
        public void delete(Plot plot) {}
    }

    @Test
    @DisplayName("Performance Benchmark: 200 Concurrent Players under high load (200,000 operations)")
    void test200PlayersConcurrentStress() throws InterruptedException {
        int playerCount = 200;
        int operationsPerPlayer = 1000;
        int totalOperations = playerCount * operationsPerPlayer;

        // Pre-populate with 2,000 active plots
        Map<Long, Plot> initialPlots = new ConcurrentHashMap<>();
        for (int i = 0; i < 2000; i++) {
            UUID ownerUuid = UUID.randomUUID();
            Plot plot = new Plot(ownerUuid, i, i, "world", UUID.randomUUID());
            initialPlots.put(PlotManager.chunkKey(i, i), plot);
        }

        PlotRepository memoryRepo = new PlotRepository() {
            @Override
            public List<Plot> loadAll() {
                return new ArrayList<>(initialPlots.values());
            }

            @Override
            public Plot loadByChunk(int chunkX, int chunkZ, String worldName) {
                return initialPlots.get(PlotManager.chunkKey(chunkX, chunkZ));
            }

            @Override
            public int count() {
                return initialPlots.size();
            }

            @Override
            public void save(Plot plot) {
                initialPlots.put(PlotManager.chunkKey(plot.getChunkX(), plot.getChunkZ()), plot);
            }

            @Override
            public void delete(Plot plot) {
                initialPlots.remove(PlotManager.chunkKey(plot.getChunkX(), plot.getChunkZ()));
            }
        };

        PlotManager plotManager = new PlotManager(memoryRepo, Mockito.mock(ItemStack.class));

        ExecutorService playerThreadPool = Executors.newFixedThreadPool(playerCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(playerCount);

        long[][] allThreadLatencies = new long[playerCount][operationsPerPlayer];
        AtomicLong errorCount = new AtomicLong(0);

        for (int p = 0; p < playerCount; p++) {
            final int playerIndex = p;
            final UUID playerUuid = UUID.randomUUID();
            final long[] localLatencies = allThreadLatencies[playerIndex];

            playerThreadPool.submit(() -> {
                try {
                    startLatch.await(); // Synchronize all 200 threads to start simultaneously
                    Random rnd = new Random(playerIndex * 31L);

                    for (int op = 0; op < operationsPerPlayer; op++) {
                        long t0 = System.nanoTime();

                        int choice = rnd.nextInt(10);
                        if (choice < 6) {
                            // 60% coordinate lookups (moving across chunks)
                            int x = (playerIndex * 10 + op) % 2000;
                            int z = (playerIndex * 10 + op) % 2000;
                            Plot plot = plotManager.getPlot(x, z, "world");
                            if (plot != null && plot.getChunkX() != x) {
                                errorCount.incrementAndGet();
                            }
                        } else if (choice < 9) {
                            // 30% wilderness lookups (unclaimed land outside plot zone)
                            int x = 50000 + rnd.nextInt(5000);
                            int z = 50000 + rnd.nextInt(5000);
                            Plot wilderness = plotManager.getPlot(x, z, "world");
                            if (wilderness != null) {
                                errorCount.incrementAndGet();
                            }
                        } else {
                            // 10% getPlotsByOwner or UUID lookups
                            List<Plot> owned = plotManager.getPlotsByOwner(playerUuid);
                            if (owned == null) {
                                errorCount.incrementAndGet();
                            }
                        }

                        long t1 = System.nanoTime();
                        localLatencies[op] = (t1 - t0);
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Trigger simultaneous execution
        long globalStart = System.nanoTime();
        startLatch.countDown();

        boolean finishedInTime = doneLatch.await(15, TimeUnit.SECONDS);
        long globalDurationNs = System.nanoTime() - globalStart;
        playerThreadPool.shutdown();

        assertTrue(finishedInTime, "200 concurrent players must complete all operations within 15 seconds!");
        assertEquals(0, errorCount.get(), "Zero concurrency errors or race conditions must occur!");

        double durationMs = globalDurationNs / 1_000_000.0;
        double throughputOpsPerSec = ((double) totalOperations / globalDurationNs) * 1_000_000_000.0;

        // Flatten and sort latencies for percentiles
        long[] combined = new long[totalOperations];
        int idx = 0;
        for (int p = 0; p < playerCount; p++) {
            System.arraycopy(allThreadLatencies[p], 0, combined, idx, operationsPerPlayer);
            idx += operationsPerPlayer;
        }
        Arrays.sort(combined);

        long p50Ns = combined[(int) (totalOperations * 0.50)];
        long p95Ns = combined[(int) (totalOperations * 0.95)];
        long p99Ns = combined[(int) (totalOperations * 0.99)];
        long maxNs = combined[totalOperations - 1];

        double avgLatencyUs = (Arrays.stream(combined).average().orElse(0)) / 1000.0;
        double p50Us = p50Ns / 1000.0;
        double p95Us = p95Ns / 1000.0;
        double p99Us = p99Ns / 1000.0;

        System.out.println("==========================================================");
        System.out.println("200 CONCURRENT PLAYERS STRESS TEST RESULTS:");
        System.out.printf("  Total Operations: %d across %d threads%n", totalOperations, playerCount);
        System.out.printf("  Total Time: %.2f ms%n", durationMs);
        System.out.printf("  Throughput: %,.0f ops/sec%n", throughputOpsPerSec);
        System.out.printf("  Latency Avg: %.2f µs%n", avgLatencyUs);
        System.out.printf("  Latency P50: %.2f µs%n", p50Us);
        System.out.printf("  Latency P95: %.2f µs%n", p95Us);
        System.out.printf("  Latency P99: %.2f µs (%.3f ms)%n", p99Us, p99Us / 1000.0);
        System.out.printf("  Latency Max: %.2f µs%n", maxNs / 1000.0);
        System.out.println("==========================================================");

        // Minecraft tick budget is 50ms (50,000 µs). P99 should easily fit within 10ms (10,000 µs).
        assertTrue(p99Us < 10000.0, "P99 latency must be < 10.0 ms to ensure smooth 20 TPS server responsiveness under 200 threads!");
        assertTrue(throughputOpsPerSec > 50_000.0, "Throughput must exceed 50,000 ops/sec under 200 player load!");
    }

    @Test
    @DisplayName("Scale Benchmark: 1 Billion Plots Database Responsiveness & Memory Bounds")
    void testOneBillionPlotsScaleSimulation() {
        OneBillionPlotRepository billionRepo = new OneBillionPlotRepository();

        long startInit = System.currentTimeMillis();
        // PlotManager detects 1 billion plots (> 50,000) and enables tiered on-demand caching
        PlotManager plotManager = new PlotManager(billionRepo, Mockito.mock(ItemStack.class));
        long initDurationMs = System.currentTimeMillis() - startInit;

        System.out.println("1 Billion Plots PlotManager Init Time: " + initDurationMs + " ms");
        assertTrue(initDurationMs < 200, "Initialization with 1 billion plots in database must be instantaneous (< 200ms)!");

        // Simulate 200 players moving around within an 8x8 active chunk cluster (64 chunks each)
        int playerCount = 200;
        int operationsPerPlayer = 250;
        int totalOps = playerCount * operationsPerPlayer;

        billionRepo.dbQueryCount.set(0);
        long startOps = System.nanoTime();

        for (int p = 0; p < playerCount; p++) {
            int baseChunkX = (p * 50);
            int baseChunkZ = (p * 50);

            for (int op = 0; op < operationsPerPlayer; op++) {
                // Moving back and forth inside the player's 8x8 chunk area
                int chunkX = baseChunkX + (op % 8);
                int chunkZ = baseChunkZ + ((op / 8) % 8);

                Plot plot = plotManager.getPlot(chunkX, chunkZ, "world");
                if (chunkX < OneBillionPlotRepository.GRID_SIZE && chunkZ < OneBillionPlotRepository.GRID_SIZE) {
                    assertNotNull(plot, "Plot in 1-billion grid must be found!");
                    assertEquals(chunkX, plot.getChunkX());
                    assertEquals(chunkZ, plot.getChunkZ());
                }
            }
        }

        long durationNs = System.nanoTime() - startOps;
        double durationMs = durationNs / 1_000_000.0;
        double avgLatencyUs = (double) durationNs / totalOps / 1000.0;
        int dbQueries = billionRepo.dbQueryCount.get();

        System.out.println("==========================================================");
        System.out.println("1 BILLION PLOTS SIMULATION RESULTS:");
        System.out.printf("  Total Ops across 200 Players: %d%n", totalOps);
        System.out.printf("  Total Time: %.2f ms%n", durationMs);
        System.out.printf("  Avg Latency per Lookup: %.2f µs%n", avgLatencyUs);
        System.out.printf("  Database Queries (Cache Misses): %d (%.1f%%)%n", dbQueries, (dbQueries * 100.0 / totalOps));
        System.out.printf("  L1 Cache Hits: %d (%.1f%%)%n", (totalOps - dbQueries), ((totalOps - dbQueries) * 100.0 / totalOps));
        System.out.println("==========================================================");

        // Cache must absorb the vast majority of lookups
        assertTrue(dbQueries < totalOps, "L1 Spatial cache must prevent redundant database queries!");
        assertTrue(avgLatencyUs < 100.0, "Average lookup latency in 1-billion plot database must be < 100 µs!");
    }

    @Test
    @DisplayName("Wilderness Negative-Cache: Zero database queries on repeated wilderness chunks")
    void testWildernessNegativeCache() {
        OneBillionPlotRepository billionRepo = new OneBillionPlotRepository();
        PlotManager plotManager = new PlotManager(billionRepo, Mockito.mock(ItemStack.class));

        int wildernessX = -99999;
        int wildernessZ = -99999;

        billionRepo.dbQueryCount.set(0);

        // First lookup: misses L1 and wilderness cache, queries DB once
        Plot first = plotManager.getPlot(wildernessX, wildernessZ, "world");
        assertNull(first);
        assertEquals(1, billionRepo.dbQueryCount.get(), "First wilderness lookup should query repository once.");

        // Subsequent 10,000 lookups by 200 players crossing this chunk
        for (int i = 0; i < 10000; i++) {
            Plot next = plotManager.getPlot(wildernessX, wildernessZ, "world");
            assertNull(next);
        }

        assertEquals(1, billionRepo.dbQueryCount.get(), "Subsequent 10,000 wilderness lookups must hit negative-cache with 0 DB queries!");
    }

    @Test
    @DisplayName("Chunk Eviction: Unloaded chunks are cleanly purged from active memory")
    void testChunkEvictionOnUnload() {
        OneBillionPlotRepository billionRepo = new OneBillionPlotRepository();
        PlotManager plotManager = new PlotManager(billionRepo, Mockito.mock(ItemStack.class));

        int x = 10;
        int z = 20;

        // Load into cache
        Plot plot = plotManager.getPlot(x, z, "world");
        assertNotNull(plot);
        assertEquals(1, plotManager.getPlots().size());

        // Simulate Paper ChunkUnloadEvent
        plotManager.evictChunk(x, z, "world");

        // Cache should now be empty
        assertEquals(0, plotManager.getPlots().size());
    }

    @Test
    @DisplayName("SQLite Database B-Tree Index: EXPLAIN QUERY PLAN verifies O(log N) index seeks")
    void testSqliteBTreeIndexQueryPlan(@TempDir File tempDir) throws SQLException {
        DatabaseManager dbManager = new DatabaseManager(tempDir, Logger.getLogger("SqliteScaleTest"));
        try (Connection conn = dbManager.getConnection()) {

            // 1. Verify coordinate lookup uses idx_plots_coords (or sqlite_autoindex_plots_2 on unique coords)
            try (PreparedStatement stmt = conn.prepareStatement(
                    "EXPLAIN QUERY PLAN SELECT uuid, owner_uuid, chunk_x, chunk_z, world_name FROM plots WHERE world_name = ? AND chunk_x = ? AND chunk_z = ?;");
                 ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                String detail = rs.getString("detail");
                System.out.println("Coordinate Query Plan: " + detail);
                assertTrue(detail.contains("idx_plots_coords") || detail.contains("USING INDEX"),
                        "Coordinates query must use B-Tree index for O(log N) seeks!");
            }

            // 2. Verify owner lookup uses idx_plots_owner
            try (PreparedStatement stmt = conn.prepareStatement(
                    "EXPLAIN QUERY PLAN SELECT uuid, owner_uuid, chunk_x, chunk_z, world_name FROM plots WHERE owner_uuid = ?;");
                 ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                String detail = rs.getString("detail");
                System.out.println("Owner Query Plan: " + detail);
                assertTrue(detail.contains("idx_plots_owner") || detail.contains("USING INDEX"),
                        "Owner query must use idx_plots_owner B-Tree index for O(log N) seeks!");
            }

            // 3. Verify member lookup uses idx_plot_members_plot
            try (PreparedStatement stmt = conn.prepareStatement(
                    "EXPLAIN QUERY PLAN SELECT player_uuid, member_type FROM plot_members WHERE plot_uuid = ?;");
                 ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                String detail = rs.getString("detail");
                System.out.println("Plot Members Query Plan: " + detail);
                assertTrue(detail.contains("idx_plot_members_plot") || detail.contains("USING INDEX"),
                        "Plot members query must use idx_plot_members_plot B-Tree index!");
            }
        } finally {
            dbManager.close();
        }
    }
}
