package pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.subcommands;

import org.bukkit.command.CommandSender;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.config.Lang;

import java.util.ArrayList;

public class PlotAdminHelpCommand extends Subcommand {
    ArrayList<Subcommand> subcommands;

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "lista dostępnych komend";
    }

    @Override
    public String getSyntax() {
        return "/plotadmin help";
    }

    @Override
    public String getPermission() {
        return "chunkyplots.plotadmin";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sendHelpMessage(sender);
    }

    public void sendHelpMessage(CommandSender sender){
        if(subcommands != null) {
            Lang.sendNoPrefixMessage(sender, "&9-----------{ " + Config.getInstance().getPrefix() + " &c&lADMIN" + " &9}-----------");
            for(Subcommand s: subcommands){
                Lang.sendNoPrefixMessage(sender, "&a/pa " + s.getName() + " &8- &7" + s.getDescription());
            }
            Lang.sendNoPrefixMessage(sender, "&9-----------{ " + Config.getInstance().getPrefix() + " &c&lADMIN" + " &9}-----------");
        } else {
            Lang.sendMessage(sender, "&cSpecified subcommand was not found.");
        }
    }

    public void updateSubcommandList(ArrayList<Subcommand> subcommands) {
        this.subcommands = subcommands;
    }
}
