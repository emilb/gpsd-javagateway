package se.panamach.web.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.panamach.services.gps.PositionHistoryService;
import se.panamach.services.gps.type.Location;
import se.panamach.services.gps.type.TimeDistanceVector;
import se.panamach.web.rest.types.DistanceVector;
import se.panamach.web.rest.types.GpsData;

@Path("/distance")
@Component
public class DistanceResource {

	@Autowired
	PositionHistoryService positionHistory;
	
	@GET
	@Path("/to/{lat}/{long}")
	@Produces({MediaType.APPLICATION_XML})
	public DistanceVector getDistanceAndDirectionFromCurrentPositionTo(
			@PathParam("lat") double lat, 
			@PathParam("long") double lon) {
		
		Location destination = new Location();
		destination.longitude = lon;
		destination.latitude = lat;
		
		TimeDistanceVector tdv = positionHistory.getDistanceAndVectorTo(destination);
		DistanceVector dv = new DistanceVector();
		dv.currentPosition = new GpsData(tdv.latitude, tdv.longitude);
		dv.destination = new GpsData(lat, lon);
		dv.distanceToDestination = tdv.distance;
		dv.trackToDestination = tdv.track;
		dv.closing = tdv.closing;
		
		return dv;
	}
	
}
