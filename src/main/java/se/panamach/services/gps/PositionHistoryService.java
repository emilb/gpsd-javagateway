package se.panamach.services.gps;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import se.panamach.services.gps.type.Location;
import se.panamach.services.gps.type.TimeDistanceVector;
import se.panamach.services.gps.type.TimePositionVelocity;
import se.panamach.util.datastructure.FixedSizeList;
import se.panamach.util.map.MapUtils;
import se.panamach.util.map.TpvUtils;

@Service
public class PositionHistoryService {

	FixedSizeList<TimePositionVelocity> lastHourPositions;
	FixedSizeList<TimePositionVelocity> everyFiveMinutePosition;
	FixedSizeList<TimePositionVelocity> everyHourPosition;
	
	private static long FIVE_MINUTES = 5*60*1000;
	private static long ONE_HOUR = 60*60*1000;
	
	public PositionHistoryService() {
		lastHourPositions = new FixedSizeList<TimePositionVelocity>(3600);
		everyFiveMinutePosition = new FixedSizeList<TimePositionVelocity>(100);
		everyHourPosition = new FixedSizeList<TimePositionVelocity>(100);
	}
	
	/**
	 * Returns true if current speed > 2 m/s
	 * 
	 * @return
	 */
	public boolean isStatic() {
		
		TimePositionVelocity tpv = lastHourPositions.getLastElement();
		if (tpv != null)
			return tpv.speed < 2;
		
		return true;
	}

	public TimePositionVelocity getLastKnownPosition() {
		return lastHourPositions.getLastElement();
	}
	
	public TimePositionVelocity getPositionAt(Date time) {
		List<TimePositionVelocity> bestMatches = new ArrayList<TimePositionVelocity>();
		bestMatches.add(TpvUtils.getTPVClosestInTime(time, lastHourPositions));
		bestMatches.add(TpvUtils.getTPVClosestInTime(time, everyFiveMinutePosition));
		bestMatches.add(TpvUtils.getTPVClosestInTime(time, everyHourPosition));
		
		return TpvUtils.getTPVClosestInTime(time, bestMatches);
	}
	
	public List<TimePositionVelocity> getListOfPositionsBetween(Date start, Date end) {
		
		if (TpvUtils.hasTPVWithinTime(start, end, lastHourPositions))
			return TpvUtils.getTPVWithinTime(start, end, lastHourPositions);
		
		if (TpvUtils.hasTPVWithinTime(start, end, everyFiveMinutePosition))
			return TpvUtils.getTPVWithinTime(start, end, everyFiveMinutePosition);
		
		if (TpvUtils.hasTPVWithinTime(start, end, everyHourPosition))
			return TpvUtils.getTPVWithinTime(start, end, everyHourPosition);
		
		return new ArrayList<TimePositionVelocity>();
	}

	public TimeDistanceVector getDistanceAndVectorTo(Location loc) {
		TimeDistanceVector tdv = new TimeDistanceVector();
		tdv.destination = loc;
		tdv.setLocation(getLastKnownPosition());
		
		tdv.distance = MapUtils.getDistanceBetween(tdv, tdv.destination);
		tdv.track = MapUtils.getDirectionBetween(tdv, tdv.destination);
		
		boolean directionDecided = false;
		int ix = lastHourPositions.size() - 2; // TODO: Handle case where only one reading
		while (!directionDecided) {
			
		}
		
		tdv.closing = 
		
		return tdv;
	}
	
	public void registerPosition(TimePositionVelocity tpv) {
		registerPosition(tpv, 0, lastHourPositions);
		registerPosition(tpv, FIVE_MINUTES, everyFiveMinutePosition);
		registerPosition(tpv, ONE_HOUR, everyHourPosition);
	}
	
	private void registerPosition(TimePositionVelocity tpv, long minInterval, FixedSizeList<TimePositionVelocity> list) {
		TimePositionVelocity lastTpv = list.getLastElement();
		
		if (list.getLastElement() != null) {
			if ((tpv.time - lastTpv.time) < minInterval)
				return;
		}
		
		list.add(tpv);
	}
}
