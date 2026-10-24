package pl.kacpermajkowski.ChunkyPlots.protections;

import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles sending rate-limited feedback messages to players who attempt to break protections.
 */
public class ProtectionNotifier {
    public static final long DEFAULT_COOLDOWN_MILLIS = 1200L;
    private static long cooldownMillis = DEFAULT_COOLDOWN_MILLIS;

    private static final Map<UUID, Long> LAST_NOTIFICATION_TIME = new ConcurrentHashMap<>();
    private static Message lastSentMessage;
    private static UUID lastNotifiedPlayer;

    public static boolean notify(Player player, Message message, Plot plot) {
        if (player == null || message == null) return false;

        UUID uuid = null;
        try {
            uuid = player.getUniqueId();
        } catch (Throwable ignored) {
        }

        long now = System.currentTimeMillis();
        if (uuid != null) {
            Long last = LAST_NOTIFICATION_TIME.get(uuid);
            if (last != null && (now - last) < cooldownMillis) {
                return false;
            }
            LAST_NOTIFICATION_TIME.put(uuid, now);
        }
        lastSentMessage = message;
        lastNotifiedPlayer = uuid;

        try {
            MessageBuilder builder = new MessageBuilder(message);
            if (plot != null) {
                builder.plot(plot);
            } else {
                builder.plotOwnerName("Innego gracza");
            }
            builder.user(player);
            builder.sendAll(player);
        } catch (Throwable ignored) {
            // Defensive against un-mocked players or offline/headless test invocations
        }
        return true;
    }

    public static Message getLastSentMessage() {
        return lastSentMessage;
    }

    public static UUID getLastNotifiedPlayer() {
        return lastNotifiedPlayer;
    }

    public static void setCooldownMillis(long cooldown) {
        cooldownMillis = cooldown;
    }

    public static void resetCooldown() {
        cooldownMillis = DEFAULT_COOLDOWN_MILLIS;
    }

    public static void clear(UUID playerId) {
        if (playerId != null) {
            LAST_NOTIFICATION_TIME.remove(playerId);
        }
    }

    public static void clearAll() {
        LAST_NOTIFICATION_TIME.clear();
        lastSentMessage = null;
        lastNotifiedPlayer = null;
    }
}
