package org.gpsd.client.message;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * ?DEVICES; Returns a device list object
 * 
 * 
 * Here's an example:
 * 
 * {"class"="DEVICES","devices":[
 *   {"class":"DEVICE","path":"/dev/pts/1","flags":1,"driver":"SiRF binary"},
 *   {"class":"DEVICE","path":"/dev/pts/3","flags":4,"driver":"AIVDM"}]}
 *   
 * @author emibre
 * 
 */
public class Devices extends GPSdMessage {

	@JsonProperty(value="class")
	public String className = "DEVICES";
	public List<Device> devices = new ArrayList<Device>();
}
