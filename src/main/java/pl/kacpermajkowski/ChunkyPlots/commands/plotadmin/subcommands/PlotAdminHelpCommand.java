package pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.subcommands;

import org.bukkit.command.CommandSender;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class PlotAdminHelpCommand implements Subcommand {
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

    @Override
    public List<String> getTabCompletion(CommandSender sender, String[] args) {
        return List.of();
    }

    public void sendHelpMessage(CommandSender sender){
        if(subcommands != null) {
            TextUtil.sendNoPrefixMessage(sender, "&9-----------{ " + Config.getInstance().getPrefix() + " &c&lADMIN" + " &9}-----------");
            for(Subcommand s: subcommands){
                TextUtil.sendNoPrefixMessage(sender, "&a/pa " + s.getName() + " &8- &7" + s.getDescription());
            }
            TextUtil.sendNoPrefixMessage(sender, "&9-----------{ " + Config.getInstance().getPrefix() + " &c&lADMIN" + " &9}-----------");
        } else {
            TextUtil.sendMessage(sender, "&cSpecified subcommand was not found.");
        }
    }

    public void updateSubcommandList(ArrayList<Subcommand> subcommands) {
        this.subcommands = subcommands;
    }
}
