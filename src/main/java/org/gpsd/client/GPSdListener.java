package org.gpsd.client;

import org.gpsd.client.message.ATT;
import org.gpsd.client.message.GPSdError;
import org.gpsd.client.message.GST;
import org.gpsd.client.message.SKY;

public interface GPSdListener {

	public void onATT(ATT att);
	public void onError(GPSdError error);
	public void onSKY(SKY sky);
	public void onGST(GST gst);
	
}
