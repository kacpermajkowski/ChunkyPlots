package pl.kacpermajkowski.ChunkyPlots.commands.plotadmin;

import org.bukkit.command.CommandSender;
import pl.kacpermajkowski.ChunkyPlots.commands.Command;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.subcommands.block.PlotAdminBlockCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.subcommands.bypass.PlotAdminBypassCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.subcommands.help.PlotAdminHelpCommand;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlotAdminCommand extends Command {
    private final List<Subcommand> subcommands;

    public PlotAdminCommand(PlotManager plotManager, UserManager userManager) {
        subcommands = new ArrayList<>();
        subcommands.add(new PlotAdminHelpCommand());
        subcommands.add(new PlotAdminBlockCommand(plotManager));
        subcommands.add(new PlotAdminBypassCommand(userManager));
    }

    protected List<Subcommand> getSubcommands() {
        return Collections.unmodifiableList(subcommands);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        getSubcommand("help").execute(sender, args);
    }
}
