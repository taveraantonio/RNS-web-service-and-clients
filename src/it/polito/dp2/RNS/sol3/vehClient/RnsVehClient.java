package it.polito.dp2.RNS.sol3.vehClient;

import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.EntranceRefusedException;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.lab3.UnknownPlaceException;
import it.polito.dp2.RNS.lab3.VehClient;
import it.polito.dp2.RNS.lab3.VehClientException;
import it.polito.dp2.RNS.lab3.WrongPlaceException;
import it.polito.dp2.RNS.sol3.jaxb.EntranceRequest;
import it.polito.dp2.RNS.sol3.jaxb.ExitRequest;
import it.polito.dp2.RNS.sol3.jaxb.MoveRequest;
import it.polito.dp2.RNS.sol3.jaxb.RnsRoot;
import it.polito.dp2.RNS.sol3.jaxb.SuggestedPath;
import it.polito.dp2.RNS.sol3.jaxb.Vstate;

/**
 * An Interface for interacting with the RNS service as a vehicle
 * Through this interface a vehicle can perform all the operations allowed by the RNS service for that vehicle
 *
 */
public class RnsVehClient implements VehClient{

	private String plateId; 
	private RnsRoot rnsRoot;
	private Client client; 
	private WebTarget target;
	
	
	public RnsVehClient() throws VehClientException{
		this.client = ClientBuilder.newClient(); 
		this.target = client.target(this.getURI()); 
		this.rnsRoot = new RnsRoot();
		this.plateId = null; 
		
		//get rns root 
		try{
			Response response = target.path("rns")
								.request(MediaType.APPLICATION_XML)
								.get(); 
			if(response.getStatus() == 500){
				throw new VehClientException(); 
			}else if(response.getStatus() == 200){
				response.bufferEntity(); 
				this.rnsRoot = response.readEntity(RnsRoot.class); 
			}
		}catch(Exception e){
			System.out.println("Catched exception while reading root response vehicle");
			e.printStackTrace();
			throw new VehClientException(); 
		}
	}
	
	
	private URI getURI() {
		if(System.getProperty("it.polito.dp2.RNS.lab3.URL") == null || System.getProperty("it.polito.dp2.RNS.lab3.URL").isEmpty()) {
			if(System.getProperty("it.polito.dp2.RNS.lab3.PORT") == null || System.getProperty("it.polito.dp2.RNS.lab3.PORT").isEmpty())
				return UriBuilder.fromUri("http://localhost:8080/RnsSystem/rest").build();
			else
				return UriBuilder.fromUri("http://localhost:" + System.getProperty("it.polito.dp2.RNS.lab3.PORT") + "/RnsSystem/rest").build();
		}
		return UriBuilder.fromUri(System.getProperty("it.polito.dp2.RNS.lab3.URL")).build();
	}
	
	/**
	 * Requests permission to the service to enter the system as a tracked vehicle
	 * If permission is granted by the system, returns the suggested path to the desired destination
	 * 
	 * @param plateId the plate id of the vehicle that is requesting permission
	 * @param inGate the id of the input gate for which the vehicle is requesting permission
	 * @param destination the destination place for which the vehicle is requesting permission
	 * @return the suggested path (the list of ids of the places of the suggested path, including source and destination)
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 * @throws UnknownPlaceException if the source or the destination is not a known place
	 * @throws WrongPlaceException if inGate is not the id of an IN or INOUT gate
	 * @throws EntranceRefusedException if permission to enter is not granted
	 */
	@Override
	public List<String> enter(String plateId, VehicleType type, String inGate, String destination)
			throws ServiceException, UnknownPlaceException, WrongPlaceException, EntranceRefusedException {
		
		EntranceRequest request = new EntranceRequest(); 
		request.setPlateID(plateId);
		request.setPosition(inGate);
		request.setVehicleType(it.polito.dp2.RNS.sol3.jaxb.VehicleType.valueOf(type.toString()));
		request.setDestination(destination);
		
		//only for debug
		//System.out.println("Plate: "+ request.getPlateID());
		//System.out.println("Destination: " + request.getDestination());
		//System.out.println("Gate: " + request.getPosition());
		
		try{
			//perform post to server
			WebTarget wt = client.target(this.rnsRoot.getVehicles());
			Response response = wt.request(MediaType.APPLICATION_XML)
								.post(Entity.entity(request, MediaType.APPLICATION_XML));
			
			if(response.getStatus() == 201){
				response.bufferEntity(); 
				this.plateId = plateId; 
				System.out.println("Vehicle Created");
				SuggestedPath path = response.readEntity(SuggestedPath.class); 
				return path.getPath();
				
			}else if(response.getStatus() == 400){
				System.out.println("Bad Request"); 
				throw new WrongPlaceException(); 
			}else if(response.getStatus() == 403){
				System.out.println("Not granted permission");
				throw new EntranceRefusedException(); 
			}else if(response.getStatus() == 404){
				System.out.println("Unknown source or destination");
				throw new UnknownPlaceException(); 
			}else if(response.getStatus() == 500){
				System.out.println("Server error");
				throw new ServiceException(); 
			}else{
				throw new ServiceException(); 
			}
		}catch(WrongPlaceException we){
			throw new WrongPlaceException(); 
		}catch(EntranceRefusedException re){
			throw new EntranceRefusedException(); 
		}catch(UnknownPlaceException ue){
			throw new UnknownPlaceException(); 
		}catch(ServiceException se){
			throw new ServiceException(); 
		}catch(Exception e){
			throw new ServiceException(); 
		}
	}

	
	/**
	 * Communicates to the service that the vehicle has changed its position to a new place
	 * Returns the new suggested path or null if the path has not changed
	 * 
	 * @param newPlace the id of the new place
	 * @return the new suggested path (the list of the ids of the places of the new suggested path) or null if the suggested path has not changed
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 * @throws UnknownPlaceException if newPlace is not the id of a known place
	 * @throws WrongPlaceException if newPlace is not is not the id of a place reachable from the previous position of the vehicle
	 */
	@Override
	public List<String> move(String newPlace) throws ServiceException, UnknownPlaceException, WrongPlaceException {
		
		//vehicle not entered the system yet
		if(this.plateId == null)
			throw new ServiceException(); 
		
		MoveRequest moveRequest = new MoveRequest();  
		moveRequest.setNewPosition(newPlace);
		
		//only for debug 
		//System.out.println();
		//System.out.println("Requesting move for vehicle: " + this.plateId); 
		//System.out.println("New Position " + moveRequest.getNewPosition());
	
		try{
			//perform post to server
			WebTarget wt = client.target(this.rnsRoot.getVehicles());
			Response response = wt
								.path(this.plateId)
								.path("position")
								.request(MediaType.APPLICATION_XML)
								.put(Entity.entity(moveRequest, MediaType.APPLICATION_XML));
			
			if(response.getStatus() == 200){
				System.out.println("Vehicle moved to the new position: " + newPlace +", new path retrieved");
				response.bufferEntity(); 
				SuggestedPath path = response.readEntity(SuggestedPath.class); 
				return path.getPath();
			}else if(response.getStatus() == 204){
				System.out.println("Vehicle moved to the new position: " + newPlace + ", not new path");
				return null; 
			}else if(response.getStatus() == 403){
				System.out.println("Place not reachable from the previous position");
				throw new WrongPlaceException(); 
			}else if(response.getStatus() == 404){
				System.out.println("Place is unknown");
				throw new UnknownPlaceException(); 
			}else if(response.getStatus() == 500){
				System.out.println("Server error");
				throw new ServiceException(); 
			}else{
				throw new ServiceException(); 
			}
		}catch(WrongPlaceException we){
			throw new WrongPlaceException(); 
		}catch(UnknownPlaceException ue){
			throw new UnknownPlaceException(); 
		}catch(ServiceException se){
			throw new ServiceException(); 
		}catch(Exception e){
			throw new ServiceException(); 
		}

		
	}

	/**
	 * Communicates to the service the new state of the vehicle
	 * 
	 * @param newState the new state of the vehicle
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 */
	@Override
	public void changeState(VehicleState newState) throws ServiceException {
		
		//vehicle not entered the system yet
		if(this.plateId == null)
			throw new ServiceException(); 
		
		Vstate state = new Vstate(); 
		state.setState(it.polito.dp2.RNS.sol3.jaxb.VehicleState.valueOf(newState.name()));
		 
		try{
			//perform post to server
			WebTarget wt = client.target(this.rnsRoot.getVehicles()).path(this.plateId).path("state");
			Response response = wt.request(MediaType.APPLICATION_XML)
								.put(Entity.entity(state, MediaType.APPLICATION_XML));
			
			if(response.getStatus() == 204){
				System.out.println("Vehicle state changed");
				return; 
			}else if(response.getStatus() == 500){
				System.out.println("Server error");
				throw new ServiceException(); 
			}else{
				throw new ServiceException(); 
			} 
		}catch(ServiceException se){
			throw new ServiceException(); 
		}catch(Exception e){
			throw new ServiceException(); 
		}

	}

	/**
	 * Communicates to the service that the vehicle has exited the system
	 * 
	 * @param outGate the gate at which the vehicle has exited the system
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 * @throws UnknownPlaceException if outGate is not the id of a known place
	 * @throws WrongPlaceException if outGate is not the id of an OUT or INOUT gate or is not reachable from the previous position of the vehicle
	 */
	@Override
	public void exit(String outGate) throws ServiceException, UnknownPlaceException, WrongPlaceException {
		
		//vehicle not entered the system yet
		if(this.plateId == null)
			throw new ServiceException(); 
		
		ExitRequest request = new ExitRequest(); 
		request.setClient(true);
		request.setExitPosition(outGate);
		
		
		try{
			//perform post to server
			WebTarget wt = client.target(this.rnsRoot.getVehicles()).path(this.plateId);
			Response response = wt.request(MediaType.APPLICATION_XML)
								.put(Entity.entity(request, MediaType.APPLICATION_XML));
			
			if(response.getStatus() == 204){
				System.out.println("Vehicle exited the system");
				this.plateId = null; 
				return; 
			}else if(response.getStatus() == 404){
				System.out.println("Unknown  gate");
				throw new UnknownPlaceException(); 
			}else if(response.getStatus() == 409){
				System.out.println("Not a INOUT or OUT gate or not reachable");
				throw new WrongPlaceException(); 
			}else if(response.getStatus() == 500){
				System.out.println("Server Error");
				throw new ServiceException(); 
			}else{
				throw new ServiceException();
			}
		}catch(WrongPlaceException we){
			throw new WrongPlaceException(); 
		}catch(UnknownPlaceException ue){
			throw new UnknownPlaceException(); 
		}catch(ServiceException se){
			throw new ServiceException(); 
		}catch(Exception e){
			throw new ServiceException(); 
		}

		
	}

}
