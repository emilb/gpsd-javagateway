package org.gpsd.client.connector;

import org.gpsd.client.GPSd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class GPSdConnectionWatcher {

	@Autowired
	GPSd gpsd;
	
	@Scheduled(fixedDelay=5000)
	public void checkConnection() {
		if (!gpsd.isConnected()) {
			System.out.println("Initializing gpsd connection");
			gpsd.initializeConnection();
		}
	}
}
