package pl.kacpermajkowski.ChunkyPlots.protections;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.TestChunkFactory;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotRepository;
import pl.kacpermajkowski.ChunkyPlots.protections.block.BlockBreakProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.block.BlockPlaceListener;
import pl.kacpermajkowski.ChunkyPlots.protections.block.IgniteProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.block.LecternBookProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.*;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerBucketEmptyListener;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerInteractListener;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProtectionNotificationTest {
    private Server mockServer;
    private World mockWorld;
    private PlotManager plotManager;
    private PlotPermissionChecker checker;

    private Plot plot;
    private UUID ownerUuid;
    private UUID intruderUuid;
    private Player owner;
    private Player intruder;

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
        mockWorld = Mockito.mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        when(mockServer.getWorld("world")).thenReturn(mockWorld);

        when(mockWorld.getChunkAt(anyInt(), anyInt())).thenAnswer(inv ->
                TestChunkFactory.createChunk(inv.getArgument(0), inv.getArgument(1), mockWorld));
        when(mockWorld.getChunkAt(any(Location.class))).thenAnswer(inv -> {
            Location loc = inv.getArgument(0);
            return TestChunkFactory.createChunk(loc.getBlockX() >> 4, loc.getBlockZ() >> 4, mockWorld);
        });

        ownerUuid = UUID.randomUUID();
        intruderUuid = UUID.randomUUID();

        owner = Mockito.mock(Player.class);
        when(owner.getUniqueId()).thenReturn(ownerUuid);
        when(owner.getName()).thenReturn("PlotOwner");

        intruder = Mockito.mock(Player.class);
        when(intruder.getUniqueId()).thenReturn(intruderUuid);
        when(intruder.getName()).thenReturn("Intruder");

        // Chunk (0, 0)
        plot = new Plot(ownerUuid, 0, 0, "world", UUID.randomUUID());

        PlotRepository mockRepo = Mockito.mock(PlotRepository.class);
        when(mockRepo.loadAll()).thenReturn(List.of(plot));
        plotManager = new PlotManager(mockRepo, Mockito.mock(ItemStack.class));

        UserManager mockUserManager = Mockito.mock(UserManager.class);
        checker = new PlotPermissionChecker(plotManager, mockUserManager);

        ProtectionNotifier.clearAll();
        ProtectionNotifier.setCooldownMillis(0); // Cooldown disabled for unit testing specific checks
    }

    @AfterEach
    void tearDown() {
        BukkitTestHelper.tearDownMockServer();
        ProtectionNotifier.clearAll();
        ProtectionNotifier.resetCooldown();
    }

    private Block mockPlotBlock() {
        Block block = Mockito.mock(Block.class);
        when(block.getX()).thenReturn(5);
        when(block.getZ()).thenReturn(5);
        when(block.getWorld()).thenReturn(mockWorld);
        Chunk chunk = TestChunkFactory.createChunk(0, 0, mockWorld);
        when(block.getChunk()).thenReturn(chunk);
        return block;
    }

    @Test
    @DisplayName("BlockBreakProtection sends PROTECTION_BLOCK_BREAK when unauthorized player breaks block")
    void testBlockBreakNotification() {
        BlockBreakProtection listener = new BlockBreakProtection(checker);
        Block block = mockPlotBlock();

        // 1. Intruder breaks block -> cancelled and notified
        BlockBreakEvent eventIntruder = new BlockBreakEvent(block, intruder);
        listener.onBlockBreak(eventIntruder);
        assertTrue(eventIntruder.isCancelled());
        assertEquals(Message.PROTECTION_BLOCK_BREAK, ProtectionNotifier.getLastSentMessage());
        assertEquals(intruderUuid, ProtectionNotifier.getLastNotifiedPlayer());

        // 2. Owner breaks block -> allowed, no notification
        ProtectionNotifier.clearAll();
        BlockBreakEvent eventOwner = new BlockBreakEvent(block, owner);
        listener.onBlockBreak(eventOwner);
        assertFalse(eventOwner.isCancelled());
        assertNull(ProtectionNotifier.getLastSentMessage());
    }

    @Test
    @DisplayName("BlockPlaceListener sends PROTECTION_BLOCK_PLACE when unauthorized player places block")
    void testBlockPlaceNotification() {
        BlockPlaceListener listener = new BlockPlaceListener(checker);
        Block block = mockPlotBlock();

        BlockPlaceEvent event = Mockito.mock(BlockPlaceEvent.class);
        when(event.getPlayer()).thenReturn(intruder);
        when(event.getBlockPlaced()).thenReturn(block);

        listener.onBlockPlace(event);
        verify(event).setCancelled(true);
        assertEquals(Message.PROTECTION_BLOCK_PLACE, ProtectionNotifier.getLastSentMessage());
        assertEquals(intruderUuid, ProtectionNotifier.getLastNotifiedPlayer());
    }

    @Test
    @DisplayName("PlayerInteractListener sends PROTECTION_BLOCK_INTERACT and PROTECTION_CROP_TRAMPLE")
    void testPlayerInteractNotification() {
        PlayerInteractListener listener = new PlayerInteractListener(checker, null);
        Block block = mockPlotBlock();

        // 1. Right click block -> interact notification
        PlayerInteractEvent clickEvent = Mockito.mock(PlayerInteractEvent.class);
        when(clickEvent.getAction()).thenReturn(Action.RIGHT_CLICK_BLOCK);
        when(clickEvent.getClickedBlock()).thenReturn(block);
        when(clickEvent.getPlayer()).thenReturn(intruder);

        listener.onPlayerInteract(clickEvent);
        verify(clickEvent).setCancelled(true);
        assertEquals(Message.PROTECTION_BLOCK_INTERACT, ProtectionNotifier.getLastSentMessage());

        // 2. Physical trample on Farmland -> crop trample notification
        ProtectionNotifier.clearAll();
        when(block.getType()).thenReturn(Material.FARMLAND);
        PlayerInteractEvent physicalEvent = Mockito.mock(PlayerInteractEvent.class);
        when(physicalEvent.getAction()).thenReturn(Action.PHYSICAL);
        when(physicalEvent.hasBlock()).thenReturn(true);
        when(physicalEvent.getClickedBlock()).thenReturn(block);
        when(physicalEvent.getPlayer()).thenReturn(intruder);

        listener.onPlayerInteract(physicalEvent);
        verify(physicalEvent).setCancelled(true);
        assertEquals(Message.PROTECTION_CROP_TRAMPLE, ProtectionNotifier.getLastSentMessage());
    }

    @Test
    @DisplayName("PlayerBucketEmptyListener sends PROTECTION_BUCKET_EMPTY")
    void testBucketEmptyNotification() {
        PlayerBucketEmptyListener listener = new PlayerBucketEmptyListener(checker);
        Block block = mockPlotBlock();

        PlayerBucketEmptyEvent event = Mockito.mock(PlayerBucketEmptyEvent.class);
        when(event.getPlayer()).thenReturn(intruder);
        when(event.getBlockClicked()).thenReturn(block);

        listener.onPlayerBucketEmpty(event);
        verify(event).setCancelled(true);
        assertEquals(Message.PROTECTION_BUCKET_EMPTY, ProtectionNotifier.getLastSentMessage());
    }

    @Test
    @DisplayName("EntityDamageProtection sends PROTECTION_ENTITY_DAMAGE")
    void testEntityDamageNotification() {
        EntityDamageProtection listener = new EntityDamageProtection(checker);
        Sheep animal = Mockito.mock(Sheep.class);
        when(animal.getLocation()).thenReturn(new Location(mockWorld, 5, 64, 5));

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(intruder);
        when(event.getEntity()).thenReturn(animal);

        listener.onEntityDamageByEntity(event);
        verify(event).setCancelled(true);
        assertEquals(Message.PROTECTION_ENTITY_DAMAGE, ProtectionNotifier.getLastSentMessage());
    }

    @Test
    @DisplayName("EntityInteractionProtection sends PROTECTION_ENTITY_INTERACT")
    void testEntityInteractionNotification() {
        EntityInteractionProtection listener = new EntityInteractionProtection(checker);
        Sheep animal = Mockito.mock(Sheep.class);
        when(animal.getLocation()).thenReturn(new Location(mockWorld, 5, 64, 5));

        PlayerInteractEntityEvent event = Mockito.mock(PlayerInteractEntityEvent.class);
        when(event.getPlayer()).thenReturn(intruder);
        when(event.getRightClicked()).thenReturn(animal);

        listener.onPlayerInteractAtEntity(event);
        verify(event).setCancelled(true);
        assertEquals(Message.PROTECTION_ENTITY_INTERACT, ProtectionNotifier.getLastSentMessage());
    }

    @Test
    @DisplayName("ItemFrameProtection sends PROTECTION_HANGING_BREAK")
    void testItemFrameNotification() {
        ItemFrameProtection listener = new ItemFrameProtection(checker);
        ItemFrame itemFrame = Mockito.mock(ItemFrame.class);
        when(itemFrame.getLocation()).thenReturn(new Location(mockWorld, 5, 64, 5));

        HangingBreakByEntityEvent event = Mockito.mock(HangingBreakByEntityEvent.class);
        when(event.getEntity()).thenReturn(itemFrame);
        when(event.getRemover()).thenReturn(intruder);

        listener.onItemFrameProtection(event);
        verify(event).setCancelled(true);
        assertEquals(Message.PROTECTION_HANGING_BREAK, ProtectionNotifier.getLastSentMessage());
    }

    @Test
    @DisplayName("VehicleEnterListener and VehicleDamageListener send specific notifications")
    void testVehicleNotifications() {
        VehicleEnterListener enterListener = new VehicleEnterListener(checker);
        VehicleDamageListener damageListener = new VehicleDamageListener(checker);

        Vehicle vehicle = Mockito.mock(Vehicle.class);
        when(vehicle.getLocation()).thenReturn(new Location(mockWorld, 5, 64, 5));

        // Enter
        VehicleEnterEvent enterEvent = Mockito.mock(VehicleEnterEvent.class);
        when(enterEvent.getEntered()).thenReturn(intruder);
        when(enterEvent.getVehicle()).thenReturn(vehicle);

        enterListener.onVehicleEnter(enterEvent);
        verify(enterEvent).setCancelled(true);
        assertEquals(Message.PROTECTION_VEHICLE_ENTER, ProtectionNotifier.getLastSentMessage());

        // Damage
        ProtectionNotifier.clearAll();
        VehicleDamageEvent damageEvent = Mockito.mock(VehicleDamageEvent.class);
        when(damageEvent.getAttacker()).thenReturn(intruder);
        when(damageEvent.getVehicle()).thenReturn(vehicle);

        damageListener.onVehicleDamage(damageEvent);
        verify(damageEvent).setCancelled(true);
        assertEquals(Message.PROTECTION_VEHICLE_DAMAGE, ProtectionNotifier.getLastSentMessage());
    }

    @Test
    @DisplayName("PlayerShearEntityProtection and PlayerLeashEntityProtection send specific notifications")
    void testShearAndLeashNotifications() {
        PlayerShearEntityProtection shearListener = new PlayerShearEntityProtection(checker);
        PlayerLeashEntityProtection leashListener = new PlayerLeashEntityProtection(checker);

        Entity entity = Mockito.mock(Entity.class);
        when(entity.getLocation()).thenReturn(new Location(mockWorld, 5, 64, 5));

        // Shear
        PlayerShearEntityEvent shearEvent = Mockito.mock(PlayerShearEntityEvent.class);
        when(shearEvent.getPlayer()).thenReturn(intruder);
        when(shearEvent.getEntity()).thenReturn(entity);

        shearListener.onPlayerShearEntity(shearEvent);
        verify(shearEvent).setCancelled(true);
        assertEquals(Message.PROTECTION_ENTITY_SHEAR, ProtectionNotifier.getLastSentMessage());

        // Leash
        ProtectionNotifier.clearAll();
        PlayerLeashEntityEvent leashEvent = Mockito.mock(PlayerLeashEntityEvent.class);
        when(leashEvent.getPlayer()).thenReturn(intruder);
        when(leashEvent.getEntity()).thenReturn(entity);

        leashListener.onPlayerLeashEntity(leashEvent);
        verify(leashEvent).setCancelled(true);
        assertEquals(Message.PROTECTION_ENTITY_LEASH, ProtectionNotifier.getLastSentMessage());
    }

    @Test
    @DisplayName("LecternBookProtection and IgniteProtection send specific notifications")
    void testLecternAndIgniteNotifications() {
        LecternBookProtection lecternListener = new LecternBookProtection(checker);
        IgniteProtection igniteListener = new IgniteProtection(checker);

        // Lectern
        Block block = mockPlotBlock();
        org.bukkit.block.Lectern lectern = Mockito.mock(org.bukkit.block.Lectern.class);
        when(lectern.getLocation()).thenReturn(new Location(mockWorld, 5, 64, 5));

        PlayerTakeLecternBookEvent lecternEvent = Mockito.mock(PlayerTakeLecternBookEvent.class);
        when(lecternEvent.getPlayer()).thenReturn(intruder);
        when(lecternEvent.getLectern()).thenReturn(lectern);

        lecternListener.onPlayerTakeLecternBook(lecternEvent);
        verify(lecternEvent).setCancelled(true);
        assertEquals(Message.PROTECTION_LECTERN, ProtectionNotifier.getLastSentMessage());

        // Ignite
        ProtectionNotifier.clearAll();
        BlockIgniteEvent igniteEvent = Mockito.mock(BlockIgniteEvent.class);
        when(igniteEvent.getBlock()).thenReturn(block);
        when(igniteEvent.getIgnitingEntity()).thenReturn(intruder);

        igniteListener.onBlockIgnite(igniteEvent);
        verify(igniteEvent).setCancelled(true);
        assertEquals(Message.PROTECTION_IGNITE, ProtectionNotifier.getLastSentMessage());
    }

    @Test
    @DisplayName("ProtectionNotifier debounce prevents message spam within cooldown window")
    void testProtectionNotifierDebounce() {
        ProtectionNotifier.setCooldownMillis(1000);

        // 1st notification -> succeeds
        boolean sent1 = ProtectionNotifier.notify(intruder, Message.PROTECTION_BLOCK_BREAK, plot);
        assertTrue(sent1, "First notification should be sent");

        // 2nd notification immediately after -> suppressed by debounce
        boolean sent2 = ProtectionNotifier.notify(intruder, Message.PROTECTION_BLOCK_BREAK, plot);
        assertFalse(sent2, "Immediate subsequent notification should be debounced");

        // After clearing or cooldown -> succeeds again
        ProtectionNotifier.clear(intruderUuid);
        boolean sent3 = ProtectionNotifier.notify(intruder, Message.PROTECTION_BLOCK_BREAK, plot);
        assertTrue(sent3, "Notification after cooldown clear should be sent");
    }
}
