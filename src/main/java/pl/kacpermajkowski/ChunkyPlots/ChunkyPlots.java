package pl.kacpermajkowski.ChunkyPlots;

import org.bukkit.plugin.java.JavaPlugin;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.PlotAdminCommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.listeners.PlotTransitionNotifier;
import pl.kacpermajkowski.ChunkyPlots.crafting.*;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotBlockPlaceListener;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.block.*;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.*;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.LingeringPotionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.SplashPotionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerBucketEmptyListener;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerEntryProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.EntityInteractionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerInteractListener;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.DispenserProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.HopperProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.PistonProtection;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

public class ChunkyPlots extends JavaPlugin {
	private static ChunkyPlots instance;

	@Override
	public void onEnable(){
		instance = this;

		Config.getInstance();
		PlotManager.getInstance();
		UserManager.getInstance();
		CraftingManager.getInstance();

		registerListeners();
		registerCommands();
	}

	private void registerListeners(){
		this.getServer().getPluginManager().registerEvents(new LingeringPotionProtection(),this);
		this.getServer().getPluginManager().registerEvents(new StructureOvergrowProtection(),this);
		this.getServer().getPluginManager().registerEvents(new BlockBreakProtection(),this);
		this.getServer().getPluginManager().registerEvents(new BlockBurnProtection(),this);
		this.getServer().getPluginManager().registerEvents(new IgniteProtection(),this);
		this.getServer().getPluginManager().registerEvents(new BlockFromToListener(),this);
		this.getServer().getPluginManager().registerEvents(new BlockSpreadListener(),this);
		this.getServer().getPluginManager().registerEvents(new PistonProtection(),this);
		this.getServer().getPluginManager().registerEvents(new BlockPlaceListener(),this);
		this.getServer().getPluginManager().registerEvents(new DispenserProtection(),this);
		this.getServer().getPluginManager().registerEvents(new HopperProtection(),this);
		this.getServer().getPluginManager().registerEvents(new EntityKnockbackProtection(),this);
		this.getServer().getPluginManager().registerEvents(new ExplodeProtection(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerBucketEmptyListener(),this);
		this.getServer().getPluginManager().registerEvents(new EntityInteractionProtection(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerLeashEntityProtection(),this);
		this.getServer().getPluginManager().registerEvents(new PlotTransitionNotifier(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerEntryProtection(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerShearEntityProtection(),this);
		this.getServer().getPluginManager().registerEvents(new LecternBookProtection(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerUnleashEntityProtection(),this);
		this.getServer().getPluginManager().registerEvents(new SplashPotionProtection(),this);
		this.getServer().getPluginManager().registerEvents(new VehicleDamageListener(),this);
		this.getServer().getPluginManager().registerEvents(new EntityDamageProtection(),this);
		this.getServer().getPluginManager().registerEvents(new VehicleEnterListener(),this);
		this.getServer().getPluginManager().registerEvents(new BoatSpamProtection(),this);
		this.getServer().getPluginManager().registerEvents(new ShriekerTriggerProtection(),this);
		this.getServer().getPluginManager().registerEvents(new FertilizedGrassOvergrowthProtection(),this);


		this.getServer().getPluginManager().registerEvents(new PlotBlockPlaceListener(), this);
	}

	private void registerCommands() {
		getCommand("plot").setExecutor(PlotCommand.getInstance());
		getCommand("plotadmin").setExecutor(PlotAdminCommand.getInstance());
	}

	public static ChunkyPlots getInstance(){
		return instance;
	}
}
