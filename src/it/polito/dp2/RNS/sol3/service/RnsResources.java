package it.polito.dp2.RNS.sol3.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import java.net.URI;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

import it.polito.dp2.RNS.sol3.jaxb.*;


@Path("/rns")
@Api(value = "/rns")

public class RnsResources {
	
	public UriInfo uriInfo; 
	RnsService service = new RnsService(); 
	
	public RnsResources(@Context UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}
	
	
	@GET
	@ApiOperation(value = "getRnsRoot", notes = "Get the RNS System root")
	@ApiResponses(value = {
		    		@ApiResponse(code = 200, message = "OK root found"),
		    		@ApiResponse(code = 500, message = "Server Error"), 
		    		})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRnsRoot(){
		
		RnsRoot rnsRoot = new RnsRoot(); 
		UriBuilder root = uriInfo.getAbsolutePathBuilder();
		
		rnsRoot.setSelf(root.toTemplate().toString());
		rnsRoot.setPlaces(root.clone().path("places").toTemplate().toString());
		rnsRoot.setVehicles(root.clone().path("vehicles").toTemplate().toString());
		rnsRoot.setGates(root.clone().path("places/gates").toTemplate().toString());
		rnsRoot.setRoadSegments(root.clone().path("places/roadSegments").toTemplate().toString());
		rnsRoot.setParkingAreas(root.clone().path("places/parkingAreas").toTemplate().toString());
		rnsRoot.setConnections(root.clone().path("connections").toTemplate().toString());
		
		return Response.status(200).entity(rnsRoot).build();
	}
	
	
	@GET 
	@Path("/vehicles")
	@ApiOperation(value = "getVehicles", notes = "Get all the vehicles that are in the RNS system with the specifiedcharacteristic ")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Ok vehicles found"),
				@ApiResponse(code = 500, message = "Server Error"),
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getVehicles(
				@QueryParam("since") String since,
				@QueryParam("types") VehicleType type, 
				@QueryParam("state") VehicleState state, 
				@QueryParam("page") int page ){
		
		Vehicles vehicles = service.getVehicles(since, type, state, page);
		return Response.status(200).entity(vehicles).build(); 
	}
	
	@POST 
	@Path("/vehicles")
	@ApiOperation(value = "addVehicle", notes = "Add Vehicle to the system")
	@ApiResponses(value = {
				@ApiResponse(code = 201, message = "Vehicle Created"), 
				@ApiResponse(code = 400, message = "Bad Request"), 
				@ApiResponse(code = 403, message = "Not granted permission"),
				@ApiResponse(code = 404, message = "Source or Destination not found"),
				@ApiResponse(code = 500, message = "Server Error")
				})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addVehicle(EntranceRequest request){
		
		UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(request.getPlateID()); 
		URI self = builder.build(); 
		SuggestedPath path = service.createVehicle(request); 
		if(path != null)
			return Response.created(self).entity(path).build();
		else
			throw new InternalServerErrorException(); 
	}
	
	@GET
	@Path("/vehicles/{id}")
	@ApiOperation(value = "getVehicle", notes="Get the vehicle for that id")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Vehicle Found"), 
				@ApiResponse(code = 400, message = "Bad Request"),
				@ApiResponse(code = 404, message = "Vehicle Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getVehicle( 
				@PathParam("id") String id){
		
		Vehicle vehicle = service.getVehicle(id);
		if(vehicle == null)
			throw new NotFoundException(); 
		
		return Response.status(200).entity(vehicle).build(); 
		
	}
	
	@PUT
	@Path("/vehicles/{id}")
	@ApiOperation(value = "deleteVehicle", notes = "Delete the vehicle from the system")
	@ApiResponses(value = {
				@ApiResponse(code = 204, message = "No content. Successfully deleted"), 
				@ApiResponse(code = 400, message = "Bad Request"), 
				@ApiResponse(code = 409, message = "Not Allowed to delete"),
				@ApiResponse(code = 404, message = "Exit Places Not Found"), 
				@ApiResponse(code = 500, message = "Server Error"), 
				})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response deleteVehicle(
				@PathParam("id") String id, 
				ExitRequest request
				){
		
		Vehicle vehicle = service.deleteVehicle(id, request); 
		if(vehicle == null){
			throw new InternalServerErrorException(); 
		}
		return Response.status(204).build(); 
	}
	
	@GET
	@Path("/vehicles/{id}/destination")
	@ApiOperation(value = "getDestination", notes="Gets the destination place of this vehicle in the system")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Destination of Vehicle Found"), 
				@ApiResponse(code = 400, message = "Bad Request, Destinatio not found"),
				@ApiResponse(code = 404, message = "Vehicle Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getDestination(
			@PathParam("id") String id){
		
		Place place = service.getDestination(id); 
		if(place == null){
				throw new NotFoundException(); 
		}
		
		return Response.status(200).entity(place).build(); 
	}
	
	@GET 
	@Path("/vehicles/{id}/entryTime")
	@ApiOperation(value = "getEntryTime", notes = "Gets the date and time when this vehicle entered the RNS system")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "vehicle entry time found"), 
				@ApiResponse(code = 404, message = "Vehicle non found"), 
				@ApiResponse(code = 500, message = "Server Error"), 
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getEntryTime(
			@PathParam("id") String id)	{
	
		Time entryTime = service.getEntryTime(id); 
		if(entryTime == null)
				throw new NotFoundException(); 
		
		return Response.status(200).entity(entryTime).build(); 
	}
	
	@GET
	@Path("/vehicles/{id}/origin")
	@ApiOperation(value = "getOrigin", notes="Gets the origin place of this vehicle in the system")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Origin place of Vehicle Found"), 
				@ApiResponse(code = 400, message = "Bad Request, Destination Place not found"),
				@ApiResponse(code = 404, message = "Vehicle Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getOrigin(
			@PathParam("id") String id){
		
		Place place = service.getOrigin(id); 
		if(place == null){
				throw new NotFoundException(); 
		}
		return Response.status(200).entity(place).build(); 
	}
	
	@GET
	@Path("/vehicles/{id}/position")
	@ApiOperation(value = "getPosition", notes="Gets the current position of the vehicle")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Current position of Vehicle Found"), 
				@ApiResponse(code = 400, message = "Bad Request, Place not found"),
				@ApiResponse(code = 404, message = "Vehicle Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getPosition(
			@PathParam("id") String id){
		
		Place place = service.getPosition(id); 
		if(place == null){
				throw new NotFoundException(); 
		}
		return Response.status(200).entity(place).build(); 
	}
	
	@PUT
	@Path("/vehicles/{id}/position")
	@ApiOperation(value = "updatePosition", notes = "Update the current position of the vehicle")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Position Updated. New path retrieved"),  
				@ApiResponse(code = 204, message = "Position Updated. Not new Path"),
				@ApiResponse(code = 400, message = "Bad Request"), 
				@ApiResponse(code = 403, message = "Forbidden, the place is not reachable from the previous position"), 
				@ApiResponse(code = 404, message = "Place not found"),
				@ApiResponse(code = 500, message = "Server Error"),
				})
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Response updatePosition(
			@PathParam("id") String id, 
			MoveRequest moveRequest){
		
		SuggestedPath path = service.updatePosition(id, moveRequest); 
		if(path == null){
			return Response.status(204).build();
		}
		return Response.status(200).entity(path).build(); 
	}
	
	@GET
	@Path("/vehicles/{id}/state")
	@ApiOperation(value = "getState", notes="Gets the current state of the vehicle")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Current state of Vehicle Found"), 
				@ApiResponse(code = 400, message = "Bad Request"),
				@ApiResponse(code = 404, message = "Vehicle Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getState(
			@PathParam("id") String id){
		
		Vstate state = service.getState(id); 
		if(state == null){
			throw new NotFoundException(); 
		}
		return Response.status(200).entity(state).build(); 
	}
	
	@PUT
	@Path("/vehicles/{id}/state")
	@ApiOperation(value = "updateState", notes = "Update the state of the vehicle")
	@ApiResponses(value = {
				@ApiResponse(code = 204, message = "State updated"), 
				@ApiResponse(code = 400, message = "Bad Request"),
				@ApiResponse(code = 404, message = "Vehicle Not found"), 
				@ApiResponse(code = 500, message = "Server Error"),
				})
	@Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Response updateState(
			@PathParam("id") String id, 
			Vstate state){
		
		Vstate s = service.updateState(id, state); 
		if(s == null){
			throw new InternalServerErrorException(); 
		}
		return Response.status(204).build(); 
	}
	
	@GET
	@Path("/vehicles/{id}/type")
	@ApiOperation(value = "getType", notes="Gets the current type of the vehicle")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Current typee of Vehicle Found"), 
				@ApiResponse(code = 400, message = "Bad Request"),
				@ApiResponse(code = 404, message = "Vehicle Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getType(
			@PathParam("id") String id){
		
		Vtype type = service.getType(id); 
		if(type == null){
			throw new NotFoundException(); 
		}
		return Response.status(200).entity(type).build(); 
	}
	
	@GET
	@Path("/vehicles/{id}/path")
	@ApiOperation(value = "getPath", notes="Gets the suggested path of the tracked vehicle")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Suggested path of the vehicle found"), 
				@ApiResponse(code = 404, message = "Vehicle Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getPath(
			@PathParam("id") String id){
		
		SuggestedPath path = service.getPath(id); 
		if(path == null){
				throw new NotFoundException(); 
		}
		return Response.status(200).entity(path).build(); 
	}
	
	@GET 
	@Path("/places")
	@ApiOperation(value = "getPlaces", notes = "Gets all the places available in the RNS system whose ids have the specified prefix.")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Ok places found"),
				@ApiResponse(code = 500, message = "Server Error"),
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getPlaces(
				@QueryParam("idPrefix") String idPrefix,
				@QueryParam("page") int page ){
		
		Places places = service.getPlaces(idPrefix, page);
		return Response.status(200).entity(places).build(); 
	}
	
	@GET 
	@Path("/places/roadSegments")
	@ApiOperation(value = "getRoadSegments", notes = "Gets all the road segments available in the RNS system belonging to the road with the given name")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Ok places found"),
				@ApiResponse(code = 500, message = "Server Error"),
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRoadSegments(
				@QueryParam("roadName") String roadName,
				@QueryParam("page") int page ){
		
		Places places = service.getRoadSegments(roadName, page);
		return Response.status(200).entity(places).build(); 
	}
	
	@GET 
	@Path("/places/gates")
	@ApiOperation(value = "getGates", notes = "Gets all the gates available in the RNS system with the given type")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Ok places found"),
				@ApiResponse(code = 500, message = "Server Error"),
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getGates(
				@QueryParam("type") GateType type,
				@QueryParam("page") int page ){
		
		Places places = service.getGates(type, page);
		return Response.status(200).entity(places).build(); 
	}
	
	@GET 
	@Path("/places/parkingAreas")
	@ApiOperation(value = "getParkingAreas", notes = "Gets all the parking areas available in the RNS system having the specified services")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Ok places found"),
				@ApiResponse(code = 500, message = "Server Error"),
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getParkingAreas(
				@QueryParam("services") List<String> services,
				@QueryParam("page") int page ){
		
		Places places = service.getParkingAreas(services, page);
		return Response.status(200).entity(places).build(); 
	}
	
	@GET
	@Path("/places/{id}")
	@ApiOperation(value = "getPlace", notes="Get the place for the given place ID")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Place Found"), 
				@ApiResponse(code = 404, message = "Place Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getPlace( 
				@PathParam("id") String id){
		
		Place place = service.getPlace(id);
		if(place == null)
			throw new NotFoundException(); 
		return Response.status(200).entity(place).build(); 
	}

	
	@GET
	@Path("/places/{id}/capacity")
	@ApiOperation(value = "getCapacity", notes="Gets the capacity of the place ")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Capacity retrieved"), 
				@ApiResponse(code = 404, message = "Place Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCapacity(
			@PathParam("id") String id){
		
		Capacity capacity = service.getCapacity(id); 
		if(capacity == null){
				throw new NotFoundException(); 
		}
		return Response.status(200).entity(capacity).build();  
	}
	
	@GET
	@Path("/places/{id}/next")
	@ApiOperation(value = "getNextPlaces", notes="Gets the set of places to which this place is connected to ")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Places retrieved"), 
				@ApiResponse(code = 404, message = "Place Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getNextPlaces(
			@PathParam("id") String id, 
			@QueryParam("page") int page){
		
		Places places = service.getNextPlaces(id, page);
		return Response.status(200).entity(places).build(); 
	}
	
	@GET
	@Path("/places/{id}/vehicles")
	@ApiOperation(value = "getPlaceVehicles", notes="Gets the information about vehicles that are in the given place")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Vehicles Retrieved"), 
				@ApiResponse(code = 404, message = "Place Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getPlaceVehicles(
			@PathParam("id") String id, 
			@QueryParam("page") int page){
		
		Vehicles vehicles = service.getPlaceVehicles(id, page);
		return Response.status(200).entity(vehicles).build(); 
	}
	
	@GET
	@Path("/places/gates/{id}/type")
	@ApiOperation(value = "getGateType", notes="Gets the type of this gate.")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Gate found, type retrieved"), 
				@ApiResponse(code = 404, message = "Place Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getGateType(
			@PathParam("id") String id){
		
		Gtype type = service.getGateType(id); 
		if(type == null){
				throw new NotFoundException(); 
		}
		return Response.status(200).entity(type).build(); 
	}
	
	@GET
	@Path("/places/roadSegments/{id}/name")
	@ApiOperation(value = "getName", notes="Gets the name of this road segments.")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "RoadSegment found, name retrieved"), 
				@ApiResponse(code = 404, message = "Place Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getName(
			@PathParam("id") String id){
		
		Name roadSegmentName = service.getRoadSegmentName(id); 
		if(roadSegmentName == null){
				throw new NotFoundException(); 
		}
		return Response.status(200).entity(roadSegmentName).build(); 
	}
	
	@GET
	@Path("/places/roadSegments/{id}/roadName")
	@ApiOperation(value = "getRoadName", notes="Gets the name of the road this road segments belongs to .")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "RoadSegment found, road name retrieved"), 
				@ApiResponse(code = 404, message = "Place Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRoadName(
			@PathParam("id") String id){
		
		Name roadName = service.getRoadName(id); 
		if(roadName == null){
				throw new NotFoundException(); 
		}
		return Response.status(200).entity(roadName).build(); 
	}
	
	@GET
	@Path("/places/parkingAreas/{id}/services")
	@ApiOperation(value = "getServices", notes="Gets the set of services this parking area has")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "PArkingArea found, services retrieved"), 
				@ApiResponse(code = 404, message = "Place Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getServices(
			@PathParam("id") String id){
		
		Services services = service.getServices(id);
		if(services == null){
				throw new NotFoundException(); 
		}
		return Response.status(200).entity(services).build(); 
	}
	
	
	@GET 
	@Path("/connections")
	@ApiOperation(value = "getConnections", notes = "Gets all the connections available in the RNS system whose ids have the specified prefix.")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Ok connections found"),
				@ApiResponse(code = 500, message = "Server Error"),
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getConnections(
				@QueryParam("page") int page ){
		
		Connections connections = service.getConnections(page);
		return Response.status(200).entity(connections).build();
	}
	
	@GET
	@Path("/connections/{id}/from")
	@ApiOperation(value = "getFrom", notes="Get the starting place for this connection")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Place Found"), 
				@ApiResponse(code = 400, message = "Bad Request. From not found"),
				@ApiResponse(code = 404, message = "Place Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getFrom( 
				@PathParam("id") String id){
		
		Place place = service.getFrom(id);
		if(place == null)
			throw new NotFoundException(); 
		return Response.status(200).entity(place).build(); 
	}
	
	@GET
	@Path("/connections/{id}/to")
	@ApiOperation(value = "getTo", notes="Get the target place for this connection")
	@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Place Found"), 
				@ApiResponse(code = 400, message = "Bad Request. Place to not found"),
				@ApiResponse(code = 404, message = "Place Not Found"),
				@ApiResponse(code = 500, message = "Server Error"),			
				})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTo( 
				@PathParam("id") String id){
		
		Place place = service.getTo(id);
		if(place == null)
			throw new NotFoundException(); 
		return Response.status(200).entity(place).build(); 
	}
	
	
	
}
