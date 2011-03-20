package org.gpsd.client.connector.factory;

import org.gpsd.client.GPSd;
import org.gpsd.client.connector.GPSdConnection;
import org.gpsd.client.connector.GPSdTcpConnection;

public class GPSdConnectionFactoryTcp implements GPSdConnectionFactory {

	@Override
	public GPSdConnection getGPSdConnection(GPSd gpsd) {
		return new GPSdTcpConnection(gpsd);
	}
}
