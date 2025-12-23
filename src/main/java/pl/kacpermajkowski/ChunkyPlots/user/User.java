package pl.kacpermajkowski.ChunkyPlots.user;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.kacpermajkowski.ChunkyPlots.plot.group.Group;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class User {
    @Getter
	private final UUID playerUUID;
    private final List<Group> groups = new ArrayList<>();

	@Setter
    @Getter
    private Plot cachedCurrentPlot;
	@Setter
    @Getter
    private boolean isBypassingRestrictions = false;
    @Setter
    @Getter
    private boolean isTeleportOnCooldown = false;
    @Setter
    @Getter
    private boolean isTeleporting = false;

	public User(final UUID playerUUID) {
		this.playerUUID = playerUUID;
		groups.add(new Group("all"));
	}
	public User(OfflinePlayer player) {
		this(player.getUniqueId());
	}

    // USER NAME
	public String getName(){
        return Bukkit.getServer().getOfflinePlayer(playerUUID).getName();
    }

    // GROUPS
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

	public Player getPlayer(){
		return Bukkit.getServer().getPlayer(playerUUID);
	}
}
