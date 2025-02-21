package pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.blacklist.subcommands.add;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;

import java.util.List;

public class PlotBlacklistAddCommand implements Subcommand {
        @Override
        public String getName() {
            return "add";
        }
        @Override
        public String getDescription() {
            return "blokowanie dostępu do działki wyznaczonemu graczowi";
        }
        @Override
        public String getSyntax() {
            return "/plot blacklist add";
        }
        @Override
        public String getPermission() {
            return "chunkyplots.blacklist";
        }
        
        @Override
        public void execute(CommandSender sender, String[] args) {
            if(sender instanceof Player player) {
                   
            }
        }
        
        @Override
        public List<String> getTabCompletion(CommandSender sender, String[] args) {
            return List.of();
        }
}
