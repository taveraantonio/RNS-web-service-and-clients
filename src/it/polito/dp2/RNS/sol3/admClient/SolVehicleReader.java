package it.polito.dp2.RNS.sol3.admClient;

import java.util.Calendar;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.VehicleReader;
import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;

public class SolVehicleReader implements VehicleReader {

	// Defining variables
	private String plateId;
	private SolPlaceReader destination, origin, position; 
	private VehicleState state; 
	private VehicleType type;
	private Calendar entryTime; 
	
	// Constructor
	public SolVehicleReader(String plateId,
			SolPlaceReader destination, 
			SolPlaceReader position, 
			SolPlaceReader origin, 
			XMLGregorianCalendar date,
			String state,
			String type) {
		
		// Initializing variables 
		this.plateId = plateId;
		this.destination = destination; 
		this.origin = origin; 
		this.position = position;
		this.entryTime = date.toGregorianCalendar(); 
		this.state = VehicleState.valueOf(state); 
		this.type = VehicleType.valueOf(type); 
	}

	@Override
	public String getId() {
		// Returns the plateId
		return this.plateId; 
	}

	@Override
	public PlaceReader getDestination() {
		// Returns the destination
		return this.destination;
	}

	@Override
	public Calendar getEntryTime() {
		// Returns the entry time 
		return this.entryTime; 
	}

	@Override
	public PlaceReader getOrigin() {
		// Returns the origin point 
		return this.origin;
	}

	@Override
	public PlaceReader getPosition() {
		// Returns the current position in the system 
		return this.position; 
	}

	@Override
	public VehicleState getState() {
		// Returns the vehicle state 
		return this.state;
	}

	@Override
	public VehicleType getType() {
		// Returns the vehicle type 
		return this.type; 
	}

	public boolean entranceSince(Calendar arg0) {
		// Check if entrance time and date is before arg0
		// if the received argument is null return true, means to select the current vehicle 
		if (arg0 == null)
			return true; 
		if(this.getEntryTime().before(arg0))
			return true; 
		else
			return false;
	}

	public boolean hasType(Set<VehicleType> arg1) {
		// Check if the vehicle has one of the type specified in the set arg1
		// if the received argument is null return true, means to select the current vehicle
		if(arg1 == null)
			return true;
		
		boolean returnValue = false; 
		for(VehicleType t: arg1){
			//compare this vehicle type to all the received type 
			if(this.getType().compareTo(t) == 0)
				returnValue = true; 
		}
		return returnValue; 
	}

	
	public boolean hasState(VehicleState arg2) {
		// Check if the vehicle has the specified state
		// if the received argument is null return true, means to select the current vehicle
		if(arg2 == null)
			return true; 
		
		if(this.getState().compareTo(arg2) == 0)
			return true;
		else
			return false;
	}


}
