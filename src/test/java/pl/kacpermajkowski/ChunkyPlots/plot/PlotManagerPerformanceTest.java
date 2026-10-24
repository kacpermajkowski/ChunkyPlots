package pl.kacpermajkowski.ChunkyPlots.plot;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.TestChunkFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

public class PlotManagerPerformanceTest {

    private Server mockServer;
    private World mockWorld;
    private final AtomicLong getChunkAtCallCount = new AtomicLong(0);
    private final Map<Long, Chunk> chunkCache = new ConcurrentHashMap<>();

    private static long packCoords(int x, int z) {
        return ((long) x & 0xFFFFFFFFL) | (((long) z & 0xFFFFFFFFL) << 32);
    }

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
        mockWorld = Mockito.mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        when(mockServer.getWorld("world")).thenReturn(mockWorld);
        chunkCache.clear();

        // Increment invocation counter if world.getChunkAt(x, z) is ever called
        when(mockWorld.getChunkAt(anyInt(), anyInt())).thenAnswer(invocation -> {
            getChunkAtCallCount.incrementAndGet();
            int x = invocation.getArgument(0);
            int z = invocation.getArgument(1);
            return chunkCache.computeIfAbsent(packCoords(x, z), k -> TestChunkFactory.createChunk(x, z, mockWorld));
        });
    }

    @AfterEach
    void tearDown() {
        BukkitTestHelper.tearDownMockServer();
        chunkCache.clear();
    }

    @Test
    @DisplayName("Fix Verification: getPlot(Chunk) executes with ZERO calls to world.getChunkAt()")
    void testZeroChunkAtCalls() {
        int plotCount = 200;
        List<Plot> plotList = new ArrayList<>();
        for (int i = 0; i < plotCount; i++) {
            plotList.add(new Plot(UUID.randomUUID(), i, 0, "world", UUID.randomUUID()));
        }

        PlotRepository mockRepo = Mockito.mock(PlotRepository.class);
        when(mockRepo.loadAll()).thenReturn(plotList);

        PlotManager plotManager = new PlotManager(mockRepo, Mockito.mock(ItemStack.class));

        // 1. Miss lookup
        Chunk missChunk = TestChunkFactory.createChunk(9999, 9999, mockWorld);
        getChunkAtCallCount.set(0);

        Plot missResult = plotManager.getPlot(missChunk);
        assertNull(missResult);
        assertEquals(0, getChunkAtCallCount.get(), "Miss lookup must never call world.getChunkAt()!");

        // 2. Hit lookup
        Chunk hitChunk = TestChunkFactory.createChunk(100, 0, mockWorld);
        Plot hitResult = plotManager.getPlot(hitChunk);
        assertNotNull(hitResult);
        assertEquals(100, hitResult.getChunkX());
        assertEquals(0, getChunkAtCallCount.get(), "Hit lookup must never call world.getChunkAt()!");
    }

    @ParameterizedTest(name = "Scaling Benchmark with {0} plots")
    @ValueSource(ints = {10, 200, 1000, 15000})
    @DisplayName("Fix Verification: O(1) Scaling Matrix across 10, 200, 1000, and 15000 plots")
    void testScalingMatrix(int plotCount) {
        List<Plot> plotList = new ArrayList<>(plotCount);
        for (int i = 0; i < plotCount; i++) {
            plotList.add(new Plot(UUID.randomUUID(), i, i, "world", UUID.randomUUID()));
        }

        PlotRepository mockRepo = Mockito.mock(PlotRepository.class);
        when(mockRepo.loadAll()).thenReturn(plotList);

        PlotManager plotManager = new PlotManager(mockRepo, Mockito.mock(ItemStack.class));

        // Now that lookups are O(1) hash lookups with 0 chunk loads,
        // we can easily run 5,000 queries even for 15,000 plots!
        int queryCount = 5000;

        // 50% hits (registered plots) and 50% misses (unclaimed chunks)
        List<Chunk> queryChunks = new ArrayList<>(queryCount);
        for (int i = 0; i < queryCount; i++) {
            if (i % 2 == 0) {
                int targetIndex = (i * 37) % plotCount;
                queryChunks.add(TestChunkFactory.createChunk(targetIndex, targetIndex, mockWorld));
            } else {
                queryChunks.add(TestChunkFactory.createChunk(plotCount + i + 100, plotCount + i + 100, mockWorld));
            }
        }

        // Reset counters and record benchmark timing
        getChunkAtCallCount.set(0);
        long startTime = System.nanoTime();

        for (Chunk queryChunk : queryChunks) {
            plotManager.getPlot(queryChunk);
        }

        long durationNs = System.nanoTime() - startTime;
        double durationMs = durationNs / 1_000_000.0;
        long totalChunkCalls = getChunkAtCallCount.get();
        double avgCallsPerLookup = (double) totalChunkCalls / queryCount;
        double avgLatencyPerLookupMs = durationMs / queryCount;

        System.out.println("--------------------------------------------------");
        System.out.printf("POST-FIX READING: Scale = %5d plots | Queries = %4d%n", plotCount, queryCount);
        System.out.printf("  Total Execution Time: %.2f ms%n", durationMs);
        System.out.printf("  Average Latency/Lookup: %.5f ms (%.2f µs)%n", avgLatencyPerLookupMs, avgLatencyPerLookupMs * 1000);
        System.out.printf("  Total world.getChunkAt() calls: %d%n", totalChunkCalls);
        System.out.printf("  Average getChunkAt() per lookup: %.1f calls%n", avgCallsPerLookup);
        System.out.println("--------------------------------------------------");

        assertEquals(0, totalChunkCalls, "Zero calls to world.getChunkAt() must occur during plot lookups!");
    }
}
