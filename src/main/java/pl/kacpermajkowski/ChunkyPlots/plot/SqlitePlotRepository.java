package pl.kacpermajkowski.ChunkyPlots.plot;

import pl.kacpermajkowski.ChunkyPlots.database.DatabaseManager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class SqlitePlotRepository implements PlotRepository {
    private final DatabaseManager databaseManager;
    private final File dataFolder;
    private final Logger logger;

    public SqlitePlotRepository(DatabaseManager databaseManager, File dataFolder, Logger logger) {
        this.databaseManager = databaseManager;
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    @Override
    public List<Plot> loadAll() {
        List<Plot> plots = new ArrayList<>();
        Map<UUID, Plot> plotMap = new HashMap<>();

        String selectPlots = "SELECT uuid, owner_uuid, chunk_x, chunk_z, world_name FROM plots;";
        String selectMembers = "SELECT plot_uuid, player_uuid, member_type FROM plot_members;";

        try (Connection conn = databaseManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(selectPlots);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    UUID ownerUuid = UUID.fromString(rs.getString("owner_uuid"));
                    int chunkX = rs.getInt("chunk_x");
                    int chunkZ = rs.getInt("chunk_z");
                    String worldName = rs.getString("world_name");

                    Plot plot = new Plot(ownerUuid, chunkX, chunkZ, worldName, uuid);
                    plotMap.put(uuid, plot);
                    plots.add(plot);
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(selectMembers);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID plotUuid = UUID.fromString(rs.getString("plot_uuid"));
                    UUID playerUuid = UUID.fromString(rs.getString("player_uuid"));
                    String type = rs.getString("member_type");

                    Plot plot = plotMap.get(plotUuid);
                    if (plot != null) {
                        if ("WHITELIST".equalsIgnoreCase(type)) {
                            plot.whitelistPlayer(playerUuid);
                        } else if ("BLACKLIST".equalsIgnoreCase(type)) {
                            plot.blacklistPlayer(playerUuid);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to load plots from SQLite: " + e.getMessage());
        }

        // Automatic migration from YAML if database is empty
        if (plots.isEmpty() && dataFolder != null) {
            File yamlPlotsDir = new File(dataFolder, "plots");
            if (yamlPlotsDir.exists() && yamlPlotsDir.isDirectory()) {
                YamlPlotRepository yamlRepo = new YamlPlotRepository(dataFolder, logger);
                List<Plot> migratedPlots = yamlRepo.loadAll();
                if (!migratedPlots.isEmpty()) {
                    logger.info("Migrating " + migratedPlots.size() + " plots from YAML to SQLite database...");
                    for (Plot plot : migratedPlots) {
                        saveSync(plot);
                    }
                    return migratedPlots;
                }
            }
        }

        return plots;
    }

    @Override
    public void save(Plot plot) {
        if (plot == null) return;
        databaseManager.runAsync(() -> saveSync(plot));
    }

    public void saveSync(Plot plot) {
        if (plot == null) return;

        String upsertPlot = """
            INSERT INTO plots (uuid, owner_uuid, chunk_x, chunk_z, world_name)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(uuid) DO UPDATE SET
                owner_uuid = excluded.owner_uuid,
                chunk_x = excluded.chunk_x,
                chunk_z = excluded.chunk_z,
                world_name = excluded.world_name;
        """;

        String deleteMembers = "DELETE FROM plot_members WHERE plot_uuid = ?;";
        String insertMember = "INSERT INTO plot_members (plot_uuid, player_uuid, member_type) VALUES (?, ?, ?);";

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(upsertPlot)) {
                    stmt.setString(1, plot.getUUID().toString());
                    stmt.setString(2, plot.getOwnerUUID().toString());
                    stmt.setInt(3, plot.getChunkX());
                    stmt.setInt(4, plot.getChunkZ());
                    stmt.setString(5, plot.getWorldName());
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(deleteMembers)) {
                    stmt.setString(1, plot.getUUID().toString());
                    stmt.executeUpdate();
                }

                if (!plot.getWhitelist().isEmpty() || !plot.getBlacklist().isEmpty()) {
                    try (PreparedStatement stmt = conn.prepareStatement(insertMember)) {
                        for (UUID memberUuid : plot.getWhitelist()) {
                            stmt.setString(1, plot.getUUID().toString());
                            stmt.setString(2, memberUuid.toString());
                            stmt.setString(3, "WHITELIST");
                            stmt.addBatch();
                        }
                        for (UUID memberUuid : plot.getBlacklist()) {
                            stmt.setString(1, plot.getUUID().toString());
                            stmt.setString(2, memberUuid.toString());
                            stmt.setString(3, "BLACKLIST");
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
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
            logger.severe("Failed to save plot " + plot.getUUID() + " to SQLite: " + e.getMessage());
        }
    }

    @Override
    public void delete(Plot plot) {
        if (plot == null) return;
        databaseManager.runAsync(() -> deleteSync(plot));
    }

    public void deleteSync(Plot plot) {
        if (plot == null) return;

        String deletePlot = "DELETE FROM plots WHERE uuid = ?;";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deletePlot)) {
            stmt.setString(1, plot.getUUID().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to delete plot " + plot.getUUID() + " from SQLite: " + e.getMessage());
        }
    }
}
