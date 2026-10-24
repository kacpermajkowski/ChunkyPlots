package pl.kacpermajkowski.ChunkyPlots.plot.border;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages chunk border particle visualization for players.
 * Particles are client-specific (only visible to the command sender)
 * and dynamically reflect the status of the plot the player is currently standing on.
 */
public class PlotBorderVisualizer implements Listener {
    private final Plugin plugin;
    private final PlotManager plotManager;
    private final Map<UUID, BukkitTask> activeTasks = new ConcurrentHashMap<>();

    // Visualization interval: every 10 ticks (0.5 seconds)
    private static final long TASK_PERIOD_TICKS = 10L;
    // Default auto-expire duration: 60 seconds (120 executions at 10-tick interval)
    private static final int MAX_ITERATIONS = 120;

    public PlotBorderVisualizer(Plugin plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    /**
     * Checks if a player currently has active border visualization.
     */
    public boolean isVisualizing(Player player) {
        return player != null && isVisualizing(player.getUniqueId());
    }

    public boolean isVisualizing(UUID playerUuid) {
        return activeTasks.containsKey(playerUuid);
    }

    /**
     * Toggles border visualization for a player.
     */
    public void toggle(Player player) {
        if (player == null) return;
        if (isVisualizing(player)) {
            stop(player);
            new MessageBuilder(Message.BORDER_DISABLED).sendChat(player);
        } else {
            start(player);
        }
    }

    /**
     * Starts the repeating border visualization task for a player.
     */
    public void start(Player player) {
        if (player == null) return;

        // Cancel previous task if one exists
        stop(player.getUniqueId());

        // Send feedback message and color legend
        sendStartMessage(player);

        if (plugin != null && plugin.isEnabled()) {
            AtomicInteger remainingIterations = new AtomicInteger(MAX_ITERATIONS);

            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (!player.isOnline()) {
                    stop(player.getUniqueId());
                    return;
                }

                if (remainingIterations.decrementAndGet() <= 0) {
                    stop(player.getUniqueId());
                    new MessageBuilder(Message.BORDER_EXPIRED).sendChat(player);
                    return;
                }

                renderBorder(player);
            }, 0L, TASK_PERIOD_TICKS);

            activeTasks.put(player.getUniqueId(), task);
        } else {
            // Fallback for tests or synchronous rendering
            renderBorder(player);
        }
    }

    /**
     * Stops border visualization for a player.
     */
    public void stop(Player player) {
        if (player == null) return;
        stop(player.getUniqueId());
    }

    public void stop(UUID playerUuid) {
        BukkitTask task = activeTasks.remove(playerUuid);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Stops all active border visualizations (called on plugin disable).
     */
    public void stopAll() {
        for (BukkitTask task : activeTasks.values()) {
            try {
                task.cancel();
            } catch (Exception ignored) {}
        }
        activeTasks.clear();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stop(event.getPlayer().getUniqueId());
    }

    /**
     * Determines relationship between player and the plot player is standing on.
     */
    public PlotRelation determineRelation(Player player, Plot plot) {
        if (plot == null) {
            return PlotRelation.WILDERNESS;
        }
        if (plot.isPlayerOwner(player)) {
            return PlotRelation.OWN_PLOT;
        }
        if (plot.isPlayerWhitelisted(player)) {
            return PlotRelation.MEMBER_PLOT;
        }
        return PlotRelation.THIRD_PARTY_PLOT;
    }

    /**
     * Renders client-only particle border of the chunk the player is currently on.
     */
    public void renderBorder(Player player) {
        if (player == null || !player.isOnline()) return;

        Location playerLoc = player.getLocation();
        World world = playerLoc.getWorld();
        if (world == null) return;

        Chunk chunk = playerLoc.getChunk();
        if (chunk == null) return;

        Plot plot = plotManager != null ? plotManager.getPlot(chunk) : null;
        PlotRelation relation = determineRelation(player, plot);

        Particle.DustOptions dust = new Particle.DustOptions(relation.getColor(), 1.25f);

        double minX = chunk.getX() << 4;
        double maxX = minX + 16.0;
        double minZ = chunk.getZ() << 4;
        double maxZ = minZ + 16.0;
        double playerY = playerLoc.getY();

        double step = 0.5;

        // North edge: from (minX, minZ) to (maxX, minZ)
        for (double x = minX; x <= maxX; x += step) {
            spawnBorderParticle(player, world, x, minZ, playerY, dust);
        }
        // East edge: from (maxX, minZ) to (maxX, maxZ)
        for (double z = minZ; z <= maxZ; z += step) {
            spawnBorderParticle(player, world, maxX, z, playerY, dust);
        }
        // South edge: from (minX, maxZ) to (maxX, maxZ)
        for (double x = minX; x <= maxX; x += step) {
            spawnBorderParticle(player, world, x, maxZ, playerY, dust);
        }
        // West edge: from (minX, minZ) to (minX, maxZ)
        for (double z = minZ; z <= maxZ; z += step) {
            spawnBorderParticle(player, world, minX, z, playerY, dust);
        }

        // Corner vertical pillars for immediate visual recognition
        spawnCornerPillar(player, world, minX, minZ, playerY, dust);
        spawnCornerPillar(player, world, maxX, minZ, playerY, dust);
        spawnCornerPillar(player, world, maxX, maxZ, playerY, dust);
        spawnCornerPillar(player, world, minX, maxZ, playerY, dust);
    }

    private void spawnBorderParticle(Player player, World world, double x, double z, double playerY, Particle.DustOptions dust) {
        double y = getGroundY(world, x, z, playerY);
        player.spawnParticle(Particle.DUST, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, dust);
    }

    private void spawnCornerPillar(Player player, World world, double x, double z, double playerY, Particle.DustOptions dust) {
        double baseY = getGroundY(world, x, z, playerY);
        for (double offset = 0.5; offset <= 2.0; offset += 0.5) {
            player.spawnParticle(Particle.DUST, x, baseY + offset, z, 1, 0.0, 0.0, 0.0, 0.0, dust);
        }
    }

    /**
     * Determines ground Y near player level so particles follow terrain elevations
     * without climbing onto ceilings or jumping onto mountains.
     */
    public double getGroundY(World world, double x, double z, double playerY) {
        try {
            int blockX = (int) Math.floor(x);
            int blockZ = (int) Math.floor(z);

            if (!world.isChunkLoaded(blockX >> 4, blockZ >> 4)) {
                return playerY + 0.1;
            }

            int playerBlockY = (int) Math.floor(playerY);
            for (int checkY = playerBlockY + 2; checkY >= playerBlockY - 4; checkY--) {
                if (checkY < world.getMinHeight() || checkY >= world.getMaxHeight() - 1) {
                    continue;
                }
                Block block = world.getBlockAt(blockX, checkY, blockZ);
                if (!block.isPassable() || block.isLiquid()) {
                    Block above = world.getBlockAt(blockX, checkY + 1, blockZ);
                    if (above.isPassable() && !above.isLiquid()) {
                        return checkY + 1.05;
                    }
                }
            }
        } catch (Exception ignored) {}

        return playerY + 0.1;
    }

    private void sendStartMessage(Player player) {
        player.sendMessage(TextUtil.toComponent("&8&m----------------------------------------"));
        new MessageBuilder(Message.BORDER_ENABLED).sendChat(player);
        player.sendMessage(TextUtil.toComponent("&a■ &fTwoja działka &7(Twoja własność / Own Plot)"));
        player.sendMessage(TextUtil.toComponent("&b■ &fDziałka członka &7(Jesteś dodany / Member Plot)"));
        player.sendMessage(TextUtil.toComponent("&c■ &fObca działka &7(Zajęta przez innego gracza / Third-Party)"));
        player.sendMessage(TextUtil.toComponent("&e■ &fDzicz &7(Nieobjęta działką / Wilderness)"));
        player.sendMessage(TextUtil.toComponent("&7Granica jest widoczna &ntylko dla Ciebie&7. Wpisz &f/plot showborder &7aby wyłączyć."));
        player.sendMessage(TextUtil.toComponent("&8&m----------------------------------------"));
    }
}
