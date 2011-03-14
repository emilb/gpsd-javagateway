package org.gpsd.client.message;

import java.io.IOException;

import junit.framework.TestCase;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.gpsd.client.type.TPVMode;
import org.junit.Test;

public class TPVTest {

	private static String jsonString = "{\"class\":\"TPV\",\"tag\":\"MID2\",\"device\":\"/dev/pts/1\"," + 
			"    \"time\":\"2005-06-08T10:34:48.28Z\",\"ept\":0.005," + 
			"    \"lat\":46.498293369,\"lon\":7.567411672,\"alt\":1343.127," + 
			"    \"eph\":36.000,\"epv\":32.321," + 
			"    \"track\":10.3788,\"speed\":0.091,\"climb\":-0.085,\"mode\":2}";
	
	@Test
	public void testParseTPV() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		TPV tpv = mapper.readValue(jsonString, TPV.class);
		TestCase.assertEquals(tpv.tag, "MID2");
		TestCase.assertEquals(tpv.mode, TPVMode.TWO_DIMENSIONAL);
		TestCase.assertEquals(tpv.lat, 46.498293369);
		TestCase.assertEquals(tpv.lon, 7.567411672);
	}
}
