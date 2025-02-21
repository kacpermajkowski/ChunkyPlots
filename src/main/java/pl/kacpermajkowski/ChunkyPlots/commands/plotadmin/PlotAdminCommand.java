package pl.kacpermajkowski.ChunkyPlots.commands.plotadmin;

import org.bukkit.command.CommandSender;
import pl.kacpermajkowski.ChunkyPlots.commands.Command;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.subcommands.block.PlotAdminBlockCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.subcommands.help.PlotAdminHelpCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlotAdminCommand extends Command<PlotAdminCommand> {
    private static PlotAdminCommand instance;
    private final List<Subcommand> subcommands;

    private PlotAdminCommand() {
        subcommands = new ArrayList<>();
        subcommands.add(new PlotAdminHelpCommand());
        subcommands.add(new PlotAdminBlockCommand());
    }

    protected List<Subcommand> getSubcommands() {
        return Collections.unmodifiableList(subcommands);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        getSubcommand("help").execute(sender, args);
    }

    public static PlotAdminCommand getInstance() {
        if(instance == null) {
            instance = new PlotAdminCommand();
        }
        return instance;
    }
}
