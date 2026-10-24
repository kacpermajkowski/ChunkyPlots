package pl.kacpermajkowski.ChunkyPlots.commands;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.dispose.PlotDisposeCommand;
import pl.kacpermajkowski.ChunkyPlots.events.PlotDisposeEvent;
import pl.kacpermajkowski.ChunkyPlots.listeners.PlotLifecycleListener;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotRepository;
import pl.kacpermajkowski.ChunkyPlots.user.User;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;
import pl.kacpermajkowski.ChunkyPlots.user.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class PlotDisposeCacheTest {

    private Server mockServer;

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
    }

    @AfterEach
    void tearDown() {
        BukkitTestHelper.tearDownMockServer();
    }

    @Test
    @DisplayName("Fix Verification: PlotDisposeCommand clears cachedCurrentPlot via PlotDisposeEvent")
    void testDisposalClearsStaleCacheAcrossAllUsers() {
        UUID ownerId = UUID.randomUUID();
        UUID bystanderId = UUID.randomUUID();

        World world = Mockito.mock(World.class);
        when(world.getName()).thenReturn("world");
        when(mockServer.getWorld("world")).thenReturn(world);
        when(mockServer.getOnlinePlayers()).thenReturn(Collections.emptyList());

        Plot plot = new Plot(ownerId, 10, 10, "world", UUID.randomUUID());

        PlotRepository mockPlotRepo = Mockito.mock(PlotRepository.class);
        when(mockPlotRepo.loadAll()).thenReturn(List.of(plot));
        PlotManager plotManager = new PlotManager(mockPlotRepo, Mockito.mock(ItemStack.class));

        OfflinePlayer ownerOffline = Mockito.mock(OfflinePlayer.class);
        when(ownerOffline.getUniqueId()).thenReturn(ownerId);
        when(ownerOffline.getName()).thenReturn("Owner");

        OfflinePlayer bystanderOffline = Mockito.mock(OfflinePlayer.class);
        when(bystanderOffline.getUniqueId()).thenReturn(bystanderId);
        when(bystanderOffline.getName()).thenReturn("Bystander");

        User ownerUser = new User(ownerOffline);
        User bystanderUser = new User(bystanderOffline);

        // Both users have this plot cached in their session
        ownerUser.setCachedCurrentPlot(plot);
        bystanderUser.setCachedCurrentPlot(plot);

        UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
        when(mockUserRepo.loadAll()).thenReturn(Set.of(ownerUser, bystanderUser));
        UserManager userManager = new UserManager(mockUserRepo);

        // Wire reactive PlotLifecycleListener to callEvent
        PlotLifecycleListener lifecycleListener = new PlotLifecycleListener(userManager);
        PluginManager pm = mockServer.getPluginManager();
        doAnswer(inv -> {
            Object event = inv.getArgument(0);
            if (event instanceof PlotDisposeEvent disposeEvent) {
                lifecycleListener.onPlotDispose(disposeEvent);
            }
            return null;
        }).when(pm).callEvent(any());

        Player ownerPlayer = Mockito.mock(Player.class);
        when(ownerPlayer.getUniqueId()).thenReturn(ownerId);
        when(ownerPlayer.getLocation()).thenReturn(new Location(world, 160, 64, 160));
        when(ownerPlayer.getInventory()).thenReturn(Mockito.mock(PlayerInventory.class));

        PlotDisposeCommand disposeCommand = new PlotDisposeCommand(plotManager, userManager);
        disposeCommand.execute(ownerPlayer, new String[]{});

        // Verification: Both owner and bystander cached plot MUST be invalidated via PlotDisposeEvent
        assertNull(ownerUser.getCachedCurrentPlot(), "Owner cached plot should be cleared upon disposal");
        assertNull(bystanderUser.getCachedCurrentPlot(), "Bystander cached plot must be cleared upon disposal, preventing stale reference leak");
    }
}
