package pl.kacpermajkowski.ChunkyPlots.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
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
    @Nullable
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
}
