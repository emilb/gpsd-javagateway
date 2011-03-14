package org.gpsd.client.message;

import java.io.IOException;

import junit.framework.TestCase;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gpsd.client.type.ATTDeviceStatus;
import org.junit.Test;

public class ATTTest {

	private static String jsonString = "{\"class\":\"ATT\",\"tag\":\"PTNTHTM\",\"time\":1270938096.843,\"heading\":14223.00,\"mag_st\":\"N\",\"pitch\":169.00,\"pitch_st\":\"N\", \"roll\":-43.00,\"roll_st\":\"N\",\"dip\":13641.000,\"mag_x\":2454.000,\"temperature\":0.000,\"depth\":0.000}";
	
	@Test
	public void testParseAtt() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		
		Class<? extends GPSdMessage> cl = GPSdMessage.parseAndGetClassForMessage(jsonString);
		TestCase.assertEquals(ATT.class, cl);
		
		ATT att = mapper.readValue(jsonString, ATT.class);
		TestCase.assertEquals("PTNTHTM", att.tag);
		TestCase.assertEquals(14223.00, att.heading);
		TestCase.assertEquals(169.00, att.pitch);
		TestCase.assertEquals(ATTDeviceStatus.NORMAL, att.rollStatus);
	}
}
