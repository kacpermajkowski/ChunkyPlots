package pl.kacpermajkowski.ChunkyPlots.protections.block.explosion;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WitherProtection implements Listener {
    private final PlotPermissionChecker checker;
    private final Map<UUID, UUID> witherSummonerUuids = new ConcurrentHashMap<>();

    public WitherProtection(PlotPermissionChecker checker) {
        this.checker = checker;
    }

    public void registerWitherSummoner(UUID witherUuid, UUID summonerUuid) {
        if (witherUuid != null && summonerUuid != null) {
            witherSummonerUuids.put(witherUuid, summonerUuid);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(final EntityExplodeEvent event) {
        Entity exploder = event.getEntity();
        Wither wither = null;
        if (exploder instanceof WitherSkull witherSkull) {
            if (witherSkull.getShooter() instanceof Wither w) {
                wither = w;
            }
        } else if (exploder instanceof Wither w) {
            wither = w;
        }

        if (wither == null) return;

        UUID summonerUuid = witherSummonerUuids.get(wither.getUniqueId());

        // Remove blocks from explosion list that should be protected
        event.blockList().removeIf(block -> {
            Plot plot = checker.getPlot(block);
            if (plot == null) {
                return false; // Wilderness blocks can be destroyed
            }
            if (summonerUuid == null) {
                return true; // Unknown summoner -> Protect claimed block
            }
            return !checker.canOwnerAffect(summonerUuid, plot); // Protect if summoner is unauthorized
        });
    }
}
