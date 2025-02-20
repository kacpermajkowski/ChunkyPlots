package pl.kacpermajkowski.ChunkyPlots;

import org.bukkit.plugin.java.JavaPlugin;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotCommandManager;
import pl.kacpermajkowski.ChunkyPlots.listeners.PlayerJoinListener;
import pl.kacpermajkowski.ChunkyPlots.listeners.PlayerLeftListener;
import pl.kacpermajkowski.ChunkyPlots.listeners.PlayerMoveListener;
import pl.kacpermajkowski.ChunkyPlots.manager.*;
import pl.kacpermajkowski.ChunkyPlots.protections.block.*;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.*;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.LingeringPotionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.SplashPotionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerBucketEmptyListener;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerInteractAtEntityListener;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerInteractListener;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.DispenserProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.PistonProtection;

public class ChunkyPlots extends JavaPlugin {
	private static ChunkyPlots instance;
	public PlotManager plotManager;
	public UserManager userManager;
	public VisitManager visitManager;
	public CraftingManager craftingManager;

	@Override
	public void onEnable(){
		instance = this;

		initializeManagers();

		registerListeners();
		registerCommands();
	}

	private void initializeManagers(){
		plotManager = new PlotManager();
		userManager = new UserManager();
		visitManager = new VisitManager();
		craftingManager = new CraftingManager();
	}

	private void registerListeners(){
		this.getServer().getPluginManager().registerEvents(new LingeringPotionProtection(),this);
		this.getServer().getPluginManager().registerEvents(new BlockBreakProtection(),this);
		this.getServer().getPluginManager().registerEvents(new BlockBurnProtection(),this);
		this.getServer().getPluginManager().registerEvents(new IgniteProtection(),this);
		this.getServer().getPluginManager().registerEvents(new BlockFromToListener(),this);
		this.getServer().getPluginManager().registerEvents(new BlockSpreadListener(),this);
		this.getServer().getPluginManager().registerEvents(new PistonProtection(),this);
		this.getServer().getPluginManager().registerEvents(new BlockPlaceListener(),this);
		this.getServer().getPluginManager().registerEvents(new DispenserProtection(),this);
		this.getServer().getPluginManager().registerEvents(new EntityDamageProtection(),this);
		this.getServer().getPluginManager().registerEvents(new ExplodeProtection(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerBucketEmptyListener(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerInteractAtEntityListener(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerLeftListener(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerLeashEntityProtection(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerMoveListener(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerShearEntityProtection(),this);
		this.getServer().getPluginManager().registerEvents(new LecternBookProtection(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerUnleashEntityProtection(),this);
		this.getServer().getPluginManager().registerEvents(new SplashPotionProtection(),this);
		this.getServer().getPluginManager().registerEvents(new VehicleDamageListener(),this);
		this.getServer().getPluginManager().registerEvents(new VehicleEnterListener(),this);
	}
	private void registerCommands(){
		getCommand("plot").setExecutor(new PlotCommandManager());
	}

	public static ChunkyPlots getInstance(){
		return instance;
	}
}
