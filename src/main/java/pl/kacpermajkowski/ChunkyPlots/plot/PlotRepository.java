package pl.kacpermajkowski.ChunkyPlots.plot;

import java.util.List;
import java.util.UUID;

public interface PlotRepository {
    List<Plot> loadAll();
    void save(Plot plot);
    void delete(Plot plot);

    default Plot loadByChunk(int chunkX, int chunkZ, String worldName) {
        return null;
    }

    default Plot loadByUuid(UUID uuid) {
        return null;
    }

    default List<Plot> loadByOwner(UUID ownerUuid) {
        return List.of();
    }

    default int count() {
        return loadAll().size();
    }
}
