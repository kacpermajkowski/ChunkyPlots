package pl.kacpermajkowski.ChunkyPlots;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.PlotAdminCommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.exceptions.InvalidStateException;
import pl.kacpermajkowski.ChunkyPlots.listeners.PlotTransitionNotifier;
import pl.kacpermajkowski.ChunkyPlots.crafting.*;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotBlockPlaceListener;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.protections.block.*;
import pl.kacpermajkowski.ChunkyPlots.protections.block.explosion.*;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.*;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.ProjectileHitProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.LingeringPotionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.SplashPotionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerBucketEmptyListener;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerEntryProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.EntityInteractionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerInteractListener;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.*;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

public class ChunkyPlots extends JavaPlugin {
	@Override
	public void onEnable(){
        //initializing singletons
		Config.getInstance();

		PlotManager.getInstance();
		UserManager.getInstance();
		CraftingManager.getInstance();

		registerListeners();
        tryRegisterCommands();
	}

	private void registerListeners(){
//		misc
		this.getServer().getPluginManager().registerEvents(new LingeringPotionProtection(),this);
		this.getServer().getPluginManager().registerEvents(new SplashPotionProtection(),this);

//		block - explosion
		this.getServer().getPluginManager().registerEvents(new EnderCrystalProtection(),this);
		this.getServer().getPluginManager().registerEvents(new ExplodeProtection(),this);
		this.getServer().getPluginManager().registerEvents(new TNTMinecartProtection(),this);
		this.getServer().getPluginManager().registerEvents(new TNTProtection(),this);
		this.getServer().getPluginManager().registerEvents(new WitherProtection(),this);

//		block
		this.getServer().getPluginManager().registerEvents(new BlockBreakProtection(),this);
		this.getServer().getPluginManager().registerEvents(new BlockBurnProtection(),this);
		this.getServer().getPluginManager().registerEvents(new BlockFromToListener(),this);
		this.getServer().getPluginManager().registerEvents(new BlockPlaceListener(),this);
		this.getServer().getPluginManager().registerEvents(new BlockSpreadListener(),this);
		this.getServer().getPluginManager().registerEvents(new FertilizedGrassOvergrowthProtection(),this);
		this.getServer().getPluginManager().registerEvents(new IgniteProtection(),this);
		this.getServer().getPluginManager().registerEvents(new LecternBookProtection(),this);
		this.getServer().getPluginManager().registerEvents(new StructureOvergrowProtection(),this);

//		entity
		this.getServer().getPluginManager().registerEvents(new BoatSpamProtection(),this);
		this.getServer().getPluginManager().registerEvents(new EntityDamageProtection(),this);
		this.getServer().getPluginManager().registerEvents(new EntityInteractionProtection(),this);
		this.getServer().getPluginManager().registerEvents(new EntityKnockbackProtection(),this);
		this.getServer().getPluginManager().registerEvents(new ItemFrameProtection(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerLeashEntityProtection(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerShearEntityProtection(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerUnleashEntityProtection(),this);
		this.getServer().getPluginManager().registerEvents(new ProjectileHitProtection(),this);
		this.getServer().getPluginManager().registerEvents(new VehicleDamageListener(),this);
		this.getServer().getPluginManager().registerEvents(new VehicleEnterListener(),this);

//		player
		this.getServer().getPluginManager().registerEvents(new PlayerBucketEmptyListener(),this);
		this.getServer().getPluginManager().registerEvents(new PlayerEntryProtection(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(),this);

//		redstone
		this.getServer().getPluginManager().registerEvents(new DispenserProtection(),this);
		this.getServer().getPluginManager().registerEvents(new HopperProtection(),this);
		this.getServer().getPluginManager().registerEvents(new PistonProtection(),this);
		this.getServer().getPluginManager().registerEvents(new RedstoneSignalProtection(),this);
		this.getServer().getPluginManager().registerEvents(new ShriekerProtection(),this);

//		regular listeners
		this.getServer().getPluginManager().registerEvents(new PlotTransitionNotifier(),this);
		this.getServer().getPluginManager().registerEvents(new PlotBlockPlaceListener(), this);
	}

    private void tryRegisterCommands(){
        try {
            registerCommands();
        } catch (InvalidStateException e) {
            getLogger().severe("Couldn't start the plugin because of invalid plugin or server state");
            getLogger().severe("Reason: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

	private void registerCommands() throws InvalidStateException {
		requireCommand("plot").setExecutor(PlotCommand.getInstance());
		requireCommand("plotadmin").setExecutor(PlotAdminCommand.getInstance());
	}

    private PluginCommand requireCommand(String commandName) throws InvalidStateException {
        PluginCommand cmd = getCommand(commandName);
        if(cmd == null) {
            throw new InvalidStateException(
                    "Command 'plot' was not registered with the server, so the plugin couldn't hook up to it. " +
                    "This is probably a plugin.yml configuration issue. Please report this to the plugin developer.");
        }
        return cmd;
    }

	public static ChunkyPlots instance(){
		return getPlugin(ChunkyPlots.class);
	}
}
