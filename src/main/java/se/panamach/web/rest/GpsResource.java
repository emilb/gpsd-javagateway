package se.panamach.web.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.gpsd.client.message.TPV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.panamach.services.gps.GPSMonitor;
import se.panamach.services.gps.TimePositionVelocity;
import se.panamach.web.rest.types.GpsData;

@Path("/location")
@Component
public class GpsResource {

	@Autowired
	GPSMonitor gpsMonitor;
	
	@GET
	@Path("/current")
	@Produces({MediaType.APPLICATION_XML})
	public GpsData getCurrentLocation() {
		TimePositionVelocity tpv = gpsMonitor.getLastKnownPosition();
		if (tpv == null)
			return new GpsData(0d, 0d);
		
		return new GpsData(tpv.latitude, tpv.longitude);
	}
}
