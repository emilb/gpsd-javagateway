package org.gpsd.client.message;

import java.io.IOException;

import junit.framework.TestCase;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class GSTTest {

	private static String jsonString = "{\"class\":\"GST\",\"tag\":\"GST\",\"device\":\"/dev/ttyUSB0\",\"time\":\"2010-12-07T10:23:07.09Z\",\"rms\":2.440,\"major\":1.660,\"minor\":1.120,\"orient\":68.989,\"lat\":1.600,\"lon\":1.200,\"alt\":2.520}";
	
	@Test
	public void testParseGST() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		GST gst = mapper.readValue(jsonString, GST.class);

		TestCase.assertEquals(gst.tag, "GST");
		TestCase.assertEquals(gst.device, "/dev/ttyUSB0");
		TestCase.assertEquals(gst.lat, 1.600);
		TestCase.assertEquals(gst.lon, 1.200);
	}
}
