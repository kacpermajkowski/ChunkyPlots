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
        return "wyświetla cząsteczkową granicę aktualnego chunku/działki";
    }

    @Override
    public String getSyntax() {
        return "/plot showborder [on|off]";
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

        if (args.length == 0) {
            borderVisualizer.toggle(player);
            return;
        }

        if (args.length == 1) {
            String sub = args[0].toLowerCase();
            if (sub.equals("on") || sub.equals("start") || sub.equals("enable")) {
                borderVisualizer.start(player);
                return;
            } else if (sub.equals("off") || sub.equals("stop") || sub.equals("disable")) {
                borderVisualizer.stop(player);
                new MessageBuilder(Message.BORDER_DISABLED).sendChat(player);
                return;
            }
        }

        new MessageBuilder().subcommand(this).message(Message.COMMAND_USAGE).sendChat(player);
    }

    @Override
    public List<String> getTabCompletion(Player player, String[] args) {
        if (args.length == 1) {
            String query = args[0].toLowerCase();
            return List.of("on", "off").stream()
                    .filter(s -> s.startsWith(query))
                    .toList();
        }
        return List.of();
    }
}
