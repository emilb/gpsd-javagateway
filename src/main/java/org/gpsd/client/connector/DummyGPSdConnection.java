package org.gpsd.client.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gpsd.client.GPSd;
import org.gpsd.client.message.Devices;
import org.gpsd.client.message.GPSdMessage;
import org.gpsd.client.message.Poll;
import org.gpsd.client.message.TPV;
import org.gpsd.client.message.Version;
import org.gpsd.client.message.Watch;
import org.gpsd.client.type.TPVMode;

public class DummyGPSdConnection implements Runnable, IGPSdConnection {

	private GPSd gpsd;
	private boolean running = false;
	private ObjectMapper jsonMapper = new ObjectMapper();
	
	private List<GPSdMessage> responseQueue = new ArrayList<GPSdMessage>();
	Thread t;
	
	public DummyGPSdConnection(GPSd gpsd) {
		this.gpsd = gpsd;
	}

	@Override
	public void connect(String host, int port) throws IOException {

		running = true;
		t = new Thread(this);
		t.start();
	}

	@Override
	public void disconnect() {
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void send(String msg) throws IOException {
		GPSdMessage response = null;
		
		if (msg.startsWith("?WATCH")) {
			Watch w = new Watch();
			w.device = "/dev/ttyUSB0";
			w.json = true;
			w.raw = 0;
			
			response = w;
		} else if (msg.startsWith("?POLL")) {
			Poll p = new Poll();
			p.active = 1;
			p.setTime(System.currentTimeMillis());
			
			response = p;
		} else if (msg.startsWith("?VERSION")) {
			Version v = new Version();
			v.proto_major = RandomUtils.nextDouble();
			v.proto_minor = RandomUtils.nextFloat();
			v.release = "Dummy gpsd";
			
			response = v;
		} else if (msg.startsWith("?DEVICE")) {
			Devices d = new Devices();
			d.setTime(System.currentTimeMillis());
			
			response = d;
		}
		
		if (response != null) {
			responseQueue.add(response);
			t.interrupt();
		}
	}
	
	@Override
	public void run() {
		try {
			while (running) {
				String response = checkIfResponseIsExpected();
				
				if (response == null) {
					response = getRandomTPV();
				}
				
				gpsd.messageReceived(response);
				tryToSleep(950);
			}
		} catch (Exception e) {
			e.printStackTrace();
			disconnect();
		}
	}

	private void tryToSleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ie) {
		}
	}
	
	private String checkIfResponseIsExpected() throws JsonGenerationException, JsonMappingException, IOException {
		if (responseQueue.isEmpty())
			return null;
		
		return jsonMapper.writeValueAsString(responseQueue.remove(0));
	}
	
	private String getRandomTPV() throws JsonGenerationException, JsonMappingException, IOException {
		TPV t = new TPV();
		t.climb = RandomUtils.nextDouble();
		t.alt = RandomUtils.nextDouble();
		t.device = "/dev/ttyUSB0";
		t.epc = RandomUtils.nextDouble();
		t.mode = TPVMode.THREE_DIMENSIONAL;
		t.setTime(System.currentTimeMillis());
		return jsonMapper.writeValueAsString(t);
	}
}
