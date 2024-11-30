package pl.kacpermajkowski.ChunkyPlots.manager;


import pl.kacpermajkowski.ChunkyPlots.basic.VisitPoint;

import java.util.ArrayList;
import java.util.List;

public class VisitManager {
	private List<VisitPoint> visitPoints = new ArrayList<>();

	public VisitManager(){ loadVisitPoints(); }
	//TODO: Find better solution to save visit points on server shutdown
	protected void finalize(){ saveVisitPoints(); }


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
}
