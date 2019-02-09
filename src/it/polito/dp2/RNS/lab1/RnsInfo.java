package it.polito.dp2.RNS.lab1;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

import it.polito.dp2.RNS.*;


public class RnsInfo {
	private RnsReader monitor;
	private DateFormat dateFormat;

	
	/**
	 * Default constructror
	 * @throws RnsReaderException 
	 */
	public RnsInfo() throws RnsReaderException {
		RnsReaderFactory factory = RnsReaderFactory.newInstance();
		monitor = factory.newRnsReader();
		dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
	}
	
	public RnsInfo(RnsReader monitor) {
		super();
		this.monitor = monitor;
		dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RnsInfo wf;
		try {
			wf = new RnsInfo();
			wf.printAll();
		} catch (RnsReaderException e) {
			System.err.println("Could not instantiate data generator.");
			e.printStackTrace();
			System.exit(1);
		}
	}


	public void printAll() {
		printLine(' ');
		printGates();
		printRoadSegments();
		printParkingAreas();
		printConnections();
		printVehicles();

	}

	private void printGates() {
		// Get the list of Gates
		Set<GateReader> set = monitor.getGates(null);
		
		/* Print the header of the table */
		printHeader('#',"#Information about GATES");
		printHeader("#Number of Gates: "+set.size());
		printHeader("#List of Gates:");
		printHeader("Id"+"\tCapacity"+"\tType",'-');
		
		// For each Gate print related data
		for (GateReader gate: set) {
			printHeader(gate.getId()+"\t"+gate.getCapacity()+"\t"+gate.getType().name());
		}
		printBlankLine();
	}

	private void printRoadSegments() {
		// Get the list of Road Segments
		Set<RoadSegmentReader> set = monitor.getRoadSegments(null);
		
		/* Print the header of the table */
		printHeader('#',"#Information about ROAD SEGMENTS");
		printHeader("#Number of Road Segments: "+set.size());
		printHeader("#List of Road Segments:");
		printHeader("Id"+"\tCapacity"+"\tName"+"\tRoad name",'-');
		
		// For each Road segment print related data
		for (RoadSegmentReader seg: set) {
			printHeader(seg.getId()+"\t"+seg.getCapacity()+"\t"+seg.getName()+"\t"+seg.getRoadName());
		}
		printBlankLine();
	}
	
	private void printParkingAreas() {
		// Get the list of Parking Areas
		Set<ParkingAreaReader> set = monitor.getParkingAreas(null);
		
		/* Print the header of the table */
		printHeader('#',"#Information about PARKING AREAS");
		printHeader("#Number of Parking Areas: "+set.size());
		printHeader("#List of Parking Areas:");
		printHeader("Id"+"\tCapacity"+"\tServices",'-');
		
		// For each Parking Area print related data
		for (ParkingAreaReader pa: set) {
			String services = "";
			for (String s:pa.getServices())
				services += s+" ";
			printHeader(pa.getId()+"\t"+pa.getCapacity()+"\t"+services);
		}
		printBlankLine();
		
	}

	private void printConnections() {
		// Get the list of Connections
		Set<ConnectionReader> set = monitor.getConnections();
		
		/* Print the header of the table */
		printHeader('#',"#Information about CONNECTIONS");
		printHeader("#Number of Connections: "+set.size());
		printHeader("#List of Connections:");
		printHeader("From (Id)"+"\tTo (Id)",'-');
		
		// For each connection, print related data
		for (ConnectionReader conn: set) {
			printHeader(conn.getFrom().getId()+"\t"+conn.getTo().getId());
		}
		printBlankLine();
		
	}

	private void printVehicles() {
		// Get the list of Vehicles
		Set<VehicleReader> set = monitor.getVehicles(null,null,null);
		
		/* Print the header of the table */
		printHeader('#',"#Information about VEHICLES");
		printHeader("#Number of Vehicles: "+set.size());
		printHeader("#List of Vehicles:");
		printHeader("Id"+"\t\tType"+"\tEntry time"+"\tDestination"+"\tOrigin"+"\tPosition"+"\tState",'-');
		
		// For each Road segment print related data
		for (VehicleReader v: set) {
			printHeader(v.getId()+"\t"+v.getType().name()+"\t"+dateFormat.format(v.getEntryTime().getTime())+"\t"+
					v.getDestination().getId()+"\t"+v.getOrigin().getId()+"\t"+v.getPosition().getId()+"\t"+v.getState().name());
		}
		printBlankLine();
		
	}



	private void printBlankLine() {
		System.out.println(" ");
	}

	
	private void printLine(char c) {
		System.out.println(makeLine(c));
	}

	private void printHeader(String header) {
		System.out.println(header);
	}

	private void printHeader(String header, char c) {		
		System.out.println(header);
		printLine(c);	
	}
	
	private void printHeader(char c, String header) {		
		printLine(c);	
		System.out.println(header);
	}
	
	private StringBuffer makeLine(char c) {
		StringBuffer line = new StringBuffer(132);
		
		for (int i = 0; i < 132; ++i) {
			line.append(c);
		}
		return line;
	}

}
