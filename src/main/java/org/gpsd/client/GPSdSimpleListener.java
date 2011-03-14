package org.gpsd.client;

import org.gpsd.client.message.ATT;
import org.gpsd.client.message.GPSdError;
import org.gpsd.client.message.GST;
import org.gpsd.client.message.SKY;

public class GPSdSimpleListener implements GPSdListener {

	@Override
	public void onATT(ATT att) {
	}

	@Override
	public void onError(GPSdError error) {
	}

	@Override
	public void onSKY(SKY sky) {
	}

	@Override
	public void onGST(GST gst) {
	}

}
