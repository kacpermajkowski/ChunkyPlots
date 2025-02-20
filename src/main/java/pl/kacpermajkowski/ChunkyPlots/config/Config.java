package pl.kacpermajkowski.ChunkyPlots.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.Flag;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class Config {
	private static Config instance;

	private FileConfiguration config;
	private FileConfiguration messageConfig;
	private FileConfiguration flagConfig;

	// config.yml options
	private boolean isUsingMessagePrefix;
	private String prefix;
	private String prefixSpacer;

	private Material plotItemMaterial;
	private String plotItemName;
	private List<String> plotItemLore;

	// messages.yml options
	private HashMap<Message, String> messages;

	// flags.yml options
	private HashMap<Flag, Boolean> defaultFlags;

	private Config(){
		loadFileConfigurations();
		loadConfigValues();
	}

	private void loadFileConfigurations() {
		File dataFolder = ChunkyPlots.getInstance().getDataFolder();
		dataFolder.mkdirs();

		ChunkyPlots.getInstance().saveDefaultConfig();
		List<File> files = List.of(
				new File(dataFolder, "messages.yml"),
				new File(dataFolder, "default_flags.yml")
		);
		for (File file : files) {
			if(!file.exists())
				ChunkyPlots.getInstance().saveResource(file.getName(), false);
		}

		this.config = ChunkyPlots.getInstance().getConfig();
		this.messageConfig = YamlConfiguration.loadConfiguration(new File(dataFolder, "messages.yml"));
		this.flagConfig = YamlConfiguration.loadConfiguration(new File(dataFolder, "default_flags.yml"));
	}
	private void loadConfigValues() {
		try {
			loadIsUsingMessagePrefix();
			loadPrefix();
			loadPrefixSpacer();
			loadPlotItemMaterial();
			loadPlotItemName();
			loadPlotItemLore();

			loadMessages();

			loadDefaultFlagValues();
		} catch (InvalidConfigException e){
			ChunkyPlots plugin = ChunkyPlots.getInstance();
			Logger logger = plugin.getLogger();

			logger.severe("Couldn't start the plugin because of invalid configuration");
			logger.severe("Reason: " + e.getMessage());
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}
	}

	// config.yml loaders
	private void loadIsUsingMessagePrefix() {
		this.isUsingMessagePrefix = config.getBoolean("use-message-prefix");
		if(config.getString("use-message-prefix") == null){
			ChunkyPlots.getInstance().getLogger().warning("'use-message-prefix' in 'config.yml' is not set. Prefix will not be displayed.");
		}
	}
	private void loadPrefix(){
		this.prefix = this.config.getString("prefix");
		if(this.prefix == null){
			ChunkyPlots.getInstance().getLogger().warning("'prefix' in 'config.yml' is not set. Using empty string instead.");
			this.prefix = "";
		}
	}
	private void loadPrefixSpacer(){
		this.prefixSpacer = this.config.getString("prefix-spacer");
		if(prefixSpacer == null){
			ChunkyPlots.getInstance().getLogger().warning("'prefix-spacer' in 'config.yml' is not set. Using empty string instead.");
			this.prefixSpacer = "";
		}
	}

	private void loadPlotItemMaterial() throws InvalidConfigException {
		String materialName = this.config.getString("plot-item.material");
		if(materialName == null)
			throw new InvalidConfigException("'plot-item.material' in 'config.yml' is not set");

		Material material = Material.getMaterial(materialName);
		if(material == null)
			throw new InvalidConfigException("'plot-item.material' in 'config.yml' is not a valid material");

		this.plotItemMaterial = material;
	}
	private void loadPlotItemName() throws InvalidConfigException {
		this.plotItemName = this.config.getString("plot-item.name");
		if(this.plotItemName == null)
			throw new InvalidConfigException("'plot-item.name' in 'config.yml' is not set");
	}
	private void loadPlotItemLore() throws InvalidConfigException {
		this.plotItemLore = this.config.getStringList("plot-item.lore");
	}

	// messages.yml loaders
	private void loadMessages(){
		ChunkyPlots plugin = ChunkyPlots.getInstance();
		Logger logger = plugin.getLogger();

		messages = new HashMap<>();
		for(Message key : Message.values()){
			String messageKey = key.name().toLowerCase();
			String message = this.messageConfig.getString("messages." + messageKey);
			if(message == null)
				logger.warning("'messages." + messageKey + "' in 'messages.yml' is not set");
			messages.put(key, message);
		}
	}

	// flags.yml loaders
	private void loadDefaultFlagValues() throws InvalidConfigException {
		this.defaultFlags = new HashMap<>();
		for(Flag flag : Flag.values()){
			String flagKey = flag.name().toUpperCase();
			if(!this.flagConfig.contains("default-flags." + flagKey))
				throw new InvalidConfigException("Flag '" + flagKey + "' does not have a default value set in default_flags.yml");

			this.defaultFlags.put(flag, this.flagConfig.getBoolean("default-flags." + flagKey));
		}
	}

	public static void reload(){
		instance = new Config();
	}

	public static Config getInstance(){
		if(instance == null){
			instance = new Config();
		}
		return instance;
	}

	// config.yml getters
	public boolean isUsingMessagePrefix() {
		return isUsingMessagePrefix;
	}
	public String getPrefix() {
		return this.prefix;
	}
	public String getPrefixSpacer() {
		return this.prefixSpacer;
	}

	public Material getPlotItemMaterial() {
		return plotItemMaterial;
    }
	public String getPlotItemName() {
		return plotItemName;
	}
	public List<String> getPlotItemLore() {
		return plotItemLore;
	}

	// messages.yml getters
	public String getMessage(Message type){
		return messageConfig.getString("messages." + type.name().toLowerCase());
	}

	// flags.yml getters
	public boolean getDefaultFlagValue(Flag flag){
		return defaultFlags.get(flag);
	}
	public HashMap<Flag, Boolean> getDefaultFlags() {
		return new HashMap<>(defaultFlags);
	}
}
