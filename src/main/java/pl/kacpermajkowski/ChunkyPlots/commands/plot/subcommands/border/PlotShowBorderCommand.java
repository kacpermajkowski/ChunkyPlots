package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.border;

import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.border.PlotBorderVisualizer;

import java.util.List;

public class PlotShowBorderCommand implements PlotSubcommand {
    private final PlotBorderVisualizer borderVisualizer;

    public PlotShowBorderCommand(PlotBorderVisualizer borderVisualizer) {
        this.borderVisualizer = borderVisualizer;
    }

    @Override
    public String getName() {
        return "showborder";
    }

    @Override
    public String getDescription() {
        return "przełącza widoczność cząsteczkowej granicy aktualnego chunku/działki";
    }

    @Override
    public String getSyntax() {
        return "/plot showborder";
    }

    @Override
    public String getPermission() {
        return "chunkyplots.player";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (borderVisualizer == null) {
            new MessageBuilder(Message.ERROR_UNSPECIFIED).sendChat(player);
            return;
        }

        // Pure toggle: /plot showborder toggles border visibility
        borderVisualizer.toggle(player);
    }

    @Override
    public List<String> getTabCompletion(Player player, String[] args) {
        return List.of();
    }
}
