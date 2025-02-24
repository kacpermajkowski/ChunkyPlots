package pl.kacpermajkowski.ChunkyPlots.basic;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class User {
	private final UUID playerUUID;
	private Plot currentPlot;
	private boolean isBypassingRestrictions, isTeleportOnCooldown, isTeleporting = false;
	private List<Group> groups = new ArrayList<>();

	public User(UUID playerUUID) {
		this.playerUUID = playerUUID;
		groups.add(new Group("all"));
	}
	public User(OfflinePlayer player) {
		this(player.getUniqueId());
	}
	public UUID getPlayerUUID() {
		return playerUUID;
	}
	public String getName(){ return Bukkit.getServer().getOfflinePlayer(playerUUID).getName(); }
	public List<Group> getGroups(){
		return Collections.unmodifiableList(groups);
	}
	public void createGroup(String groupName){
		groups.add(new Group(groupName));
	}
	public void removeGroup(String groupName){
		groups.remove(getGroup(groupName));
	}
	public Group getGroup(String groupName){
		for(Group group:groups){
			if(group.getName().equals(groupName)) {
				return group;
			}
		}
		return null;
	}
	public boolean hasGroup(String groupName){
		for(Group group:groups){
			if(group.getName().equals(groupName)) {
				return true;
			}
		}
		return false;
	}

	public boolean isBypassingRestrictions() {
		return isBypassingRestrictions;
	}

	public void setBypassingRestrictions(boolean bypassingRestrictions) {
		isBypassingRestrictions = bypassingRestrictions;
	}

	public Plot getCurrentPlot(){
		return currentPlot;
	}

	public void setCurrentPlot(Plot currentPlot) {
		this.currentPlot = currentPlot;
	}

	public boolean isTeleportOnCooldown() {
		return isTeleportOnCooldown;
	}

	public void setTeleportOnCooldown(boolean teleportOnCooldown) {
		this.isTeleportOnCooldown = teleportOnCooldown;
	}

	public boolean isTeleporting() {
		return isTeleporting;
	}

	public void setTeleporting(boolean teleporting) {
		isTeleporting = teleporting;
	}
}
