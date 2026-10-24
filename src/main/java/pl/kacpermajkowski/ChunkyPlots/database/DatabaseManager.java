package pl.kacpermajkowski.ChunkyPlots.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class DatabaseManager {
    private final HikariDataSource dataSource;
    private final Logger logger;

    public DatabaseManager(File dataFolder, Logger logger) {
        this.logger = logger;

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File dbFile = new File(dataFolder, "chunkyplots.db");

        HikariConfig config = new HikariConfig();
        config.setPoolName("ChunkyPlots-SQLite");
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);

        // SQLite PRAGMA tuning
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");
        config.addDataSourceProperty("foreign_keys", "ON");

        this.dataSource = new HikariDataSource(config);

        initSchema();
    }

    public DatabaseManager(HikariDataSource dataSource, Logger logger) {
        this.dataSource = dataSource;
        this.logger = logger;
        initSchema();
    }

    private void initSchema() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Enable WAL and Foreign Keys
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA foreign_keys=ON;");

            // Plots table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS plots (
                    uuid VARCHAR(36) PRIMARY KEY,
                    owner_uuid VARCHAR(36) NOT NULL,
                    chunk_x INTEGER NOT NULL,
                    chunk_z INTEGER NOT NULL,
                    world_name VARCHAR(255) NOT NULL,
                    UNIQUE(world_name, chunk_x, chunk_z)
                );
            """);

            // Plot members table (whitelist / blacklist)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS plot_members (
                    plot_uuid VARCHAR(36) NOT NULL,
                    player_uuid VARCHAR(36) NOT NULL,
                    member_type VARCHAR(16) NOT NULL,
                    PRIMARY KEY (plot_uuid, player_uuid),
                    FOREIGN KEY (plot_uuid) REFERENCES plots(uuid) ON DELETE CASCADE
                );
            """);

            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    uuid VARCHAR(36) PRIMARY KEY,
                    is_teleport_on_cooldown BOOLEAN NOT NULL DEFAULT 0
                );
            """);

            // Performance indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_plots_coords ON plots(world_name, chunk_x, chunk_z);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_plot_members_plot ON plot_members(plot_uuid);");

        } catch (SQLException e) {
            logger.severe("Failed to initialize SQLite database schema: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
