package org.gpsd.client;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.gpsd.client.connector.GPSdConnection;
import org.gpsd.client.message.ATT;
import org.gpsd.client.message.GPSdError;
import org.gpsd.client.message.GPSdMessage;
import org.gpsd.client.message.GST;
import org.gpsd.client.message.SKY;
import org.gpsd.client.message.Version;
import org.gpsd.client.message.Watch;

public class GPSd {

	private String host;
	private int port;
	private GPSdConnection gpsdConnection;
	private GPSdListenerManager listenerManager;
	private Version gpsdVersion;
	private ObjectMapper jsonMapper = new ObjectMapper();

	public GPSd(String host, int port) {
		this.host = host;
		this.port = port;
		listenerManager = new GPSdListenerManager();
	}

	public Version getGPSdVersion() {
		return gpsdVersion;
	}
	
	public void connect() throws IOException {
		gpsdConnection = new GPSdConnection(this);
		gpsdConnection.connect(host, port);
		
		Watch w = new Watch();
		w.enable = true;
		w.json = true;
		w.raw = 0;
		String sendMsg = "?" + w.className + "=" + jsonMapper.writeValueAsString(w) + ";\n\r";
		System.out.println(sendMsg);
		gpsdConnection.send(sendMsg);
	}
	
	public void disconnect() {
		gpsdConnection.disconnect();
	}

	public void messageReceived(String message) {
		try {
			System.out.println("Msg received: " + message);
			Class<? extends GPSdMessage> msgClass = GPSdMessage.parseAndGetClassForMessage(message);
			GPSdMessage gpsdMsg = jsonMapper.readValue(message, msgClass);
			notifyListeners(gpsdMsg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void notifyListeners(GPSdMessage gpsdMsg) {
		
		long time = (long)gpsdMsg.getTime();
		System.out.println("GPSdMessage time: " + time);
		
		if (gpsdMsg instanceof ATT) {
			listenerManager.onATT((ATT)gpsdMsg);
		}
		
		else if (gpsdMsg instanceof GST) {
			listenerManager.onGST((GST)gpsdMsg);
		}
		
		else if (gpsdMsg instanceof SKY) {
			listenerManager.onSKY((SKY)gpsdMsg);
		}
		
		else if (gpsdMsg instanceof GPSdError) {
			listenerManager.onError((GPSdError)gpsdMsg);
		}
	}
	
	public void addListener(GPSdListener listener) {
		listenerManager.add(listener);
	}

	public void removeListener(GPSdListener listener) {
		listenerManager.remove(listener);
	}
	
	public static void main(String[] args) throws Exception {
		GPSd gpsd = new GPSd("localhost", 2947);
		gpsd.connect();
		
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException ie) {
						System.out.println("interrupted");
					}
				}
				
			}
		});
		t.start();
	}
}
