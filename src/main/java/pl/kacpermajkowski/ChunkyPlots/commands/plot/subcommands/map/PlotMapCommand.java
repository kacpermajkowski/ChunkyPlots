package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.map;

import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.map.PlotMapManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand /plot map [chunkX] [chunkZ] or /plot map [plotId] opening the interactive chunk map GUI.
 */
public class PlotMapCommand implements PlotSubcommand {
    private final PlotMapManager mapManager;

    public PlotMapCommand(PlotMapManager mapManager) {
        this.mapManager = mapManager;
    }

    @Override
    public String getName() {
        return "map";
    }

    @Override
    public String getDescription() {
        return "otwiera graficzną mapę chunków i działek w okolicy";
    }

    @Override
    public String getSyntax() {
        return "/plot map [chunkX] [chunkZ] lub /plot map [id/kod]";
    }

    @Override
    public String getPermission() {
        return "chunkyplots.player";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (mapManager == null) {
            new MessageBuilder(Message.ERROR_UNSPECIFIED).sendChat(player);
            return;
        }

        if (args.length >= 2) {
            try {
                int cx = Integer.parseInt(args[0]);
                int cz = Integer.parseInt(args[1]);
                mapManager.openMap(player, cx, cz);
                return;
            } catch (NumberFormatException ignored) {}
        }

        if (args.length == 1 && mapManager.getPlotManager() != null) {
            Plot plot = mapManager.getPlotManager().getPlot(args[0]);
            if (plot != null) {
                mapManager.openMap(player, plot);
                return;
            }
        }

        mapManager.openMap(player);
    }

    @Override
    public List<String> getTabCompletion(Player player, String[] args) {
        if (player == null) return List.of();
        PlotManager pm = mapManager != null ? mapManager.getPlotManager() : null;

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (player.getLocation() != null && player.getLocation().getChunk() != null) {
                completions.add(String.valueOf(player.getLocation().getChunk().getX()));
            }
            if (pm != null) {
                for (Plot plot : pm.getAccessiblePlots(player.getUniqueId())) {
                    if (plot != null) {
                        if (plot.getID() != null) completions.add(plot.getID());
                        if (plot.getMnemonicId() != null) completions.add(plot.getMnemonicId());
                    }
                }
            }
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && player.getLocation() != null && player.getLocation().getChunk() != null) {
            return List.of(String.valueOf(player.getLocation().getChunk().getZ()));
        }
        return List.of();
    }
}
