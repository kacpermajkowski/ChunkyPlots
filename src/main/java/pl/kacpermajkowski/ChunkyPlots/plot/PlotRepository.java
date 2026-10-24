package pl.kacpermajkowski.ChunkyPlots.plot;

import java.util.List;

public interface PlotRepository {
    List<Plot> loadAll();
    void save(Plot plot);
    void delete(Plot plot);
}
