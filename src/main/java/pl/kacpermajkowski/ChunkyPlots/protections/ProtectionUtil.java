package pl.kacpermajkowski.ChunkyPlots.protections;

import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;

public abstract class ProtectionUtil {
    public static boolean canPlayerAffectPlot(Player player, Plot plot){
        if(plot == null) return true;
        else if(player == null) return false;
        else return plot.isPlayerOwner(player) || plot.isPlayerWhitelisted(player);
    }
}
