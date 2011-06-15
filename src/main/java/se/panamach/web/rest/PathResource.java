package se.panamach.web.rest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.panamach.services.gps.PositionHistoryService;
import se.panamach.services.gps.type.TimePositionVelocity;
import se.panamach.web.rest.types.gpx.Gpx;
import se.panamach.web.rest.types.gpx.Track;
import se.panamach.web.rest.types.gpx.TrackPoint;
import se.panamach.web.rest.types.gpx.TrackSegment;
import se.panamach.web.rest.types.gpx.WayPoint;

@Path("/path")
@Component
public class PathResource {

	@Autowired
	PositionHistoryService positionHistory;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	@GET
	@Path("/all")
	@Produces({MediaType.APPLICATION_XML})
	public Gpx getPath() {
		TimePositionVelocity tpv = positionHistory.getLastKnownPosition();
		
		Gpx gpx = new Gpx();
		gpx.name = "Example GPX";
		gpx.waypoint = new WayPoint();
		gpx.waypoint.name = "Current position";
		gpx.waypoint.longitude = tpv.longitude;
		gpx.waypoint.latititude = tpv.latitude;
		gpx.waypoint.elevation = tpv.altitude;
		
		gpx.track = new Track();
		gpx.track.name = "Path taken";
		gpx.track.number = 1;
		gpx.track.trackSegment = new TrackSegment();
		
		List<TimePositionVelocity> travelHistory = positionHistory.getTravelHistory(15);
		for (TimePositionVelocity tvpHist : travelHistory) {
			TrackPoint tp = new TrackPoint();
			tp.longitude = tvpHist.longitude;
			tp.latititude = tvpHist.latitude;
			tp.elevation = tvpHist.altitude;
			tp.time = sdf.format(new Date(tvpHist.time));
			
			gpx.track.trackSegment.trackPoints.add(tp);
		}
		return gpx;
	}
	
}
