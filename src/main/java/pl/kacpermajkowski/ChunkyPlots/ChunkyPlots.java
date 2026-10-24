package pl.kacpermajkowski.ChunkyPlots;

import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.PlotAdminCommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.crafting.CraftingManager;
import pl.kacpermajkowski.ChunkyPlots.crafting.PlotItemFactory;
import pl.kacpermajkowski.ChunkyPlots.database.DatabaseManager;
import pl.kacpermajkowski.ChunkyPlots.exceptions.InvalidStateException;
import pl.kacpermajkowski.ChunkyPlots.listeners.PlotTransitionNotifier;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotBlockPlaceListener;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotRepository;
import pl.kacpermajkowski.ChunkyPlots.plot.SqlitePlotRepository;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;
import pl.kacpermajkowski.ChunkyPlots.protections.block.*;
import pl.kacpermajkowski.ChunkyPlots.protections.block.explosion.*;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.*;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.LightningProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.LingeringPotionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.SplashPotionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerBucketEmptyListener;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerEntryProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerInteractListener;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.*;
import pl.kacpermajkowski.ChunkyPlots.user.SqliteUserRepository;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;
import pl.kacpermajkowski.ChunkyPlots.user.UserRepository;

public class ChunkyPlots extends JavaPlugin {
	private Config config;
	private DatabaseManager databaseManager;
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
		if(databaseManager != null) {
			databaseManager.close();
		}
	}

	private void initializeDependencies(){
		this.config = new Config(this);
		MessageBuilder.init(config);

		this.databaseManager = new DatabaseManager(getDataFolder(), getLogger());
		PlotRepository plotRepository = new SqlitePlotRepository(databaseManager, getDataFolder(), getLogger());
		ItemStack plotItem = new PlotItemFactory(config).createPlotItem();

		UserRepository userRepository = new SqliteUserRepository(databaseManager, getDataFolder(), getLogger());

		this.plotManager = new PlotManager(plotRepository, plotItem);
		this.userManager = new UserManager(userRepository);
		this.craftingManager = new CraftingManager(this, plotItem);

		this.permissionChecker = new PlotPermissionChecker(plotManager, userManager);
		ProtectionUtil.init(permissionChecker);
	}

	private void registerListeners(){
		var pm = this.getServer().getPluginManager();

//		misc
		pm.registerEvents(new LingeringPotionProtection(permissionChecker), this);
		pm.registerEvents(new SplashPotionProtection(permissionChecker), this);
		pm.registerEvents(new LightningProtection(permissionChecker), this);

//		block - explosion
		pm.registerEvents(new EnderCrystalProtection(permissionChecker), this);
		pm.registerEvents(new ExplodeProtection(), this);
		pm.registerEvents(new TNTMinecartProtection(permissionChecker), this);
		pm.registerEvents(new TNTProtection(permissionChecker, this), this);
		pm.registerEvents(new WitherProtection(permissionChecker), this);

//		block
		pm.registerEvents(new BlockBreakProtection(permissionChecker), this);
		pm.registerEvents(new BlockBurnProtection(permissionChecker), this);
		pm.registerEvents(new BlockFromToListener(permissionChecker), this);
		pm.registerEvents(new BlockPlaceListener(permissionChecker), this);
		pm.registerEvents(new BlockSpreadListener(permissionChecker), this);
		pm.registerEvents(new FertilizedGrassOvergrowthProtection(permissionChecker), this);
		pm.registerEvents(new IgniteProtection(permissionChecker), this);
		pm.registerEvents(new LecternBookProtection(permissionChecker), this);
		pm.registerEvents(new StructureOvergrowProtection(permissionChecker), this);

//		entity
		pm.registerEvents(new EntityRunawayProtection(permissionChecker), this);
		pm.registerEvents(new AnimalTemptingProtection(permissionChecker), this);
		pm.registerEvents(new EndermiteSpawnProtection(permissionChecker), this);
		pm.registerEvents(new VehicleSpamProtection(permissionChecker), this);
		pm.registerEvents(new EntityDamageProtection(permissionChecker), this);
		pm.registerEvents(new EntityInteractionProtection(permissionChecker), this);
		pm.registerEvents(new EntityKnockbackProtection(permissionChecker), this);
		pm.registerEvents(new ItemFrameProtection(permissionChecker), this);
		pm.registerEvents(new PlayerLeashEntityProtection(permissionChecker), this);
		pm.registerEvents(new PlayerShearEntityProtection(permissionChecker), this);
		pm.registerEvents(new PlayerUnleashEntityProtection(permissionChecker), this);
		pm.registerEvents(new ProjectileHitProtection(permissionChecker), this);
		pm.registerEvents(new VehicleDamageListener(permissionChecker), this);
		pm.registerEvents(new VehicleEnterListener(permissionChecker), this);
		pm.registerEvents(new VehicleMoveProtection(permissionChecker), this);

//		player
		pm.registerEvents(new PlayerBucketEmptyListener(permissionChecker), this);
		pm.registerEvents(new PlayerEntryProtection(permissionChecker), this);
		pm.registerEvents(new PlayerInteractListener(permissionChecker, this), this);

//		redstone
		pm.registerEvents(new DispenserProtection(permissionChecker), this);
		pm.registerEvents(new HopperProtection(permissionChecker), this);
		pm.registerEvents(new PistonProtection(permissionChecker), this);
		pm.registerEvents(new RedstoneSignalProtection(permissionChecker), this);
		pm.registerEvents(new ShriekerProtection(permissionChecker), this);

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

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
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
