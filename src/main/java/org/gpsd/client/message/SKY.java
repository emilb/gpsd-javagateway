package org.gpsd.client.message;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

public class SKY extends GPSdMessage {

	@JsonProperty(value="class")
	public String className = "SKY";
	public String tag;
	public String device;
	public double xdop;
	public double ydop;
	public double vdop;
	public double tdop;
	public double hdop;
	public double pdop;
	public double gdop;
	public List<Satellite> satellites = new ArrayList<Satellite>();
}
