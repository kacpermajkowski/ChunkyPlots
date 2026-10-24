package pl.kacpermajkowski.ChunkyPlots.plot;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class YamlPlotRepository implements PlotRepository {
    private final File plotDirectory;
    private final Logger logger;

    public YamlPlotRepository(File dataFolder, Logger logger) {
        this.plotDirectory = new File(dataFolder, "plots");
        this.logger = logger;
        if (!plotDirectory.exists()) {
            plotDirectory.mkdirs();
        }
    }

    @Override
    public List<Plot> loadAll() {
        List<Plot> plots = new ArrayList<>();
        File[] files = plotDirectory.listFiles();
        if (files == null) {
            return plots;
        }

        for (File file : files) {
            if (file != null && !file.isDirectory()) {
                Plot plot = loadPlot(file);
                if (plot != null) {
                    plots.add(plot);
                }
            }
        }
        return plots;
    }

    private Plot loadPlot(File file) {
        try {
            FileConfiguration plotData = YamlConfiguration.loadConfiguration(file);
            String ownerUUIDstring = plotData.getString("owner.uuid");
            if (ownerUUIDstring == null) {
                logger.warning("Missing owner.uuid in plot file: " + file.getName());
                return null;
            }

            UUID ownerUUID = UUID.fromString(ownerUUIDstring);
            int chunkX = plotData.getInt("chunk.x");
            int chunkZ = plotData.getInt("chunk.z");
            String worldName = plotData.getString("world-name");
            List<String> whitelist = plotData.getStringList("whitelist");
            List<String> blacklist = plotData.getStringList("blacklist");

            Plot plot = new Plot(ownerUUID, chunkX, chunkZ, worldName, UUID.fromString(file.getName()));
            for (String playerUUIDString : blacklist) {
                plot.blacklistPlayer(UUID.fromString(playerUUIDString));
            }
            for (String playerUUIDString : whitelist) {
                plot.whitelistPlayer(UUID.fromString(playerUUIDString));
            }

            return plot;
        } catch (Exception e) {
            logger.warning("Could not load plot file: " + file.getName() + " - " + e.getMessage());
            return null;
        }
    }

    @Override
    public void save(Plot plot) {
        try {
            File file = new File(plotDirectory, plot.getUUID().toString());
            FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
            fc.set("owner.uuid", plot.getOwnerUUID().toString());
            fc.set("chunk.x", plot.getChunkX());
            fc.set("chunk.z", plot.getChunkZ());
            fc.set("world-name", plot.getWorldName());
            fc.set("whitelist", plot.getWhitelist().stream().map(UUID::toString).toList());
            fc.set("blacklist", plot.getBlacklist().stream().map(UUID::toString).toList());
            fc.save(file);
        } catch (IOException e) {
            logger.severe("Plot '" + plot.getUUID() + "' save failed: " + e.getMessage());
        }
    }

    @Override
    public void delete(Plot plot) {
        File file = new File(plotDirectory, plot.getUUID().toString());
        if (file.exists()) {
            file.delete();
        }
    }
}
