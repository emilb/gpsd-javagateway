package se.panamach.util.converter;

import org.gpsd.client.message.TPV;

import se.panamach.services.gps.type.TimePositionVelocity;

public class TPVConverter {

	public static TimePositionVelocity getTimePositionVelocity(TPV tpv) {
		TimePositionVelocity timePosV = new TimePositionVelocity();
		timePosV.time = tpv.getTime();
		timePosV.estimatedTimestampError = tpv.ept;
		timePosV.latitude = tpv.lat;
		timePosV.estimatedLatitudeError = tpv.epy;
		timePosV.longitude = tpv.lon;
		timePosV.estimatedLongitudeError = tpv.epx;
		timePosV.altitude = tpv.alt;
		timePosV.estimatedAltitudeError = tpv.epv;
		timePosV.track = tpv.track;
		timePosV.estimatedTrackError = tpv.epd;
		timePosV.speed = tpv.speed;
		timePosV.estimatedSpeedError = tpv.eps;
		timePosV.climb = tpv.climb;
		timePosV.estimatedClimbError = tpv.epc;
		
		return timePosV;
	}
	
}
