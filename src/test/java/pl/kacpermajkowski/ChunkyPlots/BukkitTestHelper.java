package pl.kacpermajkowski.ChunkyPlots;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import static org.mockito.Mockito.when;

public class BukkitTestHelper {

    public static Server setUpMockServer() {
        Server mockServer = Mockito.mock(Server.class);
        when(mockServer.getLogger()).thenReturn(Logger.getLogger("Minecraft"));

        PluginManager mockPluginManager = Mockito.mock(PluginManager.class);
        when(mockServer.getPluginManager()).thenReturn(mockPluginManager);

        try {
            Field serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(null, mockServer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set mock server on Bukkit", e);
        }
        return mockServer;
    }

    public static void tearDownMockServer() {
        try {
            Field serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(null, null);
        } catch (Exception ignored) {
        }
    }
}
