package pl.kacpermajkowski.ChunkyPlots.commands.plot;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.kacpermajkowski.ChunkyPlots.BukkitTestHelper;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.group.PlotGroupCommand;
import pl.kacpermajkowski.ChunkyPlots.database.DatabaseManager;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.SqlitePlotRepository;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;
import pl.kacpermajkowski.ChunkyPlots.plot.group.Group;
import pl.kacpermajkowski.ChunkyPlots.plot.group.gui.GroupGuiManager;
import pl.kacpermajkowski.ChunkyPlots.user.SqliteUserRepository;
import pl.kacpermajkowski.ChunkyPlots.user.User;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlotGroupCommandTest {
    private Server mockServer;
    private File tempFolder;
    private DatabaseManager databaseManager;
    private SqlitePlotRepository plotRepository;
    private SqliteUserRepository userRepository;
    private PlotManager plotManager;
    private UserManager userManager;
    private GroupGuiManager mockGroupGuiManager;
    private PlotGroupCommand groupCommand;
    private Player mockOwner;
    private Player mockFriend;
    private OfflinePlayer mockOfflineFriend;
    private World mockWorld;
    private final UUID ownerUuid = UUID.randomUUID();
    private final UUID friendUuid = UUID.randomUUID();
    private Plot plot1;
    private Plot plot2;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = BukkitTestHelper.setUpMockServer();
        tempFolder = Files.createTempDirectory("chunkyplots-group-cmd-test").toFile();
        Logger logger = Logger.getLogger("ChunkyPlotsGroupCmdTest");

        mockWorld = mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        when(mockServer.getWorld("world")).thenReturn(mockWorld);

        databaseManager = new DatabaseManager(tempFolder, logger);
        plotRepository = new SqlitePlotRepository(databaseManager, tempFolder, logger);
        userRepository = new SqliteUserRepository(databaseManager, tempFolder, logger);

        Plot p1 = new Plot(ownerUuid, 5, 5, "world", UUID.randomUUID());
        Plot p2 = new Plot(ownerUuid, 6, 6, "world", UUID.randomUUID());
        plotRepository.saveSync(p1);
        plotRepository.saveSync(p2);

        plotManager = new PlotManager(plotRepository, null);
        userManager = new UserManager(userRepository, plotManager);
        mockGroupGuiManager = mock(GroupGuiManager.class);

        plot1 = plotManager.getPlot(p1.getUUID());
        plot2 = plotManager.getPlot(p2.getUUID());

        groupCommand = new PlotGroupCommand(plotManager, userManager, mockGroupGuiManager);

        mockOwner = mock(Player.class);
        when(mockOwner.getUniqueId()).thenReturn(ownerUuid);
        when(mockOwner.getName()).thenReturn("GroupOwner");
        when(mockOwner.getLocation()).thenAnswer(inv -> new Location(mockWorld, 88.0, 64.0, 88.0));

        mockFriend = mock(Player.class);
        when(mockFriend.getUniqueId()).thenReturn(friendUuid);
        when(mockFriend.getName()).thenReturn("GroupFriend");
        when(mockServer.getPlayer("GroupFriend")).thenReturn(mockFriend);

        mockOfflineFriend = mock(OfflinePlayer.class);
        when(mockOfflineFriend.getUniqueId()).thenReturn(friendUuid);
        when(mockOfflineFriend.getName()).thenReturn("GroupFriend");
        when(mockServer.getOfflinePlayer(friendUuid)).thenReturn(mockOfflineFriend);
        when(mockServer.getOfflinePlayer("GroupFriend")).thenReturn(mockOfflineFriend);
    }

    @AfterEach
    void tearDown() throws IOException {
        BukkitTestHelper.tearDownMockServer();
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (tempFolder != null && tempFolder.exists()) {
            Files.walk(tempFolder.toPath())
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {}
                    });
        }
    }

    @Test
    @DisplayName("Command metadata: name, aliases, permission, syntax")
    void testCommandMetadata() {
        assertEquals("group", groupCommand.getName());
        assertTrue(groupCommand.getAliases().contains("groups"));
        assertTrue(groupCommand.getAliases().contains("grupa"));
        assertTrue(groupCommand.getAliases().contains("grupy"));
        assertEquals("chunkyplots.player", groupCommand.getPermission());
    }

    @Test
    @DisplayName("GUI invocation: /plot group or /plot group gui opens GroupGuiManager")
    void testOpenGui() {
        groupCommand.execute(mockOwner, new String[0]);
        verify(mockGroupGuiManager, times(1)).openGroupListGui(mockOwner);

        groupCommand.execute(mockOwner, new String[]{"gui"});
        verify(mockGroupGuiManager, times(2)).openGroupListGui(mockOwner);
    }

    @Test
    @DisplayName("Create group /plot group create <name> and reject duplicates / 'all'")
    void testCreateGroup() {
        // Create custom group
        groupCommand.execute(mockOwner, new String[]{"create", "myfarms"});
        User user = userManager.getUser(mockOwner);
        assertTrue(user.hasGroup("myfarms"));

        // Reject 'all'
        groupCommand.execute(mockOwner, new String[]{"create", "all"});
        verify(mockOwner, atLeastOnce()).sendMessage(any(Component.class));

        // Reject duplicate
        groupCommand.execute(mockOwner, new String[]{"create", "myfarms"});
        assertEquals(2, user.getGroups().size()); // 'all' + 'myfarms'
    }

    @Test
    @DisplayName("Delete group /plot group delete <name> and reject deleting 'all'")
    void testDeleteGroup() {
        User user = userManager.getUser(mockOwner);
        user.createGroup("Castles");
        assertTrue(user.hasGroup("Castles"));

        // Reject deleting 'all'
        groupCommand.execute(mockOwner, new String[]{"delete", "all"});
        assertTrue(user.hasGroup("all"));

        // Delete Castles
        groupCommand.execute(mockOwner, new String[]{"delete", "castles"});
        assertFalse(user.hasGroup("castles"));
    }

    @Test
    @DisplayName("Add and remove plots to/from custom group and protect 'all'")
    void testAddAndRemovePlots() {
        User user = userManager.getUser(mockOwner);
        Group group = user.createGroup("Industry");

        // Add plot1 using ID
        groupCommand.execute(mockOwner, new String[]{"addplot", "industry", plot1.getID()});
        assertTrue(group.contains(plot1));

        // Add plot2 using mnemonic ID
        groupCommand.execute(mockOwner, new String[]{"addplot", "industry", plot2.getMnemonicId()});
        assertTrue(group.contains(plot2));

        // Reject adding plot to 'all'
        groupCommand.execute(mockOwner, new String[]{"addplot", "all", plot1.getID()});

        // Remove plot1
        groupCommand.execute(mockOwner, new String[]{"removeplot", "industry", plot1.getID()});
        assertFalse(group.contains(plot1));
        assertTrue(group.contains(plot2));

        // Reject removing from 'all'
        groupCommand.execute(mockOwner, new String[]{"removeplot", "all", plot2.getID()});
    }

    @Test
    @DisplayName("Group bulk flag configuration /plot group <name> flag <flag> <value>")
    void testGroupBulkFlag() {
        User user = userManager.getUser(mockOwner);
        Group group = user.createGroup("PvPZone");
        group.add(plot1);
        group.add(plot2);

        assertFalse(plot1.getFlag(PlotFlag.PVP));
        assertFalse(plot2.getFlag(PlotFlag.PVP));

        // Set flag pvp true via /plot group pvpzone flag pvp true
        groupCommand.execute(mockOwner, new String[]{"pvpzone", "flag", "pvp", "true"});
        assertTrue(plot1.getFlag(PlotFlag.PVP));
        assertTrue(plot2.getFlag(PlotFlag.PVP));

        // Toggle flag via /plot group pvpzone pvp toggle
        groupCommand.execute(mockOwner, new String[]{"pvpzone", "pvp", "toggle"});
        assertFalse(plot1.getFlag(PlotFlag.PVP));
        assertFalse(plot2.getFlag(PlotFlag.PVP));
    }

    @Test
    @DisplayName("Group bulk whitelist /plot group <name> add/remove <player>")
    void testGroupBulkWhitelist() {
        User user = userManager.getUser(mockOwner);
        Group group = user.createGroup("TeamBases");
        group.add(plot1);
        group.add(plot2);

        // Add member to group
        groupCommand.execute(mockOwner, new String[]{"teambases", "add", "GroupFriend"});
        assertTrue(plot1.isPlayerWhitelisted(friendUuid));
        assertTrue(plot2.isPlayerWhitelisted(friendUuid));

        // Remove member from group
        groupCommand.execute(mockOwner, new String[]{"teambases", "remove", "GroupFriend"});
        assertFalse(plot1.isPlayerWhitelisted(friendUuid));
        assertFalse(plot2.isPlayerWhitelisted(friendUuid));
    }

    @Test
    @DisplayName("Group bulk blacklist /plot group <name> deny/undeny <player>")
    void testGroupBulkBlacklist() {
        User user = userManager.getUser(mockOwner);
        Group group = user.createGroup("SecureZones");
        group.add(plot1);
        group.add(plot2);

        // Deny member
        groupCommand.execute(mockOwner, new String[]{"securezones", "deny", "GroupFriend"});
        assertTrue(plot1.isPlayerBlacklisted(friendUuid));
        assertTrue(plot2.isPlayerBlacklisted(friendUuid));

        // Undeny member
        groupCommand.execute(mockOwner, new String[]{"securezones", "undeny", "GroupFriend"});
        assertFalse(plot1.isPlayerBlacklisted(friendUuid));
        assertFalse(plot2.isPlayerBlacklisted(friendUuid));
    }

    @Test
    @DisplayName("Tab completion suggests subcommands, group names, flags, and values")
    void testTabCompletion() {
        User user = userManager.getUser(mockOwner);
        user.createGroup("Farms");

        // Arg 0
        List<String> arg0 = groupCommand.getTabCompletion(mockOwner, new String[]{""});
        assertTrue(arg0.contains("gui"));
        assertTrue(arg0.contains("list"));
        assertTrue(arg0.contains("create"));
        assertTrue(arg0.contains("delete"));
        assertTrue(arg0.contains("addplot"));
        assertTrue(arg0.contains("removeplot"));
        assertTrue(arg0.contains("all"));
        assertTrue(arg0.contains("farms"));

        // Arg 1 for delete
        List<String> deleteGroups = groupCommand.getTabCompletion(mockOwner, new String[]{"delete", ""});
        assertTrue(deleteGroups.contains("farms"));
        assertFalse(deleteGroups.contains("all")); // 'all' cannot be deleted

        // Arg 1 for addplot
        List<String> addPlotGroups = groupCommand.getTabCompletion(mockOwner, new String[]{"addplot", ""});
        assertTrue(addPlotGroups.contains("farms"));
        assertFalse(addPlotGroups.contains("all")); // 'all' cannot have plots manually added

        // Arg 1 for group bulk action
        List<String> groupActions = groupCommand.getTabCompletion(mockOwner, new String[]{"farms", ""});
        assertTrue(groupActions.contains("flag"));
        assertTrue(groupActions.contains("add"));
        assertTrue(groupActions.contains("remove"));
        assertTrue(groupActions.contains("deny"));
        assertTrue(groupActions.contains("pvp"));
    }
}
