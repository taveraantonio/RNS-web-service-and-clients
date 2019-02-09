package it.polito.dp2.RNS.sol3.service;

import java.util.List;

import it.polito.dp2.RNS.sol3.jaxb.SuggestedPath;
import it.polito.dp2.RNS.sol3.jaxb.Vehicle;

public class VehicleExt {

	private String id; 
	private Vehicle vehicle; 
	private SuggestedPath path; 

	public VehicleExt(String id, Vehicle vehicle){
		this.setId(id); 
		this.setVehicle(vehicle); 
		this.path = new SuggestedPath(); 
	}

	public String getId() {
		return id;
	}

	public synchronized void setId(String id) {
		this.id = id;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public synchronized void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public SuggestedPath getPath() {
		return path;
	}

	public synchronized void setPath(List<String> path) {
		SuggestedPath p = new SuggestedPath(); 
		p.getPath().addAll(path); 
		this.path = p;
	}

	public synchronized void removePath() {
		this.path = new SuggestedPath(); 
		
	}
	
	
}
