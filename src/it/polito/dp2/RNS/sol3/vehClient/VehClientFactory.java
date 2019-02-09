package it.polito.dp2.RNS.sol3.vehClient;

import it.polito.dp2.RNS.lab3.VehClient;
import it.polito.dp2.RNS.lab3.VehClientException;

public class VehClientFactory extends it.polito.dp2.RNS.lab3.VehClientFactory{

	@Override
	public VehClient newVehClient() throws VehClientException {
		
		
		try{
			RnsVehClient vehClient = new RnsVehClient();
			return vehClient; 
			
		}catch(Exception e){
			System.out.println("Error while creating vehicle client");
			throw new VehClientException(); 
		}
		
		
	}

	
}
