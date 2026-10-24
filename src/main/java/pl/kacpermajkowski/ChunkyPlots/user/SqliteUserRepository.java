package pl.kacpermajkowski.ChunkyPlots.user;

import pl.kacpermajkowski.ChunkyPlots.database.DatabaseManager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class SqliteUserRepository implements UserRepository {
    private final DatabaseManager databaseManager;
    private final File dataFolder;
    private final Logger logger;

    public SqliteUserRepository(DatabaseManager databaseManager, File dataFolder, Logger logger) {
        this.databaseManager = databaseManager;
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    @Override
    public Set<User> loadAll() {
        Set<User> users = new HashSet<>();
        String selectUsers = "SELECT uuid, is_teleport_on_cooldown FROM users;";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectUsers);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                boolean cooldown = rs.getBoolean("is_teleport_on_cooldown");

                User user = new User(uuid);
                user.setTeleportOnCooldown(cooldown);
                users.add(user);
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

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(upsertUser)) {
            stmt.setString(1, user.getPlayerUUID().toString());
            stmt.setBoolean(2, user.isTeleportOnCooldown());
            stmt.executeUpdate();
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

        String upsertUser = """
            INSERT INTO users (uuid, is_teleport_on_cooldown)
            VALUES (?, ?)
            ON CONFLICT(uuid) DO UPDATE SET
                is_teleport_on_cooldown = excluded.is_teleport_on_cooldown;
        """;

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(upsertUser)) {
                for (User user : users) {
                    stmt.setString(1, user.getPlayerUUID().toString());
                    stmt.setBoolean(2, user.isTeleportOnCooldown());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.severe("Failed to save all users to SQLite: " + e.getMessage());
        }
    }
}
