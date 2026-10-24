package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.gui;

import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.gui.PlotGuiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand /plot gui [plotId] opening the main plot management GUI.
 */
public class PlotGuiCommand implements PlotSubcommand {
    private final PlotGuiManager guiManager;
    private final PlotManager plotManager;

    public PlotGuiCommand(PlotGuiManager guiManager, PlotManager plotManager) {
        this.guiManager = guiManager;
        this.plotManager = plotManager;
    }

    @Override
    public String getName() {
        return "gui";
    }

    @Override
    public String getDescription() {
        return "otwiera graficzny interfejs zarządzania działką";
    }

    @Override
    public String getSyntax() {
        return "/plot gui [id]";
    }

    @Override
    public String getPermission() {
        return "chunkyplots.player";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (guiManager == null) {
            new MessageBuilder(Message.ERROR_UNSPECIFIED).sendChat(player);
            return;
        }

        if (args.length >= 1) {
            String plotId = args[0];
            Plot plot = plotManager.getPlot(plotId);
            if (plot == null) {
                new MessageBuilder(Message.NULL_PLOT).sendChat(player);
                return;
            }
            guiManager.openPlotDashboard(player, plot);
            return;
        }

        guiManager.openMainGui(player);
    }

    @Override
    public List<String> getTabCompletion(Player player, String[] args) {
        if (args.length == 1 && plotManager != null && player != null) {
            List<String> completions = new ArrayList<>();
            for (Plot plot : plotManager.getPlotsByOwner(player.getUniqueId())) {
                if (plot != null) {
                    if (plot.getID() != null) completions.add(plot.getID());
                    if (plot.getMnemonicId() != null) completions.add(plot.getMnemonicId());
                }
            }
            return completions.stream()
                    .filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
