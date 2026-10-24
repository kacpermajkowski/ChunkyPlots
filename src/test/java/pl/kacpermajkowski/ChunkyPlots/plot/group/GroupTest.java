package pl.kacpermajkowski.ChunkyPlots.plot.group;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GroupTest {

    @Test
    @DisplayName("Default group 'all' returns all plots dynamically via supplier")
    void testDefaultGroupPlotsSupplier() {
        UUID ownerUuid = UUID.randomUUID();
        User user = new User(ownerUuid);

        List<Plot> ownedPlots = new ArrayList<>();
        Plot plot1 = new Plot(ownerUuid, 0, 0, "world", UUID.randomUUID());
        Plot plot2 = new Plot(ownerUuid, 1, 1, "world", UUID.randomUUID());
        ownedPlots.add(plot1);

        user.bindPlotSupplier(() -> ownedPlots);

        Group defaultGroup = user.getDefaultGroup();
        assertNotNull(defaultGroup);
        assertEquals("all", defaultGroup.getName());
        assertTrue(defaultGroup.isDefault());
        assertEquals(1, defaultGroup.getPlots().size());
        assertTrue(defaultGroup.contains(plot1));

        // Dynamically add a second plot
        ownedPlots.add(plot2);
        assertEquals(2, defaultGroup.getPlots().size());
        assertTrue(defaultGroup.contains(plot2));

        // Default group cannot have plots manually added or removed
        defaultGroup.add(new Plot(ownerUuid, 2, 2, "world", UUID.randomUUID()));
        assertEquals(2, defaultGroup.getPlots().size()); // unchanged

        defaultGroup.remove(plot1);
        assertEquals(2, defaultGroup.getPlots().size()); // unchanged
    }

    @Test
    @DisplayName("User group management: create, delete, and case-insensitivity")
    void testUserGroupManagement() {
        UUID ownerUuid = UUID.randomUUID();
        User user = new User(ownerUuid);

        // Cannot create group named 'all'
        assertThrows(IllegalArgumentException.class, () -> user.createGroup("all"));
        assertThrows(IllegalArgumentException.class, () -> user.createGroup("ALL"));

        // Create custom group
        Group group = user.createGroup("Farms");
        assertNotNull(group);
        assertEquals("Farms", group.getName());
        assertFalse(group.isDefault());
        assertTrue(user.hasGroup("farms"));
        assertTrue(user.hasGroup("FARMS"));
        assertEquals(group, user.getGroup("farms"));

        // Cannot create duplicate
        assertThrows(IllegalArgumentException.class, () -> user.createGroup("farms"));

        // Cannot delete default group
        assertFalse(user.removeGroup("all"));

        // Delete custom group
        assertTrue(user.removeGroup("farms"));
        assertFalse(user.hasGroup("farms"));
    }

    @Test
    @DisplayName("Custom group plot addition and removal")
    void testCustomGroupPlots() {
        UUID ownerUuid = UUID.randomUUID();
        User user = new User(ownerUuid);
        Group group = user.createGroup("Castles");

        Plot plot1 = new Plot(ownerUuid, 10, 10, "world", UUID.randomUUID());
        Plot plot2 = new Plot(ownerUuid, 20, 20, "world", UUID.randomUUID());

        group.add(plot1);
        group.add(plot1); // already added
        assertTrue(group.contains(plot1));
        assertEquals(1, group.getPlots().size());

        group.add(plot2);
        assertEquals(2, group.getPlots().size());

        group.remove(plot1);
        assertFalse(group.contains(plot1));
        assertTrue(group.contains(plot2));
        assertEquals(1, group.getPlots().size());
    }

    @Test
    @DisplayName("Group bulk flags: setFlag, resetFlag, resetAllFlags, getFlagStatus")
    void testGroupBulkFlags() {
        UUID ownerUuid = UUID.randomUUID();
        User user = new User(ownerUuid);
        Group group = user.createGroup("PvpArenas");

        // Empty group status
        assertEquals(Group.GroupFlagStatus.EMPTY, group.getFlagStatus(PlotFlag.PVP));

        Plot p1 = new Plot(ownerUuid, 1, 1, "world", UUID.randomUUID());
        Plot p2 = new Plot(ownerUuid, 2, 2, "world", UUID.randomUUID());
        group.add(p1);
        group.add(p2);

        // Default PVP is false on both plots
        assertEquals(Group.GroupFlagStatus.ALL_FALSE, group.getFlagStatus(PlotFlag.PVP));

        // Set PVP true on group
        group.setFlag(PlotFlag.PVP, true);
        assertEquals(Group.GroupFlagStatus.ALL_TRUE, group.getFlagStatus(PlotFlag.PVP));
        assertTrue(p1.getFlag(PlotFlag.PVP));
        assertTrue(p2.getFlag(PlotFlag.PVP));

        // Change p1 directly -> status becomes DIFFERENT
        p1.setFlag(PlotFlag.PVP, false);
        assertEquals(Group.GroupFlagStatus.DIFFERENT, group.getFlagStatus(PlotFlag.PVP));

        // Reset PVP on group
        group.resetFlag(PlotFlag.PVP);
        assertFalse(p1.getFlag(PlotFlag.PVP));
        assertFalse(p2.getFlag(PlotFlag.PVP));
        assertEquals(Group.GroupFlagStatus.ALL_FALSE, group.getFlagStatus(PlotFlag.PVP));

        // Reset all flags
        p1.setFlag(PlotFlag.EXPLOSIONS, true);
        p2.setFlag(PlotFlag.FIRE_SPREAD, true);
        group.resetAllFlags();
        assertTrue(p1.getCustomFlags().isEmpty());
        assertTrue(p2.getCustomFlags().isEmpty());
    }

    @Test
    @DisplayName("Group bulk whitelist and blacklist operations")
    void testGroupBulkWhitelistAndBlacklist() {
        UUID ownerUuid = UUID.randomUUID();
        User user = new User(ownerUuid);
        Group group = user.createGroup("SharedBases");

        Plot p1 = new Plot(ownerUuid, 1, 1, "world", UUID.randomUUID());
        Plot p2 = new Plot(ownerUuid, 2, 2, "world", UUID.randomUUID());
        group.add(p1);
        group.add(p2);

        UUID friend = UUID.randomUUID();
        UUID enemy = UUID.randomUUID();

        // Whitelist friend across group
        group.whitelistPlayer(friend);
        assertTrue(p1.isPlayerWhitelisted(friend));
        assertTrue(p2.isPlayerWhitelisted(friend));

        // Unwhitelist friend
        group.unwhitelistPlayer(friend);
        assertFalse(p1.isPlayerWhitelisted(friend));
        assertFalse(p2.isPlayerWhitelisted(friend));

        // Blacklist enemy across group
        group.blacklistPlayer(enemy);
        assertTrue(p1.isPlayerBlacklisted(enemy));
        assertTrue(p2.isPlayerBlacklisted(enemy));

        // Unblacklist enemy
        group.unblacklistPlayer(enemy);
        assertFalse(p1.isPlayerBlacklisted(enemy));
        assertFalse(p2.isPlayerBlacklisted(enemy));
    }
}
