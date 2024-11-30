package pl.kacpermajkowski.ChunkyPlots.basic;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.manager.MessageManager;

import java.util.UUID;


public class VisitPoint {
	private Location location;
	private String ownerName;
	private String name;
	private String description;
	private UUID plotUUID;
	public boolean isOpen = true;

	public VisitPoint(Location location, UUID plotUUID, String ownerName, String name, String description){
		if(description != null) this.description = description;
		else this.description = MessageManager.fixColors(ChunkyPlots.plugin.configManager.getMessage(MessageType.DEFAULT_VISIT_POINT_DESCRIPTION)).replace("%userName%", ownerName);
		this.ownerName = ownerName;
		this.location = location;
		this.name = name;
		this.plotUUID = plotUUID;
	}

	public Location getLocation() { return location; }
	public String getName() { return name; }
	public String getOwnerName() { return ownerName; }
	public String getDescription() { return description; }
	public UUID getPlotUUID() { return plotUUID; }
	public boolean isSafe() {
		Block feet = location.getBlock();
		if (feet.getType().isOccluding() && feet.getLocation().add(0, 1, 0).getBlock().getType().isOccluding()) return false;

		Block head = feet.getRelative(BlockFace.UP);
		if (head.getType().isOccluding()) return false;

		Block ground = feet.getRelative(BlockFace.DOWN);
		return ground.getType().isSolid();
	}
}
