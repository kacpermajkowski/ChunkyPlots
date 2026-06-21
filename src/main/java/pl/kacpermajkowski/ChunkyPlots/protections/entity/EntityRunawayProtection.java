package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import io.papermc.paper.event.entity.EntityMoveEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

import java.util.Objects;
import java.util.Optional;

public class EntityRunawayProtection implements Listener {
    private static final java.lang.reflect.Field GOAL_SELECTOR_FIELD;
    private static final java.lang.reflect.Field TEMPT_GOAL_PLAYER;

    static {
        try {
            // 1. GoalSelector Reflection
            GOAL_SELECTOR_FIELD = net.minecraft.world.entity.Mob.class.getDeclaredField("goalSelector");
            GOAL_SELECTOR_FIELD.setAccessible(true);

            // 2. TemptGoal Player Reflection
            TEMPT_GOAL_PLAYER = net.minecraft.world.entity.ai.goal.TemptGoal.class.getDeclaredField("player");
            TEMPT_GOAL_PLAYER.setAccessible(true);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException("[Runaway] Failed to initialize NMS reflection fields", e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityMove(EntityMoveEvent event) {
        if (!isAMob(event.getEntity())) {
            return;
        }

        Mob nms = convertEntityToNmsMob(event.getEntity());

        if (isNmsMobDespawnable(nms)) {
            return;
        }

        Plot source = PlotManager.getInstance().getPlot(event.getFrom());
        if (source == null) {
            return;
        }

        Plot destination = PlotManager.getInstance().getPlot(event.getTo());
        if (ProtectionUtil.canPlotAffectPlot(destination, source)) {
            return;
        }

        if (nms instanceof Animal animal) {
            Player tempter = getTemptingPlayer(animal);
            if (tempter != null) {
                if (ProtectionUtil.canPlayerAffect(tempter, source)) {
                    return;
                }
            }
        }

        event.setCancelled(true);
    }

    private Player getTemptingPlayer(Animal animal) {
        // 1. Try checking the Brain
        try {
            Optional<net.minecraft.world.entity.player.Player> brainTempter =
                    animal.getBrain().getMemory(MemoryModuleType.TEMPTING_PLAYER);

            if (brainTempter.isPresent()) {
                return (Player) brainTempter.get().getBukkitEntity();
            }
        } catch (IllegalStateException | IllegalArgumentException _) {
        }

        // 2. Extract the GoalSelector via Reflection
        net.minecraft.world.entity.ai.goal.GoalSelector goalSelector;
        try {
            goalSelector = (net.minecraft.world.entity.ai.goal.GoalSelector) GOAL_SELECTOR_FIELD.get(animal);
        } catch (IllegalAccessException e) {
            return null;
        }

        // 3. Fall back to GoalSelector logic
        return goalSelector.getAvailableGoals().stream()
                .filter(w -> w.getGoal() instanceof TemptGoal && w.isRunning())
                .map(w -> {
                    try {
                        return (net.minecraft.world.entity.player.Player) TEMPT_GOAL_PLAYER.get(w.getGoal());
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(p -> (Player) p.getBukkitEntity())
                .findFirst()
                .orElse(null);
    }

    private boolean isAMob(Entity entity) {
        return entity instanceof org.bukkit.entity.Mob;
    }

    private Mob convertEntityToNmsMob(Entity entity) {
        return ((CraftMob) entity).getHandle();
    }

    private boolean isNmsMobDespawnable(Mob nms) {
        return !nms.persistenceRequired &&
                !nms.requiresCustomPersistence() &&
                !nms.getType().getCategory().isPersistent();
    }
}