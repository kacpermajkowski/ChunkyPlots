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
import java.util.function.Supplier;

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
		groups.add(new Group("all", playerUUID, true));
	}
	public User(OfflinePlayer player) {
		this(player.getUniqueId());
	}

    // USER NAME
	public String getName(){
        if (Bukkit.getServer() == null) return playerUUID.toString().substring(0, 8);
        OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(playerUUID);
        return op != null && op.getName() != null ? op.getName() : playerUUID.toString().substring(0, 8);
    }

    // GROUPS
	public List<Group> getGroups(){
		return Collections.unmodifiableList(groups);
	}

    public Group getDefaultGroup() {
        return getGroup("all");
    }

    public void bindPlotSupplier(Supplier<List<Plot>> supplier) {
        Group defaultGroup = getDefaultGroup();
        if (defaultGroup != null) {
            defaultGroup.setAllPlotsSupplier(supplier);
        }
    }

	public Group createGroup(String groupName){
        if (groupName == null) throw new IllegalArgumentException("Group name cannot be null");
        String cleanName = groupName.trim();
        if (cleanName.isEmpty()) throw new IllegalArgumentException("Group name cannot be empty");
        if (cleanName.equalsIgnoreCase("all")) throw new IllegalArgumentException("Cannot create group named 'all'");
        if (hasGroup(cleanName)) throw new IllegalArgumentException("Group '" + cleanName + "' already exists");

        Group newGroup = new Group(cleanName, playerUUID, false);
		groups.add(newGroup);
        return newGroup;
	}

    public void addGroup(Group group) {
        if (group == null) return;
        if (hasGroup(group.getName())) return;
        groups.add(group);
    }

	public boolean removeGroup(String groupName){
        if (groupName == null) return false;
        Group g = getGroup(groupName);
        if (g == null || g.isDefault()) return false;
		return groups.remove(g);
	}

	public Group getGroup(String groupName){
        if (groupName == null) return null;
        String clean = groupName.trim();
		for(Group group : groups){
			if(group.getName().equalsIgnoreCase(clean)) {
				return group;
			}
		}
		return null;
	}

	public boolean hasGroup(String groupName){
        return getGroup(groupName) != null;
	}

	public Player getPlayer(){
        if (Bukkit.getServer() == null) return null;
        return Bukkit.getServer().getPlayer(playerUUID);
	}
}
