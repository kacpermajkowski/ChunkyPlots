package pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.subcommands.block;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;

import java.util.List;

public class PlotAdminBlockCommand implements Subcommand {
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

    @Override
    public List<String> getTabCompletion(CommandSender sender, String[] args) {
        return List.of();
    }
}
