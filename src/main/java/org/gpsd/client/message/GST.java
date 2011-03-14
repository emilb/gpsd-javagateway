package org.gpsd.client.message;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A GST object is a pseudorange noise report.


Here's an example:

{"class":"GST","tag":"GST","device":"/dev/ttyUSB0",
        "time":"2010-12-07T10:23:07.09Z","rms":2.440,
        "major":1.660,"minor":1.120,"orient":68.989,
        "lat":1.600,"lon":1.200,"alt":2.520}
    
 * @author emibre
 * 
 */
public class GST extends GPSdMessage {
	
	@JsonProperty(value="class")
	public String className = "GST";
	public String tag;
	public String device;
	public double rms;
	public double major;
	public double minor;
	public double orient;
	public double lat;
	public double lon;
	public double alt;

}
