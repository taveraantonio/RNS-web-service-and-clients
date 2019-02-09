package it.polito.dp2.RNS.sol3.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.polito.dp2.RNS.sol3.jaxb.Place;
import it.polito.dp2.RNS.sol3.jaxb.Vehicle;


public class PlaceExt {
	
	private String id; 
	private Place place; 
	private Map<String, Vehicle> vehicles; 
	
	public PlaceExt(String id, Place place) {
		this.setId(id);
		this.setPlace(place);
		this.vehicles = new ConcurrentHashMap<String, Vehicle>(); 
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

	public synchronized Map<String, Vehicle> getVehicles() {
		return vehicles;
	}

	public synchronized void addVehicle(String id, Vehicle vehicle){
		this.vehicles.putIfAbsent(id, vehicle); 
	}
	
	public synchronized void removeVehicle(String id){
		this.vehicles.remove(id);
	}
	
	public synchronized Vehicle getVehicle(String id){
		return this.vehicles.get(id); 
	}

}
