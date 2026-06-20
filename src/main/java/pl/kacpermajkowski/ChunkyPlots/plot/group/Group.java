package pl.kacpermajkowski.ChunkyPlots.plot.group;

import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import pl.kacpermajkowski.ChunkyPlots.plot.Plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Group {
	private final List<Plot> plots = new ArrayList<>();
	@Getter
    private final String name;
	@Getter
    private final boolean isDefault;

	public Group(){
		this.name = "all";
		isDefault = true;
	}

	public Group(String name){
		this.name = name;
		isDefault = false;
	}

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
