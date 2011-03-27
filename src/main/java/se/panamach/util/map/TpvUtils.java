package se.panamach.util.map;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang.math.Range;

import se.panamach.services.gps.type.TimePositionVelocity;

public class TpvUtils {

	/**
	 * Returns the TimePositionVelocity reading closest to the supplied time.
	 * If an empty list of TimePositionVelocities is supplied, null is returned.
	 * 
	 * @param time
	 * @param tpvs
	 * @return
	 */
	public static TimePositionVelocity getTPVClosestInTime(Date time, List<TimePositionVelocity> tpvs) {

		if (CollectionUtils.isEmpty(tpvs))
			return null;
		
		long timeStamp = time.getTime();
		long currentSmallestDiff = Long.MAX_VALUE;
		TimePositionVelocity timeNearestTpv = null;
		
		for (TimePositionVelocity tpv : tpvs) {
			long diff = Math.abs(timeStamp - (long)tpv.time); 
			if (diff < currentSmallestDiff) {
				currentSmallestDiff = diff;
				timeNearestTpv = tpv;
			}
		}
		
		return timeNearestTpv;
	}
	
	/**
	 * Returns a list with TimePositionVelocity readings within the specified date range from
	 * the supplied list of TimePositionVelocities. Returns an empty list if none found.
	 * 
	 * @param startTime
	 * @param endTime
	 * @param tpvs
	 * @return
	 */
	public static List<TimePositionVelocity> getTPVWithinTime(Date startTime, Date endTime, List<TimePositionVelocity> tpvs) {
		long startT = startTime.getTime();
		long endT = endTime.getTime();
		
		List<TimePositionVelocity> result = new ArrayList<TimePositionVelocity>();
		
		for (TimePositionVelocity tpv : tpvs) {
			if (tpv.time <= endT && tpv.time >= startT)
				result.add(tpv);
		}
		
		return result;
	}
	
	/**
	 * Checks whether the supplied list of TimePositionVelocity readings falls within the date range.
	 * 
	 * @param startTime
	 * @param endTime
	 * @param tpvs
	 * @return true if there is a reading within the date range.
	 */
	public static boolean hasTPVWithinTime(Date startTime, Date endTime, List<TimePositionVelocity> tpvs) {
		if (CollectionUtils.isEmpty(tpvs))
			return false;
		long lowTime = (long)tpvs.get(0).time;
		long highTime = (long)tpvs.get(tpvs.size()-1).time;
		
		Range tpvRange = new LongRange(lowTime, highTime);
		Range dateRange = new LongRange(startTime.getTime(), endTime.getTime());
		
		return tpvRange.overlapsRange(dateRange);
	}
	
	public static double convertMeterPerSecondToKmPerHour(double mps) {
		 return mps * 3.6d;
	}
}
