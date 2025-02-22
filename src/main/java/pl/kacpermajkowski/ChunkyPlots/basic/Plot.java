package pl.kacpermajkowski.ChunkyPlots.basic;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.config.Config;

import java.util.*;

public class Plot {
	private UUID ownerUUID;
	private final HashSet<UUID> blacklist = new HashSet<UUID>();
	private final HashSet<UUID> whitelist = new HashSet<UUID>();
	private HashMap<Flag, Boolean> flags = new HashMap<Flag, Boolean>(Config.getInstance().getDefaultFlags());
	private final int chunkX;
	private final int chunkZ;
	private final String worldName;
	private final UUID uuid;

	public Plot(Player owner, Chunk chunk){
		this(owner.getUniqueId(), chunk.getX(), chunk.getZ(), chunk.getWorld().getName(), UUID.randomUUID());
	}

	public Plot(UUID ownerUUID, int chunkX, int chunkZ, String worldName, UUID uuid){
		this.ownerUUID = ownerUUID;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.worldName = worldName;
		this.uuid = uuid;
	}

	public void setFlag(Flag flag, boolean value){ flags.put(flag, value); }
	public void whitelistPlayer(OfflinePlayer player){
		whitelistPlayer(player.getUniqueId());
	}
	public void whitelistPlayer(UUID playerUUID){
		if(whitelist.contains(playerUUID)) return;
		if(blacklist.contains(playerUUID)){
			throw new IllegalArgumentException("Cannot add a blacklisted player to whitelist");
		}
		whitelist.add(playerUUID);
	}
	public void blacklistPlayer(OfflinePlayer player){
		blacklistPlayer(player.getUniqueId());
	}
	public void blacklistPlayer(UUID playerUUID){
		if(blacklist.contains(playerUUID)) return;
		if(whitelist.contains(playerUUID)){
			throw new IllegalArgumentException("Cannot add a whitelisted player to blacklist");
		}
		blacklist.add(playerUUID);
	}
	public void unwhitelistPlayer(OfflinePlayer player){
		unwhitelistPlayer(player.getUniqueId());
	}
	public void unwhitelistPlayer(UUID playerUUID){
		if(!whitelist.contains(playerUUID)) return;
		whitelist.remove(playerUUID);
	}
	public void unblacklistPlayer(OfflinePlayer player){
		unblacklistPlayer(player.getUniqueId());
	}
	public void unblacklistPlayer(UUID playerUUID){
		if(!blacklist.contains(playerUUID)) return;
		blacklist.remove(playerUUID);
	}

	public UUID getOwnerUUID(){
		return ownerUUID;
	}
	public String getOwnerName(){
		return Bukkit.getOfflinePlayer(ownerUUID).getName();
	}
	public Map<Flag, Boolean> getFlags() {
		return Collections.unmodifiableMap(flags);
	}
	public int getChunkX() {
		return chunkX;
	}
	public int getChunkZ() {
		return chunkZ;
	}
	public String getWorldName(){
		return worldName;
	}
	public UUID getUUID(){
		return uuid;
	}
	@Deprecated
	public String getID(){
		return chunkX + ";" + chunkZ;
	}
	public Set<UUID> getBlacklist(){
		return Collections.unmodifiableSet(blacklist);
	}
	public Set<UUID> getWhitelist(){
		return Collections.unmodifiableSet(whitelist);
	}


	public boolean isLocationInside(Location location){
		return location.getChunk().getX() == chunkX && location.getChunk().getZ() == chunkZ;
	}

	public boolean isPlayerWhitelisted(UUID playerUUID){
		return whitelist.contains(playerUUID);
	}
	public boolean isPlayerWhitelisted(OfflinePlayer player){
		return whitelist.contains(player.getUniqueId());
	}

	public boolean isPlayerBlacklisted(UUID playerUUID){
		return blacklist.contains(playerUUID);
	}
	public boolean isPlayerBlacklisted(OfflinePlayer player){
		return blacklist.contains(player.getUniqueId());
	}

	public boolean isPlayerOwner(UUID playerUUID){
		return ownerUUID.equals(playerUUID);
	}
	public boolean isPlayerOwner(OfflinePlayer player){
		return ownerUUID.equals(player.getUniqueId());
	}
	public boolean hasTheSameOwnerAs(Plot plot){
		if(plot == null) {
			return false;
		} else {
			return ownerUUID.equals(plot.getOwnerUUID());
		}
	}
}
