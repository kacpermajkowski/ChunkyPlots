package pl.kacpermajkowski.ChunkyPlots.config;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.TestChunkFactory;
import pl.kacpermajkowski.ChunkyPlots.api.ChunkyPlotsAPI;
import pl.kacpermajkowski.ChunkyPlots.api.ChunkyPlotsAPIImpl;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotRepository;

import java.io.StringReader;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConfigWorldsTest {
    private Server mockServer;
    private JavaPlugin mockPlugin;

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
        mockPlugin = mock(JavaPlugin.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("ChunkyPlotsTest"));
        when(mockPlugin.getServer()).thenReturn(mockServer);
    }

    @AfterEach
    void tearDown() {
        Config.setInstance(null);
        BukkitTestHelper.tearDownMockServer();
    }

    private YamlConfiguration createBaseConfig(String extraYaml) throws Exception {
        String base = """
                use-message-prefix: true
                prefix: "[ChunkyPlots]"
                prefix-spacer: " > "
                interaction-message-display: [CHAT]
                plot-item:
                  material: "STONE"
                  name: "Plot Block"
                  lore: ["Claim chunk"]
                plot-limits:
                  default: 5
                """ + extraYaml;
        YamlConfiguration config = new YamlConfiguration();
        config.load(new StringReader(base));
        return config;
    }

    private YamlConfiguration createBaseMessages() throws Exception {
        YamlConfiguration messages = new YamlConfiguration();
        for (Message msg : Message.values()) {
            messages.set("messages." + msg.name().toLowerCase(), "Message: " + msg.name());
        }
        return messages;
    }

    @Test
    @DisplayName("Default worlds fallback: world='', world_nether='nether', world_end='end'")
    void testDefaultWorlds() throws Exception {
        YamlConfiguration configYaml = createBaseConfig("");
        YamlConfiguration messagesYaml = createBaseMessages();

        Config config = new Config(mockPlugin, configYaml, messagesYaml);

        assertTrue(config.isWorldAllowed("world"));
        assertTrue(config.isWorldAllowed("world_nether"));
        assertTrue(config.isWorldAllowed("world_end"));
        assertFalse(config.isWorldAllowed("custom_realm"));

        assertEquals("", config.getWorldAlias("world"));
        assertEquals("nether", config.getWorldAlias("world_nether"));
        assertEquals("end", config.getWorldAlias("world_end"));

        assertEquals("world", config.getWorldByAlias(""));
        assertEquals("world_nether", config.getWorldByAlias("nether"));
        assertEquals("world_end", config.getWorldByAlias("end"));
        assertEquals("world_nether", config.getWorldByAlias("world_nether"));
    }

    @Test
    @DisplayName("Custom configured worlds and unique aliases load correctly")
    void testCustomWorldsConfig() throws Exception {
        String customWorldsYaml = """
                worlds:
                  overworld: ""
                  nether_realm: "nether"
                  mining_world: "mining"
                """;
        YamlConfiguration configYaml = createBaseConfig(customWorldsYaml);
        YamlConfiguration messagesYaml = createBaseMessages();

        Config config = new Config(mockPlugin, configYaml, messagesYaml);

        assertTrue(config.isWorldAllowed("overworld"));
        assertTrue(config.isWorldAllowed("nether_realm"));
        assertTrue(config.isWorldAllowed("mining_world"));
        assertFalse(config.isWorldAllowed("world"));

        assertEquals("", config.getWorldAlias("overworld"));
        assertEquals("nether", config.getWorldAlias("nether_realm"));
        assertEquals("mining", config.getWorldAlias("mining_world"));

        assertEquals("overworld", config.getWorldByAlias(""));
        assertEquals("nether_realm", config.getWorldByAlias("nether"));
        assertEquals("mining_world", config.getWorldByAlias("mining"));
    }

    @Test
    @DisplayName("Duplicate world aliases disable the plugin and log error")
    void testDuplicateWorldAliasesThrows() throws Exception {
        String duplicateYaml = """
                worlds:
                  world1: "nether"
                  world2: "nether"
                """;
        YamlConfiguration configYaml = createBaseConfig(duplicateYaml);
        YamlConfiguration messagesYaml = createBaseMessages();

        new Config(mockPlugin, configYaml, messagesYaml);

        // Plugin should be disabled due to invalid configuration
        PluginManager pm = mockServer.getPluginManager();
        verify(pm).disablePlugin(mockPlugin);
    }

    @Test
    @DisplayName("Duplicate empty aliases disable the plugin")
    void testDuplicateEmptyAliasesThrows() throws Exception {
        String duplicateYaml = """
                worlds:
                  world1: ""
                  world2: ""
                """;
        YamlConfiguration configYaml = createBaseConfig(duplicateYaml);
        YamlConfiguration messagesYaml = createBaseMessages();

        new Config(mockPlugin, configYaml, messagesYaml);

        PluginManager pm = mockServer.getPluginManager();
        verify(pm).disablePlugin(mockPlugin);
    }

    @Test
    @DisplayName("Case-insensitive duplicate aliases disable the plugin")
    void testCaseInsensitiveDuplicateAliasesThrows() throws Exception {
        String duplicateYaml = """
                worlds:
                  world_nether: "Nether"
                  custom_nether: "nether"
                """;
        YamlConfiguration configYaml = createBaseConfig(duplicateYaml);
        YamlConfiguration messagesYaml = createBaseMessages();

        new Config(mockPlugin, configYaml, messagesYaml);

        PluginManager pm = mockServer.getPluginManager();
        verify(pm).disablePlugin(mockPlugin);
    }

    @Test
    @DisplayName("Plot.getID() formats ID with alias: 10;20 for '' and 10;20;nether for 'nether'")
    void testPlotGetIdWithAlias() throws Exception {
        YamlConfiguration configYaml = createBaseConfig("""
                worlds:
                  world: ""
                  world_nether: "nether"
                """);
        YamlConfiguration messagesYaml = createBaseMessages();
        new Config(mockPlugin, configYaml, messagesYaml);

        UUID owner = UUID.randomUUID();
        Plot overworldPlot = new Plot(owner, 10, 20, "world", UUID.randomUUID());
        Plot netherPlot = new Plot(owner, 10, 20, "world_nether", UUID.randomUUID());

        assertEquals("10;20", overworldPlot.getID());
        assertEquals("", overworldPlot.getWorldAlias());

        assertEquals("10;20;nether", netherPlot.getID());
        assertEquals("nether", netherPlot.getWorldAlias());
    }

    @Test
    @DisplayName("PlotManager enforces allowed worlds and resolves plots with world aliases")
    void testPlotManagerWorldClaimingAndResolving() throws Exception {
        YamlConfiguration configYaml = createBaseConfig("""
                worlds:
                  world: ""
                  world_nether: "nether"
                """);
        YamlConfiguration messagesYaml = createBaseMessages();
        Config config = new Config(mockPlugin, configYaml, messagesYaml);

        PlotRepository mockRepo = mock(PlotRepository.class);
        when(mockRepo.count()).thenReturn(0);
        when(mockRepo.loadAll()).thenReturn(List.of());

        ItemStack mockPlotItem = mock(ItemStack.class);
        PlotManager plotManager = new PlotManager(mockRepo, mockPlotItem);
        plotManager.setConfig(config);

        World allowedWorld = mock(World.class);
        when(allowedWorld.getName()).thenReturn("world");

        World disallowedWorld = mock(World.class);
        when(disallowedWorld.getName()).thenReturn("disallowed_world");

        World netherWorld = mock(World.class);
        when(netherWorld.getName()).thenReturn("world_nether");

        when(mockServer.getWorld("world")).thenReturn(allowedWorld);
        when(mockServer.getWorld("world_nether")).thenReturn(netherWorld);
        when(mockServer.getWorld("disallowed_world")).thenReturn(disallowedWorld);

        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());

        // 1. Claim in disallowed world -> fails
        Block disallowedBlock = mock(Block.class);
        Chunk disallowedChunk = TestChunkFactory.createChunk(5, 5, disallowedWorld);
        when(disallowedBlock.getChunk()).thenReturn(disallowedChunk);
        when(disallowedBlock.getWorld()).thenReturn(disallowedWorld);

        boolean claimedDisallowed = plotManager.claimPlot(player, disallowedBlock);
        assertFalse(claimedDisallowed, "Claiming in disallowed world must return false");
        verify(mockRepo, never()).save(any());

        // 2. Claim in allowed overworld -> succeeds
        Block overworldBlock = mock(Block.class);
        Chunk overworldChunk = TestChunkFactory.createChunk(10, 20, allowedWorld);
        when(overworldBlock.getChunk()).thenReturn(overworldChunk);
        when(overworldBlock.getWorld()).thenReturn(allowedWorld);

        boolean claimedOverworld = plotManager.claimPlot(player, overworldBlock);
        assertTrue(claimedOverworld, "Claiming in allowed world must succeed");

        // 3. Claim in nether -> succeeds
        Block netherBlock = mock(Block.class);
        Chunk netherChunk = TestChunkFactory.createChunk(10, 20, netherWorld);
        when(netherBlock.getChunk()).thenReturn(netherChunk);
        when(netherBlock.getWorld()).thenReturn(netherWorld);

        boolean claimedNether = plotManager.claimPlot(player, netherBlock);
        assertTrue(claimedNether, "Claiming in allowed nether world must succeed");

        // 4. Resolve plots by ID string
        Plot foundOverworld = plotManager.getPlot("10;20");
        assertNotNull(foundOverworld, "Should find overworld plot by 10;20");
        assertEquals("world", foundOverworld.getWorldName());

        Plot foundNether = plotManager.getPlot("10;20;nether");
        assertNotNull(foundNether, "Should find nether plot by 10;20;nether");
        assertEquals("world_nether", foundNether.getWorldName());

        // Lookup with full world name should also work
        Plot foundNetherByFullName = plotManager.getPlot("10;20;world_nether");
        assertNotNull(foundNetherByFullName, "Should find nether plot by 10;20;world_nether");
    }

    @Test
    @DisplayName("ChunkyPlotsAPI exposes world querying methods correctly")
    void testChunkyPlotsAPIWorldMethods() throws Exception {
        YamlConfiguration configYaml = createBaseConfig("""
                worlds:
                  world: ""
                  world_nether: "nether"
                  world_end: "end"
                """);
        YamlConfiguration messagesYaml = createBaseMessages();
        Config config = new Config(mockPlugin, configYaml, messagesYaml);

        PlotRepository mockRepo = mock(PlotRepository.class);
        PlotManager plotManager = new PlotManager(mockRepo, mock(ItemStack.class));
        plotManager.setConfig(config);

        ChunkyPlotsAPI api = new ChunkyPlotsAPIImpl(plotManager, null, null, null, null, null);

        assertTrue(api.isWorldAllowed("world"));
        assertTrue(api.isWorldAllowed("world_nether"));
        assertFalse(api.isWorldAllowed("unknown_world"));

        assertEquals("", api.getWorldAlias("world"));
        assertEquals("nether", api.getWorldAlias("world_nether"));
        assertEquals("end", api.getWorldAlias("world_end"));

        assertEquals("world", api.getWorldByAlias(""));
        assertEquals("world_nether", api.getWorldByAlias("nether"));
        assertEquals("world_end", api.getWorldByAlias("end"));

        assertEquals(3, api.getAllowedWorlds().size());
        assertTrue(api.getAllowedWorlds().contains("world"));
        assertTrue(api.getAllowedWorlds().contains("world_nether"));
        assertTrue(api.getAllowedWorlds().contains("world_end"));
    }
}
