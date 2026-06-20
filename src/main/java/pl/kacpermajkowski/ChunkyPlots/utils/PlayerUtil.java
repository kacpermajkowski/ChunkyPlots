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
        for(final OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            final String offlinePlayerName = offlinePlayer.getName();
            if(offlinePlayerName == null){
                throw new IllegalStateException(
                        "OfflinePlayer.getName() from Bukkit.getOfflinePlayer() is null. " +
                        "This is most likely a Bukkit issue. Try a newer/an older server build.");
            }
            if(offlinePlayerName.equalsIgnoreCase(playerName)) {
                return offlinePlayer;
            }
        }
        return null;
    }

    public static List<Player> getPlayersInChunk(Chunk chunk) {
        if(chunk == null) return List.of();
        List<Player> result = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().getChunk().equals(chunk)) {
                result.add(player);
            }
        }

        return result;
    }

    public static List<Player> getPlayersInPlot(Plot plot){
        if(plot == null) return List.of();
        return getPlayersInChunk(plot.getChunk());
    }
}
