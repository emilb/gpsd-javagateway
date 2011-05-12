package se.panamach.util.map;

import java.util.List;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import se.panamach.services.gps.type.Location;
import se.panamach.services.gps.type.TimePositionVelocity;

public class MapUtils {

	private static double EARTH_AVERAGE_RADIUS_METERS = 6371009d;
	
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

	public static Location getLocationAfterTravel(Location start, double bearing, double distance) {
		
		double angularDistance = distance / EARTH_AVERAGE_RADIUS_METERS; // convert dist to angular distance in radians
		double bearingRad = Math.toRadians(bearing); // Bearing in radians

		double lat1 = Math.toRadians(start.latitude);
		double lon1 = Math.toRadians(start.longitude);
		
		double lat2 = Math.asin( Math.sin(lat1) * Math.cos(angularDistance) +
				Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(bearingRad));
		double lon2 = lon1 + Math.atan2(Math.sin(bearingRad) * Math.sin(angularDistance) * Math.cos(lat1),
				Math.cos(angularDistance) - Math.sin(lat1) * Math.sin(lat2));
		lon2 = (lon2 + 3 * Math.PI)%(2 * Math.PI) - Math.PI; // normalize to -180...+180

		return new Location(Math.toDegrees(lat2), Math.toDegrees(lon2));
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

	public static double getDistanceTraveled(List<TimePositionVelocity> tpvs) {
		double totalDistance = 0;
		Location previousTpv = null;
		for (Location tpv : tpvs) {
			totalDistance += getDistanceBetween(previousTpv, tpv);
			previousTpv = tpv;
		}
		
		return totalDistance;
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
	
	public static String getTileNumber(final TimePositionVelocity tpvs,
			final int zoom) {
		
		final double lon = tpvs.longitude;
		final double lat = tpvs.latitude;
		
		int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
		int ytile = (int) Math
				.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1
						/ Math.cos(Math.toRadians(lat)))
						/ Math.PI)
						/ 2 * (1 << zoom));
		
		return ("" + zoom + "/" + xtile + "/" + ytile);
	}
}
