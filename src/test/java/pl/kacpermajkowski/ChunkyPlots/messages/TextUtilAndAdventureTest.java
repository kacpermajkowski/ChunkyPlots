package pl.kacpermajkowski.ChunkyPlots.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TextUtilAndAdventureTest {

    @BeforeEach
    void setUp() {
        Config mockConfig = mock(Config.class);
        when(mockConfig.isUsingMessagePrefix()).thenReturn(true);
        when(mockConfig.getPrefix()).thenReturn("&8[&6ChunkyPlots&8]");
        when(mockConfig.getPrefixSpacer()).thenReturn(" ");
        when(mockConfig.getMessage(Message.PLOT_CREATED)).thenReturn("&aPlot &f{plotID}&a created!");
        MessageBuilder.init(mockConfig);
    }

    @Test
    @DisplayName("TextUtil parses legacy ampersand color codes into Component")
    void testLegacyAmpersandParsing() {
        Component comp = TextUtil.toComponent("&aHello &6World");
        assertNotNull(comp);
        String plain = PlainTextComponentSerializer.plainText().serialize(comp);
        assertEquals("Hello World", plain);
    }

    @Test
    @DisplayName("TextUtil parses MiniMessage tags into Component")
    void testMiniMessageParsing() {
        Component comp = TextUtil.toComponent("<green>Hello <gold>World</gold></green>");
        assertNotNull(comp);
        String plain = PlainTextComponentSerializer.plainText().serialize(comp);
        assertEquals("Hello World", plain);
    }

    @Test
    @DisplayName("TextUtil safely handles mixed MiniMessage and legacy codes")
    void testMixedSyntaxParsing() {
        Component comp = TextUtil.toComponent("&aPrefix <yellow>MiniMessage</yellow> &6Suffix");
        assertNotNull(comp);
        String plain = PlainTextComponentSerializer.plainText().serialize(comp);
        assertEquals("Prefix MiniMessage Suffix", plain);
    }

    @Test
    @DisplayName("TextUtil handles null and empty gracefully")
    void testNullAndEmpty() {
        Component empty1 = TextUtil.toComponent(null);
        Component empty2 = TextUtil.toComponent("");
        assertEquals(Component.empty(), empty1);
        assertEquals(Component.empty(), empty2);
    }

    @Test
    @DisplayName("MessageBuilder builds Kyori Adventure Component with placeholders")
    void testMessageBuilderComponent() {
        MessageBuilder builder = new MessageBuilder(Message.PLOT_CREATED)
                .plotID("plot-42");

        Component component = builder.buildComponent();
        assertNotNull(component);

        String plain = PlainTextComponentSerializer.plainText().serialize(component);
        assertTrue(plain.contains("[ChunkyPlots]"));
        assertTrue(plain.contains("plot-42"));
        assertTrue(plain.contains("created!"));
    }

    @Test
    @DisplayName("MessageBuilder sends Adventure Component to Player directly")
    void testSendChatPlayer() {
        Player player = mock(Player.class);
        MessageBuilder builder = new MessageBuilder(Message.PLOT_CREATED)
                .plotID("plot-99");

        builder.sendChat(player);

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(player).sendMessage(captor.capture());

        Component sent = captor.getValue();
        String plain = PlainTextComponentSerializer.plainText().serialize(sent);
        assertTrue(plain.contains("plot-99"));
    }

    @Test
    @DisplayName("TextUtil sends action bar directly using Adventure API")
    void testSendActionBar() {
        Player player = mock(Player.class);
        TextUtil.sendActionBarMessage(player, "&aAction bar message");

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(player).sendActionBar(captor.capture());

        String plain = PlainTextComponentSerializer.plainText().serialize(captor.getValue());
        assertEquals("Action bar message", plain);
    }
}
