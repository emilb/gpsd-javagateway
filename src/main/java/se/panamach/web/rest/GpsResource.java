package se.panamach.web.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.panamach.services.gps.PositionHistoryService;
import se.panamach.services.gps.type.TimePositionVelocity;
import se.panamach.web.rest.types.GPSStatus;
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
	
	@GET
	@Path("/status")
	@Produces({MediaType.APPLICATION_XML})
	public GPSStatus getGPSStatus() {
		TimePositionVelocity tpv = positionHistory.getLastKnownPosition();
		if (tpv == null)
			return new GPSStatus(0d, 0d);
		
		GPSStatus status = new GPSStatus(tpv.latitude, tpv.longitude);
		status.isStatic = positionHistory.isStatic();
		status.track = tpv.track;
		status.speed = tpv.speed;
		status.altitude = tpv.altitude;
		
		return status;
	}
}
