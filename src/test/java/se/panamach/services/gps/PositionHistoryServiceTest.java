package se.panamach.services.gps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gpsd.client.GPSd;
import org.gpsd.client.connector.GPSdSimulationConnection;
import org.gpsd.client.message.GPSdMessage;
import org.gpsd.client.message.TPV;
import org.junit.Test;

import se.panamach.services.gps.type.TimePositionVelocity;
import se.panamach.util.converter.TPVConverter;


public class PositionHistoryServiceTest {

	PositionHistoryService historyService;
	private ObjectMapper jsonMapper = new ObjectMapper();
	
	private boolean simulationForward = true;
	private List<String> simulationData = new ArrayList<String>();
	private DecimalFormat df = new DecimalFormat("#.##");
	int lineNumber = 0;
	long currentSimulationTime = System.currentTimeMillis();
	
	
	@Test
	public void testGetHistory() {
		populateHistoryService();
		
		List<TimePositionVelocity> tpvs = historyService.getTravelHistory(100);
		System.out.println(tpvs.size());
	}
	
	public void populateHistoryService() {
		populateSimulationData();
		
		historyService = new PositionHistoryService();
		
		for (int i = 0; i < simulationData.size() * 1.5; i++) {
				
				String message = getNextTPV();
				
				Class<? extends GPSdMessage> msgClass = GPSdMessage.parseAndGetClassForMessage(message);
				try {
					GPSdMessage gpsdMsg = jsonMapper.readValue(message, msgClass);
					if (gpsdMsg instanceof TPV) {
						historyService.registerPosition(TPVConverter.getTimePositionVelocity((TPV)gpsdMsg));
					}
				} catch (JsonParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				currentSimulationTime += 1000L;
		}
	}
	
	private String getNextTPV() {
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
	
	private void populateSimulationData() {
		simulationData = new ArrayList<String>();
		lineNumber = 0;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(GPSdSimulationConnection.class.getResourceAsStream("/birkacruises_gps_simulation.log")));
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
}
