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
        String selectFlags = "SELECT plot_uuid, flag_name, flag_value FROM plot_flags;";

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

            try (PreparedStatement stmt = conn.prepareStatement(selectFlags);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID plotUuid = UUID.fromString(rs.getString("plot_uuid"));
                    String flagName = rs.getString("flag_name");
                    boolean flagValue = rs.getBoolean("flag_value");

                    Plot plot = plotMap.get(plotUuid);
                    if (plot != null) {
                        plot.setFlag(flagName, flagValue);
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
    public Plot loadByChunk(int chunkX, int chunkZ, String worldName) {
        if (worldName == null) return null;
        String selectPlot = "SELECT uuid, owner_uuid, chunk_x, chunk_z, world_name FROM plots WHERE world_name = ? AND chunk_x = ? AND chunk_z = ? LIMIT 1;";
        String selectMembers = "SELECT player_uuid, member_type FROM plot_members WHERE plot_uuid = ?;";
        String selectFlags = "SELECT flag_name, flag_value FROM plot_flags WHERE plot_uuid = ?;";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectPlot)) {
            stmt.setString(1, worldName);
            stmt.setInt(2, chunkX);
            stmt.setInt(3, chunkZ);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    UUID ownerUuid = UUID.fromString(rs.getString("owner_uuid"));
                    Plot plot = new Plot(ownerUuid, chunkX, chunkZ, worldName, uuid);

                    try (PreparedStatement memStmt = conn.prepareStatement(selectMembers)) {
                        memStmt.setString(1, uuid.toString());
                        try (ResultSet memRs = memStmt.executeQuery()) {
                            while (memRs.next()) {
                                UUID memberUuid = UUID.fromString(memRs.getString("player_uuid"));
                                String type = memRs.getString("member_type");
                                if ("WHITELIST".equalsIgnoreCase(type)) {
                                    plot.whitelistPlayer(memberUuid);
                                } else if ("BLACKLIST".equalsIgnoreCase(type)) {
                                    plot.blacklistPlayer(memberUuid);
                                }
                            }
                        }
                    }

                    try (PreparedStatement flagStmt = conn.prepareStatement(selectFlags)) {
                        flagStmt.setString(1, uuid.toString());
                        try (ResultSet flagRs = flagStmt.executeQuery()) {
                            while (flagRs.next()) {
                                plot.setFlag(flagRs.getString("flag_name"), flagRs.getBoolean("flag_value"));
                            }
                        }
                    }

                    return plot;
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to load plot at " + worldName + "[" + chunkX + "," + chunkZ + "] from SQLite: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Plot loadByUuid(UUID uuid) {
        if (uuid == null) return null;
        String selectPlot = "SELECT uuid, owner_uuid, chunk_x, chunk_z, world_name FROM plots WHERE uuid = ? LIMIT 1;";
        String selectMembers = "SELECT player_uuid, member_type FROM plot_members WHERE plot_uuid = ?;";
        String selectFlags = "SELECT flag_name, flag_value FROM plot_flags WHERE plot_uuid = ?;";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectPlot)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID ownerUuid = UUID.fromString(rs.getString("owner_uuid"));
                    int chunkX = rs.getInt("chunk_x");
                    int chunkZ = rs.getInt("chunk_z");
                    String worldName = rs.getString("world_name");
                    Plot plot = new Plot(ownerUuid, chunkX, chunkZ, worldName, uuid);

                    try (PreparedStatement memStmt = conn.prepareStatement(selectMembers)) {
                        memStmt.setString(1, uuid.toString());
                        try (ResultSet memRs = memStmt.executeQuery()) {
                            while (memRs.next()) {
                                UUID memberUuid = UUID.fromString(memRs.getString("player_uuid"));
                                String type = memRs.getString("member_type");
                                if ("WHITELIST".equalsIgnoreCase(type)) {
                                    plot.whitelistPlayer(memberUuid);
                                } else if ("BLACKLIST".equalsIgnoreCase(type)) {
                                    plot.blacklistPlayer(memberUuid);
                                }
                            }
                        }
                    }

                    try (PreparedStatement flagStmt = conn.prepareStatement(selectFlags)) {
                        flagStmt.setString(1, uuid.toString());
                        try (ResultSet flagRs = flagStmt.executeQuery()) {
                            while (flagRs.next()) {
                                plot.setFlag(flagRs.getString("flag_name"), flagRs.getBoolean("flag_value"));
                            }
                        }
                    }

                    return plot;
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to load plot by UUID " + uuid + " from SQLite: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Plot> loadByOwner(UUID ownerUuid) {
        if (ownerUuid == null) return List.of();
        List<Plot> plots = new ArrayList<>();
        Map<UUID, Plot> plotMap = new HashMap<>();
        String selectPlots = "SELECT uuid, owner_uuid, chunk_x, chunk_z, world_name FROM plots WHERE owner_uuid = ?;";
        String selectMembers = "SELECT plot_uuid, player_uuid, member_type FROM plot_members WHERE plot_uuid = ?;";
        String selectFlags = "SELECT plot_uuid, flag_name, flag_value FROM plot_flags WHERE plot_uuid = ?;";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectPlots)) {
            stmt.setString(1, ownerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    int chunkX = rs.getInt("chunk_x");
                    int chunkZ = rs.getInt("chunk_z");
                    String worldName = rs.getString("world_name");
                    Plot plot = new Plot(ownerUuid, chunkX, chunkZ, worldName, uuid);
                    plotMap.put(uuid, plot);
                    plots.add(plot);
                }
            }

            if (!plots.isEmpty()) {
                try (PreparedStatement memStmt = conn.prepareStatement(selectMembers);
                     PreparedStatement flagStmt = conn.prepareStatement(selectFlags)) {
                    for (Plot plot : plots) {
                        memStmt.setString(1, plot.getUUID().toString());
                        try (ResultSet memRs = memStmt.executeQuery()) {
                            while (memRs.next()) {
                                UUID memberUuid = UUID.fromString(memRs.getString("player_uuid"));
                                String type = memRs.getString("member_type");
                                if ("WHITELIST".equalsIgnoreCase(type)) {
                                    plot.whitelistPlayer(memberUuid);
                                } else if ("BLACKLIST".equalsIgnoreCase(type)) {
                                    plot.blacklistPlayer(memberUuid);
                                }
                            }
                        }

                        flagStmt.setString(1, plot.getUUID().toString());
                        try (ResultSet flagRs = flagStmt.executeQuery()) {
                            while (flagRs.next()) {
                                plot.setFlag(flagRs.getString("flag_name"), flagRs.getBoolean("flag_value"));
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to load plots by owner " + ownerUuid + " from SQLite: " + e.getMessage());
        }
        return plots;
    }

    @Override
    public List<Plot> loadByMember(UUID memberUuid) {
        if (memberUuid == null) return List.of();
        List<Plot> plots = new ArrayList<>();
        Map<UUID, Plot> plotMap = new HashMap<>();
        String selectPlots = """
            SELECT p.uuid, p.owner_uuid, p.chunk_x, p.chunk_z, p.world_name 
            FROM plots p 
            INNER JOIN plot_members pm ON p.uuid = pm.plot_uuid 
            WHERE pm.player_uuid = ? AND pm.member_type = 'WHITELIST';
        """;
        String selectMembers = "SELECT plot_uuid, player_uuid, member_type FROM plot_members WHERE plot_uuid = ?;";
        String selectFlags = "SELECT plot_uuid, flag_name, flag_value FROM plot_flags WHERE plot_uuid = ?;";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectPlots)) {
            stmt.setString(1, memberUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
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

            if (!plots.isEmpty()) {
                try (PreparedStatement memStmt = conn.prepareStatement(selectMembers);
                     PreparedStatement flagStmt = conn.prepareStatement(selectFlags)) {
                    for (Plot plot : plots) {
                        memStmt.setString(1, plot.getUUID().toString());
                        try (ResultSet memRs = memStmt.executeQuery()) {
                            while (memRs.next()) {
                                UUID mUuid = UUID.fromString(memRs.getString("player_uuid"));
                                String type = memRs.getString("member_type");
                                if ("WHITELIST".equalsIgnoreCase(type)) {
                                    plot.whitelistPlayer(mUuid);
                                } else if ("BLACKLIST".equalsIgnoreCase(type)) {
                                    plot.blacklistPlayer(mUuid);
                                }
                            }
                        }

                        flagStmt.setString(1, plot.getUUID().toString());
                        try (ResultSet flagRs = flagStmt.executeQuery()) {
                            while (flagRs.next()) {
                                plot.setFlag(flagRs.getString("flag_name"), flagRs.getBoolean("flag_value"));
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to load plots by member " + memberUuid + " from SQLite: " + e.getMessage());
        }
        return plots;
    }

    @Override
    public int count() {
        String countSql = "SELECT COUNT(*) FROM plots;";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(countSql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.severe("Failed to count plots in SQLite: " + e.getMessage());
        }
        return 0;
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

        String deleteFlags = "DELETE FROM plot_flags WHERE plot_uuid = ?;";
        String insertFlag = "INSERT INTO plot_flags (plot_uuid, flag_name, flag_value) VALUES (?, ?, ?);";

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

                try (PreparedStatement stmt = conn.prepareStatement(deleteFlags)) {
                    stmt.setString(1, plot.getUUID().toString());
                    stmt.executeUpdate();
                }

                if (!plot.getFlags().isEmpty()) {
                    try (PreparedStatement stmt = conn.prepareStatement(insertFlag)) {
                        for (Map.Entry<String, Boolean> entry : plot.getFlags().entrySet()) {
                            stmt.setString(1, plot.getUUID().toString());
                            stmt.setString(2, entry.getKey());
                            stmt.setBoolean(3, entry.getValue());
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
