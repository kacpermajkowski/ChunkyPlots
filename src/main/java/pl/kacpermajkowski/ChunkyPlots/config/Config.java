package pl.kacpermajkowski.ChunkyPlots.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Config {
	private static Config instance;

	private final JavaPlugin plugin;
	private FileConfiguration config;
	private FileConfiguration messageConfig;

	// config.yml options
	private boolean isUsingMessagePrefix;
	private String prefix;
	private String prefixSpacer;

	private Material plotItemMaterial;
	private String plotItemName;
	private List<String> plotItemLore;

	private Set<MessageDisplayType> messageDisplayTypes;

	// messages.yml options
	private HashMap<Message, String> messages;

	public Config(JavaPlugin plugin){
		this.plugin = plugin;
		instance = this;
		loadFileConfigurations();
		loadConfigValues();
	}

	@Deprecated
	public static Config getInstance(){
		return instance;
	}

	private void loadFileConfigurations() {
		File dataFolder = plugin.getDataFolder();
		dataFolder.mkdirs();

		plugin.saveDefaultConfig();
		List<File> files = List.of(
				new File(dataFolder, "messages.yml")
		);
		for (File file : files) {
			if(!file.exists())
				plugin.saveResource(file.getName(), false);
		}

		this.config = plugin.getConfig();
		File messagesFile = new File(dataFolder, "messages.yml");
		this.messageConfig = YamlConfiguration.loadConfiguration(messagesFile);

		InputStream defaultMessagesStream = plugin.getResource("messages.yml");
		if (defaultMessagesStream != null) {
			YamlConfiguration defaultMessages = YamlConfiguration.loadConfiguration(
					new InputStreamReader(defaultMessagesStream, StandardCharsets.UTF_8)
			);
			this.messageConfig.setDefaults(defaultMessages);
			this.messageConfig.options().copyDefaults(true);
			try {
				this.messageConfig.save(messagesFile);
			} catch (Exception ignored) {}
		}
	}

	private void loadConfigValues() {
		try {
			loadIsUsingMessagePrefix();
			loadPrefix();
			loadPrefixSpacer();
			loadPlotItemMaterial();
			loadPlotItemName();
			loadPlotItemLore();
			loadMessageDisplayTypes();

			loadMessages();
		} catch (InvalidConfigException e){
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
			plugin.getLogger().warning("'use-message-prefix' in 'config.yml' is not set. Prefix will not be displayed.");
		}
	}
	private void loadPrefix(){
		this.prefix = this.config.getString("prefix");
		if(this.prefix == null){
			plugin.getLogger().warning("'prefix' in 'config.yml' is not set. Using empty string instead.");
			this.prefix = "";
		}
	}
	private void loadPrefixSpacer(){
		this.prefixSpacer = this.config.getString("prefix-spacer");
		if(prefixSpacer == null){
			plugin.getLogger().warning("'prefix-spacer' in 'config.yml' is not set. Using empty string instead.");
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

	private void loadMessageDisplayTypes() throws InvalidConfigException {
		List<String> messageDisplayTypeStrings = this.config.getStringList("interaction-message-display");
		if(messageDisplayTypeStrings == null)
			throw new InvalidConfigException("'interaction-message-display' in 'config.yml' is not set");

		this.messageDisplayTypes = new HashSet<>();
		for(String messageDisplayType : messageDisplayTypeStrings){
			try {
				messageDisplayTypes.add(MessageDisplayType.valueOf(messageDisplayType.toUpperCase()));
			} catch (IllegalArgumentException ignored) {}
		}
		if(messageDisplayTypes.isEmpty()){
			throw new InvalidConfigException("'interaction-message-display' in 'config.yml' cannot be an empty list");
		}
	}

	// messages.yml loaders
	private void loadMessages(){
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

	public Set<MessageDisplayType> getMessageDisplayTypes() { return messageDisplayTypes; }

	// messages.yml getters
	public String getMessage(Message type){
		return messageConfig.getString("messages." + type.name().toLowerCase());
	}
}
