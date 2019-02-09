package it.polito.dp2.RNS.lab3;

import java.util.List;

import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;

/**
 * An Interface for interacting with the RNS service as a vehicle
 * Through this interface a vehicle can perform all the operations allowed by the RNS service for that vehicle
 *
 */
public interface VehClient {
	
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
	public List<String> enter(String plateId, VehicleType type, String inGate, String destination)
			throws ServiceException, UnknownPlaceException, WrongPlaceException, EntranceRefusedException;
	
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
	public List<String> move(String newPlace) 
			throws ServiceException, UnknownPlaceException, WrongPlaceException;
	
	/**
	 * Communicates to the service the new state of the vehicle
	 * 
	 * @param newState the new state of the vehicle
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 */
	public void changeState(VehicleState newState) throws ServiceException;
	
	/**
	 * Communicates to the service that the vehicle has exited the system
	 * 
	 * @param outGate the gate at which the vehicle has exited the system
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 * @throws UnknownPlaceException if outGate is not the id of a known place
	 * @throws WrongPlaceException if outGate is not the id of an OUT or INOUT gate or is not reachable from the previous position of the vehicle
	 */
	public void exit(String outGate) throws ServiceException, UnknownPlaceException, WrongPlaceException;

}
