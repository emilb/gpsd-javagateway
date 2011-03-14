package org.gpsd.client.message;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The command ?VERSION returns this message
 * 
 * 
 * Here's an example:
 * 
 * {"class":"VERSION","version":"2.40dev","rev":
 * "06f62e14eae9886cde907dae61c124c53eb1101f","proto_major":3,"proto_minor":1}
 * 
 * @author emibre
 * 
 */
public class Version extends GPSdMessage {

	@JsonProperty(value="class")
	public String className = "VERSION";
	public String release;
	public String rev;
	public double proto_major;
	public double proto_minor;
}
