package it.polito.dp2.RNS.sol3.service;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import it.polito.dp2.RNS.sol2.jaxb.Connection;
import it.polito.dp2.RNS.sol2.jaxb.Node;
import it.polito.dp2.RNS.sol2.jaxb.ObjectFactory;
import it.polito.dp2.RNS.sol2.jaxb.ShortestPath;
import it.polito.dp2.RNS.sol2.jaxb.ShortestPath.Relationships;
import it.polito.dp2.RNS.sol2.jaxb.ShortestResult;
import it.polito.dp2.RNS.sol3.jaxb.Place;

public class PathFinder {

	//necessary for neo4j
	private ConcurrentHashMap<String, URI> mapNodes;	// Map the placeID to its URI in the neo4j 
	private ConcurrentHashMap<URI, String> mapURIs; 	// Map the URI to the placeID 
	private ConcurrentSkipListSet<URI> relationships; 	// Set of all the URIs of the created relationship
	private Client client;								// The client object  
	private WebTarget target;							// The target object 
	private Boolean loaded; 							//if model is loaed or not
	private ObjectFactory ob; 							//sol2 object factory 
	
	public PathFinder(){
		//initialize neo4j structures
		this.client = ClientBuilder.newClient();
		this.mapNodes = new ConcurrentHashMap<String, URI>(); 
		this.mapURIs = new ConcurrentHashMap<URI, String>(); 
		this.relationships = new ConcurrentSkipListSet<URI>();
		this.ob = new ObjectFactory(); 
		this.loaded = false; 
		
		try{
			if(System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL") == null || System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL").isEmpty()){
				//System.setProperty("it.polito.dp2.RNS.lab3.Neo4JURL", "http://localhost:7474/db");
				//this.target = client.target(System.getProperty("it.polito.dp2.RNS.lab3.Neo4jURL"));
				this.target = client.target(UriBuilder.fromUri("http://localhost:7474/db").build());
			}else{
				//this.target = client.target(System.getProperty("it.polito.dp2.RNS.lab3.Neo4jURL"));
				this.target = client.target(UriBuilder.fromUri(System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL")).build());
			}	
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void loadToNeo4j(Collection<PlaceExt> places){
		
		// Upload places node to neo4j
		
		try {
			for(PlaceExt place : places){
				URI uri;
				uri = this.performPostNode(place.getPlace());
				this.mapNodes.put(place.getPlace().getPlaceID(), uri);
				this.mapURIs.put(uri, place.getPlace().getPlaceID()); 
			}
		} catch (PathServiceException e) {
			e.printStackTrace();
		} 
						
		// Load relationship to neoj4
		try {
			for(PlaceExt place : places){
				for(String connTo: place.getPlace().getConnectedTo()){
					URI uri = this.performPostRelationship(place.getPlace(), connTo);
					this.relationships.add(uri); 
				}
			}
		} catch (PathServiceException e) {
			e.printStackTrace();
		} 
		this.loaded = true; 
	
	}
	
	
	private URI performPostNode(Place place) throws PathServiceException {
		System.out.println("\nPOST -> Creating Node for " + place.getPlaceID());
		Node n = this.ob.createNode();  
	    n.setId(place.getPlaceID());
	    
	    //Perform POST  
	    try{
	    	Response response = target.path("data")
	    						.path("node")
	    						.request(MediaType.APPLICATION_JSON)
	    						.post(Entity.entity(n, MediaType.APPLICATION_JSON));
	    
	    	if(response.getStatus() == 201){
	    		System.out.println("POST Response <- 201 Created");
	    		URI location = response.getLocation();
	    		return location;
	    	}else{
	    		System.out.println("Post failed with status " + response.getStatus());
	    		throw new Exception();
	    	}	
	    } catch(RuntimeException re){
	    	throw new PathServiceException("RuntimeException while creating Neo4j Node");
	    } catch(Exception e){
	    	throw new PathServiceException("ServiceException while creating Neo4j Node");
	    }
	}

	private URI performPostRelationship(Place place, String dest) throws PathServiceException {
		System.out.println("\nPOST -> Creating Relationship from: " + place.getPlaceID() + " to: " + dest);
		Connection co = this.ob.createConnection(); 
		co.setTo(this.mapNodes.get(dest).toString());
		co.setType("ConnectedTo");
	   
	    //Perform POST 
	    try{
	    	WebTarget wt = client.target(this.mapNodes.get(place.getPlaceID()));
	    	Response response = wt.path("relationships")
	    						.request(MediaType.APPLICATION_JSON)
	    						.post(Entity.entity(co, MediaType.APPLICATION_JSON));
	    	
	    	if(response.getStatus() == 201){
	    		System.out.println("POST Response <- 201 Created");
	    		URI location = response.getLocation();
	    		return location;
	    	}else{
	    		System.out.println("Post relationship failed with status " + response.getStatus());
	    		throw new Exception();
	    	}
	    }catch(RuntimeException re){
	    	throw new PathServiceException("RuntimeException while creating Neo4j Relationship");
	    }catch(Exception e){
	    	throw new PathServiceException("ServiceException while creating Neo4j Relationship");
	    }	
	}
	
	public Set<List<String>> findShortestPaths(String source, String destination, int maxlength)
			throws UnknownIdException, BadStateException, PathServiceException {
		
		//throw bad state exception if the model is not loaded yet
		if(!this.loaded){
			throw new BadStateException(); 
		}
		//throw UnknownIdException if the source is not a known place id
		if(!this.mapNodes.containsKey(source)){
			throw new UnknownIdException("Wrong source"); 
		}
		//thrown unknownIdException if the destination is not a known place id 
		if(!this.mapNodes.containsKey(destination)){
			throw new UnknownIdException("Wrong Destination");
		}
	
		//generate the entity to pass to the post request
		ShortestPath request = this.ob.createShortestPath();
		Relationships rel =  this.ob.createShortestPathRelationships();
		request.setRelationships(rel);
		String t = new String("ConnectedTo");
		request.getRelationships().setType(t);
		String d = new String("out");
		request.getRelationships().setDirection(d);
		request.setTo(this.mapNodes.get(destination).toString());
		String a = new String("shortestPath");
		request.setAlgorithm(a);
		if(maxlength>0){
			request.setMaxDepth(BigInteger.valueOf(maxlength));
		}else{				
			int maxPathDepth = this.mapNodes.size()-1; 
			request.setMaxDepth(BigInteger.valueOf(maxPathDepth));
		}
		/* DEBUG 
		System.out.println("From: " + this.mapNodes.get(source) );
		System.out.println("To: " + request.getTo()); 
		System.out.println("Relationship Type: " + request.getRelationships().getType()); 
		System.out.println("Relationship DIr: " + request.getRelationships().getDirection());
		System.out.println("Length: " + request.getMaxDepth()); 
		System.out.println("Algorithm " + request.getAlgorithm());
		*/
		
		//perform post request for shortest path
		try{
			System.out.println("POST -> Requesting Shortest Path");
			WebTarget wt = client.target(this.mapNodes.get(source)).path("paths");
	    	List<ShortestResult> paths = wt
	    			.request(MediaType.APPLICATION_JSON)
	    			.post(Entity.entity(request, MediaType.APPLICATION_JSON), new GenericType<List<ShortestResult>>() {});
	 
	    	if(paths == null){
    			System.out.println("Post shortest path failed");
	    		throw new PathServiceException();
    		
	    	}else{
    			// If response is 200 OK
    			System.out.println("POST Response <- 200 OK Shortest Path Received");
	    		Set<List<String>> setList = new HashSet<List<String>>();
	    		for(ShortestResult result : paths){
	    			List<String> listPlaces = new ArrayList<String>();
	    			for(String str : result.getNodes()){
	    				URI uri = new URI(str);
	    				String id = this.mapURIs.get(uri);
	    				if(id == null){
	    					throw new UnknownIdException("Unknown URI received from server"); 
	    				}
	    				listPlaces.add(id);
	    			}
	    			setList.add(listPlaces);
	       		}
	    		paths.clear();
	    		return setList;
    		} 
	    	
	    } catch(UnknownIdException ie){
	    	System.out.println("UnknownID Exception");
	    	ie.printStackTrace();
	    	throw new UnknownIdException(ie.getMessage());
	    } catch(ProcessingException pe){
	    	System.out.println("Error during JAX-RS request processing");
			pe.printStackTrace();
			throw new PathServiceException();
	    } catch(RuntimeException re){
			System.out.println("Runtime Exception");
	    	re.printStackTrace();
	    	throw new PathServiceException();
	    } catch(PathServiceException se){
	    	System.out.println("Service Exception");
	    	se.printStackTrace();
	    	throw new PathServiceException();
	    } catch(Exception e){
	    	e.printStackTrace();
	    	throw new PathServiceException("Exception while requesting Neo4j ShortesPath");
	    }	
	}

}
