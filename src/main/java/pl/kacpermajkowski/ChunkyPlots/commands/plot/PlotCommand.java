package pl.kacpermajkowski.ChunkyPlots.commands.plot;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.commands.Command;
import pl.kacpermajkowski.ChunkyPlots.commands.Subcommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.blacklist.PlotDenyCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.blacklist.PlotUndenyCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.border.PlotShowBorderCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.dispose.PlotDisposeCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.flag.PlotFlagCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.gui.PlotGuiCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.help.PlotHelpCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.info.PlotInfoCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.list.PlotListCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.map.PlotMapCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.shared.PlotSharedCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.whitelist.PlotAddCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.subcommands.whitelist.PlotRemoveCommand;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.border.PlotBorderVisualizer;
import pl.kacpermajkowski.ChunkyPlots.plot.gui.PlotGuiManager;
import pl.kacpermajkowski.ChunkyPlots.plot.map.PlotMapManager;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlotCommand extends Command {
	private final ArrayList<PlotSubcommand> subcommands;
	private final PlotHelpCommand help;
	private final PlotGuiManager guiManager;

	public PlotCommand(PlotManager plotManager, UserManager userManager) {
		this(plotManager, userManager,
				ChunkyPlots.instance() != null ? ChunkyPlots.instance().getBorderVisualizer() : null,
				ChunkyPlots.instance() != null ? ChunkyPlots.instance().getMapManager() : null,
				ChunkyPlots.instance() != null ? ChunkyPlots.instance().getGuiManager() : null);
	}

	public PlotCommand(PlotManager plotManager, UserManager userManager, PlotBorderVisualizer borderVisualizer) {
		this(plotManager, userManager, borderVisualizer,
				ChunkyPlots.instance() != null ? ChunkyPlots.instance().getMapManager() : null,
				ChunkyPlots.instance() != null ? ChunkyPlots.instance().getGuiManager() : null);
	}

	public PlotCommand(PlotManager plotManager, UserManager userManager, PlotBorderVisualizer borderVisualizer, PlotMapManager mapManager) {
		this(plotManager, userManager, borderVisualizer, mapManager,
				ChunkyPlots.instance() != null ? ChunkyPlots.instance().getGuiManager() : null);
	}

	public PlotCommand(PlotManager plotManager, UserManager userManager, PlotBorderVisualizer borderVisualizer,
					   PlotMapManager mapManager, PlotGuiManager guiManager) {
		this.guiManager = guiManager;
		this.subcommands = new ArrayList<>();
		this.help = new PlotHelpCommand();
		subcommands.add(help);
		subcommands.add(new PlotDisposeCommand(plotManager, userManager));
		subcommands.add(new PlotDenyCommand(plotManager));
		subcommands.add(new PlotUndenyCommand(plotManager));
		subcommands.add(new PlotAddCommand(plotManager));
		subcommands.add(new PlotRemoveCommand(plotManager));
		subcommands.add(new PlotFlagCommand(plotManager));
//		subcommands.add(new PlotGroupCommand());
		subcommands.add(new PlotInfoCommand(plotManager));
//		subcommands.add(new PlotVisitCommand());
		subcommands.add(new PlotListCommand(plotManager));
		subcommands.add(new PlotSharedCommand(plotManager));
		subcommands.add(new PlotShowBorderCommand(borderVisualizer));
		subcommands.add(new PlotMapCommand(mapManager));
		subcommands.add(new PlotGuiCommand(guiManager, plotManager));
		help.setSubcommands(subcommands);
	}

	@Override
	protected void execute(CommandSender sender, String[] args) {
		if (sender instanceof Player player && guiManager != null) {
			guiManager.openMainGui(player);
			return;
		}
		getSubcommand("help").execute(sender, args);
	}

	protected List<Subcommand> getSubcommands() {
		return Collections.unmodifiableList(subcommands);
	}
}
