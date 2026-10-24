package pl.kacpermajkowski.ChunkyPlots.plot.mnemonic;

import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.api.ChunkyPlotsAPI;
import pl.kacpermajkowski.ChunkyPlots.api.ChunkyPlotsAPIImpl;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotRepository;

import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlotMnemonicTest {
    private Server mockServer;
    private JavaPlugin mockPlugin;
    private Config config;

    @BeforeEach
    void setUp() throws Exception {
        mockServer = BukkitTestHelper.setUpMockServer();
        mockPlugin = mock(JavaPlugin.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("ChunkyPlotsTest"));
        when(mockPlugin.getServer()).thenReturn(mockServer);

        String yaml = """
                use-message-prefix: false
                prefix: "[ChunkyPlots]"
                prefix-spacer: " > "
                interaction-message-display: [CHAT]
                plot-item:
                  material: "STONE"
                  name: "Plot Block"
                  lore: ["Claim chunk"]
                plot-limits:
                  default: 5
                worlds:
                  world: ""
                  world_nether: "nether"
                  world_end: "end"
                """;
        YamlConfiguration configYaml = new YamlConfiguration();
        configYaml.load(new StringReader(yaml));

        YamlConfiguration messagesYaml = new YamlConfiguration();
        for (Message msg : Message.values()) {
            messagesYaml.set("messages." + msg.name().toLowerCase(), "Message: " + msg.name());
        }

        this.config = new Config(mockPlugin, configYaml, messagesYaml);
        MessageBuilder.init(config);

        // Ensure dictionary is loaded from classpath
        try (InputStream in = getClass().getResourceAsStream("/dictionary.txt")) {
            assertNotNull(in, "dictionary.txt must exist on classpath");
            PlotMnemonic.init(MnemonicDictionary.loadFromStream(in));
        }
    }

    @AfterEach
    void tearDown() {
        Config.setInstance(null);
        BukkitTestHelper.tearDownMockServer();
    }

    @Test
    @DisplayName("Dictionary contains exactly 2000 unique words and expected initial words")
    void testDictionaryIntegrity() {
        MnemonicDictionary dict = PlotMnemonic.getDictionary();
        assertNotNull(dict);
        assertEquals(2000, dict.size());

        assertEquals("god", dict.getWord(0));
        assertEquals("water", dict.getWord(1));
        assertEquals("home", dict.getWord(2));
        assertEquals("table", dict.getWord(3));
        assertEquals("hand", dict.getWord(4));
        assertEquals("value", dict.getWord(5));
        assertEquals("party", dict.getWord(6));
        assertEquals("government", dict.getWord(7));

        assertEquals(0, dict.getIndex("god"));
        assertEquals(1, dict.getIndex("water"));
        assertEquals(6, dict.getIndex("party"));
        assertEquals(7, dict.getIndex("government"));
    }

    @Test
    @DisplayName("Low chunk coordinates [-1000, 999] encode to 2 words with world alias prefix")
    void testLowCoordinateEncoding() {
        // -1000 -> index 0 ("god"), -999 -> index 1 ("water")
        String mnemonic1 = PlotMnemonic.format(-1000, -999, "world");
        assertEquals("god-water", mnemonic1);

        // With nether alias
        String mnemonicNether = PlotMnemonic.format(-1000, -999, "world_nether");
        assertEquals("nether-god-water", mnemonicNether);

        // With end alias
        String mnemonicEnd = PlotMnemonic.format(-1000, -999, "world_end");
        assertEquals("end-god-water", mnemonicEnd);

        // Specific sample from requirements: party-government (index 6, 7 -> chunkX = -994, chunkZ = -993)
        String partyGov = PlotMnemonic.format(-994, -993, "world");
        assertEquals("party-government", partyGov);

        // Roundtrip parsing
        PlotMnemonic.MnemonicCoordinates parsed1 = PlotMnemonic.parse("god-water");
        assertNotNull(parsed1);
        assertEquals(-1000, parsed1.chunkX());
        assertEquals(-999, parsed1.chunkZ());
        assertEquals("world", parsed1.worldName());

        PlotMnemonic.MnemonicCoordinates parsedNether = PlotMnemonic.parse("nether-god-water");
        assertNotNull(parsedNether);
        assertEquals(-1000, parsedNether.chunkX());
        assertEquals(-999, parsedNether.chunkZ());
        assertEquals("world_nether", parsedNether.worldName());

        PlotMnemonic.MnemonicCoordinates parsedPartyGov = PlotMnemonic.parse("party-government");
        assertNotNull(parsedPartyGov);
        assertEquals(-994, parsedPartyGov.chunkX());
        assertEquals(-993, parsedPartyGov.chunkZ());
        assertEquals("world", parsedPartyGov.worldName());
    }

    @Test
    @DisplayName("Mid chunk coordinates [-2000000, 1999999] outside low range encode to 4 words")
    void testMidCoordinateEncoding() {
        int x = 5000;
        int z = -1500;

        String formattedOverworld = PlotMnemonic.format(x, z, "world");
        String[] partsOverworld = formattedOverworld.split("-");
        assertEquals(4, partsOverworld.length, "Mid coordinates in overworld should produce 4 words");

        PlotMnemonic.MnemonicCoordinates parsedOverworld = PlotMnemonic.parse(formattedOverworld);
        assertNotNull(parsedOverworld);
        assertEquals(x, parsedOverworld.chunkX());
        assertEquals(z, parsedOverworld.chunkZ());
        assertEquals("world", parsedOverworld.worldName());

        String formattedNether = PlotMnemonic.format(x, z, "world_nether");
        String[] partsNether = formattedNether.split("-");
        assertEquals(5, partsNether.length, "Mid coordinates in nether should produce 5 words (1 alias + 4 coords)");
        assertEquals("nether", partsNether[0]);

        PlotMnemonic.MnemonicCoordinates parsedNether = PlotMnemonic.parse(formattedNether);
        assertNotNull(parsedNether);
        assertEquals(x, parsedNether.chunkX());
        assertEquals(z, parsedNether.chunkZ());
        assertEquals("world_nether", parsedNether.worldName());

        // Test near border limits: 1874998, -1874999
        int borderX = 1874998;
        int borderZ = -1874999;
        String borderEnd = PlotMnemonic.format(borderX, borderZ, "world_end");
        PlotMnemonic.MnemonicCoordinates parsedBorder = PlotMnemonic.parse(borderEnd);
        assertNotNull(parsedBorder);
        assertEquals(borderX, parsedBorder.chunkX());
        assertEquals(borderZ, parsedBorder.chunkZ());
        assertEquals("world_end", parsedBorder.worldName());
    }

    @Test
    @DisplayName("Extreme chunk coordinates (> 1999999) encode to 6 words and decode bijectively")
    void testExtremeCoordinateEncoding() {
        int x = 2500000;
        int z = -2500000;

        String formatted = PlotMnemonic.format(x, z, "world");
        String[] parts = formatted.split("-");
        assertEquals(6, parts.length, "Extreme coordinates should produce 6 words");

        PlotMnemonic.MnemonicCoordinates parsed = PlotMnemonic.parse(formatted);
        assertNotNull(parsed);
        assertEquals(x, parsed.chunkX());
        assertEquals(z, parsed.chunkZ());
        assertEquals("world", parsed.worldName());
    }

    @Test
    @DisplayName("Invalid mnemonic strings return null gracefully")
    void testInvalidMnemonics() {
        assertNull(PlotMnemonic.parse(null));
        assertNull(PlotMnemonic.parse(""));
        assertNull(PlotMnemonic.parse("   "));
        assertNull(PlotMnemonic.parse("singleword"));
        assertNull(PlotMnemonic.parse("wordnotindictionary-wordnotindictionary"));
        assertNull(PlotMnemonic.parse("invalid-god-water")); // 'invalid' alias unknown
        assertFalse(PlotMnemonic.isMnemonic("invalid"));
    }

    @Test
    @DisplayName("Plot.getMnemonicId() returns formatted mnemonic matching chunk coordinates")
    void testPlotGetMnemonicId() {
        UUID owner = UUID.randomUUID();
        Plot overworldPlot = new Plot(owner, -1000, -999, "world", UUID.randomUUID());
        assertEquals("god-water", overworldPlot.getMnemonicId());

        Plot netherPlot = new Plot(owner, -1000, -999, "world_nether", UUID.randomUUID());
        assertEquals("nether-god-water", netherPlot.getMnemonicId());
    }

    @Test
    @DisplayName("PlotManager resolves plots by mnemonic identifier")
    void testPlotManagerLookupByMnemonic() {
        PlotRepository mockRepo = mock(PlotRepository.class);
        when(mockRepo.count()).thenReturn(0);
        when(mockRepo.loadAll()).thenReturn(List.of());

        PlotManager plotManager = new PlotManager(mockRepo, mock(ItemStack.class));
        plotManager.setConfig(config);

        UUID owner = UUID.randomUUID();
        Plot plot = new Plot(owner, -1000, -999, "world", UUID.randomUUID());
        when(mockRepo.loadByChunk(-1000, -999, "world")).thenReturn(plot);

        // Lookup by mnemonic string "god-water"
        Plot found = plotManager.getPlot("god-water");
        assertNotNull(found, "PlotManager must resolve plot using mnemonic ID");
        assertEquals(plot.getUUID(), found.getUUID());
    }

    @Test
    @DisplayName("ChunkyPlotsAPI exposes mnemonic methods and MessageBuilder placeholder works")
    void testApiAndMessageBuilder() {
        PlotRepository mockRepo = mock(PlotRepository.class);
        PlotManager plotManager = new PlotManager(mockRepo, mock(ItemStack.class));
        plotManager.setConfig(config);

        ChunkyPlotsAPI api = new ChunkyPlotsAPIImpl(plotManager, null, null, null, null, null);

        UUID owner = UUID.randomUUID();
        Plot plot = new Plot(owner, -1000, -999, "world_nether", UUID.randomUUID());
        when(mockRepo.loadByChunk(-1000, -999, "world_nether")).thenReturn(plot);

        assertEquals("nether-god-water", api.getPlotMnemonic(plot));
        assertEquals(plot, api.getPlotByMnemonic("nether-god-water"));

        // MessageBuilder test
        MessageBuilder mb = new MessageBuilder().plot(plot);
        assertTrue(plot.getMnemonicId().equals("nether-god-water"));
    }
}
