package it.polito.dp2.RNS.sol3.admClient;

import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.datatype.XMLGregorianCalendar;

import it.polito.dp2.RNS.ConnectionReader;
import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.ParkingAreaReader;
import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RoadSegmentReader;
import it.polito.dp2.RNS.VehicleReader;
import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.AdmClient;
import it.polito.dp2.RNS.lab3.AdmClientException;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.sol1.jaxb.ParkingArea.Services;
import it.polito.dp2.RNS.sol1.jaxb.ParkingArea.Services.Service;
import it.polito.dp2.RNS.sol3.jaxb.Connection;
import it.polito.dp2.RNS.sol3.jaxb.Connections;
import it.polito.dp2.RNS.sol3.jaxb.Place;
import it.polito.dp2.RNS.sol3.jaxb.Places;
import it.polito.dp2.RNS.sol3.jaxb.RnsRoot;
import it.polito.dp2.RNS.sol3.jaxb.Vehicle;
import it.polito.dp2.RNS.sol3.jaxb.Vehicles;

/**
* AdmClient is an interface for interacting with the RNS service as administrator.
* AdmClient extends RnsReader: an implementation of AdmClient is expected to contact the RNS service
* at initialization time, and get from the service the information about places and their connections.
* This information is stored in the implementation object and remains fixed for the lifetime of the object.
* For what concerns vehicles, an implementation of AdmClient always returns fresh information through
* getUpdatedVehicles or getUpdatedVehicle. Whenever one of these methods is called, the implementation
* contacts the service and returns the fresh information just obtained from the service.
* Instead, when the getVehicles or getVehicle inherited from RnsReader are called, an implementation of
* AdmClient does not return any information about vehicles (i.e. it returns, respectively, an empty set or null).
*/
public class RnsAdmClient implements AdmClient {

	private Client client; 
	private WebTarget target; 
	
	private Map<String, SolPlaceReader> places; 
	private Map<String, SolVehicleReader> vehicles;
	private Map<String, SolGateReader> gates; 
	private Map<String, SolParkingAreaReader> parkingAreas;
	private Map<String, SolRoadSegmentReader> roadSegments; 
	private Set<SolConnectionReader> connections;
	
	private RnsRoot root; 

	public RnsAdmClient() throws AdmClientException{
		this.client = ClientBuilder.newClient(); 
		this.target = client.target(this.getURI()); 
		
		this.places = new HashMap<String, SolPlaceReader>(); 
		this.vehicles = new HashMap<String, SolVehicleReader>();
		this.gates = new HashMap<String, SolGateReader>();
		this.parkingAreas = new HashMap<String, SolParkingAreaReader>();
		this.roadSegments = new HashMap<String, SolRoadSegmentReader>();
		this.connections = new HashSet<SolConnectionReader>();  
		
		this.root = new RnsRoot(); 
		
		try {
			this.loadDataFromServer();
		} catch (ServiceException e) {
			System.out.println("Cathed Service Exception while loading data from service");
			e.printStackTrace();
			throw new AdmClientException(); 
		} catch (Exception e){
			System.out.println("Catched another exception");
			e.printStackTrace();
			throw new AdmClientException();
		}
	}
	

	private URI getURI() throws AdmClientException {
		try{
			if(System.getProperty("it.polito.dp2.RNS.lab3.URL") == null || System.getProperty("it.polito.dp2.RNS.lab3.URL").isEmpty()) {
				if(System.getProperty("it.polito.dp2.RNS.lab3.PORT") == null || System.getProperty("it.polito.dp2.RNS.lab3.PORT").isEmpty())
					return UriBuilder.fromUri("http://localhost:8080/RnsSystem/rest").build();
				else
					return UriBuilder.fromUri("http://localhost:" + System.getProperty("it.polito.dp2.RNS.lab3.PORT") + "/RnsSystem/rest").build();
			}
			return UriBuilder.fromUri(System.getProperty("it.polito.dp2.RNS.lab3.URL")).build();
	
		}catch(Exception e){
			System.out.println("Catched Exception here");
			System.out.println(System.getProperty("it.polito.dp2.RNS.lab3.URL"));
			System.out.println(System.getProperty("it.polito.dp2.RNS.lab3.PORT"));
			throw new AdmClientException(); 
		}
	}
	
	
	private void loadDataFromServer() throws ServiceException {
		//1) get rns root 
		Response resp1 = target.path("rns")
							.request(MediaType.APPLICATION_XML)
							.get(); 
		if(resp1.getStatus() == 500){
			System.out.println("Error while getting rns root");
			throw new ServiceException(); 
		}else if(resp1.getStatus() == 200){
			resp1.bufferEntity();
			this.root = resp1.readEntity(RnsRoot.class); 
			
		}
		
		//2) get places
		//2a) get roadsegments
		WebTarget wt = client.target(this.root.getRoadSegments());
		Response resp2 = wt.request(MediaType.APPLICATION_XML)
								.get();
		
		if(resp2.getStatus()==500){
			System.out.println("Error while getting road segments");
			throw new ServiceException(); 
		}
		else if(resp2.getStatus()==200){
			resp2.bufferEntity();
			Places places = resp2.readEntity(Places.class);
			if(!places.getPlace().isEmpty()){
				for(Place place: places.getPlace()){
					SolPlaceReader newPlace = new SolPlaceReader(place.getPlaceID(), place.getCapacity()); 
					String roadName = place.getRoadSegment().getRoadName(); 
					String roadSegmentName = place.getRoadSegment().getRoadSegmentName(); 
					SolRoadSegmentReader roadS = new SolRoadSegmentReader(newPlace, roadSegmentName, roadName);
					this.roadSegments.put(newPlace.getId(), roadS);
					this.places.put(newPlace.getId(), newPlace); 
				}
			}
		}
		//2b) get parking areas
		wt = client.target(this.root.getParkingAreas());
		Response resp3 = wt.request(MediaType.APPLICATION_XML)
								.get();
		
		if(resp3.getStatus()==500){
			System.out.println("Error while getting parking areas");
			throw new ServiceException();
		}
		else if(resp3.getStatus()==200){
			resp3.bufferEntity();
			Places places = resp3.readEntity(Places.class);
			if(!places.getPlace().isEmpty()){
				for(Place place: places.getPlace()){
					SolPlaceReader newPlace = new SolPlaceReader(place.getPlaceID(), place.getCapacity()); 

					Services s = new Services(); 
					List<String> services = place.getParkingArea().getServices().getServiceName(); 
					for(String service: services){
						Service newService = new Service();
						newService.setServiceName(service);
						s.getService().add(newService); 
					}
					SolParkingAreaReader newParking = new SolParkingAreaReader(newPlace, s); 
					this.parkingAreas.put(newPlace.getId(), newParking); 
					this.places.put(newPlace.getId(), newPlace); 
				}
			}
		}
		
		//2c) get gates 
		wt = client.target(this.root.getGates());
		Response resp4 = wt.request(MediaType.APPLICATION_XML)
								.get();
		
		if(resp4.getStatus()==500){
			System.out.println("Error while getting gates");
			throw new ServiceException(); 
		}
		else if(resp4.getStatus()==200){
			resp4.bufferEntity();
			Places places = resp4.readEntity(Places.class);
			if(!places.getPlace().isEmpty()){
				for(Place place: places.getPlace()){
					SolPlaceReader newPlace = new SolPlaceReader(place.getPlaceID(), place.getCapacity()); 
				
					SolGateReader newGate = new SolGateReader(newPlace, place.getGate().name()); 
					this.gates.put(newPlace.getId(), newGate); 
					this.places.put(newPlace.getId(), newPlace); 
				}
			}
		}
		
		//3) get connections
		wt = client.target(this.root.getConnections());
		Response resp5 = wt.request(MediaType.APPLICATION_XML)
								.get();
		
		if(resp5.getStatus()==500){
			System.out.println("Error while getting parking areas");
			throw new ServiceException();
		}
		else if(resp5.getStatus()==200){
			resp5.bufferEntity();
			Connections connections = resp5.readEntity(Connections.class);
			
			//only for debug
			//System.out.println("Number of connections: " + connections.getConnection().size());
			if(!connections.getConnection().isEmpty()){
				for(Connection connection: connections.getConnection()){	
				
					SolPlaceReader from = this.places.get(connection.getFrom()); 
					SolPlaceReader to = this.places.get(connection.getTo()); 
				
					if(!from.containsId(to.getId()))
						from.addConnection(to);
			
					SolConnectionReader newConnection = new SolConnectionReader(from, to); 
					this.connections.add(newConnection); 
				}
			}
		}
		
	}


	@Override
	public Set<ConnectionReader> getConnections() {
		return new LinkedHashSet<ConnectionReader>(this.connections); 
	}

	@Override
	public Set<GateReader> getGates(GateType arg0) {
		// Gets readers for all the gates available in the RNS system with the given type
		// Returns a set of interfaces for reading the gates with the given type
		// or all the gates if arg0 is null 
		return new LinkedHashSet<GateReader>(this.gates
												.values()
												.stream()
												.filter(p -> p.hasType(arg0))
												.collect(Collectors.toSet()));
	}

	@Override
	public Set<ParkingAreaReader> getParkingAreas(Set<String> arg0) {
		// Gets readers for all the parking areas available in the RNS system having the specified services
		// Returns a set of interfaces for reading the parking areas with the specified services,
		// or all the parking areas if arg0 is null
		return new LinkedHashSet<ParkingAreaReader>(this.parkingAreas
														.values()
														.stream()
														.filter(p-> p.hasServices(arg0))
														.collect(Collectors.toSet())); 
	}

	@Override
	public PlaceReader getPlace(String arg0) {
		// Returns the place given its id or null if the place doesn't exist 
		return this.places.get(arg0); 

	}

	@Override
	public Set<PlaceReader> getPlaces(String arg0) {
		// Returns a set of interface for reading places that have  arg0 has prefix to their id
		// Returns all the places if arg0 is null
		return new LinkedHashSet<PlaceReader>(this.places
												.values()
												.stream()
												.filter(p -> p.hasPrefix(arg0))
												.collect(Collectors.toSet()));
	}

	@Override
	public Set<RoadSegmentReader> getRoadSegments(String arg0) {
		// Returns readers for all the road segments available in the RNS system belonging to the road with the given name
		// Returns all the road segments if arg0 is null
		return new LinkedHashSet<RoadSegmentReader>(this.roadSegments
														.values()
														.stream()
														.filter(p -> p.belongsToRoad(arg0))
														.collect(Collectors.toSet()));

	}

	@Override
	public VehicleReader getVehicle(String arg0) {
		return null;
	}

	@Override
	public Set<VehicleReader> getVehicles(Calendar arg0, Set<VehicleType> arg1, VehicleState arg2) {
		return null;
	}

	/**
	 * Gets readers for all the vehicles that are currently in the place with the given id, in the RNS system.
	 * If no place is specified, return the readers of all the vehicles that are currently in the system
	 * @param place the place for which we want to get vehicle readers or null to get readers for all places.
	 * @return a set of interfaces for reading the selected vehicles.
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 */
	@Override
	public Set<VehicleReader> getUpdatedVehicles(String place) throws ServiceException {
		this.vehicles = new HashMap<String, SolVehicleReader>();
		
		if(place == null){
			//get all the vehicles from the system 
			
			WebTarget wt = client.target(this.root.getVehicles());
			Response response = wt.request(MediaType.APPLICATION_XML)
									.get();
			
			if(response.getStatus()==500)
				throw new ServiceException(); 
			else if(response.getStatus()==200){
				response.bufferEntity();
				Vehicles vehicles = response.readEntity(Vehicles.class);
				if(vehicles.getVehicle().isEmpty())
					return  new LinkedHashSet<VehicleReader>(this.vehicles.values().stream().collect(Collectors.toSet()));
				for(Vehicle vehicle: vehicles.getVehicle()){
					
					SolPlaceReader origin = this.places.get(vehicle.getFrom());
					SolPlaceReader current = this.places.get(vehicle.getPosition()); 
					SolPlaceReader destination = this.places.get(vehicle.getTo()); 
					XMLGregorianCalendar date = vehicle.getEntryTime(); 
					String type = vehicle.getType().name(); 
					String state = vehicle.getState().name(); 
					String id = vehicle.getPlateID(); 
					
					SolVehicleReader newVehicle = new SolVehicleReader(id, destination, current, origin, date, state, type);
					this.vehicles.put(id, newVehicle); 
				}
				return new LinkedHashSet<VehicleReader>(this.vehicles.values().stream().collect(Collectors.toSet()));
			}else{
				System.out.println("Something Wrong happen with all vehicles");
				throw new ServiceException(); 
			}
		}else{
			//get all the vehicles in the specified place 
			WebTarget wt = client.target(this.root.getPlaces()).path(place).path("vehicles");
			Response response = wt.request(MediaType.APPLICATION_XML)
									.get();
			
			if(response.getStatus()==500)
				throw new ServiceException(); 
			else if(response.getStatus()==200){
				response.bufferEntity();
				Vehicles vehicles = response.readEntity(Vehicles.class);
				if(vehicles.getVehicle().isEmpty())
					return  new LinkedHashSet<VehicleReader>(this.vehicles.values().stream().collect(Collectors.toSet()));
				
				for(Vehicle vehicle: vehicles.getVehicle()){
					
					SolPlaceReader origin = this.places.get(vehicle.getFrom());
					SolPlaceReader current = this.places.get(vehicle.getPosition()); 
					SolPlaceReader destination = this.places.get(vehicle.getTo()); 
					XMLGregorianCalendar date = vehicle.getEntryTime(); 
					String type = vehicle.getType().name(); 
					String state = vehicle.getState().name();  
					String id = vehicle.getPlateID(); 
					
					SolVehicleReader newVehicle = new SolVehicleReader(id, destination, current, origin, date, state, type);
					this.vehicles.put(id, newVehicle); 
				}
				return new LinkedHashSet<VehicleReader>(this.vehicles.values().stream().collect(Collectors.toSet()));
			}else{
				System.out.println("Something Wrong happen with vehicles in places");
				throw new ServiceException(); 
			}

		}
		
	}

	/**
	 * Gets a reader for a single vehicle, available in the RNS system, given its plate id.
	 * The returned information is obtained from the remote service when the method is called.
	 * @param id the plate id of the vehicle to get.
	 * @return an interface for reading the vehicle with the given plate id or null if a vehicle with the given plate id is not available in the system.
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 */
	@Override
	public VehicleReader getUpdatedVehicle(String id) throws ServiceException {
		//get all the vehicles from the system 
		
		WebTarget wt = client.target(this.root.getVehicles()).path(id);
		Response response = wt.request(MediaType.APPLICATION_XML)
								.get();
		
		if(response.getStatus() == 500)
			throw new ServiceException(); 
		else if(response.getStatus() == 404)
			return null; 
		else if(response.getStatus() == 200){
			response.bufferEntity();
			Vehicle vehicle = response.readEntity(Vehicle.class);
			
			SolPlaceReader origin = this.places.get(vehicle.getFrom());
			SolPlaceReader current = this.places.get(vehicle.getPosition()); 
			SolPlaceReader destination = this.places.get(vehicle.getTo()); 
			XMLGregorianCalendar date = vehicle.getEntryTime(); 
			String type = vehicle.getType().name(); 
			String state = vehicle.getState().name(); 
			String plateId = vehicle.getPlateID(); 
			if(!plateId.equals(id)){
				System.out.println("Server returned wrong vehicle");
				throw new ServiceException(); 
			}	
			SolVehicleReader newVehicle = new SolVehicleReader(plateId, destination, current, origin, date, state, type);
		
			return newVehicle;
		}
		else{
			System.out.println("Something wrong happen");
			throw new ServiceException(); 
		}
	}

}
