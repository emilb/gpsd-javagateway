package se.panamach.services.gps;

import org.gpsd.client.GPSd;
import org.gpsd.client.GPSdSimpleListener;
import org.gpsd.client.message.TPV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.panamach.util.converter.TPVConverter;
import se.panamach.util.datastructure.FixedSizeList;

@Service
public class GPSMonitor extends GPSdSimpleListener {

	GPSd gpsdClient;
	FixedSizeList<TimePositionVelocity> positionEvents;
	
	@Autowired
	public GPSMonitor(GPSd gpsdClient) {
		gpsdClient.addListener(this);
		positionEvents = new FixedSizeList<TimePositionVelocity>(1000);
	}

	public TimePositionVelocity getLastKnownPosition() {
		return positionEvents.getLastElement();
	}
	
	@Override
	public void onTPV(TPV tpvEvent) {
		TimePositionVelocity tpv = TPVConverter.getTimePositionVelocity(tpvEvent);
		positionEvents.add(tpv);
	}
	
}