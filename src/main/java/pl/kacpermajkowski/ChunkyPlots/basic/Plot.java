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
	private final HashSet<UUID> members = new HashSet<UUID>();
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

	public UUID getOwnerUUID(){
		return ownerUUID;
	}
	public String getOwnerName(){
		return Bukkit.getOfflinePlayer(ownerUUID).getName();
	}
	public HashMap<Flag, Boolean> getFlags() {
		return flags;
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
	public String getID(){
		return chunkX + ";" + chunkZ;
	}
	public HashSet<UUID> getBlacklist(){
		return blacklist;
	}
	public HashSet<UUID> getMembers(){
		return members;
	}



	public boolean isLocationInside(Location location){
		return location.getChunk().getX() == chunkX && location.getChunk().getZ() == chunkZ;
	}

	public boolean isPlayerMember(UUID playerUUID){
		return members.contains(playerUUID);
	}
	public boolean isPlayerMember(OfflinePlayer player){
		return members.contains(player.getUniqueId());
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
