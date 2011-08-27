package se.panamach.web.rest;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.gpsd.client.GPSd;
import org.gpsd.client.connector.GPSdConnection;
import org.gpsd.client.connector.GPSdSimulationConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/simulation")
@Component
public class SimulationResource {

	@Autowired
	GPSd gpsd;
	
	@GET
	@Path("/timefactor/{timefactor}")
	@Produces({MediaType.TEXT_PLAIN})
	public String setSimulationSpeed(
			@PathParam("timefactor") int timefactor) throws IOException {
		
		GPSdConnection connection = gpsd.getGPSdConnection();
		if (connection == null) {
			return "Not connected to GPS device";
		}
		
		if (connection instanceof GPSdSimulationConnection) {
			GPSdSimulationConnection simConnection = (GPSdSimulationConnection) connection;
			simConnection.setTimeFactor(timefactor);
			return "TimeFactor set to: " + timefactor;
		}
		
		return "Not in simulation mode";
	}
	
	@GET
	@Path("/timefactor")
	@Produces({MediaType.TEXT_PLAIN})
	public String getSimulationSpeed() throws IOException {
		
		GPSdConnection connection = gpsd.getGPSdConnection();
		if (connection == null) {
			return "Not connected to GPS device";
		}
		
		if (connection instanceof GPSdSimulationConnection) {
			GPSdSimulationConnection simConnection = (GPSdSimulationConnection) connection;
			return "" + simConnection.getTimeFactor();
		}
		
		return "Not in simulation mode";
	}
}
