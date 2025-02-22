package pl.kacpermajkowski.ChunkyPlots.basic;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Group {
	private final List<Plot> plots = new ArrayList<>();
	private String name;

	public Group(String name){
		this.name = name;
	}

	public String getName() { return name; }
	public List<Plot> getPlots(){
		return Collections.unmodifiableList(plots);
	}

	public void add(Plot plot){
		if(!contains(plot)){
			plots.add(plot);
		}
	}

	public void remove(Plot plot){
		if(contains(plot)){
			plots.remove(plot);
		}
	}

	public boolean contains(Plot plot){
		return plots.contains(plot);
	}
	public boolean contains(UUID plotUUID){
		if(plotUUID == null) return false;
		for(Plot plot: plots){
			if(plot.getUUID().equals(plotUUID)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (!(o instanceof Group group)) return false;

        return new EqualsBuilder()
				.append(plots, group.plots)
				.append(name, group.name)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(plots)
				.append(name)
				.toHashCode();
	}
}
