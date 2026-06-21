package pl.kacpermajkowski.ChunkyPlots.protections.entity;

import io.papermc.paper.event.entity.EntityMoveEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;

import java.lang.reflect.Field;
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
            Bukkit.getLogger().info("[Runaway] " + event.getEntity().getType() + " is despawnable, skipping");
            return;
        }

        Plot source = PlotManager.getInstance().getPlot(event.getFrom());
        if (source == null) {
            Bukkit.getLogger().info("[Runaway] source plot is null, skipping");
            return;
        }
        Bukkit.getLogger().info("[Runaway] source plot: " + source.getOwnerName());

        Plot destination = PlotManager.getInstance().getPlot(event.getTo());
        if (ProtectionUtil.canPlotAffectPlot(destination, source)) {
            Bukkit.getLogger().info("[Runaway] destination can affect source, allowing");
            return;
        }
        Bukkit.getLogger().info("[Runaway] destination cannot affect source, checking animal tempt");

        if (nms instanceof Animal animal) {
            Bukkit.getLogger().info("[Runaway] entity is an animal, checking tempter");
            Player tempter = getTemptingPlayer(animal);
            if (tempter != null) {
                Bukkit.getLogger().info("[Runaway] tempter found: " + tempter.getName());
                if (ProtectionUtil.canPlayerAffect(tempter, source)) {
                    Bukkit.getLogger().info("[Runaway] tempter can affect source, allowing");
                    return;
                }
                Bukkit.getLogger().info("[Runaway] tempter cannot affect source, cancelling");
            } else {
                Bukkit.getLogger().info("[Runaway] no tempter found, cancelling");
            }
        } else {
            Bukkit.getLogger().info("[Runaway] entity is not an animal, cancelling");
        }

        event.setCancelled(true);
    }

    private Player getTemptingPlayer(Animal animal) {
        // 1. Try checking the Brain
        try {
            Optional<net.minecraft.world.entity.player.Player> brainTempter =
                    animal.getBrain().getMemory(MemoryModuleType.TEMPTING_PLAYER);

            if (brainTempter.isPresent()) {
                Bukkit.getLogger().info("[Runaway] tempter found via brain memory");
                return (Player) brainTempter.get().getBukkitEntity();
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            Bukkit.getLogger().info("[Runaway] TEMPTING_PLAYER memory unregistered, falling back...");
        }

        // 2. Extract the GoalSelector via Reflection
        net.minecraft.world.entity.ai.goal.GoalSelector goalSelector;
        try {
            goalSelector = (net.minecraft.world.entity.ai.goal.GoalSelector) GOAL_SELECTOR_FIELD.get(animal);
        } catch (IllegalAccessException e) {
            Bukkit.getLogger().warning("[Runaway] Failed to access goalSelector via reflection: " + e.getMessage());
            return null;
        }

        // 3. Fall back to GoalSelector logic
        Bukkit.getLogger().info("[Runaway] checking goal selector");
        Bukkit.getLogger().info("[Runaway] goals: " + goalSelector.getAvailableGoals().stream()
                .map(w -> w.getGoal().getClass().getSimpleName() + "(running=" + w.isRunning() + ")")
                .toList());

        return goalSelector.getAvailableGoals().stream()
                .filter(w -> w.getGoal() instanceof TemptGoal && w.isRunning())
                .map(w -> {
                    Bukkit.getLogger().info("[Runaway] found running TemptGoal, extracting player");
                    try {
                        return (net.minecraft.world.entity.player.Player) TEMPT_GOAL_PLAYER.get(w.getGoal());
                    } catch (IllegalAccessException e) {
                        Bukkit.getLogger().warning("[Runaway] failed to access TemptGoal player field: " + e.getMessage());
                        return null;
                    }
                })
                .filter(p -> p != null)
                .map(p -> {
                    Bukkit.getLogger().info("[Runaway] tempter found via TemptGoal: " + p.getName().getString());
                    return (Player) p.getBukkitEntity();
                })
                .findFirst()
                .orElseGet(() -> {
                    Bukkit.getLogger().info("[Runaway] no running TemptGoal found");
                    return null;
                });
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