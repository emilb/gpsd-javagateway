package org.gpsd.client.message;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import junit.framework.TestCase;

public class SKYTest extends TestCase {
	
	private String jsonString ="{\"class\":\"SKY\",\"tag\":\"MID2\",\"device\":\"/dev/pts/1\",\"time\":\"2005-07-08T11:28:07.11Z\",\"xdop\":1.55,\"hdop\":1.24,\"pdop\":1.99,\"satellites\":[{\"PRN\":23,\"el\":6,\"az\":84,\"ss\":0,\"used\":false},{\"PRN\":28,\"el\":7,\"az\":160,\"ss\":0,\"used\":false},{\"PRN\":8,\"el\":66,\"az\":189,\"ss\":44,\"used\":true},{\"PRN\":29,\"el\":13,\"az\":273,\"ss\":0,\"used\":false},{\"PRN\":10,\"el\":51,\"az\":304,\"ss\":29,\"used\":true},{\"PRN\":4,\"el\":15,\"az\":199,\"ss\":36,\"used\":true},{\"PRN\":2,\"el\":34,\"az\":241,\"ss\":43,\"used\":true},{\"PRN\":27,\"el\":71,\"az\":76,\"ss\":43,\"used\":true}]}";

	public void testParseSKY() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		SKY sky = mapper.readValue(jsonString, SKY.class);
		TestCase.assertEquals("MID2", sky.tag);
		TestCase.assertEquals("/dev/pts/1", sky.device);
		TestCase.assertEquals(1.99, sky.pdop);
		TestCase.assertEquals(8, sky.satellites.size());
		
		Satellite sat1 = sky.satellites.get(0);
		TestCase.assertEquals(23, sat1.PRN);
		TestCase.assertEquals(6.0, sat1.el);
	}
}
