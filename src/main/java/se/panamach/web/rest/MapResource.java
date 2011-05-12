package se.panamach.web.rest;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.panamach.services.gps.PositionHistoryService;
import se.panamach.util.converter.LocationListConverter;
import se.panamach.util.image.TileMerger;

@Path("/map")
@Component
public class MapResource {

	@Autowired
	PositionHistoryService positionHistory;
	
	@GET
	@Path("/zoom/{zoom}/width/{width}/height/{height}")
	@Produces("image/png")
	public byte[] getCurrentLocation(
			@PathParam("zoom") int zoom,
			@PathParam("width") int width,
			@PathParam("height") int height) throws IOException {
		
		TileMerger tm = new TileMerger(positionHistory.getLastKnownPosition(), width, height, zoom);
		tm.setPath(LocationListConverter.toLocation(positionHistory.getTravelHistory(getMinMetersBetweenPoints(zoom))));
		
		return tm.generateMap();
	}
	
	/**
	 * Returns meters between points in path so that:
	 * 	at zoom level 18 => 5 meters between
	 * 	at zoom level 0  => 1000 meters between
	 * 
	 * @param zoom
	 * @return
	 */
	private int getMinMetersBetweenPoints(int zoom) {
		return (int)(zoom * -55.27777777 + 1000d);
	}
}
