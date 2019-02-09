package it.polito.dp2.RNS.sol3.admClient;

import it.polito.dp2.RNS.ConnectionReader;
import it.polito.dp2.RNS.PlaceReader;

public class SolConnectionReader implements ConnectionReader{

	// Defining private variable 
	SolPlaceReader from;
	SolPlaceReader to; 
	
	// Constructor
	public SolConnectionReader(SolPlaceReader from, SolPlaceReader to) {
		// Initialize from and to variables received in input
		this.from = from; 
		this.to = to; 
	}

	@Override
	public PlaceReader getFrom() {
		// Get from
		return from; 
	}

	@Override
	public PlaceReader getTo() {
		// Get to 
		return to; 
	}

}
