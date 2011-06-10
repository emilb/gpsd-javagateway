package org.gpsd.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gpsd.client.connector.GPSdConnection;
import org.gpsd.client.connector.factory.GPSdConnectionFactory;
import org.gpsd.client.message.ATT;
import org.gpsd.client.message.Devices;
import org.gpsd.client.message.GPSdError;
import org.gpsd.client.message.GPSdMessage;
import org.gpsd.client.message.GST;
import org.gpsd.client.message.Poll;
import org.gpsd.client.message.SKY;
import org.gpsd.client.message.TPV;
import org.gpsd.client.message.Version;
import org.gpsd.client.message.Watch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GPSd {

	private static Marker logMarkerGPSdMessage = MarkerFactory.getMarker("GPSD_MESSAGE");
	private static Logger log = LoggerFactory.getLogger(GPSd.class);
	
	private String host;
	private int port;
	
	private long timeForLastGPSdMessage = 0;
	
	@Autowired
	private GPSdConnectionFactory connectionFactory;
	private GPSdConnection gpsdConnection;
	
	private GPSdListenerManager listenerManager;
	private ObjectMapper jsonMapper = new ObjectMapper();
	private SyncMessageResponseManager responseManager;
	
	public GPSd() {
		this("localhost", 2947);
	}
	
	public GPSd(String host, int port) {
		System.out.println("Creating GPSd");
		this.host = host;
		this.port = port;
		listenerManager = new GPSdListenerManager();
		responseManager = new SyncMessageResponseManager();
	}

	public boolean isConnected() {
		if (gpsdConnection == null)
			return false;
		
		if (gpsdConnection.isConnected()) {
			return ((System.currentTimeMillis() - timeForLastGPSdMessage) < 3000);
		}
		
		return false;
	}
	
	public void connect() throws IOException {
		gpsdConnection.connect(host, port);
	}
	
	public void disconnect() {
		gpsdConnection.disconnect();
	}
	
	public void initializeConnection() {
		try {
			if (gpsdConnection != null) {
				gpsdConnection.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println("Trying to connect...");
			gpsdConnection = connectionFactory.getGPSdConnection(this);
			connect();
			
			Watch w = new Watch();
			w.enable = true;
			w.json = true;
			w.raw = 0;
			System.out.println("Starting watch...");
			startGPSdWatch(w);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public GPSdConnection getGPSdConnection() {
		return gpsdConnection;
	}
	
	public Version getGPSdVersion() throws JsonGenerationException, JsonMappingException, IOException {
		return sendAndWaitForResponse("VERSION", null, Version.class);
	}
	
	public Watch startGPSdWatch(Watch watch) throws JsonGenerationException, JsonMappingException, IOException {
		return sendAndWaitForResponse("WATCH", watch, Watch.class);
	}
	
	public Poll getGPSdPoll() throws JsonGenerationException, JsonMappingException, IOException {
		return sendAndWaitForResponse("POLL", null, Poll.class);
	}
	
	public Devices listGPSdDevices() throws JsonGenerationException, JsonMappingException, IOException {
		return sendAndWaitForResponse("DEVICE", null, Devices.class);
	}
	
	public void messageReceived(String message) {
		log.info(logMarkerGPSdMessage, message);
		timeForLastGPSdMessage = System.currentTimeMillis();
		try {
			Class<? extends GPSdMessage> msgClass = GPSdMessage.parseAndGetClassForMessage(message);
			GPSdMessage gpsdMsg = jsonMapper.readValue(message, msgClass);
			notifyListeners(gpsdMsg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void notifyListeners(GPSdMessage gpsdMsg) {
		/*
		 * Broadcast messages
		 */
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
		
		else if (gpsdMsg instanceof TPV) {
			listenerManager.onTPV((TPV)gpsdMsg);
		}
		
		/*
		 * Response messages
		 */
		else {
			ResponseLock responseLock = responseManager.pop(gpsdMsg.getClass());
			if (responseLock == null) {
				System.out.println("Got response without a waiting lock, discarding. Type was: " + gpsdMsg.getClass().getSimpleName());
				return;
			}
			
			synchronized (responseLock) {
				responseLock.setResponse(gpsdMsg);
				responseLock.notifyAll();
			}
		}
	}
	
	public void addListener(GPSdListener listener) {
		listenerManager.add(listener);
	}

	public void removeListener(GPSdListener listener) {
		listenerManager.remove(listener);
	}
	
	private <T extends GPSdMessage> T sendAndWaitForResponse(String cmd, GPSdMessage msg, Class<T> expectedResponseClass) throws JsonGenerationException, JsonMappingException, IOException {
		ResponseLock lock = sendRequest(cmd, msg, expectedResponseClass);
		synchronized (lock) {
			try {
				lock.wait(2000);
			} catch (InterruptedException e) {
			}
		}
		return expectedResponseClass.cast(lock.getResponse());
	}
	
	private ResponseLock sendRequest(String cmd, GPSdMessage msg, Class<? extends GPSdMessage> expectedResponseClass) throws JsonGenerationException, JsonMappingException, IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("?").append(cmd);
		
		if (msg != null) {
			sb.append("=");
			sb.append(jsonMapper.writeValueAsString(msg));
		}
		
		sb.append(";\n\r");
		
		ResponseLock lock = responseManager.push(expectedResponseClass);
		gpsdConnection.send(sb.toString());
		return lock;
	}
	
	private class SyncMessageResponseManager {
		
		private Object listLock = new Object();
		private Map<Class<? extends GPSdMessage>, List<ResponseLock>> responseLocks;
		
		public SyncMessageResponseManager() {
			responseLocks = new HashMap<Class<? extends GPSdMessage>, List<ResponseLock>>();
		}
		
		public ResponseLock push(Class<? extends GPSdMessage> expectedResponseClass) {
			ResponseLock lock = new ResponseLock();
			synchronized (listLock) {
				getLockList(expectedResponseClass).add(lock);
			}
			return lock;
		}
		
		public ResponseLock pop(Class<? extends GPSdMessage> expectedResponseClass) {
			if (getLockList(expectedResponseClass).isEmpty()) {
				System.out.println("The locklist for " + expectedResponseClass.getSimpleName() + " is empty, returning null");
				return null;
			}
			
			synchronized (listLock) {
				return getLockList(expectedResponseClass).remove(0);
			}
		}
		
		private List<ResponseLock> getLockList(Class<? extends GPSdMessage> clazz) {
			synchronized (listLock) {
				if (responseLocks.containsKey(clazz))
					return responseLocks.get(clazz);
				
				List<ResponseLock> newList = new ArrayList<ResponseLock>();
				responseLocks.put(clazz, newList);
				return newList;
			}
		}
	}

	private class ResponseLock {
		GPSdMessage response;

		public GPSdMessage getResponse() {
			return response;
		}

		public void setResponse(GPSdMessage response) {
			this.response = response;
			if (response == null) {
				System.out.println("Warning response is null in ResponseLock");
			}
		}
		
	}
}
