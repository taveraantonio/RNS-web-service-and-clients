package it.polito.dp2.RNS.sol3.admClient;

import java.util.Set;

import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.PlaceReader;



public class SolGateReader implements GateReader {

	// Defining private variable 
	private SolPlaceReader place; 
	private GateType type; 
	
	// Constructor 
	public SolGateReader(SolPlaceReader place, String type){
		// Initialize variables
		this.place = place;
		this.type = GateType.fromValue(type);
		
	}
	
	
	@Override
	public int getCapacity() {
		// Return the place capacity
		return this.place.getCapacity();
	}

	@Override
	public Set<PlaceReader> getNextPlaces() {
		// Return the set of connected places
		return this.place.getNextPlaces();
	}

	@Override
	public String getId() {
		// Return the id 
		return this.place.getId();
	}

	@Override
	public GateType getType() {
		// Return the gate type 
		return this.type;
	}


	public boolean hasType(GateType arg0) {
		// Check if the current gate has the type specified in arg0
		// if arg0 = null return true, means to select that gate 
		// otherwise check if arg0 e this type are the same 
		if(arg0 == null)
			return true;
		
		if(this.getType().compareTo(arg0) == 0)
			return true; 
		
		return false; 
	}

	
}
