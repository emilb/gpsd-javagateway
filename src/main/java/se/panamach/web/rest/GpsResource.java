package se.panamach.web.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.panamach.services.gps.PositionHistoryService;
import se.panamach.services.gps.type.TimePositionVelocity;
import se.panamach.web.rest.types.GpsData;

@Path("/location")
@Component
public class GpsResource {

	@Autowired
	PositionHistoryService positionHistory;
	
	@GET
	@Path("/current")
	@Produces({MediaType.APPLICATION_XML})
	public GpsData getCurrentLocation() {
		TimePositionVelocity tpv = positionHistory.getLastKnownPosition();
		if (tpv == null)
			return new GpsData(0d, 0d);
		
		return new GpsData(tpv.latitude, tpv.longitude);
	}
	
}
