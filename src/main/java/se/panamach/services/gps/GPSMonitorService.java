package se.panamach.services.gps;

import org.gpsd.client.GPSd;
import org.gpsd.client.GPSdSimpleListener;
import org.gpsd.client.message.TPV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.panamach.services.gps.type.TimePositionVelocity;
import se.panamach.util.converter.TPVConverter;

@Service
public class GPSMonitorService extends GPSdSimpleListener {

	GPSd gpsdClient;
	PositionHistoryService positionHistory;
	
	@Autowired
	public GPSMonitorService(GPSd gpsdClient, PositionHistoryService positionHistory) {
		this.gpsdClient = gpsdClient;
		this.positionHistory = positionHistory;
		
		this.gpsdClient.addListener(this);
	}
	
	@Override
	public void onTPV(TPV tpvEvent) {
//		System.out.println("got a tpvevent");
		TimePositionVelocity tpv = TPVConverter.getTimePositionVelocity(tpvEvent);
		positionHistory.registerPosition(tpv);
	}
	
}