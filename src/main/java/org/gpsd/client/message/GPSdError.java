package org.gpsd.client.message;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 * Here's an example:
 * 
 * 
 * @author emibre
 * 
 */
public class GPSdError extends GPSdMessage {

	@JsonProperty(value="class")
	public String className = "ERROR";
	public String message;
}
