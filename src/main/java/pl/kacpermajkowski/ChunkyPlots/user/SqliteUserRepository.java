package pl.kacpermajkowski.ChunkyPlots.user;

import pl.kacpermajkowski.ChunkyPlots.database.DatabaseManager;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.group.Group;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

public class SqliteUserRepository implements UserRepository {
    private final DatabaseManager databaseManager;
    private final File dataFolder;
    private final Logger logger;
    private Function<UUID, Plot> plotResolver;

    public SqliteUserRepository(DatabaseManager databaseManager, File dataFolder, Logger logger) {
        this.databaseManager = databaseManager;
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    public void setPlotResolver(Function<UUID, Plot> plotResolver) {
        this.plotResolver = plotResolver;
    }

    @Override
    public Set<User> loadAll() {
        Set<User> users = new HashSet<>();
        Map<UUID, User> userMap = new HashMap<>();

        String selectUsers = "SELECT uuid, is_teleport_on_cooldown FROM users;";
        String selectGroups = "SELECT owner_uuid, group_name FROM user_groups;";
        String selectGroupPlots = "SELECT owner_uuid, group_name, plot_uuid FROM user_group_plots;";

        try (Connection conn = databaseManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(selectUsers);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    boolean cooldown = rs.getBoolean("is_teleport_on_cooldown");

                    User user = new User(uuid);
                    user.setTeleportOnCooldown(cooldown);
                    userMap.put(uuid, user);
                    users.add(user);
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(selectGroups);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID ownerUuid = UUID.fromString(rs.getString("owner_uuid"));
                    String groupName = rs.getString("group_name");

                    User user = userMap.get(ownerUuid);
                    if (user == null) {
                        user = new User(ownerUuid);
                        userMap.put(ownerUuid, user);
                        users.add(user);
                    }
                    if (!user.hasGroup(groupName)) {
                        user.createGroup(groupName);
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(selectGroupPlots);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID ownerUuid = UUID.fromString(rs.getString("owner_uuid"));
                    String groupName = rs.getString("group_name");
                    UUID plotUuid = UUID.fromString(rs.getString("plot_uuid"));

                    User user = userMap.get(ownerUuid);
                    if (user != null) {
                        Group group = user.getGroup(groupName);
                        if (group != null && plotResolver != null) {
                            Plot plot = plotResolver.apply(plotUuid);
                            if (plot != null) {
                                group.add(plot);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to load users from SQLite: " + e.getMessage());
        }

        // Automatic migration from YAML if database is empty
        if (users.isEmpty() && dataFolder != null) {
            File yamlUsersDir = new File(dataFolder, "users");
            if (yamlUsersDir.exists() && yamlUsersDir.isDirectory()) {
                YamlUserRepository yamlRepo = new YamlUserRepository(dataFolder, logger);
                Set<User> migratedUsers = yamlRepo.loadAll();
                if (!migratedUsers.isEmpty()) {
                    logger.info("Migrating " + migratedUsers.size() + " users from YAML to SQLite database...");
                    saveAllSync(migratedUsers);
                    return migratedUsers;
                }
            }
        }

        return users;
    }

    @Override
    public void save(User user) {
        if (user == null) return;
        databaseManager.runAsync(() -> saveSync(user));
    }

    public void saveSync(User user) {
        if (user == null) return;

        String upsertUser = """
            INSERT INTO users (uuid, is_teleport_on_cooldown)
            VALUES (?, ?)
            ON CONFLICT(uuid) DO UPDATE SET
                is_teleport_on_cooldown = excluded.is_teleport_on_cooldown;
        """;

        String deleteGroups = "DELETE FROM user_groups WHERE owner_uuid = ?;";
        String insertGroup = "INSERT INTO user_groups (owner_uuid, group_name) VALUES (?, ?);";
        String deletePlots = "DELETE FROM user_group_plots WHERE owner_uuid = ?;";
        String insertPlot = "INSERT INTO user_group_plots (owner_uuid, group_name, plot_uuid) VALUES (?, ?, ?);";

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(upsertUser)) {
                    stmt.setString(1, user.getPlayerUUID().toString());
                    stmt.setBoolean(2, user.isTeleportOnCooldown());
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(deleteGroups)) {
                    stmt.setString(1, user.getPlayerUUID().toString());
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(deletePlots)) {
                    stmt.setString(1, user.getPlayerUUID().toString());
                    stmt.executeUpdate();
                }

                List<Group> customGroups = user.getGroups().stream()
                        .filter(g -> !g.isDefault())
                        .toList();

                if (!customGroups.isEmpty()) {
                    try (PreparedStatement groupStmt = conn.prepareStatement(insertGroup);
                         PreparedStatement plotStmt = conn.prepareStatement(insertPlot)) {
                        for (Group group : customGroups) {
                            groupStmt.setString(1, user.getPlayerUUID().toString());
                            groupStmt.setString(2, group.getName());
                            groupStmt.addBatch();

                            for (Plot plot : group.getPlots()) {
                                if (plot != null) {
                                    plotStmt.setString(1, user.getPlayerUUID().toString());
                                    plotStmt.setString(2, group.getName());
                                    plotStmt.setString(3, plot.getUUID().toString());
                                    plotStmt.addBatch();
                                }
                            }
                        }
                        groupStmt.executeBatch();
                        plotStmt.executeBatch();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.severe("Failed to save user " + user.getPlayerUUID() + " to SQLite: " + e.getMessage());
        }
    }

    @Override
    public void saveAll(Collection<User> users) {
        if (users == null || users.isEmpty()) return;
        databaseManager.runAsync(() -> saveAllSync(users));
    }

    public void saveAllSync(Collection<User> users) {
        if (users == null || users.isEmpty()) return;
        for (User user : users) {
            saveSync(user);
        }
    }
}
