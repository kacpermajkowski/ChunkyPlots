package pl.kacpermajkowski.ChunkyPlots.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.annotation.Nullable;

public class PlayerUtil {

    /**
     * Returns an OfflinePlayer object if the player has previously joined the server
     *
     * @param playerName name of a player that has previously joined the sever
     * @return OfflinePlayer object
     * @see OfflinePlayer
     * **/

    @Nullable
    public static OfflinePlayer getOfflinePlayer(String playerName) {
        for(OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            String offlinePlayerName = offlinePlayer.getName();
            if(offlinePlayerName == null){
                throw new IllegalStateException("OfflinePlayer.getName() from Bukkit.getOfflinePlayer() is null. This is most likely a bukkit issue.");
            }
            if(offlinePlayerName.equalsIgnoreCase(playerName)) {
                return offlinePlayer;
            }
        }
        return null;
    }
}
