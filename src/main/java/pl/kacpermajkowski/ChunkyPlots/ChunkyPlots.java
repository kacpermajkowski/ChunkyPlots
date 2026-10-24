package pl.kacpermajkowski.ChunkyPlots;

import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import pl.kacpermajkowski.ChunkyPlots.api.ChunkyPlotsAPI;
import pl.kacpermajkowski.ChunkyPlots.api.ChunkyPlotsAPIImpl;
import pl.kacpermajkowski.ChunkyPlots.commands.plot.PlotCommand;
import pl.kacpermajkowski.ChunkyPlots.commands.plotadmin.PlotAdminCommand;
import pl.kacpermajkowski.ChunkyPlots.config.Config;
import pl.kacpermajkowski.ChunkyPlots.crafting.CraftingManager;
import pl.kacpermajkowski.ChunkyPlots.crafting.PlotItemFactory;
import pl.kacpermajkowski.ChunkyPlots.database.DatabaseManager;
import pl.kacpermajkowski.ChunkyPlots.exceptions.InvalidStateException;
import pl.kacpermajkowski.ChunkyPlots.hook.vault.VaultHook;
import pl.kacpermajkowski.ChunkyPlots.listeners.PlotLifecycleListener;
import pl.kacpermajkowski.ChunkyPlots.listeners.PlotTransitionNotifier;
import pl.kacpermajkowski.ChunkyPlots.messages.MessageBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotBlockPlaceListener;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotManager;
import pl.kacpermajkowski.ChunkyPlots.plot.PlotRepository;
import pl.kacpermajkowski.ChunkyPlots.plot.SqlitePlotRepository;
import pl.kacpermajkowski.ChunkyPlots.plot.border.PlotBorderVisualizer;
import pl.kacpermajkowski.ChunkyPlots.plot.gui.PlotGuiListener;
import pl.kacpermajkowski.ChunkyPlots.plot.gui.PlotGuiManager;
import pl.kacpermajkowski.ChunkyPlots.plot.limit.PlotLimitManager;
import pl.kacpermajkowski.ChunkyPlots.plot.map.PlotMapGuiListener;
import pl.kacpermajkowski.ChunkyPlots.plot.map.PlotMapManager;
import pl.kacpermajkowski.ChunkyPlots.plot.mnemonic.MnemonicDictionary;
import pl.kacpermajkowski.ChunkyPlots.plot.mnemonic.PlotMnemonic;
import pl.kacpermajkowski.ChunkyPlots.protections.PlotPermissionChecker;
import pl.kacpermajkowski.ChunkyPlots.protections.ProtectionUtil;
import pl.kacpermajkowski.ChunkyPlots.protections.block.BlockBreakProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.block.BlockBurnProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.block.BlockFromToListener;
import pl.kacpermajkowski.ChunkyPlots.protections.block.BlockPlaceListener;
import pl.kacpermajkowski.ChunkyPlots.protections.block.BlockSpreadListener;
import pl.kacpermajkowski.ChunkyPlots.protections.block.FertilizedGrassOvergrowthProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.block.IgniteProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.block.LecternBookProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.block.StructureOvergrowProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.block.explosion.EnderCrystalProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.block.explosion.ExplodeProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.block.explosion.TNTMinecartProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.block.explosion.TNTProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.block.explosion.WitherProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.AnimalTemptingProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.EndermiteSpawnProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.EntityDamageProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.EntityInteractionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.EntityKnockbackProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.EntityRunawayProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.ItemFrameProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.PlayerLeashEntityProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.PlayerShearEntityProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.PlayerUnleashEntityProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.ProjectileHitProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.VehicleDamageListener;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.VehicleEnterListener;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.VehicleMoveProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.VehicleSpamProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.entity.EntityChangeBlockProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.LightningProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.LingeringPotionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.misc.SplashPotionProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerBucketEmptyListener;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerEntryProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.player.PlayerInteractListener;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.DispenserProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.HopperProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.PistonProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.RedstoneSignalProtection;
import pl.kacpermajkowski.ChunkyPlots.protections.redstone.ShriekerProtection;
import pl.kacpermajkowski.ChunkyPlots.user.SqliteUserRepository;
import pl.kacpermajkowski.ChunkyPlots.user.UserManager;
import pl.kacpermajkowski.ChunkyPlots.user.UserRepository;

import java.io.File;
import java.io.InputStream;

public class ChunkyPlots extends JavaPlugin {
	private Config config;
	private DatabaseManager databaseManager;
	private PlotManager plotManager;
	private UserManager userManager;
	private CraftingManager craftingManager;
	private PlotPermissionChecker permissionChecker;
	private PlotBorderVisualizer borderVisualizer;
	private PlotMapManager mapManager;
	private PlotGuiManager guiManager;
	private VaultHook vaultHook;
	private PlotLimitManager limitManager;
	private ChunkyPlotsAPI api;

	@Override
	public void onEnable(){
        initializeDependencies();

		registerListeners();
        tryRegisterCommands();
	}

	@Override
	public void onDisable(){
		if(borderVisualizer != null) {
			borderVisualizer.stopAll();
		}
		if(plotManager != null) {
			plotManager.savePlots();
		}
		if(userManager != null) {
			userManager.saveUsers();
		}
		if(databaseManager != null) {
			databaseManager.close();
		}
		getServer().getServicesManager().unregisterAll(this);
	}

	private void initializeDependencies(){
		this.config = new Config(this);
		MessageBuilder.init(config);

		File dictFile = new File(getDataFolder(), "dictionary.txt");
		if (!dictFile.exists()) {
			try {
				saveResource("dictionary.txt", false);
			} catch (Exception e) {
				getLogger().warning("Could not save default dictionary.txt: " + e.getMessage());
			}
		}
		try {
			if (dictFile.exists()) {
				PlotMnemonic.init(MnemonicDictionary.loadFromFile(dictFile));
			} else {
				try (InputStream in = getResource("dictionary.txt")) {
					if (in != null) {
						PlotMnemonic.init(MnemonicDictionary.loadFromStream(in));
					}
				}
			}
		} catch (Exception e) {
			getLogger().severe("Failed to load mnemonic dictionary: " + e.getMessage());
		}

		this.databaseManager = new DatabaseManager(getDataFolder(), getLogger());
		PlotRepository plotRepository = new SqlitePlotRepository(databaseManager, getDataFolder(), getLogger());
		ItemStack plotItem = new PlotItemFactory(config).createPlotItem();

		UserRepository userRepository = new SqliteUserRepository(databaseManager, getDataFolder(), getLogger());

		this.plotManager = new PlotManager(plotRepository, plotItem);
		this.plotManager.setConfig(config);
		this.userManager = new UserManager(userRepository);
		this.craftingManager = new CraftingManager(this, plotItem);

		this.permissionChecker = new PlotPermissionChecker(plotManager, userManager);
		ProtectionUtil.init(permissionChecker);

		this.borderVisualizer = new PlotBorderVisualizer(this, plotManager);
		this.mapManager = new PlotMapManager(this, plotManager);

		this.vaultHook = new VaultHook(this);
		this.limitManager = new PlotLimitManager(config, plotManager, vaultHook);
		this.plotManager.setLimitManager(limitManager);

		this.guiManager = new PlotGuiManager(this, plotManager, userManager, borderVisualizer, limitManager, vaultHook, mapManager);

		this.api = new ChunkyPlotsAPIImpl(plotManager, limitManager, borderVisualizer, vaultHook, mapManager, guiManager);
		getServer().getServicesManager().register(ChunkyPlotsAPI.class, this.api, this, ServicePriority.Normal);
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
		pm.registerEvents(new EntityChangeBlockProtection(permissionChecker), this);
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

//		domain events & reactive listeners
		pm.registerEvents(new PlotLifecycleListener(userManager), this);

//		regular listeners
		pm.registerEvents(new PlotTransitionNotifier(plotManager, userManager), this);
		pm.registerEvents(new PlotBlockPlaceListener(plotManager), this);
		pm.registerEvents(userManager, this);
		pm.registerEvents(borderVisualizer, this);
		pm.registerEvents(new PlotMapGuiListener(), this);
		pm.registerEvents(new PlotGuiListener(guiManager), this);
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
		requireCommand("plot").setExecutor(new PlotCommand(plotManager, userManager, borderVisualizer, mapManager, guiManager));
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

	public PlotBorderVisualizer getBorderVisualizer() {
		return borderVisualizer;
	}

	public PlotMapManager getMapManager() {
		return mapManager;
	}

	public PlotGuiManager getGuiManager() {
		return guiManager;
	}

	public VaultHook getVaultHook() {
		return vaultHook;
	}

	public PlotLimitManager getLimitManager() {
		return limitManager;
	}

	public ChunkyPlotsAPI getApi() {
		return api;
	}

	public static ChunkyPlotsAPI api() {
		return ChunkyPlotsAPI.get();
	}
}
