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
                throw new IllegalStateException("OfflinePlayer.getName() from Bukkit.getOfflinePlayer() is null. This is most likely a Bukkit issue.");
            }
            if(offlinePlayerName.equalsIgnoreCase(playerName)) {
                return offlinePlayer;
            }
        }
        return null;
    }

    public static Player findNearestPlayer(Location loc, int radius){
        if(loc == null) return null;
        Collection<Entity> nearbyEntities = loc.getWorld().getNearbyEntities(loc, radius, radius, radius);

        Player closestPlayer = null;
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player player)) continue;

            if (closestPlayer == null) closestPlayer = player;
            else {
                double closestPlayerDistance = closestPlayer.getLocation().distance(loc);
                double playerDistance = player.getLocation().distance(loc);
                closestPlayer = closestPlayerDistance < playerDistance ? closestPlayer : player;
            }
        }

        return closestPlayer;
    }
}
