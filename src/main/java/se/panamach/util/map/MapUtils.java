package se.panamach.util.map;

import java.util.List;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import se.panamach.services.gps.type.Location;
import se.panamach.services.gps.type.TimePositionVelocity;

public class MapUtils {

	/**
	 * Calculates the distance between two locations in meters.
	 * 
	 * @param start
	 * @param destination
	 * @return
	 */
	public static double getDistanceBetween(Location start, Location destination) {
		if (start == null || destination == null)
			return 0.0d;

		if (start.equals(destination))
			return 0.0d;

		return LatLngTool.distance(new LatLng(start.latitude, start.longitude),
				new LatLng(destination.latitude, destination.longitude),
				LengthUnit.METER);
	}

	/**
	 * Calculates the direction in degrees from true north from start to
	 * destination.
	 * 
	 * http://www.movable-type.co.uk/scripts/latlong.html
	 * 
	 * @param start
	 * @param destination
	 * @return
	 */
	public static double getStartBearing(Location start,
			Location destination) {

		double lat1 = Math.toRadians(start.latitude);
		double lat2 = Math.toRadians(destination.latitude);
		double dLon = Math.toRadians(destination.longitude - start.longitude);

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
				* Math.cos(lat2) * Math.cos(dLon);
		double brng = Math.atan2(y, x);

		return (Math.toDegrees(brng) + 360) % 360;
	}

	public static double getEndBearing(Location start,
			Location destination) {

		return (getStartBearing(destination, start) + 180.0) % 180.0;
	}
	
	/**
	 * Calculates the direction between the first element and the last element
	 * in the list of locations.
	 * 
	 * @param locations
	 * @return
	 */
	public static double getGeneralDirectionTraveled(List<Location> locations) {
		return getStartBearing(locations.get(0),
				locations.get(locations.size() - 1));
	}

	public static double getAverageSpeed(List<TimePositionVelocity> tpvs) {
		double totalDistance = 0;
		TimePositionVelocity previousTpv = null;
		for (TimePositionVelocity tpv : tpvs) {
			totalDistance += getDistanceBetween(previousTpv, tpv);
			previousTpv = tpv;
		}

		double totalTime = (tpvs.get(tpvs.size() - 1).time - tpvs.get(0).time) / 1000.0;

		return totalDistance / totalTime;
	}
}
