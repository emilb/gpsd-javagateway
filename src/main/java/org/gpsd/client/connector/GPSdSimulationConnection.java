package org.gpsd.client.connector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gpsd.client.GPSd;
import org.gpsd.client.message.Devices;
import org.gpsd.client.message.GPSdMessage;
import org.gpsd.client.message.Poll;
import org.gpsd.client.message.Version;
import org.gpsd.client.message.Watch;

public class GPSdSimulationConnection implements Runnable, GPSdConnection {

	private GPSd gpsd;
	private boolean running = false;
	private ObjectMapper jsonMapper = new ObjectMapper();
	
	private List<GPSdMessage> responseQueue = new ArrayList<GPSdMessage>();
	Thread t;
	
	private boolean simulationForward = true;
	private List<String> simulationData = new ArrayList<String>();
	private DecimalFormat df = new DecimalFormat("#.##");
	int lineNumber = 0;
	int timeFactor = 10;
	long currentSimulationTime = System.currentTimeMillis();
	
	public GPSdSimulationConnection(GPSd gpsd) {
		this.gpsd = gpsd;
	}

	@Override
	public boolean isConnected() {
		return running;
	}

	@Override
	public void connect(String host, int port) throws IOException {

//		populateSimulationDataText("/birkacruises_gps_simulation.log");
		populateSimulationDataZip("/STO-RIGA.log.zip");
		running = true;
		t = new Thread(this);
		t.start();
	}

	@Override
	public void disconnect() {
		running = false;
		simulationData.clear();
		responseQueue.clear();
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
					response = getNextTPV();
				}
				
				gpsd.messageReceived(response);
				tryToSleep((long)(1000.0 / (double)timeFactor));
				currentSimulationTime += 1000L;
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
	
	
	private String getNextTPV() throws IOException {
		if (lineNumber >= simulationData.size() && simulationForward) {
			simulationForward = false;
			lineNumber = simulationData.size() - 1;
		} else if (lineNumber < 0 && !simulationForward) {
			simulationForward = true;
			lineNumber = 0;
		}

		String line = simulationData.get(lineNumber);
		lineNumber += simulationForward ? 1 : -1;
		
		if (!simulationForward)
			line = invertTrackInLine(line);
		
		return adjustTimeInLine(line);
	}
	
	private String invertTrackInLine(String line) {
		StringBuilder sb = new StringBuilder();
		
		int startPos = StringUtils.indexOf(line, "\"track\":");
		if (startPos < 0)
			return line;
		
		startPos += 8;
		
		double track = Double.parseDouble(StringUtils.substring(line, startPos, startPos + 8));
		double reversedTrack = (track + 180.0) % 360;

		sb.append(StringUtils.substring(line, 0, startPos));
		
		sb.append(df.format(reversedTrack));
		
		int speedPos = StringUtils.indexOf(line, ",\"speed");
		sb.append(StringUtils.substring(line, speedPos));
		return sb.toString();
	}
	
	private String adjustTimeInLine(String line) {
		StringBuilder sb = new StringBuilder();
		
		int startPos = StringUtils.indexOf(line, "\"time\":");
		if (startPos < 0)
			return line;
		
		startPos += 7;
		
		sb.append(StringUtils.substring(line, 0, startPos));
		sb.append(currentSimulationTime / 1000).append(".000");
		sb.append(StringUtils.substring(line, startPos+14, line.length()));
		return sb.toString();
	}
	
	private void populateSimulationDataZip(String resource) throws IOException {
		ZipInputStream zis = new ZipInputStream(GPSdSimulationConnection.class.getResourceAsStream(resource));
		zis.getNextEntry();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(zis));
		populateSimulationData(reader);
		zis.closeEntry();
		zis.close();
	}
	
	private void populateSimulationDataText(String resource) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(GPSdSimulationConnection.class.getResourceAsStream(resource)));
		populateSimulationData(reader);
	}

	private void populateSimulationData(BufferedReader reader) {
		simulationData = new ArrayList<String>();
		lineNumber = 0;
		
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				simulationData.add(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setTimeFactor(int timefactor) {
		this.timeFactor = timefactor;
	}
}
