package it.polito.dp2.RNS.sol3.admClient;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import it.polito.dp2.RNS.PlaceReader;


public class SolPlaceReader implements PlaceReader {

	// Defining variables 
	private String id; 
	private BigInteger capacity; 
	private Map<String, SolPlaceReader> places;
	
	// Constructor
	public SolPlaceReader(String id, BigInteger capacity) {
		// Initializing variables 
		this.id = id; 
		this.capacity = capacity; 
		this.places = new HashMap<String, SolPlaceReader>(); 
	}

	@Override
	public String getId() {
		// Return the place id 
		return this.id;
	}
	
	@Override
	public int getCapacity() {
		// Return the capacity 
		return this.capacity.intValueExact();
	}

	@Override
	public Set<PlaceReader> getNextPlaces() {
		//return the set of connected places
		return new LinkedHashSet<PlaceReader>(this.places.values());
	}

	public boolean hasPrefix(String arg0) {
		//check if the id of that place has the received prefix
		//if arg0 == null return true, means to select the current place
		//otherwise check if arg0 is a prefix of the place id
		if(arg0 == null)
			return true;
		
		return this.getId().startsWith(arg0);
	}

	public void addConnection(SolPlaceReader to) {
		//add a place to the list of connected places 
		this.places.put(to.getId(), to); 
		
	}
	
	public boolean containsId(String id){
		if(this.places.containsKey(id))
			return true; 
		else
			return false; 
	}	

}
