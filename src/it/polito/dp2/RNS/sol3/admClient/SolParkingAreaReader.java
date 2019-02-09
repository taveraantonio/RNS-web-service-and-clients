package it.polito.dp2.RNS.sol3.admClient;

import java.util.HashSet;
import java.util.Set;

import it.polito.dp2.RNS.ParkingAreaReader;
import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.sol1.jaxb.ParkingArea.Services;
import it.polito.dp2.RNS.sol1.jaxb.ParkingArea.Services.Service;


public class SolParkingAreaReader implements ParkingAreaReader {

	// Defining variables 
	private SolPlaceReader place;
	private Set<String> services; 
	
	// Constructor
	public SolParkingAreaReader(SolPlaceReader place, Services s) {
		// Initialize first the variables
		this.place = place; 
		this.services = new HashSet<String>(); 
		
		// Set services received in input 
		for(Service service: s.getService()){
			this.services.add(service.getServiceName()); 
		}
		
	}

	@Override
	public int getCapacity() {
		// Return the capacity of the place
		return this.place.getCapacity(); 
	}

	@Override
	public Set<PlaceReader> getNextPlaces() {
		// Return the set of the connected place
		return this.place.getNextPlaces(); 
	}

	@Override
	public String getId() {
		// Return the place id 
		return this.place.getId(); 
	}

	@Override
	public Set<String> getServices() {
		// Return the set of services that place has
		return this.services;
	}
	

	public boolean hasServices(Set<String> arg0) {
		// Check if the current parking area has the specified set of services 
		// if the received set is null return true, means to select that parking area
		// otherwise check is all of these services are contained inside this.services 
		// return true if yes, false otherwise 
		if(arg0 == null)
			return true; 
		
		for(String s: arg0){
			if(!this.services.contains(s))
				return false; 
		}
		return true;
	}

}
