package pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.manager.PlotManager;

public class PlotAdminBlockCommand extends Subcommand {
    @Override
    public String getName() {
        return "block";
    }

    @Override
    public String getDescription() {
        return "daje do ekwipunku blok działki";
    }

    @Override
    public String getSyntax() {
        return "/plotadmin block";
    }

    @Override
    public String getPermission() {
        return "chunkyplots.plotadmin";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof Player player){
            player.getInventory().addItem(PlotManager.getInstance().getPlotItem());
            player.sendMessage("Otrzymałeś blok działki!");
        }
    }
}
