package pl.kacpermajkowski.ChunkyPlots.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pl.kacpermajkowski.ChunkyPlots.config.lang.Message;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

	private int defaultPlotLimit = 1;
	private final HashMap<String, Integer> groupPlotLimits = new HashMap<>();

	private final Map<String, String> worlds = new LinkedHashMap<>();
	private final Map<String, String> aliasToWorld = new HashMap<>();

	// messages.yml options
	private HashMap<Message, String> messages;

	public Config(JavaPlugin plugin){
		this.plugin = plugin;
		instance = this;
		loadFileConfigurations();
		loadConfigValues();
	}

	public Config(JavaPlugin plugin, FileConfiguration config, FileConfiguration messageConfig) {
		this.plugin = plugin;
		instance = this;
		this.config = config;
		this.messageConfig = messageConfig;
		loadConfigValues();
	}

	@Deprecated
	public static Config getInstance(){
		return instance;
	}

	public static void setInstance(Config config) {
		instance = config;
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
		InputStream defaultConfigStream = plugin.getResource("config.yml");
		if (defaultConfigStream != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
					new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8)
			);
			this.config.setDefaults(defaultConfig);
			this.config.options().copyDefaults(true);
			try {
				plugin.saveConfig();
			} catch (Exception ignored) {}
		}
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
			loadPlotLimits();
			loadWorlds();

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

	private void loadPlotLimits() {
		this.defaultPlotLimit = this.config.getInt("plot-limits.default", 1);
		this.groupPlotLimits.clear();
		ConfigurationSection section = this.config.getConfigurationSection("plot-limits.groups");
		if (section != null) {
			for (String group : section.getKeys(false)) {
				int limit = section.getInt(group, defaultPlotLimit);
				this.groupPlotLimits.put(group.toLowerCase(), limit);
			}
		}
	}

	private void loadWorlds() throws InvalidConfigException {
		this.worlds.clear();
		this.aliasToWorld.clear();

		ConfigurationSection section = this.config.getConfigurationSection("worlds");
		if (section == null) {
			section = this.config.getConfigurationSection("allowed-worlds");
		}

		if (section == null) {
			addWorldWithAlias("world", "");
			addWorldWithAlias("world_nether", "nether");
			addWorldWithAlias("world_end", "end");
			return;
		}

		Set<String> keys = section.getKeys(false);
		if (keys.isEmpty() && section.getDefaultSection() != null) {
			keys = section.getDefaultSection().getKeys(false);
		}

		if (keys.isEmpty()) {
			addWorldWithAlias("world", "");
			addWorldWithAlias("world_nether", "nether");
			addWorldWithAlias("world_end", "end");
			return;
		}

		for (String worldName : keys) {
			String alias = section.getString(worldName);
			if (alias == null) {
				alias = "";
			}
			addWorldWithAlias(worldName, alias.trim());
		}
	}

	private void addWorldWithAlias(String worldName, String alias) throws InvalidConfigException {
		String lowerAlias = alias.toLowerCase();
		if (aliasToWorld.containsKey(lowerAlias)) {
			String existingWorld = aliasToWorld.get(lowerAlias);
			throw new InvalidConfigException("Duplicate world alias '" + alias + "' for world '" + worldName
					+ "'. Already used by world '" + existingWorld + "'. World aliases must be unique.");
		}
		this.worlds.put(worldName, alias);
		this.aliasToWorld.put(lowerAlias, worldName);
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

	public int getDefaultPlotLimit() {
		return defaultPlotLimit;
	}

	public Map<String, Integer> getGroupPlotLimits() {
		return Collections.unmodifiableMap(groupPlotLimits);
	}

	public boolean isWorldAllowed(String worldName) {
		if (worldName == null) return false;
		for (String configuredWorld : worlds.keySet()) {
			if (configuredWorld.equalsIgnoreCase(worldName)) {
				return true;
			}
		}
		return false;
	}

	public Map<String, String> getWorldAliases() {
		return Collections.unmodifiableMap(worlds);
	}

	public Set<String> getAllowedWorlds() {
		return Collections.unmodifiableSet(worlds.keySet());
	}

	public String getWorldAlias(String worldName) {
		if (worldName == null) return null;
		for (Map.Entry<String, String> entry : worlds.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(worldName)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public String getWorldByAlias(String alias) {
		if (alias == null) return null;
		String lower = alias.trim().toLowerCase();
		String world = aliasToWorld.get(lower);
		if (world != null) {
			return world;
		}
		for (String configuredWorld : worlds.keySet()) {
			if (configuredWorld.equalsIgnoreCase(alias.trim())) {
				return configuredWorld;
			}
		}
		return null;
	}

	// messages.yml getters
	public String getMessage(Message type){
		return messageConfig.getString("messages." + type.name().toLowerCase());
	}
}
