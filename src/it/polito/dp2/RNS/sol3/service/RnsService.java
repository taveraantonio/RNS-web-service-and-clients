package it.polito.dp2.RNS.sol3.service;

import java.math.BigInteger;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.RNS.sol3.jaxb.Connection;
import it.polito.dp2.RNS.sol3.jaxb.Capacity;
import it.polito.dp2.RNS.sol3.jaxb.Connections;
import it.polito.dp2.RNS.sol3.jaxb.EntranceRequest;
import it.polito.dp2.RNS.sol3.jaxb.ExitRequest;
import it.polito.dp2.RNS.sol3.jaxb.GateType;
import it.polito.dp2.RNS.sol3.jaxb.Gtype;
import it.polito.dp2.RNS.sol3.jaxb.MoveRequest;
import it.polito.dp2.RNS.sol3.jaxb.Name;
import it.polito.dp2.RNS.sol3.jaxb.Place;
import it.polito.dp2.RNS.sol3.jaxb.Places;
import it.polito.dp2.RNS.sol3.jaxb.Services;
import it.polito.dp2.RNS.sol3.jaxb.SuggestedPath;
import it.polito.dp2.RNS.sol3.jaxb.Time;
import it.polito.dp2.RNS.sol3.jaxb.Vehicle;
import it.polito.dp2.RNS.sol3.jaxb.VehicleState;
import it.polito.dp2.RNS.sol3.jaxb.VehicleType;
import it.polito.dp2.RNS.sol3.jaxb.Vehicles;
import it.polito.dp2.RNS.sol3.jaxb.Vstate;
import it.polito.dp2.RNS.sol3.jaxb.Vtype;

public class RnsService {

	private RnsDB db = RnsDB.getDB(); 
	
	public RnsService(){
	}
	
	
	public SuggestedPath createVehicle(EntranceRequest request) {
		SuggestedPath path = new SuggestedPath(); 
		List<String> returned = db.addVehicle(request); 
		if(returned == null){
			return null; 
		}else{
			path.getPath().addAll(returned); 
			return path;
		}
	}

	public Vehicle deleteVehicle(String id, ExitRequest request) {
		return db.deleteVehicle(id, request); 
	}

	public SuggestedPath updatePosition(String id, MoveRequest moveRequest) {
		SuggestedPath path = new SuggestedPath(); 
		List<String> returned = db.updatePosition(id, moveRequest);
		if(returned == null)
			return null; 
		else{
			path.getPath().addAll(returned); 
			return path; 
		}
	}

	public Vstate updateState(String id, Vstate state) {
		Vstate vs = new Vstate();
		String s = db.updateState(id, state);
		if(s == null){
			return null; 
		}else{
			vs.setState(VehicleState.valueOf(s));
			return vs; 
		}
	}
	
	public Vehicles getVehicles(String since, VehicleType type, VehicleState state, int page) {
		Vehicles vehicles = new Vehicles(); 
		vehicles.setTotalPages(BigInteger.valueOf(1));
		vehicles.setPage(BigInteger.valueOf(0));
		List<Vehicle> vehiclelist = vehicles.getVehicle();
		List<Vehicle> vl = db.getVehicles(since, type, state); 
		if(vl == null){
			return vehicles; 
		}else{
			vehiclelist.addAll(vl); 
			return vehicles; 
		}
		
	}

	public Vehicle getVehicle(String id) {
		return db.getVehicle(id); 
	}

	public Place getDestination(String id) {
		return db.getDestination(id); 
	}

	public Time getEntryTime(String id) {
		Time time = new Time(); 
		XMLGregorianCalendar t = db.getEntryTime(id); 
		if(t == null)
			return null; 
		else{
			time.setDateTime(t);
			return time;  	
		}
	}

	public Place getOrigin(String id) {
		return db.getOrigin(id); 
	}

	public Place getPosition(String id) {
		return db.getPosition(id); 
	}

	public Vstate getState(String id) {
		Vstate vs = new Vstate(); 
		VehicleState s = db.getState(id);
		if(s == null)
			return null; 
		else{
			vs.setState(s);
			return vs; 
		}
	}

	public Vtype getType(String id) {
		Vtype vt = new Vtype(); 
		VehicleType t = db.getType(id);
		if(t == null)
			return null; 
		else{
			vt.setType(t);
			return vt; 
		}
	}

	public SuggestedPath getPath(String id) {
		SuggestedPath sp = new SuggestedPath();
		List<String> path = db.getSuggestedPath(id);
		if(path == null){
			return null;
		}else{
			sp.getPath().addAll(path);
			return sp; 
		}
	}

	public Places getPlaces(String idPrefix, int page) {
		Places places = new Places(); 
		List<Place> placelist = places.getPlace();
		places.setTotalPages(BigInteger.valueOf(1));
		places.setPage(BigInteger.valueOf(0));
		List<Place> pl = db.getPlaces(idPrefix); 
		if(pl == null)
			return places; 
		else{
			placelist.addAll(pl); 
			return places; 
		}
		
	}

	public Place getPlace(String id) {
		return db.getPlace(id);
	}

	public Places getRoadSegments(String roadName, int page) {
		Places places = new Places(); 
		List<Place> placelist = places.getPlace();
		places.setTotalPages(BigInteger.valueOf(1));
		places.setPage(BigInteger.valueOf(0));
		List<Place> pl = db.getRoadSegments(roadName); 
		if(pl == null)
			return places; 
		else {
			placelist.addAll(pl); 
			return places;
		}
		
	}

	public Places getGates(GateType type, int page) {
		Places places = new Places(); 
		List<Place> placelist = places.getPlace();
		places.setTotalPages(BigInteger.valueOf(1));
		places.setPage(BigInteger.valueOf(0));
		List<Place> pl = db.getGates(type);
		if(pl ==null)
			return places; 
		else{
			placelist.addAll(pl); 
			return places; 
		}		
		
	}

	public Places getParkingAreas(List<String> services, int page) {
		Places places = new Places(); 
		List<Place> placelist = places.getPlace();
		places.setTotalPages(BigInteger.valueOf(1));
		places.setPage(BigInteger.valueOf(0));
		List<Place> pl = db.getParkingAreas(services); 
		if(pl == null)
			return places; 
		else{
			placelist.addAll(pl);
			return places; 
		}
	}

	public Capacity getCapacity(String id) {
		Capacity c = new Capacity(); 
		BigInteger cap = db.getCapacity(id); 
		if(cap == null)
			return null; 
		else{
			c.setCapacity(cap);
			return c;
		}
	}

	public Places getNextPlaces(String id, int page) {
		Places places = new Places(); 
		List<Place> placelist = places.getPlace();
		places.setTotalPages(BigInteger.valueOf(1));
		places.setPage(BigInteger.valueOf(0));
		
		List<Place> pl = db.getNextPlaces(id); 
		if(pl == null)
			return places; 
		else{
			placelist.addAll(pl); 
			return places;
		}
	}

	public Vehicles getPlaceVehicles(String id, int page) {
		Vehicles vehicles = new Vehicles(); 
		vehicles.setTotalPages(BigInteger.valueOf(1));
		vehicles.setPage(BigInteger.valueOf(0));
		List<Vehicle> vehiclelist = vehicles.getVehicle();
		List<Vehicle> vl = db.getPlaceVehicles(id); 
		if(vl == null)
			return vehicles; 
		else{
			vehiclelist.addAll(vl); 
			return vehicles; 
		}
	}

	public Gtype getGateType(String id) {
		Gtype type = new Gtype(); 
		GateType t = db.getGateType(id); 
		if(t == null)
			return null; 
		else{
			type.setType(t);
			return type; 
		}
	}

	public Name getRoadSegmentName(String id) {
		Name name = new Name();
		String n = db.getRoadSegmentName(id); 
		if(n == null)
			return null; 
		else{
			name.setName(n);
			return name;
		}
	}

	public Name getRoadName(String id) {
		Name name = new Name(); 
		String n = db.getRoadName(id); 
		if(n == null)
			return null;
		else{
			name.setName(n);
			return name; 
		}
	}

	public Services getServices(String id) {
		Services services = new Services();
		List<String> s = db.getServices(id); 
		if(s == null)
			return null; 
		else{
			services.getService().addAll(s); 
			return services; 
		}
	}

	public Connections getConnections(int page) {
		Connections connections = new Connections(); 
		connections.setTotalPages(BigInteger.valueOf(1));
		connections.setPage(BigInteger.valueOf(0));
		List<Connection> connectionlist = connections.getConnection();
		List<Connection> cl = db.getConnections(); 
		if(cl == null)
			return connections; 
		else{
			connectionlist.addAll(cl); 
			return connections; 
		}
			
	}

	public Place getFrom(String id) {
		return db.getFrom(id); 
	}

	public Place getTo(String id) {
		return db.getTo(id); 
	}

	

}
