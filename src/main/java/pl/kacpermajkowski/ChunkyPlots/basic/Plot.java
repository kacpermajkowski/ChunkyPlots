package pl.kacpermajkowski.ChunkyPlots.basic;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.config.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Plot {
	private String ownerNickname;
	public List<String> blacklist = new ArrayList<String>();
	public List<String> members = new ArrayList<String>();
	public HashMap<Flag, Boolean> flags = new HashMap<Flag, Boolean>(Config.getInstance().getDefaultFlags());
	private int chunkX;
	private int chunkZ;
	private String worldName;
	private UUID uuid;

	public Plot(Player player, Chunk chunk){
		this.ownerNickname = player.getName();
		this.chunkX = chunk.getX();
		this.chunkZ = chunk.getZ();
		this.worldName = chunk.getWorld().getName();
		this.uuid = UUID.randomUUID();
	}
	
	public Plot(String ownerNickname, int chunkX, int chunkZ, String worldName, UUID uuid){
		this.ownerNickname = ownerNickname;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.worldName = worldName;
		this.uuid = uuid;
	}

	public void setFlag(Flag flag, boolean value){ flags.put(flag, value); }

	public String getOwnerNickname(){
		return ownerNickname;
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
	public List<String> getBlacklist(){
		return blacklist;
	}
	public List<String> getMembers(){
		return members;
	}

	public boolean isLocationInside(Location location){
		return location.getChunk().getX() == chunkX && location.getChunk().getZ() == chunkZ;
	}
	public boolean isPlayerOwner(Player player){
		if(player.getName().equals(ownerNickname)){
			return true;
		} else {
			return false;
		}
	}
	public boolean isPlayerMember(Player player){
		if(members.contains(player.getName())){
			return true;
		} else {
			return false;
		}
	}
	public boolean hasTheSameOwnerAs(Plot plot){
		if(plot == null) return false;
		else return ownerNickname.equals(plot.getOwnerNickname());
	}
}
