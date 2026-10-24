package pl.kacpermajkowski.ChunkyPlots.plot.mnemonic;

import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * Bijective mnemonic encoder/decoder for chunk coordinates and world aliases.
 * Maps coordinates to words from an ordered 2000-word dictionary.
 */
public class PlotMnemonic {
    public static final int LOW_MIN = -1000;
    public static final int LOW_MAX = 999;
    public static final int LOW_SPAN = 2000;

    public static final int MID_MIN = -2000000;
    public static final int MID_MAX = 1999999;
    public static final int MID_SPAN = 4000000;

    public static final long HIGH_OFFSET = 4000000000L;

    private static volatile MnemonicDictionary defaultDictionary;

    public record MnemonicCoordinates(int chunkX, int chunkZ, String worldName) {}

    public static void init(MnemonicDictionary dictionary) {
        defaultDictionary = dictionary;
    }

    public static MnemonicDictionary getDictionary() {
        if (defaultDictionary == null) {
            synchronized (PlotMnemonic.class) {
                if (defaultDictionary == null) {
                    try (InputStream in = PlotMnemonic.class.getResourceAsStream("/dictionary.txt")) {
                        if (in != null) {
                            defaultDictionary = MnemonicDictionary.loadFromStream(in);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        return defaultDictionary;
    }

    public static String format(Plot plot) {
        if (plot == null) return "";
        return format(plot.getChunkX(), plot.getChunkZ(), plot.getWorldName());
    }

    public static String format(int chunkX, int chunkZ, String worldName) {
        return format(chunkX, chunkZ, worldName, Config.getInstance(), getDictionary());
    }

    public static String format(int chunkX, int chunkZ, String worldName, Config config, MnemonicDictionary dict) {
        if (dict == null) {
            dict = getDictionary();
        }
        if (dict == null || dict.size() < LOW_SPAN) {
            // Fallback if dictionary unavailable
            return chunkX + ";" + chunkZ;
        }

        String alias = "";
        if (config != null && worldName != null) {
            String resolvedAlias = config.getWorldAlias(worldName);
            if (resolvedAlias != null) {
                alias = resolvedAlias.trim();
            }
        }

        String coords;
        if (chunkX >= LOW_MIN && chunkX <= LOW_MAX && chunkZ >= LOW_MIN && chunkZ <= LOW_MAX) {
            // 2 words: 1 word for X, 1 word for Z
            String wX = dict.getWord(chunkX - LOW_MIN);
            String wZ = dict.getWord(chunkZ - LOW_MIN);
            coords = wX + "-" + wZ;
        } else if (chunkX >= MID_MIN && chunkX <= MID_MAX && chunkZ >= MID_MIN && chunkZ <= MID_MAX) {
            // 4 words: 2 words for X, 2 words for Z
            int offsetX = chunkX - MID_MIN;
            int offsetZ = chunkZ - MID_MIN;
            String wX1 = dict.getWord(offsetX / dict.size());
            String wX2 = dict.getWord(offsetX % dict.size());
            String wZ1 = dict.getWord(offsetZ / dict.size());
            String wZ2 = dict.getWord(offsetZ % dict.size());
            coords = wX1 + "-" + wX2 + "-" + wZ1 + "-" + wZ2;
        } else {
            // 6 words: 3 words for X, 3 words for Z for arbitrarily large coordinates
            long base = dict.size();
            long base2 = base * base;
            long offsetX = (long) chunkX + HIGH_OFFSET;
            long offsetZ = (long) chunkZ + HIGH_OFFSET;

            String wX1 = dict.getWord((int) (offsetX / base2));
            String wX2 = dict.getWord((int) ((offsetX % base2) / base));
            String wX3 = dict.getWord((int) (offsetX % base));

            String wZ1 = dict.getWord((int) (offsetZ / base2));
            String wZ2 = dict.getWord((int) ((offsetZ % base2) / base));
            String wZ3 = dict.getWord((int) (offsetZ % base));

            coords = wX1 + "-" + wX2 + "-" + wX3 + "-" + wZ1 + "-" + wZ2 + "-" + wZ3;
        }

        if (!alias.isEmpty()) {
            return alias + "-" + coords;
        }
        return coords;
    }

    public static MnemonicCoordinates parse(String mnemonic) {
        return parse(mnemonic, Config.getInstance(), getDictionary());
    }

    public static MnemonicCoordinates parse(String mnemonic, Config config, MnemonicDictionary dict) {
        if (mnemonic == null || mnemonic.isBlank()) return null;
        if (dict == null) dict = getDictionary();
        if (dict == null || dict.size() < LOW_SPAN) return null;

        String[] parts = mnemonic.trim().toLowerCase().split("-");
        if (parts.length < 2) return null;

        String alias = "";
        String[] coordWords;

        // If odd token count, first token is the world alias prefix
        if (parts.length % 2 == 1) {
            alias = parts[0];
            coordWords = Arrays.copyOfRange(parts, 1, parts.length);
        } else {
            coordWords = parts;
        }

        // Validate all coordinate tokens exist in dictionary
        for (String word : coordWords) {
            if (!dict.containsWord(word)) {
                return null;
            }
        }

        int half = coordWords.length / 2;
        int chunkX;
        int chunkZ;

        if (half == 1) {
            int idxX = dict.getIndex(coordWords[0]);
            int idxZ = dict.getIndex(coordWords[1]);
            chunkX = idxX + LOW_MIN;
            chunkZ = idxZ + LOW_MIN;
        } else if (half == 2) {
            int base = dict.size();
            int idxX1 = dict.getIndex(coordWords[0]);
            int idxX2 = dict.getIndex(coordWords[1]);
            int idxZ1 = dict.getIndex(coordWords[2]);
            int idxZ2 = dict.getIndex(coordWords[3]);

            chunkX = (idxX1 * base + idxX2) + MID_MIN;
            chunkZ = (idxZ1 * base + idxZ2) + MID_MIN;
        } else if (half == 3) {
            long base = dict.size();
            long base2 = base * base;
            long idxX1 = dict.getIndex(coordWords[0]);
            long idxX2 = dict.getIndex(coordWords[1]);
            long idxX3 = dict.getIndex(coordWords[2]);
            long idxZ1 = dict.getIndex(coordWords[3]);
            long idxZ2 = dict.getIndex(coordWords[4]);
            long idxZ3 = dict.getIndex(coordWords[5]);

            long valX = idxX1 * base2 + idxX2 * base + idxX3;
            long valZ = idxZ1 * base2 + idxZ2 * base + idxZ3;

            chunkX = (int) (valX - HIGH_OFFSET);
            chunkZ = (int) (valZ - HIGH_OFFSET);
        } else {
            return null;
        }

        String worldName = "world";
        if (config != null) {
            if (!alias.isEmpty()) {
                String resolved = config.getWorldByAlias(alias);
                if (resolved == null) {
                    return null;
                }
                worldName = resolved;
            } else {
                String def = config.getWorldByAlias("");
                if (def != null) {
                    worldName = def;
                }
            }
        } else if (!alias.isEmpty()) {
            worldName = alias;
        }

        return new MnemonicCoordinates(chunkX, chunkZ, worldName);
    }

    public static boolean isMnemonic(String str) {
        return parse(str) != null;
    }
}
