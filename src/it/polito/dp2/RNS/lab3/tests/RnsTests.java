package it.polito.dp2.RNS.lab3.tests;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab3.EntranceRefusedException;
import it.polito.dp2.RNS.lab3.AdmClient;
import it.polito.dp2.RNS.lab3.AdmClientException;
import it.polito.dp2.RNS.lab3.AdmClientFactory;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.lab3.UnknownPlaceException;
import it.polito.dp2.RNS.lab3.VehClient;
import it.polito.dp2.RNS.lab3.VehClientException;
import it.polito.dp2.RNS.lab3.VehClientFactory;
import it.polito.dp2.RNS.lab3.WrongPlaceException;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public class RnsTests extends it.polito.dp2.RNS.lab1.tests.RnsTests {

	protected static AdmClient testAdmClient;			// AdmClient under test
	protected static VehClient testVehClient;			// VehClient under test

	protected static URL serviceUrl;
	
	protected static TreeSet<GateReader> referenceInputGates;
	protected static TreeSet<GateReader> referenceOutputGates;
	protected static TreeSet<RoadSegmentReader> referenceRoadSegments;
	protected static GateReader referenceInputGate=null;
	protected static GateReader referenceOutputGate=null;
	protected static RoadSegmentReader referenceRoadSegment=null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Create reference data generator
        System.setProperty("it.polito.dp2.RNS.RnsReaderFactory", "it.polito.dp2.RNS.Random.RnsReaderFactoryImpl");
        referenceRnsReader = RnsReaderFactory.newInstance().newRnsReader();

        
        // Create implementation under test       
        System.setProperty("it.polito.dp2.RNS.lab3.AdmClientFactory", "it.polito.dp2.RNS.sol3.admClient.AdmClientFactory");
        // Create implementation under test       
        testRnsReader = AdmClientFactory.newInstance().newAdmClient();
        
        // read testcase property
        Long testcaseObj = Long.getLong("it.polito.dp2.RNS.Random.testcase");
        if (testcaseObj == null)
        	testcase = 0;
        else
        	testcase = testcaseObj.longValue();
		
		referenceInputGates = new TreeSet<GateReader> (new IdentifiedEntityReaderComparator());
		referenceInputGates.addAll(referenceRnsReader.getGates(GateType.IN));
		referenceInputGates.addAll(referenceRnsReader.getGates(GateType.INOUT));
		referenceInputGate = referenceInputGates.iterator().next();

		referenceOutputGates = new TreeSet<GateReader> (new IdentifiedEntityReaderComparator());
		referenceOutputGates.addAll(referenceRnsReader.getGates(GateType.OUT));
		referenceOutputGates.addAll(referenceRnsReader.getGates(GateType.INOUT));
		referenceOutputGate = referenceOutputGates.iterator().next();

		referenceRoadSegments = new TreeSet<RoadSegmentReader> (new IdentifiedEntityReaderComparator());
		referenceRoadSegments.addAll(referenceRnsReader.getRoadSegments(null));
		referenceRoadSegment = referenceRoadSegments.iterator().next();

	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		assertNotNull(referenceInputGate);
		assertNotNull(referenceOutputGate);
	}
	

	// creates an instance of the AdmClient under test
	AdmClient createTestAdmClient() throws AdmClientException, FactoryConfigurationError {
		// Create AdmClient under test
		System.setProperty("it.polito.dp2.RNS.lab3.AdmClientFactory", "it.polito.dp2.RNS.sol3.admClient.AdmClientFactory");
		AdmClient testAdmClient = AdmClientFactory.newInstance().newAdmClient();
		assertNotNull("The implementation under test generated a null AdmClient", testAdmClient);
		return testAdmClient;
	}
	
	// creates an instance of the VehClient under test
	VehClient createTestVehClient() throws VehClientException, FactoryConfigurationError {
		// Create VehClient under test
		System.setProperty("it.polito.dp2.RNS.lab3.VehClientFactory", "it.polito.dp2.RNS.sol3.vehClient.VehClientFactory");
		VehClient testVehClient = VehClientFactory.newInstance().newVehClient();
		assertNotNull("The implementation under test generated a null VehClient", testVehClient);
		return testVehClient;
	}
		
    @Test
    public final void testVehicleLifecycle() throws FactoryConfigurationError, Exception {
    	// create admin client
    	AdmClient ac = createTestAdmClient();
		String plateId = "AB123CD";
		VehicleType type = VehicleType.CAR;
		
		// initialize the test object that manages the vehicle lifecycle
		VehicleClientManager vcm = new VehicleClientManager(plateId, type, referenceInputGate.getId());
		// get initial number of vehicles seen by the admin client
		int numberOfVehicles = getVehNumber(ac.getUpdatedVehicles(null));
		
		// check that initially the admin client does not find the vehicle
		assertNull("AdmClient returned non-null VehicleReader while null was expected", ac.getUpdatedVehicle(plateId));
		// perform first step (enter)
		vcm.nextStep();
		// check the number of vehicles seen by the admin client has been increased by one
		checkExpectedVehNumber(ac.getUpdatedVehicles(null), numberOfVehicles+1);
		// perform next steps (follow suggested path)
		while(!vcm.isExited())
			vcm.nextStep();
		// check that finally admin client does not find the vehicle
		assertNull("AdmClient returned non-null VehicleReader while null was expected", ac.getUpdatedVehicle(plateId));
		// and that the number of vehicles seen by the admin client is as expected
		checkExpectedVehNumber(ac.getUpdatedVehicles(null), numberOfVehicles);

    }
    
    @Test
    public final void testVehicleLifecycle2() throws FactoryConfigurationError, Exception {
    	// create admin client
    	AdmClient ac = createTestAdmClient();
		String plateId1 = "AB456CD";
		String plateId2 = "AB789CD";
		VehicleType type = VehicleType.CAR;
		
		// initialize the test object that manages the vehicle lifecycle for two vehicles
		VehicleClientManager vcm1 = new VehicleClientManager(plateId1, type, referenceInputGate.getId());
		VehicleClientManager vcm2 = new VehicleClientManager(plateId2, type, referenceInputGate.getId());
		// get initial number of vehicles seen by the admin client
		int numberOfVehicles = getVehNumber(ac.getUpdatedVehicles(null));
		
		// check that initially the admin client does not find the vehicles
		assertNull("AdmClient returned non-null VehicleReader while null was expected", ac.getUpdatedVehicle(plateId1));
		assertNull("AdmClient returned non-null VehicleReader while null was expected", ac.getUpdatedVehicle(plateId2));
		// perform first step (enter vehicle 1)
		vcm1.nextStep();
		// check the number of vehicles seen by the admin client has been increased by one
		checkExpectedVehNumber(ac.getUpdatedVehicles(null), numberOfVehicles+1);
		// perform second step (move vehicle 1 to the next position)
		vcm1.nextStep();
		// perform third step (enter vehicle 2)
		vcm2.nextStep();
		// check the number of vehicles seen by the admin client has been increased by one
		checkExpectedVehNumber(ac.getUpdatedVehicles(null), numberOfVehicles+2);
		// perform next steps (follow suggested path)
		while(!vcm1.isExited() && !vcm2.isExited()) {
			vcm1.nextStep();
			vcm2.nextStep();
		}
		while(!vcm1.isExited())
			vcm1.nextStep();
		while(!vcm2.isExited())
			vcm2.nextStep();
		// check that finally admin client does not find the vehicles
		assertNull("AdmClient returned non-null VehicleReader while null was expected", ac.getUpdatedVehicle(plateId1));
		assertNull("AdmClient returned non-null VehicleReader while null was expected", ac.getUpdatedVehicle(plateId2));
		// and that the number of vehicles seen by the admin client is as expected
		checkExpectedVehNumber(ac.getUpdatedVehicles(null), numberOfVehicles);
    }
    
    @Test(expected=UnknownPlaceException.class)
    public final void testUnknownPlace() throws FactoryConfigurationError, Exception {
    	// create admin client
    	AdmClient ac = createTestAdmClient();
		String plateId = "AC123CD";
		VehicleType type = VehicleType.CAR;
		
		// initialize the test object that manages the vehicle lifecycle
		VehicleClientManager vcm = new VehicleClientManager(plateId, type, "UnknownPlace");
		// check that initially the admin client does not find the vehicle
		assertNull("AdmClient returned non-null VehicleReader while null was expected", ac.getUpdatedVehicle(plateId));
		// perform first step (enter)
		vcm.nextStep();
    }

    @Test(expected=WrongPlaceException.class)
    public final void testWrongPlace() throws FactoryConfigurationError, Exception {
    	// create admin client
    	AdmClient ac = createTestAdmClient();
		String plateId = "AC123CD";
		VehicleType type = VehicleType.CAR;
		
		// initialize the test object that manages the vehicle lifecycle
		VehicleClientManager vcm = new VehicleClientManager(plateId, type, referenceRoadSegment.getId());
		// check that initially the admin client does not find the vehicle
		assertNull("AdmClient returned non-null VehicleReader while null was expected", ac.getUpdatedVehicle(plateId));
		// perform first step (enter)
		vcm.nextStep();
    }


	void checkExpectedVehNumber(Set<VehicleReader> vehicles, int expectedVehNumber) {
		assertNotNull("AdmClient returned null set of VehicleReader", vehicles);
		assertEquals("Wrong number of vehicles", expectedVehNumber, vehicles.size());
	}

	int getVehNumber(Set<VehicleReader> vehicles) {
		assertNotNull("AdmClient returned null set of VehicleReader", vehicles);
		return vehicles.size();
	}

	void checkVehicle(VehicleReader vehicle, String plateId, VehicleType type, VehicleState state, String source, String position,
			String dest) {
		assertNotNull("AdmClient returned null VehicleReader while a non-null one was expected", vehicle);
		compareString(plateId, vehicle.getId(), "Plate id of returned vehicle");
		assertEquals("Wrong vehicle type in returned vehicle",type,vehicle.getType());
		assertEquals("Wrong vehicle state in returned vehicle",state,vehicle.getState());
		compareString(source, vehicle.getOrigin().getId(), "Origin place id of returned vehicle");
		compareString(position, vehicle.getPosition().getId(), "Position place id of returned vehicle");
		compareString(dest, vehicle.getDestination().getId(), "Destination place id of returned vehicle");
	}

	void checkPath(String source, String destination, List<String> testPath) {
		assertTrue("Wrong number of elements in path", testPath.size()>=2);
		assertEquals("Wrong source in path", source, testPath.get(0));
		Iterator<String> listIt = testPath.iterator();
		String previous = listIt.next();
		while (listIt.hasNext()) {
			String id = listIt.next();
			assertNotNull("Wrong place name in path", referenceRnsReader.getPlace(previous));
			// check that id is the id of one of the next places of the previous place
			boolean found = false;
			for (PlaceReader p:referenceRnsReader.getPlace(previous).getNextPlaces()) {
				if (p.getId().equals(id)) {
					found=true;
					break;
				}
			}
			assertTrue("Wrong path returned",found);
			if (!listIt.hasNext()) // if is last
				assertEquals("Wong destination in path", destination, id);
			previous = id;
		}
	}
	
	class VehicleClientManager {
		String plateId = null;
		VehicleType type = VehicleType.CAR;
		String source=null;
		String dest=null;
		String position=null;
		boolean exited=false;
		List<String> suggestedPath=null;
		Iterator<String> pathIterator=null;
		VehicleState state = VehicleState.IN_TRANSIT;
		VehClient vc;
		AdmClient ac;

		public VehicleClientManager(String plateId, VehicleType type, String source) throws FactoryConfigurationError, Exception {
	    	if (plateId==null || source==null)
	    		throw new Exception("Internal tester error. Contact the teacher.");
			this.plateId = plateId;
	    	if (type!=null)
	    		this.type = type;
	    	this.source = source;
			vc = createTestVehClient();
			ac = createTestAdmClient();
		}
		
		public void nextStep() throws ServiceException, UnknownPlaceException, WrongPlaceException {
			if (dest==null) {
				// try to reach one destination gate
				for (GateReader g: referenceOutputGates) {
		    		try {
		    			// request permission
		    			dest = g.getId();
		    			suggestedPath = vc.enter(plateId, type, source, dest);
		    			// permission accepted. Check suggested path
		    			checkPath(source, dest,suggestedPath);
		    			// Suggested path ok. Check admin returns right data for vehicle
		    			checkVehicle(ac.getUpdatedVehicle(plateId), plateId, type, state, source, source, dest);
		    			position = source;
		    			pathIterator = suggestedPath.iterator();
		    			pathIterator.next(); // skip source (vehicle should be in source already)
		    			return; // step finished with success
		    		} catch (EntranceRefusedException e) {
		    			// attempt failed, continue with next destination
		    		}
		    	}
		    	fail("Vehicle was not admitted for any destination gate");
			} else if (!position.equals(dest) && pathIterator.hasNext()) {
				// move to next place on suggested path
   				String nextPlaceId = pathIterator.next();
    			assertNull("Move returned a non-null new suggested path, while a null one was expected",vc.move(nextPlaceId));
    			checkVehicle(ac.getUpdatedVehicle(plateId), plateId, type, state, source, nextPlaceId, dest);
    			position = nextPlaceId;
			} else {
    			// communicate exit
    			vc.exit(dest);
    			exited = true;
    			assertNull("AdmClient returned non-null VehicleReader while null was expected", ac.getUpdatedVehicle(plateId));
			}
		}
		
		boolean isExited() {
			return exited;
		}
	}

}

class IdentifiedEntityReaderComparator implements Comparator<IdentifiedEntityReader> {
    public int compare(IdentifiedEntityReader f0, IdentifiedEntityReader f1) {
    	return f0.getId().compareTo(f1.getId());
    }
}

