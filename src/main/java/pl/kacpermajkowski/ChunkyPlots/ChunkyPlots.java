package pl.kacpermajkowski.ChunkyPlots;

import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.PlotAdminCommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.crafting.CraftingManager;
import pl.kacpermajkowski.ChunkyPlots.crafting.PlotItemFactory;
import pl.kacpermajkowski.ChunkyPlots.exceptions.InvalidStateException;
import pl.kacpermajkowski.ChunkyPlots.listeners.PlotTransitionNotifier;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotBlockPlaceListener;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotRepository;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;
import pl.kacpermajkowski.ChunkyPlots.protections.block.*;
import pl.kacpermajkowski.ChunkyPlots.protections.block.explosion.*;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.*;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.ProjectileHitProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.LightningProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.LingeringPotionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.SplashPotionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerBucketEmptyListener;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerEntryProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.EntityInteractionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerInteractListener;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.*;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;

public class ChunkyPlots extends JavaPlugin {
	private Config config;
	private PlotManager plotManager;
	private UserManager userManager;
	private CraftingManager craftingManager;
	private PlotPermissionChecker permissionChecker;

	@Override
	public void onEnable(){
        initializeDependencies();

		registerListeners();
        tryRegisterCommands();
	}

	@Override
	public void onDisable(){
		if(plotManager != null) {
			plotManager.savePlots();
		}
		if(userManager != null) {
			userManager.saveUsers();
		}
	}

	private void initializeDependencies(){
		this.config = new Config(this);
		MessageBuilder.init(config);

		PlotRepository plotRepository = new PlotRepository(getDataFolder(), getLogger());
		ItemStack plotItem = new PlotItemFactory(config).createPlotItem();

		this.plotManager = new PlotManager(plotRepository, plotItem);
		this.userManager = new UserManager(getDataFolder(), getLogger());
		this.craftingManager = new CraftingManager(this, plotItem);

		this.permissionChecker = new PlotPermissionChecker(plotManager, userManager);
		ProtectionUtil.init(permissionChecker);
	}

	private void registerListeners(){
		var pm = this.getServer().getPluginManager();

//		misc
		pm.registerEvents(new LingeringPotionProtection(), this);
		pm.registerEvents(new SplashPotionProtection(), this);
		pm.registerEvents(new LightningProtection(), this);

//		block - explosion
		pm.registerEvents(new EnderCrystalProtection(), this);
		pm.registerEvents(new ExplodeProtection(), this);
		pm.registerEvents(new TNTMinecartProtection(), this);
		pm.registerEvents(new TNTProtection(), this);
		pm.registerEvents(new WitherProtection(), this);

//		block
		pm.registerEvents(new BlockBreakProtection(), this);
		pm.registerEvents(new BlockBurnProtection(), this);
		pm.registerEvents(new BlockFromToListener(), this);
		pm.registerEvents(new BlockPlaceListener(), this);
		pm.registerEvents(new BlockSpreadListener(), this);
		pm.registerEvents(new FertilizedGrassOvergrowthProtection(), this);
		pm.registerEvents(new IgniteProtection(), this);
		pm.registerEvents(new LecternBookProtection(), this);
		pm.registerEvents(new StructureOvergrowProtection(), this);

//		entity
		pm.registerEvents(new EntityRunawayProtection(), this);
		pm.registerEvents(new AnimalTemptingProtection(), this);
		pm.registerEvents(new VehicleSpamProtection(), this);
		pm.registerEvents(new EntityDamageProtection(), this);
		pm.registerEvents(new EntityInteractionProtection(), this);
		pm.registerEvents(new EntityKnockbackProtection(), this);
		pm.registerEvents(new ItemFrameProtection(), this);
		pm.registerEvents(new PlayerLeashEntityProtection(), this);
		pm.registerEvents(new PlayerShearEntityProtection(), this);
		pm.registerEvents(new PlayerUnleashEntityProtection(), this);
		pm.registerEvents(new ProjectileHitProtection(), this);
		pm.registerEvents(new VehicleDamageListener(), this);
		pm.registerEvents(new VehicleEnterListener(), this);

//		player
		pm.registerEvents(new PlayerBucketEmptyListener(), this);
		pm.registerEvents(new PlayerEntryProtection(), this);
		pm.registerEvents(new PlayerInteractListener(), this);

//		redstone
		pm.registerEvents(new DispenserProtection(), this);
		pm.registerEvents(new HopperProtection(), this);
		pm.registerEvents(new PistonProtection(), this);
		pm.registerEvents(new RedstoneSignalProtection(), this);
		pm.registerEvents(new ShriekerProtection(), this);

//		regular listeners
		pm.registerEvents(new PlotTransitionNotifier(plotManager, userManager), this);
		pm.registerEvents(new PlotBlockPlaceListener(plotManager), this);
		pm.registerEvents(userManager, this);
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
		requireCommand("plot").setExecutor(new PlotCommand(plotManager, userManager));
		requireCommand("plotadmin").setExecutor(new PlotAdminCommand(plotManager, userManager));
	}

    private PluginCommand requireCommand(String commandName) throws InvalidStateException {
        PluginCommand cmd = getCommand(commandName);
        if(cmd == null) {
            throw new InvalidStateException(
                    "Command '" + commandName + "' was not registered with the server, so the plugin couldn't hook up to it. " +
                    "This is probably a plugin.yml configuration issue. Please report this to the plugin developer.");
        }
        return cmd;
    }

	public static ChunkyPlots instance(){
		return getPlugin(ChunkyPlots.class);
	}

	public Config getPluginConfig() {
		return config;
	}

	public PlotManager getPlotManager() {
		return plotManager;
	}

	public UserManager getUserManager() {
		return userManager;
	}

	public CraftingManager getCraftingManager() {
		return craftingManager;
	}

	public PlotPermissionChecker getPermissionChecker() {
		return permissionChecker;
	}
}
