package pl.kacpermajkowski.ChunkyPlots;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.lang.reflect.Proxy;
import java.util.Objects;

public class TestChunkFactory {

    public static Chunk createChunk(int x, int z, World world) {
        return (Chunk) Proxy.newProxyInstance(
                Chunk.class.getClassLoader(),
                new Class<?>[]{Chunk.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    switch (name) {
                        case "getX" -> {
                            return x;
                        }
                        case "getZ" -> {
                            return z;
                        }
                        case "getWorld" -> {
                            return world;
                        }
                        case "equals" -> {
                            if (args != null && args.length > 0 && args[0] instanceof Chunk other) {
                                return other.getX() == x && other.getZ() == z
                                        && Objects.equals(other.getWorld() != null ? other.getWorld().getName() : null,
                                                          world != null ? world.getName() : null);
                            }
                            return false;
                        }
                        case "hashCode" -> {
                            return Objects.hash(x, z, world != null ? world.getName() : null);
                        }
                        case "toString" -> {
                            return "TestChunk[x=" + x + ", z=" + z + ", world=" + (world != null ? world.getName() : "null") + "]";
                        }
                        default -> {
                            return null;
                        }
                    }
                }
        );
    }
}
