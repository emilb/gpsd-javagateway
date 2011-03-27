package org.gpsd.client.connector.factory;

import org.gpsd.client.GPSd;
import org.gpsd.client.connector.GPSdConnection;
import org.gpsd.client.connector.GPSdSimulationConnection;

public class GPSdConnectionFactorySimulation implements GPSdConnectionFactory {

	@Override
	public GPSdConnection getGPSdConnection(GPSd gpsd) {
		return new GPSdSimulationConnection(gpsd);
	}
}
