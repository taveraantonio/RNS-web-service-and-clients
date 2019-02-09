package it.polito.dp2.RNS.sol3.admClient;

import java.util.Set;

import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RoadSegmentReader;

public class SolRoadSegmentReader implements RoadSegmentReader {

	// Defining variables 
	private SolPlaceReader place; 
	private String roadSegmentName; 
	private String roadName; 
	
	// Constructor 
	public SolRoadSegmentReader(SolPlaceReader place, String roadSegmentName, String roadName) {
		// Initialize variables 
		this.place = place; 
		this.roadSegmentName = roadSegmentName; 
		this.roadName = roadName; 
	}

	@Override
	public int getCapacity() {
		// Returns the place capacity 
		return this.place.getCapacity(); 
	}

	@Override
	public Set<PlaceReader> getNextPlaces() {
		// Returns the set of places to which is connected to 
		return this.place.getNextPlaces(); 
	}

	@Override
	public String getId() {
		// Return its place id 
		return this.place.getId(); 
	}

	@Override
	public String getName() {
		// Return its name 
		return this.roadSegmentName; 
	}

	@Override
	public String getRoadName() {
		// Return the roadName 
		return this.roadName;
	}

	public boolean belongsToRoad(String arg0) {
		// Check if the current road segment belong to the specified road
		// if arg0 == null true is returned, means to select the current road segment
		// otherwise check if road name is equal to arg0 
		if(arg0 == null)
			return true; 
		return this.getRoadName().equals(arg0); 
	}

}
