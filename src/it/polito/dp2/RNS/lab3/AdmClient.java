/**
 * 
 */

package it.polito.dp2.RNS.lab3;

import java.util.Calendar;
import java.util.Set;

import it.polito.dp2.RNS.ConnectionReader;
import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.ParkingAreaReader;
import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RnsReader;
import it.polito.dp2.RNS.RoadSegmentReader;
import it.polito.dp2.RNS.VehicleReader;
import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;

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
public interface AdmClient extends RnsReader {

	/**
	 * Gets readers for all the vehicles that are currently in the place with the given id, in the RNS system.
	 * If no place is specified, return the readers of all the vehicles that are currently in the system
	 * @param place the place for which we want to get vehicle readers or null to get readers for all places.
	 * @return a set of interfaces for reading the selected vehicles.
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 */
	public Set<VehicleReader> getUpdatedVehicles(String place) throws ServiceException;

	/**
	 * Gets a reader for a single vehicle, available in the RNS system, given its plate id.
	 * The returned information is obtained from the remote service when the method is called.
	 * @param id the plate id of the vehicle to get.
	 * @return an interface for reading the vehicle with the given plate id or null if a vehicle with the given plate id is not available in the system.
	 * @throws ServiceException if the operation cannot be completed because the RNS service is not reachable or not working
	 */
	public VehicleReader getUpdatedVehicle(String id) throws ServiceException;
	
}