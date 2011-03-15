package org.gpsd.client;

import java.util.ArrayList;

import org.gpsd.client.message.ATT;
import org.gpsd.client.message.GPSdError;
import org.gpsd.client.message.GST;
import org.gpsd.client.message.SKY;
import org.gpsd.client.message.TPV;

public class GPSdListenerManager extends ArrayList<GPSdListener> implements
		GPSdListener {

	private static final long serialVersionUID = 7923225052021029298L;

	@Override
	public void onATT(ATT att) {
		for (GPSdListener listener : this) {
			listener.onATT(att);
		}
	}

	@Override
	public void onError(GPSdError error) {
		for (GPSdListener listener : this) {
			listener.onError(error);
		}
	}

	@Override
	public void onSKY(SKY sky) {
		for (GPSdListener listener : this) {
			listener.onSKY(sky);
		}
	}

	@Override
	public void onGST(GST gst) {
		for (GPSdListener listener : this) {
			listener.onGST(gst);
		}
	}

	@Override
	public void onTPV(TPV tpv) {
		for (GPSdListener listener : this) {
			listener.onTPV(tpv);
		}
	}
}
