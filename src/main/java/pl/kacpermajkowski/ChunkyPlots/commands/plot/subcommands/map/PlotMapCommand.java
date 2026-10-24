package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.map;

import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.map.PlotMapManager;

import java.util.List;

/**
 * Subcommand /plot map [chunkX] [chunkZ] opening the interactive chunk map GUI.
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
        return "/plot map [chunkX] [chunkZ]";
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

        mapManager.openMap(player);
    }

    @Override
    public List<String> getTabCompletion(Player player, String[] args) {
        if (player == null || player.getLocation() == null) return List.of();
        if (args.length == 1) {
            return List.of(String.valueOf(player.getLocation().getChunk().getX()));
        }
        if (args.length == 2) {
            return List.of(String.valueOf(player.getLocation().getChunk().getZ()));
        }
        return List.of();
    }
}
