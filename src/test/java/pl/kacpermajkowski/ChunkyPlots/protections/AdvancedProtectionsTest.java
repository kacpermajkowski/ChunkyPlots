package pl.kacpermajkowski.ChunkyPlots.protections;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.block.explosion.TNTProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.ItemFrameProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerEntryProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerInteractListener;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdvancedProtectionsTest {

    private PlotManager plotManager;
    private UserManager userManager;
    private PlotPermissionChecker checker;
    private World mockWorld;
    private Plot testPlot;
    private UUID ownerUuid;
    private UUID strangerUuid;

    @BeforeEach
    void setUp() {
        plotManager = mock(PlotManager.class);
        userManager = mock(UserManager.class);
        checker = new PlotPermissionChecker(plotManager, userManager);

        mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");

        ownerUuid = UUID.randomUUID();
        strangerUuid = UUID.randomUUID();

        testPlot = new Plot(ownerUuid, 5, 5, "world", UUID.randomUUID());
    }

    @Test
    @DisplayName("PlayerEntryProtection safely handles cross-world movement without throwing distance exception")
    void testCrossWorldPlayerMove() {
        PlayerEntryProtection listener = new PlayerEntryProtection(checker);

        World world1 = mock(World.class);
        when(world1.getName()).thenReturn("world");
        World world2 = mock(World.class);
        when(world2.getName()).thenReturn("world_nether");

        Location from = new Location(world1, 10, 64, 10);
        Location to = new Location(world2, 10, 64, 10);

        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getUniqueId()).thenReturn(strangerUuid);

        PlayerMoveEvent event = new PlayerMoveEvent(mockPlayer, from, to);

        assertDoesNotThrow(() -> listener.onPlayerMove(event));
    }

    @Test
    @DisplayName("PlayerInteractListener cancels farmland trampling and redstone ore for strangers")
    void testFarmlandTramplingCancelled() {
        Plugin mockPlugin = mock(Plugin.class);
        when(mockPlugin.isEnabled()).thenReturn(false);
        PlayerInteractListener listener = new PlayerInteractListener(checker, mockPlugin);

        Player stranger = mock(Player.class);
        when(stranger.getUniqueId()).thenReturn(strangerUuid);

        Block farmlandBlock = mock(Block.class);
        when(farmlandBlock.getType()).thenReturn(Material.FARMLAND);
        when(plotManager.getPlot(farmlandBlock)).thenReturn(testPlot);

        PlayerInteractEvent farmlandEvent = new PlayerInteractEvent(stranger, Action.PHYSICAL, null, farmlandBlock, null);
        listener.onPlayerInteract(farmlandEvent);
        assertTrue(farmlandEvent.isCancelled(), "Farmland trampling must be cancelled for unauthorized players!");

        Block deepslateOre = mock(Block.class);
        when(deepslateOre.getType()).thenReturn(Material.DEEPSLATE_REDSTONE_ORE);
        when(deepslateOre.getLocation()).thenReturn(new Location(mockWorld, 80, 64, 80));
        when(deepslateOre.getBlockData()).thenReturn(mock(BlockData.class));
        when(plotManager.getPlot(deepslateOre)).thenReturn(testPlot);

        PlayerInteractEvent oreEvent = new PlayerInteractEvent(stranger, Action.PHYSICAL, null, deepslateOre, null);
        listener.onPlayerInteract(oreEvent);
        assertTrue(oreEvent.isCancelled(), "Deepslate redstone ore stepping must be cancelled for unauthorized players!");
    }

    @Test
    @DisplayName("TNTProtection cancels TNTPrimeEvent if primed by stranger or unauthorized block")
    void testTNTPrimeCancellation() {
        Plugin mockPlugin = mock(Plugin.class);
        TNTProtection tntProtection = new TNTProtection(checker, mockPlugin);

        Block tntBlock = mock(Block.class);
        when(plotManager.getPlot(tntBlock)).thenReturn(testPlot);

        Player stranger = mock(Player.class);
        when(stranger.getUniqueId()).thenReturn(strangerUuid);

        TNTPrimeEvent playerPrimeEvent = mock(TNTPrimeEvent.class);
        when(playerPrimeEvent.getBlock()).thenReturn(tntBlock);
        when(playerPrimeEvent.getPrimingEntity()).thenReturn(stranger);
        AtomicBoolean cancelledStranger = new AtomicBoolean(false);
        doAnswer(inv -> { cancelledStranger.set(inv.getArgument(0)); return null; }).when(playerPrimeEvent).setCancelled(anyBoolean());
        when(playerPrimeEvent.isCancelled()).thenAnswer(inv -> cancelledStranger.get());

        tntProtection.onTntPrimed(playerPrimeEvent);
        assertTrue(playerPrimeEvent.isCancelled(), "TNT primed by stranger must be cancelled!");

        Player owner = mock(Player.class);
        when(owner.getUniqueId()).thenReturn(ownerUuid);

        TNTPrimeEvent ownerPrimeEvent = mock(TNTPrimeEvent.class);
        when(ownerPrimeEvent.getBlock()).thenReturn(tntBlock);
        when(ownerPrimeEvent.getPrimingEntity()).thenReturn(owner);
        AtomicBoolean cancelledOwner = new AtomicBoolean(false);
        doAnswer(inv -> { cancelledOwner.set(inv.getArgument(0)); return null; }).when(ownerPrimeEvent).setCancelled(anyBoolean());
        when(ownerPrimeEvent.isCancelled()).thenAnswer(inv -> cancelledOwner.get());

        tntProtection.onTntPrimed(ownerPrimeEvent);
        assertFalse(ownerPrimeEvent.isCancelled(), "TNT primed by plot owner must be permitted!");
    }

    @Test
    @DisplayName("ItemFrameProtection permits projectile shot by plot owner and blocks stranger projectile")
    void testItemFrameProtectionShooter() {
        ItemFrameProtection listener = new ItemFrameProtection(checker);

        ItemFrame itemFrame = mock(ItemFrame.class);
        when(plotManager.getPlot(itemFrame)).thenReturn(testPlot);

        Player owner = mock(Player.class);
        when(owner.getUniqueId()).thenReturn(ownerUuid);
        Arrow ownerArrow = mock(Arrow.class);
        when(ownerArrow.getShooter()).thenReturn(owner);

        HangingBreakByEntityEvent ownerBreak = mock(HangingBreakByEntityEvent.class);
        when(ownerBreak.getEntity()).thenReturn(itemFrame);
        when(ownerBreak.getRemover()).thenReturn(ownerArrow);
        AtomicBoolean cancelledOwner = new AtomicBoolean(false);
        doAnswer(inv -> { cancelledOwner.set(inv.getArgument(0)); return null; }).when(ownerBreak).setCancelled(anyBoolean());
        when(ownerBreak.isCancelled()).thenAnswer(inv -> cancelledOwner.get());

        listener.onItemFrameProtection(ownerBreak);
        assertFalse(ownerBreak.isCancelled(), "Plot owner shooting arrow at item frame must be permitted!");

        Player stranger = mock(Player.class);
        when(stranger.getUniqueId()).thenReturn(strangerUuid);
        Arrow strangerArrow = mock(Arrow.class);
        when(strangerArrow.getShooter()).thenReturn(stranger);

        HangingBreakByEntityEvent strangerBreak = mock(HangingBreakByEntityEvent.class);
        when(strangerBreak.getEntity()).thenReturn(itemFrame);
        when(strangerBreak.getRemover()).thenReturn(strangerArrow);
        AtomicBoolean cancelledStranger = new AtomicBoolean(false);
        doAnswer(inv -> { cancelledStranger.set(inv.getArgument(0)); return null; }).when(strangerBreak).setCancelled(anyBoolean());
        when(strangerBreak.isCancelled()).thenAnswer(inv -> cancelledStranger.get());

        listener.onItemFrameProtection(strangerBreak);
        assertTrue(strangerBreak.isCancelled(), "Stranger shooting arrow at item frame must be cancelled!");
    }
}
