package pl.kacpermajkowski.ChunkyPlots.utils;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlayerUtil {

    /**
     * Returns an OfflinePlayer object if the player has previously joined the server or null
     *
     * @param playerName name of a player that has previously joined the sever
     * @return OfflinePlayer object
     * @see OfflinePlayer
     * **/
    public static OfflinePlayer getOfflinePlayer(final String playerName) {
        if (playerName == null) return null;
        for (final OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            final String offlinePlayerName = offlinePlayer.getName();
            if (offlinePlayerName == null) {
                continue;
            }
            if (offlinePlayerName.equalsIgnoreCase(playerName)) {
                return offlinePlayer;
            }
        }
        return null;
    }

    public static List<Player> getPlayersInChunk(Chunk chunk) {
        if (chunk == null || chunk.getWorld() == null) return List.of();
        List<Player> result = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Location loc = player.getLocation();
            if (loc.getWorld() != null
                    && loc.getWorld().equals(chunk.getWorld())
                    && (loc.getBlockX() >> 4) == chunk.getX()
                    && (loc.getBlockZ() >> 4) == chunk.getZ()) {
                result.add(player);
            }
        }

        return result;
    }

    public static List<Player> getPlayersInPlot(Plot plot) {
        if (plot == null) return List.of();
        List<Player> result = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plot.isLocationInside(player.getLocation())) {
                result.add(player);
            }
        }
        return result;
    }
}
