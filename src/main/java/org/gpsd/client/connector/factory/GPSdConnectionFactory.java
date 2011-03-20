package org.gpsd.client.connector.factory;

import org.gpsd.client.GPSd;
import org.gpsd.client.connector.GPSdConnection;

public interface GPSdConnectionFactory {

	public GPSdConnection getGPSdConnection(GPSd gpsd);
}
