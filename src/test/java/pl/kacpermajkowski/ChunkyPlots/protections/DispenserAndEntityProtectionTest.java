package pl.kacpermajkowski.ChunkyPlots.protections;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.TestChunkFactory;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotRepository;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.EntityChangeBlockProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.DispenserProtection;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class DispenserAndEntityProtectionTest {

    private Server mockServer;
    private World mockWorld;
    private PlotManager plotManager;
    private PlotPermissionChecker checker;

    private Plot plotA;
    private Plot plotB;
    private UUID playerA;
    private UUID playerB;

    @BeforeEach
    void setUp() {
        mockServer = BukkitTestHelper.setUpMockServer();
        mockWorld = Mockito.mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        when(mockServer.getWorld("world")).thenReturn(mockWorld);

        playerA = UUID.randomUUID();
        playerB = UUID.randomUUID();

        // Chunk (0, 0) -> x: 0..15, z: 0..15
        plotA = new Plot(playerA, 0, 0, "world", UUID.randomUUID());
        // Chunk (1, 0) -> x: 16..31, z: 0..15
        plotB = new Plot(playerB, 1, 0, "world", UUID.randomUUID());

        PlotRepository mockRepo = Mockito.mock(PlotRepository.class);
        when(mockRepo.loadAll()).thenReturn(List.of(plotA, plotB));
        plotManager = new PlotManager(mockRepo, Mockito.mock(ItemStack.class));

        UserManager mockUserManager = Mockito.mock(UserManager.class);
        checker = new PlotPermissionChecker(plotManager, mockUserManager);
    }

    @AfterEach
    void tearDown() {
        BukkitTestHelper.tearDownMockServer();
    }

    @Test
    @DisplayName("Dispenser: Blocks dispensing across border into foreign plot")
    void testDispenserBorderProtection() {
        DispenserProtection dispenserProtection = new DispenserProtection(checker);

        // Dispenser on border of Plot A at x=15, facing EAST into Plot B (x=16)
        Block dispenserBlock = mockBlock(15, 5, BlockFace.EAST);
        Block targetInPlotB = mockBlock(16, 5, null);
        when(dispenserBlock.getRelative(BlockFace.EAST)).thenReturn(targetInPlotB);

        BlockDispenseEvent borderDispenseEvent = new BlockDispenseEvent(dispenserBlock, Mockito.mock(ItemStack.class), new Vector(1, 0, 0));
        dispenserProtection.onDispense(borderDispenseEvent);
        assertTrue(borderDispenseEvent.isCancelled(), "Dispenser shooting across border into foreign plot must be cancelled!");

        // Dispenser inside Plot A facing WEST (into same plot A, x=14) -> Allowed
        Block dispenserInside = mockBlock(15, 5, BlockFace.WEST);
        Block targetInside = mockBlock(14, 5, null);
        when(dispenserInside.getRelative(BlockFace.WEST)).thenReturn(targetInside);

        BlockDispenseEvent internalDispenseEvent = new BlockDispenseEvent(dispenserInside, Mockito.mock(ItemStack.class), new Vector(-1, 0, 0));
        dispenserProtection.onDispense(internalDispenseEvent);
        assertFalse(internalDispenseEvent.isCancelled(), "Dispenser shooting inside same plot must be allowed");
    }

    @Test
    @DisplayName("EntityChangeBlock: Withers, Ravagers, and Endermen cannot modify claimed plot blocks")
    void testEntityChangeBlockProtection() {
        EntityChangeBlockProtection protection = new EntityChangeBlockProtection(checker);

        Block claimedBlock = mockBlock(5, 5, null); // inside Plot A
        Block wildernessBlock = mockBlock(100, 100, null); // wilderness

        // 1. Wither charging claimed block -> Cancelled
        Wither wither = Mockito.mock(Wither.class);
        EntityChangeBlockEvent witherEvent = new EntityChangeBlockEvent(wither, claimedBlock, Material.AIR.createBlockData());
        protection.onEntityChangeBlock(witherEvent);
        assertTrue(witherEvent.isCancelled(), "Wither destroying claimed block must be cancelled");

        // 2. Enderman picking up claimed block -> Cancelled
        Enderman enderman = Mockito.mock(Enderman.class);
        EntityChangeBlockEvent endermanEvent = new EntityChangeBlockEvent(enderman, claimedBlock, Material.AIR.createBlockData());
        protection.onEntityChangeBlock(endermanEvent);
        assertTrue(endermanEvent.isCancelled(), "Enderman picking up claimed block must be cancelled");

        // 3. Wither in wilderness -> Allowed
        EntityChangeBlockEvent wildernessEvent = new EntityChangeBlockEvent(wither, wildernessBlock, Material.AIR.createBlockData());
        protection.onEntityChangeBlock(wildernessEvent);
        assertFalse(wildernessEvent.isCancelled(), "Wither changing wilderness block must be allowed");

        // 4. Authorized owner changing block -> Allowed
        Player ownerPlayer = Mockito.mock(Player.class);
        when(ownerPlayer.getUniqueId()).thenReturn(playerA);
        EntityChangeBlockEvent ownerEvent = new EntityChangeBlockEvent(ownerPlayer, claimedBlock, Material.AIR.createBlockData());
        protection.onEntityChangeBlock(ownerEvent);
        assertFalse(ownerEvent.isCancelled(), "Plot owner changing block must be allowed");
    }

    private Block mockBlock(int x, int z, BlockFace facing) {
        Block block = Mockito.mock(Block.class);
        when(block.getX()).thenReturn(x);
        when(block.getZ()).thenReturn(z);
        when(block.getWorld()).thenReturn(mockWorld);
        Chunk chunk = TestChunkFactory.createChunk(x >> 4, z >> 4, mockWorld);
        when(block.getChunk()).thenReturn(chunk);

        if (facing != null) {
            Directional directional = Mockito.mock(Directional.class);
            when(directional.getFacing()).thenReturn(facing);
            when(block.getBlockData()).thenReturn(directional);
        }
        return block;
    }
}
