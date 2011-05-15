package se.panamach.services.gps;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import se.panamach.services.gps.type.Location;
import se.panamach.services.gps.type.TimeDistanceVector;
import se.panamach.services.gps.type.TimePositionVelocity;
import se.panamach.util.datastructure.CircularBuffer;
import se.panamach.util.map.MapUtils;
import se.panamach.util.map.TpvUtils;

@Service
public class PositionHistoryService {

	private static Comparator<TimePositionVelocity> timePositionVelocityComparator = new Comparator<TimePositionVelocity>() {
		@Override
		public int compare(TimePositionVelocity o1, TimePositionVelocity o2) {
			return Long.valueOf(o1.time).compareTo(Long.valueOf(o2.time));
		}
	
	}; 
	
	CircularBuffer<TimePositionVelocity> last24HoursPositionsPer30Seconds;
	CircularBuffer<TimePositionVelocity> last100PositionsPerFiveMinutes;
	CircularBuffer<TimePositionVelocity> last100PositionsPerHour;
	
	private static long THIRTY_SECONDS = 30*1000;
	private static long FIVE_MINUTES = 5*60*1000;
	private static long ONE_HOUR = 60*60*1000;
	
	public PositionHistoryService() {
		reset();
	}
	
	public void reset() {
		last24HoursPositionsPer30Seconds = new CircularBuffer<TimePositionVelocity>(24 * 60 * 2, timePositionVelocityComparator);
		last100PositionsPerFiveMinutes = new CircularBuffer<TimePositionVelocity>(100, timePositionVelocityComparator);
		last100PositionsPerHour = new CircularBuffer<TimePositionVelocity>(100, timePositionVelocityComparator);
	}
	
	/**
	 * Returns true if current speed < 2 m/s
	 * 
	 * @return
	 */
	public boolean isStatic() {
		
		TimePositionVelocity tpv = getLastElement(last24HoursPositionsPer30Seconds);
		if (tpv != null)
			return tpv.speed < 2;
		
		return true;
	}

	public TimePositionVelocity getLastKnownPosition() {
		return getLastElement(last24HoursPositionsPer30Seconds);
	}
	
	public TimePositionVelocity getPositionAt(Date time) {
		List<TimePositionVelocity> bestMatches = new ArrayList<TimePositionVelocity>();
		bestMatches.add(TpvUtils.getTPVClosestInTime(time, last24HoursPositionsPer30Seconds.getAll()));
		bestMatches.add(TpvUtils.getTPVClosestInTime(time, last100PositionsPerFiveMinutes.getAll()));
		bestMatches.add(TpvUtils.getTPVClosestInTime(time, last100PositionsPerHour.getAll()));
		
		return TpvUtils.getTPVClosestInTime(time, bestMatches);
	}
	
	public List<TimePositionVelocity> getListOfPositionsBetween(Date start, Date end) {
		
		if (TpvUtils.hasTPVWithinTime(start, end, last24HoursPositionsPer30Seconds.getAll()))
			return TpvUtils.getTPVWithinTime(start, end, last24HoursPositionsPer30Seconds.getAll());
		
		if (TpvUtils.hasTPVWithinTime(start, end, last100PositionsPerFiveMinutes.getAll()))
			return TpvUtils.getTPVWithinTime(start, end, last100PositionsPerFiveMinutes.getAll());
		
		if (TpvUtils.hasTPVWithinTime(start, end, last100PositionsPerHour.getAll()))
			return TpvUtils.getTPVWithinTime(start, end, last100PositionsPerHour.getAll());
		
		return new ArrayList<TimePositionVelocity>();
	}

	public List<TimePositionVelocity> getTravelHistory(double minDistanceBetweenLogs) {
		List<TimePositionVelocity> history = last24HoursPositionsPer30Seconds.getAll();
		history = truncateHistoryFromLastTimeStatic(history);
		
		if (CollectionUtils.isEmpty(history))
			return new ArrayList<TimePositionVelocity>();
		
		List<TimePositionVelocity> result = new ArrayList<TimePositionVelocity>();
		
		TimePositionVelocity lastAddedTpv = null;
		for (TimePositionVelocity tpv : history) {
			if (lastAddedTpv == null) {
				result.add(0, tpv);
				continue;
			}
				
			if (MapUtils.getDistanceBetween(lastAddedTpv, tpv) > minDistanceBetweenLogs) {
				result.add(tpv);
				lastAddedTpv = tpv;
			}
		}
		
		return result;
	}
	
	private List<TimePositionVelocity> truncateHistoryFromLastTimeStatic(List<TimePositionVelocity> history) {
		if (CollectionUtils.isEmpty(history))
			return history;
		
		// Find any pauses longer than 30 minutes and truncate from there
		// A pause is defined as no movement larger than 1000 meters during a period of 30 minutes
		int index = history.size() - 1;
		int delta = 60;
		int deltaIndex = index - delta < 0 ? 0 : index - delta;
		
		while (deltaIndex > 0) {
			List<TimePositionVelocity> sublist = history.subList(deltaIndex, index);
			double distanceTraveled = MapUtils.getDistanceTraveled(sublist);
			
			if (distanceTraveled < 1000) {
				return history.subList(deltaIndex, history.size() - 1);
			}

			index -= 1;
			deltaIndex = index - delta < 0 ? 0 : index - delta;
		}
		
		return history;
	}
	
	public TimeDistanceVector getDistanceAndVectorTo(Location loc) {
		TimeDistanceVector tdv = new TimeDistanceVector();
		tdv.destination = loc;
		tdv.setLocation(getLastKnownPosition());
		
		tdv.distance = MapUtils.getDistanceBetween(tdv, tdv.destination);
		tdv.track = MapUtils.getStartBearing(tdv, tdv.destination);
		tdv.closing = false;
		
		// If the last 5 readings indicate a general closer position 
		// we are moving closer.
		List<TimePositionVelocity> lastHourTpvs = last24HoursPositionsPer30Seconds.getAll();
		if (lastHourTpvs.size() > 5) {
			int ix = lastHourTpvs.size() - 5;
			TimePositionVelocity tpv = lastHourTpvs.get(ix);
			double distance = MapUtils.getDistanceBetween(tpv, loc);
			if ((distance - tdv.distance) > 100) // At least 100 meters closer
				tdv.closing = true;
		}
		return tdv;
	}
	
	public void registerPosition(TimePositionVelocity tpv) {
		registerPosition(tpv, THIRTY_SECONDS, last24HoursPositionsPer30Seconds);
		registerPosition(tpv, FIVE_MINUTES, last100PositionsPerFiveMinutes);
		registerPosition(tpv, ONE_HOUR, last100PositionsPerHour);
	}
	
	private void registerPosition(TimePositionVelocity tpv, long minInterval, CircularBuffer<TimePositionVelocity> list) {
		TimePositionVelocity lastTpv = getLastElement(list);
		
		if (lastTpv != null) {
			if ((tpv.time - lastTpv.time) < minInterval)
				return;
		}
		
		list.add(tpv);
	}
	
	private TimePositionVelocity getLastElement(CircularBuffer<TimePositionVelocity> buff) {
		List<TimePositionVelocity> list = buff.getAll();
		if (CollectionUtils.isEmpty(list))
			return null;
		
		return list.get(list.size()-1);
	}
}
