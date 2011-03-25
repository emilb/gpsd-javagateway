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
		
		return LatLngTool.distance(new LatLng(start.latitude, start.longitude), new LatLng(destination.latitude, destination.longitude), LengthUnit.METER);
	}
	
	
	/**
	 * Calculates the direction in degrees from true north from start to destination.
	 * 
	 * @param start
	 * @param destination
	 * @return
	 */
	public static double getDirectionBetween(Location start, Location destination) {
		return 0;
	}
	
	/**
	 * Calculates the direction between the first element and the last element in the list
	 * of locations.
	 * 
	 * @param locations
	 * @return
	 */
	public static double getGeneralDirectionTravelled(List<Location> locations) {
		return getDirectionBetween(locations.get(0), locations.get(locations.size()-1));
	}
	
	public static double getAverageSpeed(List<TimePositionVelocity> tpvs) {
		double totalDistance = 0;
		TimePositionVelocity previousTpv = null;
		for (TimePositionVelocity tpv : tpvs) {
			totalDistance += getDistanceBetween(previousTpv, tpv);
			previousTpv = tpv;
		}
		
		double totalTime = (tpvs.get(tpvs.size()-1).time - tpvs.get(0).time) / 1000.0;
		
		return totalDistance / totalTime ;
	}
}
