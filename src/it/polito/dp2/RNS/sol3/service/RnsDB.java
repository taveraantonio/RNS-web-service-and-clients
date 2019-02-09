package it.polito.dp2.RNS.sol3.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList; 
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.InternalServerErrorException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.RNS.ConnectionReader;
import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.ParkingAreaReader;
import it.polito.dp2.RNS.RnsReader;
import it.polito.dp2.RNS.RnsReaderException;
import it.polito.dp2.RNS.RnsReaderFactory;
import it.polito.dp2.RNS.RoadSegmentReader;

import it.polito.dp2.RNS.sol3.jaxb.ObjectFactory;
import it.polito.dp2.RNS.sol3.jaxb.ParkingArea;
import it.polito.dp2.RNS.sol3.jaxb.ParkingArea.Services;
import it.polito.dp2.RNS.sol3.jaxb.Connection;
import it.polito.dp2.RNS.sol3.jaxb.EntranceRequest;
import it.polito.dp2.RNS.sol3.jaxb.ExitRequest;
import it.polito.dp2.RNS.sol3.jaxb.GateType;
import it.polito.dp2.RNS.sol3.jaxb.MoveRequest;
import it.polito.dp2.RNS.sol3.jaxb.Place;
import it.polito.dp2.RNS.sol3.jaxb.RoadSegment;
import it.polito.dp2.RNS.sol3.jaxb.Vehicle;
import it.polito.dp2.RNS.sol3.jaxb.VehicleState;
import it.polito.dp2.RNS.sol3.jaxb.VehicleType;
import it.polito.dp2.RNS.sol3.jaxb.Vstate;


public class RnsDB {
	
	private static RnsDB rnsDB = new RnsDB();			
	private static long lastConnectionId = 0; 			//id for connections
	private RnsReader monitor;							//rns reader object, random gen 
	private ObjectFactory ob; 							//object factory for RnsSystem.xsd
	private PathFinder pathFinder; 						//neo4j apis
	
	//server information storage concurrent maps 
	//for vehicles
	private ConcurrentHashMap<String, VehicleExt> vehiclesById; 
	//for places
	private ConcurrentHashMap<String,PlaceExt> placesById;
	private ConcurrentHashMap<String, PlaceExt> roadSegmentsById;
	private ConcurrentHashMap<String, PlaceExt> parkingAreasById; 
	private ConcurrentHashMap<String, PlaceExt> gatesById; 
	//for connections
	private ConcurrentHashMap<String, Connection> connectionsById; 
	
	private RnsDB(){
		 
		//initialize service structures  
		this.vehiclesById = new ConcurrentHashMap<String, VehicleExt>(); 
		this.placesById = new ConcurrentHashMap<String, PlaceExt>(); 
		this.roadSegmentsById = new ConcurrentHashMap<String, PlaceExt>(); 
		this.parkingAreasById = new ConcurrentHashMap<String, PlaceExt>(); 
		this.gatesById = new ConcurrentHashMap<String, PlaceExt>(); 
		this.connectionsById = new ConcurrentHashMap<String, Connection>(); 
		this.ob = new ObjectFactory(); 
		this.pathFinder = new PathFinder();
		
		//first load the data from the random generator 
		try{
			//System.setProperty("it.polito.dp2.RNS.RnsReaderFactory", "it.polito.dp2.RNS.Random.RnsReaderFactoryImpl");
			RnsReaderFactory factory = RnsReaderFactory.newInstance();
			this.monitor = factory.newRnsReader();
		}catch(RnsReaderException re){
			re.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		this.getRnsPlaces(); 
		this.getRnsConnections(); 
		pathFinder.loadToNeo4j(this.placesById.values());
	}

	
	private void getRnsPlaces(){
		
		Set<GateReader> gates = this.monitor.getGates(null); 
		for(GateReader gate : gates){
			Place p = this.ob.createPlace(); 
			p.setPlaceID(gate.getId());
			p.setCapacity(BigInteger.valueOf(gate.getCapacity()));
			
			p.setGate(GateType.valueOf(gate.getType().toString()));
			addPlace(p, "gate"); 
		}
		
		Set<RoadSegmentReader> roadsegments = this.monitor.getRoadSegments(null);
		for(RoadSegmentReader roadsegment : roadsegments){
			Place p = this.ob.createPlace(); 
			p.setPlaceID(roadsegment.getId());
			p.setCapacity(BigInteger.valueOf(roadsegment.getCapacity()));
			
			RoadSegment rs = this.ob.createRoadSegment(); 
			rs.setRoadName(roadsegment.getRoadName());
			rs.setRoadSegmentName(roadsegment.getName());
			p.setRoadSegment(rs);
			addPlace(p, "roadsegment");
		}
		
		Set<ParkingAreaReader> parkingareas = this.monitor.getParkingAreas(null); 
		for(ParkingAreaReader parkingarea: parkingareas){
			Place p = this.ob.createPlace(); 
			p.setPlaceID(parkingarea.getId());
			p.setCapacity(BigInteger.valueOf(parkingarea.getCapacity()));
			
			ParkingArea pa = this.ob.createParkingArea(); 
			Services s = this.ob.createParkingAreaServices();
			if(!parkingarea.getServices().isEmpty())
				s.getServiceName().addAll(parkingarea.getServices());
			pa.setServices(s);
			p.setParkingArea(pa);
			addPlace(p, "parkingarea"); 
		}
		return; 
	}
	
	private synchronized void addPlace(Place p, String string) {
		
		if(p.getPlaceID() != null){
			if(!this.placesById.containsKey(p.getPlaceID())){
			
				PlaceExt pe = new PlaceExt(p.getPlaceID(), p); 
				this.placesById.putIfAbsent(p.getPlaceID(), pe); 
				
				if(string.equals("gate"))
					this.gatesById.putIfAbsent(p.getPlaceID(), pe);
				else if(string.equals("parkingarea"))
					this.parkingAreasById.putIfAbsent(p.getPlaceID(), pe); 
				else if(string.equals("roadsegment"))
					this.roadSegmentsById.putIfAbsent(p.getPlaceID(), pe); 
			}
		}	
	}

	
	private void getRnsConnections() {
		
		Set<ConnectionReader> connections = this.monitor.getConnections();
		for(ConnectionReader connection: connections){
			Connection c = this.ob.createConnection(); 
			c.setFrom(connection.getFrom().getId());
			c.setTo(connection.getTo().getId());
			
			if(!this.placesById.get(c.getFrom()).getPlace().getConnectedTo().contains(c.getTo())){
				this.placesById.get(c.getFrom()).getPlace().getConnectedTo().add(c.getTo()); 
			}
			
			addConnection(c); 
		}
	}
	
	private synchronized void addConnection(Connection c) {
		String id = this.getNextConnectionId(); 
		c.setId(id);
		this.connectionsById.put(id, c); 
		return; 
	}

	

	public static RnsDB getDB() {
		return rnsDB;	
	}
	
	public synchronized String getNextConnectionId(){
		return Long.toString(++lastConnectionId); 
	}

	public synchronized List<String> addVehicle(EntranceRequest request) {
			
		//only for debug
		//System.out.println("1)Plate: "+ request.getPlateID());
		//System.out.println("1)Destination: " + request.getDestination());
		//System.out.println("1)Source: " + request.getPosition());
				
		//check plateid correctness
		if(!request.getPlateID().matches("([A-Z]+[0-9]|[0-9]+[A-Z])[A-Z0-9]*")){
			System.out.println("Error here"+request.getPlateID());
			throw new ClientErrorException(403); 
		}
		
		//check if vehicle is already in the system 
		if(this.vehiclesById.containsKey(request.getPlateID()))
			throw new ClientErrorException(403); 
		
		//check if destination place exist in the system
		if(!this.placesById.containsKey(request.getDestination())){
			System.out.println("Destination Error");
			throw new ClientErrorException(404); 
		}
		
		//check if the current position is a gate and exist in the systemm
		if(!this.gatesById.containsKey(request.getPosition())){
			if(!this.placesById.containsKey(request.getPosition())){
				System.out.println("Source Error");
				throw new ClientErrorException(404); 
			}else{
				System.out.println("Current position is not a gate or is not in the systems");
				throw new ClientErrorException(400); 
			}
		}
		
		//check entrance gate type
		String entrancePlaceType = this.placesById.get(request.getPosition()).getPlace().getGate().toString(); 
		if(entrancePlaceType == null || entrancePlaceType.equals("OUT")){
			System.out.println("Error in gate " + entrancePlaceType);
			throw new ClientErrorException(400); 
		}
			
		//compute shortest path for destination
		Set<List<String>> paths = new HashSet<List<String>>(); 
		try {
			paths = this.pathFinder.findShortestPaths(request.getPosition(), request.getDestination(), 0);
		} catch (UnknownIdException | BadStateException | PathServiceException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(); 
		}
			
		if(paths == null || paths.isEmpty()){
			//destination not reachable
			throw new ClientErrorException(403); 
			
		}else{
			//destination reachable, select one path
			List<String> path = paths.stream().findAny().get(); 
			
			//initialize new vehicle 
			Vehicle v = new Vehicle(); 
			GregorianCalendar gregorianCalendar = new GregorianCalendar();
		    DatatypeFactory datatypeFactory = null;
			try {
				datatypeFactory = DatatypeFactory.newInstance();
			} catch (DatatypeConfigurationException e) {
				e.printStackTrace();
			}
		    XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
			v.setEntryTime(now);
			v.setFrom(request.getPosition());
			v.setPlateID(request.getPlateID());
			v.setPosition(request.getPosition());
			v.setState(VehicleState.valueOf("IN_TRANSIT"));
			v.setTo(request.getDestination());
			v.setType(request.getVehicleType());
			
			VehicleExt newVehicle = new VehicleExt(v.getPlateID(), v);
			newVehicle.setPath(path);
			
			this.vehiclesById.putIfAbsent(newVehicle.getId(), newVehicle); 
			this.placesById.get(v.getPosition()).addVehicle(newVehicle.getId(), v);
		
			return path;
		}
		
	}

	public synchronized Vehicle deleteVehicle(String id, ExitRequest request ) {
		
		if(request.isClient() == false){
			//administrator, delete vehicle 
			VehicleExt ve = this.vehiclesById.get(id); 
			if(ve == null){
				throw new InternalServerErrorException(); 
			}
			//remove vehicle from current place
			PlaceExt pe = this.placesById.get(ve.getVehicle().getPosition());
			pe.removeVehicle(ve.getId());
			//remove vehicle from list
			this.vehiclesById.remove(id); 
			return ve.getVehicle(); 
			
		}else{
			//vehicle client
			//check if position is a known place
			if(!this.placesById.containsKey(request.getExitPosition())){
				throw new ClientErrorException(404);
			}
			
			//check vehicle 
			VehicleExt ve = this.vehiclesById.get(id); 
			if(ve == null){
				throw new InternalServerErrorException(); 
			}
			
			//check if current position == to exit position 
			if(request.getExitPosition().equals(ve.getVehicle().getPosition())){
				//same position
				//check if the vehicle position is a gate
				PlaceExt pe = this.gatesById.get(ve.getVehicle().getPosition());
				if(pe == null){
					//is not a gate 
					throw new ClientErrorException(409); 
				}
				
				//check if it is a OUT or INOUT gate
				if(pe.getPlace().getGate().name().equals("OUT") || pe.getPlace().getGate().name().equals("INOUT")){
					//if the gate is OUT or INOUT
					//remove vehicle from place
					pe.removeVehicle(ve.getId());
					//remove vehicle from vehicles 
					this.vehiclesById.remove(id);
					return ve.getVehicle(); 
				}else{
					throw new ClientErrorException(409); 
				}
			}else{
				//different position 
				//check if exit position is reachable from the previous current position of the vehicle 
				String previousPlace = this.placesById.get(ve.getVehicle().getPosition()).getId();
				String newCurrentPlace = request.getExitPosition();
				Set<List<String>> paths = null;
				try {
					paths = this.pathFinder.findShortestPaths(previousPlace, newCurrentPlace, 0);
				} catch (UnknownIdException | BadStateException | PathServiceException e) {
					e.printStackTrace();
				}
				if(paths.isEmpty() || paths == null){
					//new position not reachable from the previous position
					//wrong request by the client 
					System.out.println("Current exit position not reachable from the previous position"); 
					throw new ClientErrorException(409);
				}else{
					//destination reachable	
					//move vehicle to the new position
					//remove vehicle from current place
					this.placesById.get(previousPlace).removeVehicle(ve.getId());
					//update current position
					ve.getVehicle().setPosition(newCurrentPlace);
					//add vehicle to new place
					this.placesById.get(newCurrentPlace).addVehicle(ve.getId(), ve.getVehicle());
						
					//check if it is a gate 
					//check if the vehicle position is a gate
					PlaceExt pe = this.gatesById.get(ve.getVehicle().getPosition());
					if(pe == null){
						//is not a gate 
						throw new ClientErrorException(409); 
					}
						
					//check if it is a OUT or INOUT gate
					if(pe.getPlace().getGate().name().equals("OUT") || pe.getPlace().getGate().name().equals("INOUT")){
						//if the gate is OUT or INOUT
						//remove vehicle from place
						pe.removeVehicle(ve.getId());
						//remove vehicle from vehicles 
						this.vehiclesById.remove(id);
						//remove vehicle by its plate
						return ve.getVehicle(); 
					}else{
						throw new ClientErrorException(409); 
					}
				}			
			}//close different position
		}//close client 
		
	}

	public synchronized List<String> updatePosition(String id, MoveRequest moveRequest) {
		
		//System.out.println("2) move: " + moveRequest.getNewPosition());
		//check if the new position exist or not
		if(!this.placesById.containsKey(moveRequest.getNewPosition())){
			System.out.println("Error new position doesn't exist " + moveRequest.getNewPosition());
			throw new ClientErrorException(404); 
		}
		//check if vehicle exists
		VehicleExt ve = this.vehiclesById.get(id); 
		if(ve == null)
			throw new ClientErrorException(400);
		
		//get the previous current position of the vehicle
		PlaceExt oldCurrentPos = this.placesById.get(ve.getVehicle().getPosition()); 
		
		//check if current position is in the suggested path
		if(ve.getPath().getPath().contains(moveRequest.getNewPosition())){
			System.out.println("Current position in the suggested path. Update new position");
			//remove vehicle from current place
			oldCurrentPos.removeVehicle(ve.getId());
			//update current position
			ve.getVehicle().setPosition(moveRequest.getNewPosition());
			//add vehicle to new place
			PlaceExt currentPos = this.placesById.get(moveRequest.getNewPosition());
			currentPos.addVehicle(ve.getId(), ve.getVehicle());
			return null;
			
		}else{
			//check if the new position is reachable from the previous one
			String previousPlace = this.placesById.get(ve.getVehicle().getPosition()).getId();
			String newCurrentPlace = moveRequest.getNewPosition(); 
			String destinationPlace = this.placesById.get(ve.getVehicle().getTo()).getId(); 
			//compute the path between previous and new position
			try {
				Set<List<String>> paths = this.pathFinder.findShortestPaths(previousPlace, newCurrentPlace, 0);
				if(paths.isEmpty()){
					//new position not reachable from the previous position
					//wrong request by the client 
					System.out.println("Current position not reachable from the previous position"); 
					throw new ClientErrorException(403);
					
				}else{
					//destination reachable from the previous position
					System.out.println("Current position reachable from the previous position");
					//remove vehicle from current place
					this.placesById.get(previousPlace).removeVehicle(ve.getId());
					//update current position
					ve.getVehicle().setPosition(newCurrentPlace);
					//add vehicle to new place
					this.placesById.get(newCurrentPlace).addVehicle(ve.getId(), ve.getVehicle());
					//compute new suggested path 
					Set<List<String>> paths2 = this.pathFinder.findShortestPaths(newCurrentPlace, destinationPlace, 0);
					if(paths2.isEmpty()){
						System.out.println("But destination not reachable");
						//no new suggested path
						ve.removePath();
						return null;  
					}else{
						//select a path
						System.out.println("And destination reachable");
						List<String> path = paths2.stream().findAny().get(); 
						ve.removePath();
						ve.setPath(path);
						return path; 
					}
				}
			} catch (UnknownIdException | BadStateException | PathServiceException e) {
				e.printStackTrace();
			}
		}
		return null;
		
	}

	
	public synchronized String updateState(String id, Vstate state) {
		VehicleExt ve = this.vehiclesById.get(id); 
		if(ve == null){
			System.out.println("Vehicle not found");
			return null; 
		} 
		if(state.getState().name().equals("IN_TRANSIT") || state.getState().name().equals("PARKED")){
			System.out.println("State Changed");
			ve.getVehicle().setState(state.getState());
			return state.getState().toString(); 
		}else{
			System.out.println("Wrong state");
			throw new InternalServerErrorException(); 
		}
		
	}

	public synchronized List<Vehicle> getVehicles(String since, VehicleType type, VehicleState state) {
		return new LinkedList<Vehicle>(this.vehiclesById
										.values()
										.stream()
										.filter(p1 -> since == null || p1.getVehicle().getEntryTime().toGregorianCalendar().before(since))
										.filter(p2 -> type == null || p2.getVehicle().getType().name().equals(type))
										.filter(p3 -> state == null || p3.getVehicle().getState().name().equals(state))
										.map(p4 -> p4.getVehicle())
										.collect(Collectors.toList())); 
	}

	public synchronized Vehicle getVehicle(String id) {
		VehicleExt ve = this.vehiclesById.get(id); 
		if(ve == null)
			return null; 
		else{
			return ve.getVehicle();
		}
			
	}

	public synchronized Place getDestination(String id) {
		VehicleExt ve = this.vehiclesById.get(id); 
		if(ve == null)
			return null;
		if(ve.getVehicle().getTo() == null){
			throw new InternalServerErrorException(); 
		}
		PlaceExt pe = this.placesById.get(ve.getVehicle().getTo()); 
		if(pe == null)
			throw new ClientErrorException(400);
		else 
			return pe.getPlace();
	}

	public synchronized XMLGregorianCalendar getEntryTime(String id) {
		VehicleExt ve = this.vehiclesById.get(id); 
		if(ve == null)
			return null;
		return ve.getVehicle().getEntryTime();
	}

	public synchronized Place getOrigin(String id) {
		VehicleExt ve = this.vehiclesById.get(id); 
		if(ve == null)
			return null;
		PlaceExt pe = this.placesById.get(ve.getVehicle().getFrom()); 
		if(pe == null)
			throw new ClientErrorException(400); 
		return pe.getPlace(); 
	}

	public synchronized Place getPosition(String id) {
		VehicleExt ve = this.vehiclesById.get(id); 
		if(ve == null)
			return null;
		PlaceExt pe = this.placesById.get(ve.getVehicle().getPosition()); 
		if(pe == null)
			throw new ClientErrorException(400); 
		return pe.getPlace(); 
	}

	public synchronized VehicleState getState(String id) {
		VehicleExt ve = this.vehiclesById.get(id); 
		if(ve == null)
			return null;
		return ve.getVehicle().getState();
	}

	public synchronized VehicleType getType(String id) {
		VehicleExt ve = this.vehiclesById.get(id); 
		if(ve == null)
			return null;
		return ve.getVehicle().getType();
	}

	public synchronized List<String> getSuggestedPath(String id) {
		VehicleExt ve = this.vehiclesById.get(id); 
		if(ve == null)
			return null; 
		return ve.getPath().getPath();
		
	}

	public synchronized List<Place> getPlaces(String idPrefix) {
		return new LinkedList<Place>(this.placesById
				.values()
				.stream()
				.filter(p1 -> idPrefix == null || p1.getPlace().getPlaceID().startsWith(idPrefix))
				.map(p2 -> p2.getPlace())
				.collect(Collectors.toList())); 
	}

	public synchronized Place getPlace(String id) {
		PlaceExt pe = this.placesById.get(id); 
		if(pe == null)
			return null;
		return pe.getPlace();
	}

	public synchronized List<Place> getRoadSegments(String roadName) {
		if(this.roadSegmentsById.size() == 0)
			return null; 
		return new LinkedList<Place>(this.roadSegmentsById
				.values()
				.stream()
				.filter(p1 -> roadName == null || p1.getPlace().getRoadSegment().getRoadName().equals(roadName))
				.map(p2 -> p2.getPlace())
				.collect(Collectors.toList())); 
	}

	public synchronized List<Place> getGates(GateType type) {
		if(this.gatesById.size() == 0)
			return null; 
		return new LinkedList<Place>(this.gatesById
				.values()
				.stream()
				.filter(p1 -> type == null || p1.getPlace().getGate().name().equals(type))
				.map(p2 -> p2.getPlace())
				.collect(Collectors.toList())); 
	}

	public synchronized List<Place> getParkingAreas(List<String> services) {
		if(this.parkingAreasById.size() == 0)
			return null;
		return new LinkedList<Place>(this.parkingAreasById
				.values()
				.stream()
				.filter(p1 -> services == null || p1.getPlace().getParkingArea().getServices().getServiceName().containsAll(services))
				.map(p2 -> p2.getPlace())
				.collect(Collectors.toList())); 
	}

	public BigInteger getCapacity(String id) {
		PlaceExt pe = this.placesById.get(id); 
		if(pe == null)
			return null;
		return pe.getPlace().getCapacity();
	}

	public synchronized List<Place> getNextPlaces(String id) {
		PlaceExt pe = this.placesById.get(id); 
		if(pe == null)
			throw new ClientErrorException(404);
		List<Place> lpe = new ArrayList<Place>();
		List<String> conn = pe.getPlace().getConnectedTo();
		for (String s : conn){
			PlaceExt pe1 = this.placesById.get(s); 
			if(pe1 == null)
				throw new InternalServerErrorException();
			lpe.add(pe1.getPlace()); 
		}
		return lpe; 
		
	}

	public synchronized List<Vehicle> getPlaceVehicles(String id) {
		PlaceExt pe = this.placesById.get(id); 
		if(pe == null)
			throw new ClientErrorException(404);
		return pe.getVehicles().values().stream().collect(Collectors.toList());
	}

	public GateType getGateType(String id) {
		PlaceExt pe = this.gatesById.get(id); 
		if(pe == null)
			return null; 
		return pe.getPlace().getGate(); 
	}

	public String getRoadSegmentName(String id) {
		PlaceExt pe = this.roadSegmentsById.get(id); 
		if(pe == null)
			return null; 
		return pe.getPlace().getRoadSegment().getRoadSegmentName();
	}

	public String getRoadName(String id) {
		PlaceExt pe = this.roadSegmentsById.get(id); 
		if(pe == null)
			return null; 
		return pe.getPlace().getRoadSegment().getRoadName();
	}

	public List<String> getServices(String id) {
		PlaceExt pe = this.parkingAreasById.get(id); 
		if(pe == null)
			return null; 
		return pe.getPlace().getParkingArea().getServices().getServiceName();
	}

	public List<Connection> getConnections() {
		return this.connectionsById.values().stream().collect(Collectors.toList()); 
	}

	public synchronized Place getFrom(String id) {
		Connection co = this.connectionsById.get(id); 
		if(co == null)
			return null; 
		PlaceExt pe = this.placesById.get(co.getFrom()); 
		if(pe == null)
			throw new ClientErrorException(400); 
		return pe.getPlace(); 
	}

	public synchronized Place getTo(String id) {
		Connection co = this.connectionsById.get(id); 
		if(co == null)
			return null; 
	
		PlaceExt pe = this.placesById.get(co.getTo()); 
		if(pe == null)
			throw new ClientErrorException(400); 
		return pe.getPlace();
	}



}
