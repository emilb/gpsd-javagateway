package org.gpsd.client.message;

import org.codehaus.jackson.annotate.JsonProperty;

public class Satellite extends GPSdMessage {

	@JsonProperty(value="PRN")
	public int PRN;
	public double az;
	public double el;
	public double ss;
	public boolean used;
	
}
