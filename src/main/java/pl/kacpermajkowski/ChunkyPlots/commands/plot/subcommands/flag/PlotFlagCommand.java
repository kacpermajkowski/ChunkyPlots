package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.flag;

import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotSubcommand;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.messages.TextUtil;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.flag.PlotFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlotFlagCommand implements PlotSubcommand {
    private final PlotManager plotManager;
    private static final List<String> BASE_SUBCOMMANDS = List.of("list", "info", "set", "reset");
    private static final List<String> BOOLEAN_VALUES = List.of("true", "false", "toggle");

    public PlotFlagCommand(PlotManager plotManager) {
        this.plotManager = plotManager;
    }

    @Override
    public String getName() {
        return "flag";
    }

    @Override
    public List<String> getAliases() {
        return List.of("flags", "flaga", "flagi");
    }

    @Override
    public String getDescription() {
        return "pozwala zarządzać flagami ochrony na działce";
    }

    @Override
    public String getSyntax() {
        return "/plot flag [list|set|info|reset|<flaga>] [wartość]";
    }

    @Override
    public String getPermission() {
        return "chunkyplots.player";
    }

    @Override
    public void execute(Player player, String[] args) {
        MessageBuilder messageBuilder = new MessageBuilder().subcommand(this);

        Plot plot = plotManager.getPlot(player.getLocation());
        if (plot == null) {
            messageBuilder.message(Message.NULL_PLOT).sendChat(player);
            return;
        }
        if (!plot.isPlayerOwner(player)) {
            messageBuilder.message(Message.NOT_OWNER).sendChat(player);
            return;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            sendFlagList(player, plot);
            return;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("info") || sub.equals("get")) {
            if (args.length < 2) {
                messageBuilder.message(Message.COMMAND_USAGE).sendChat(player);
                return;
            }
            PlotFlag flag = PlotFlag.getByName(args[1]);
            if (flag == null) {
                new MessageBuilder(Message.NULL_FLAG).flagName(args[1]).sendChat(player);
                return;
            }
            boolean val = plot.getFlag(flag);
            new MessageBuilder(Message.FLAG_VALUE_ON_PLOT)
                    .flagName(flag.getName())
                    .flagValue(val ? "true" : "false")
                    .sendChat(player);
            player.sendMessage(TextUtil.toComponent("&7Opis: &f" + flag.getDescription()));
            player.sendMessage(TextUtil.toComponent("&7Domyślna wartość: &f" + (flag.getDefaultValue() ? "true" : "false")));
            return;
        }

        if (sub.equals("reset")) {
            if (args.length < 2) {
                messageBuilder.message(Message.COMMAND_USAGE).sendChat(player);
                return;
            }
            if (args[1].equalsIgnoreCase("all")) {
                plot.resetAllFlags();
                plotManager.savePlot(plot);
                new MessageBuilder(Message.FLAGS_RESET_ALL_ON_PLOT).sendChat(player);
                return;
            }
            PlotFlag flag = PlotFlag.getByName(args[1]);
            if (flag == null) {
                new MessageBuilder(Message.NULL_FLAG).flagName(args[1]).sendChat(player);
                return;
            }
            plot.resetFlag(flag);
            plotManager.savePlot(plot);
            new MessageBuilder(Message.FLAG_RESET_ON_PLOT).flagName(flag.getName()).sendChat(player);
            return;
        }

        if (sub.equals("set")) {
            if (args.length < 3) {
                messageBuilder.message(Message.COMMAND_USAGE).sendChat(player);
                return;
            }
            PlotFlag flag = PlotFlag.getByName(args[1]);
            if (flag == null) {
                new MessageBuilder(Message.NULL_FLAG).flagName(args[1]).sendChat(player);
                return;
            }
            Boolean value = parseBooleanOrToggle(args[2], plot.getFlag(flag));
            if (value == null) {
                new MessageBuilder(Message.WRONG_FLAG_VALUE).sendChat(player);
                return;
            }
            plot.setFlag(flag, value);
            plotManager.savePlot(plot);
            new MessageBuilder(Message.FLAG_SET_ON_PLOT)
                    .flagName(flag.getName())
                    .flagValue(value ? "true" : "false")
                    .sendChat(player);
            return;
        }

        // Direct flag subcommand: /plot flag <flagName> [value]
        PlotFlag flag = PlotFlag.getByName(args[0]);
        if (flag != null) {
            if (args.length == 1) {
                boolean val = plot.getFlag(flag);
                new MessageBuilder(Message.FLAG_VALUE_ON_PLOT)
                        .flagName(flag.getName())
                        .flagValue(val ? "true" : "false")
                        .sendChat(player);
                player.sendMessage(TextUtil.toComponent("&7Opis: &f" + flag.getDescription()));
                player.sendMessage(TextUtil.toComponent("&7Domyślna wartość: &f" + (flag.getDefaultValue() ? "true" : "false")));
                return;
            }
            Boolean value = parseBooleanOrToggle(args[1], plot.getFlag(flag));
            if (value == null) {
                new MessageBuilder(Message.WRONG_FLAG_VALUE).sendChat(player);
                return;
            }
            plot.setFlag(flag, value);
            plotManager.savePlot(plot);
            new MessageBuilder(Message.FLAG_SET_ON_PLOT)
                    .flagName(flag.getName())
                    .flagValue(value ? "true" : "false")
                    .sendChat(player);
            return;
        }

        new MessageBuilder(Message.NULL_FLAG).flagName(args[0]).sendChat(player);
    }

    private void sendFlagList(Player player, Plot plot) {
        new MessageBuilder(Message.WIDE_HEADER).sendChat(player);
        new MessageBuilder(Message.PLOT_FLAGS).plot(plot).sendChat(player);
        for (PlotFlag flag : PlotFlag.values()) {
            boolean val = plot.getFlag(flag);
            String valColor = val ? "&a[WŁĄCZONA / true]" : "&c[WYŁĄCZONA / false]";
            String defaultStr = flag.getDefaultValue() ? "&atrue" : "&cfalse";
            player.sendMessage(TextUtil.toComponent("&7• &f" + flag.getName() + " " + valColor + " &8- &7" + flag.getDescription() + " &8(domyślnie: " + defaultStr + "&8)"));
        }
        player.sendMessage(TextUtil.toComponent("&7Użyj &f/plot flag <flaga> <true|false|toggle> &7aby zmienić wartość flagi."));
    }

    private Boolean parseBooleanOrToggle(String input, boolean currentValue) {
        if (input == null) return null;
        String lower = input.trim().toLowerCase();
        if (lower.equals("true") || lower.equals("tak") || lower.equals("yes") || lower.equals("on") || lower.equals("1") || lower.equals("włącz") || lower.equals("wlacz") || lower.equals("enable") || lower.equals("enabled")) {
            return true;
        }
        if (lower.equals("false") || lower.equals("nie") || lower.equals("no") || lower.equals("off") || lower.equals("0") || lower.equals("wyłącz") || lower.equals("wylacz") || lower.equals("disable") || lower.equals("disabled")) {
            return false;
        }
        if (lower.equals("toggle") || lower.equals("przełącz") || lower.equals("przelacz")) {
            return !currentValue;
        }
        return null;
    }

    @Override
    public List<String> getTabCompletion(Player player, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (String sub : BASE_SUBCOMMANDS) {
                if (sub.startsWith(prefix)) completions.add(sub);
            }
            for (PlotFlag flag : PlotFlag.values()) {
                if (flag.getName().startsWith(prefix)) completions.add(flag.getName());
            }
            return completions;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String prefix = args[1].toLowerCase();
            if (sub.equals("set") || sub.equals("info") || sub.equals("get")) {
                for (PlotFlag flag : PlotFlag.values()) {
                    if (flag.getName().startsWith(prefix)) completions.add(flag.getName());
                }
                return completions;
            }
            if (sub.equals("reset")) {
                if ("all".startsWith(prefix)) completions.add("all");
                for (PlotFlag flag : PlotFlag.values()) {
                    if (flag.getName().startsWith(prefix)) completions.add(flag.getName());
                }
                return completions;
            }
            PlotFlag flag = PlotFlag.getByName(args[0]);
            if (flag != null) {
                for (String val : BOOLEAN_VALUES) {
                    if (val.startsWith(prefix)) completions.add(val);
                }
                return completions;
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            String prefix = args[2].toLowerCase();
            for (String val : BOOLEAN_VALUES) {
                if (val.startsWith(prefix)) completions.add(val);
            }
            return completions;
        }

        return List.of();
    }
}
