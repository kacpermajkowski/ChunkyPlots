package pl.kacpermajkowski.ChunkyPlots.manager;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import pl.kacpermajkowski.ChunkyPlots.ChunkyPlots;
import pl.kacpermajkowski.ChunkyPlots.basic.VisitPoint;

import java.util.ArrayList;
import java.util.List;

public class VisitManager implements Listener {
	private static VisitManager instance;

	private List<VisitPoint> visitPoints = new ArrayList<>();

	private VisitManager() {
		loadVisitPoints();
		ChunkyPlots.getInstance().getServer().getPluginManager().registerEvents(this, ChunkyPlots.getInstance());
	}

	@EventHandler
	public void onPluginDisable(final PluginDisableEvent e) {
		saveVisitPoints();
	}

	public void createVisitPoint(VisitPoint visitPoint){
		visitPoints.add(visitPoint);
	}

	public void deleteVisitPoint(VisitPoint visitPoint){
		visitPoints.remove(visitPoint);
	}

	public VisitPoint getVisitPoint(String name){
		for(VisitPoint visitPoint:visitPoints){
			if(visitPoint.getName().equalsIgnoreCase(name)) return visitPoint;
		}
		return null;
	}
	public List<VisitPoint> getVisitPoints() { return visitPoints; }

	private void loadVisitPoints(){
		//TODO: Loading visit points from file
	}

	private void saveVisitPoints(){
		//TODO: Saving visit points to file
	}

	public static VisitManager getInstance(){
		if(instance == null)
			instance = new VisitManager();
		return instance;
	}
}
