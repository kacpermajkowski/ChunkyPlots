package pl.kacpermajkowski.ChunkyPlots.protections;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.TestChunkFactory;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotRepository;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.EntityKnockbackProtection;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class EntityKnockbackProtectionTest {
    private Server mockServer;
    private World mockWorld;
    private PlotManager plotManager;
    private PlotPermissionChecker checker;

    private Plot plot;
    private UUID ownerUuid;
    private UUID intruderUuid;
    private Player owner;
    private Player intruder;
    private LivingEntity targetEntity;

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
        mockWorld = mock(World.class);
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

        owner = mock(Player.class);
        when(owner.getUniqueId()).thenReturn(ownerUuid);
        when(owner.getName()).thenReturn("PlotOwner");

        intruder = mock(Player.class);
        when(intruder.getUniqueId()).thenReturn(intruderUuid);
        when(intruder.getName()).thenReturn("Intruder");

        targetEntity = mock(LivingEntity.class);
        Location targetLoc = new Location(mockWorld, 5, 64, 5); // chunk (0, 0)
        when(targetEntity.getLocation()).thenReturn(targetLoc);
        when(targetEntity.getWorld()).thenReturn(mockWorld);

        // Chunk (0, 0)
        plot = new Plot(ownerUuid, 0, 0, "world", UUID.randomUUID());

        PlotRepository mockRepo = mock(PlotRepository.class);
        when(mockRepo.loadAll()).thenReturn(List.of(plot));
        plotManager = new PlotManager(mockRepo, mock(ItemStack.class));

        UserManager mockUserManager = mock(UserManager.class);
        checker = new PlotPermissionChecker(plotManager, mockUserManager);
    }

    @AfterEach
    void tearDown() {
        BukkitTestHelper.tearDownMockServer();
    }

    private EntityKnockbackByEntityEvent mockKnockbackEvent(LivingEntity entity, Entity hitBy) {
        EntityKnockbackByEntityEvent event = mock(EntityKnockbackByEntityEvent.class);
        when(event.getEntity()).thenReturn(entity);
        when(event.getHitBy()).thenReturn(hitBy);
        when(event.getPushedBy()).thenReturn(hitBy);
        final boolean[] cancelled = {false};
        doAnswer(inv -> {
            cancelled[0] = inv.getArgument(0);
            return null;
        }).when(event).setCancelled(anyBoolean());
        when(event.isCancelled()).thenAnswer(inv -> cancelled[0]);
        return event;
    }

    @Test
    @DisplayName("PaperSpigot server environment check detects Paper platform")
    void testIsPaperServer() {
        assertTrue(ChunkyPlots.isPaperServer(), "Should detect Paper / PaperSpigot on classpath");
    }

    @Test
    @DisplayName("Intruder knocking back entity on protected plot is cancelled")
    void testIntruderKnockbackCancelled() {
        EntityKnockbackProtection listener = new EntityKnockbackProtection(checker);
        EntityKnockbackByEntityEvent event = mockKnockbackEvent(targetEntity, intruder);

        listener.onEntityKnockback(event);
        assertTrue(event.isCancelled(), "Knockback by unauthorized player must be cancelled");
    }

    @Test
    @DisplayName("Owner knocking back entity on own plot is allowed")
    void testOwnerKnockbackAllowed() {
        EntityKnockbackProtection listener = new EntityKnockbackProtection(checker);
        EntityKnockbackByEntityEvent event = mockKnockbackEvent(targetEntity, owner);

        listener.onEntityKnockback(event);
        assertFalse(event.isCancelled(), "Knockback by plot owner must be allowed");
    }

    @Test
    @DisplayName("WindCharge shot by intruder is cancelled")
    void testWindChargeIntruderCancelled() {
        EntityKnockbackProtection listener = new EntityKnockbackProtection(checker);
        WindCharge windCharge = mock(WindCharge.class);
        when(windCharge.getShooter()).thenReturn(intruder);

        EntityKnockbackByEntityEvent event = mockKnockbackEvent(targetEntity, windCharge);
        listener.onEntityKnockback(event);
        assertTrue(event.isCancelled(), "WindCharge knockback by intruder must be cancelled");
    }

    @Test
    @DisplayName("WindCharge shot by owner is allowed")
    void testWindChargeOwnerAllowed() {
        EntityKnockbackProtection listener = new EntityKnockbackProtection(checker);
        WindCharge windCharge = mock(WindCharge.class);
        when(windCharge.getShooter()).thenReturn(owner);

        EntityKnockbackByEntityEvent event = mockKnockbackEvent(targetEntity, windCharge);
        listener.onEntityKnockback(event);
        assertFalse(event.isCancelled(), "WindCharge knockback by owner must be allowed");
    }

    @Test
    @DisplayName("Dispenser block projectile from foreign plot is cancelled")
    void testDispenserProjectileKnockback() {
        EntityKnockbackProtection listener = new EntityKnockbackProtection(checker);
        WindCharge windCharge = mock(WindCharge.class);
        BlockProjectileSource bps = mock(BlockProjectileSource.class);

        // Dispenser in chunk (1, 0) which is unowned wilderness or foreign
        Block foreignBlock = mock(Block.class);
        when(foreignBlock.getX()).thenReturn(20);
        when(foreignBlock.getZ()).thenReturn(5);
        when(foreignBlock.getWorld()).thenReturn(mockWorld);
        when(foreignBlock.getChunk()).thenReturn(TestChunkFactory.createChunk(1, 0, mockWorld));

        when(bps.getBlock()).thenReturn(foreignBlock);
        when(windCharge.getShooter()).thenReturn(bps);

        EntityKnockbackByEntityEvent event = mockKnockbackEvent(targetEntity, windCharge);
        listener.onEntityKnockback(event);
        assertTrue(event.isCancelled(), "Knockback from foreign dispenser must be cancelled");
    }

    @Test
    @DisplayName("Knockback on wilderness entity is not cancelled")
    void testWildernessKnockbackAllowed() {
        EntityKnockbackProtection listener = new EntityKnockbackProtection(checker);
        LivingEntity wildernessEntity = mock(LivingEntity.class);
        Location wildLoc = new Location(mockWorld, 100, 64, 100); // chunk (6, 6) -> wilderness
        when(wildernessEntity.getLocation()).thenReturn(wildLoc);

        EntityKnockbackByEntityEvent event = mockKnockbackEvent(wildernessEntity, intruder);
        listener.onEntityKnockback(event);
        assertFalse(event.isCancelled(), "Knockback in wilderness should not be affected by plot protection");
    }
}
