package org.gpsd.client.message;

import org.codehaus.jackson.annotate.JsonProperty;
import org.gpsd.client.type.ATTDeviceStatus;

/**
 * An ATT object is a vehicle-attitude report. It is returned by 
 * digital-compass and gyroscope sensors; depending on device, it
 * may include: heading, pitch, roll, yaw, gyroscope, and magnetic-
 * field readings. Because such sensors are often bundled as part 
 * of marine-navigation systems, the ATT response may also include 
 * water depth.
 *
 * The "class", "mode", and "tag" fields will reliably be present. 
 * Others may be reported or not depending on the specific device type.
 *
 * Here's an example:

{"class":"ATT","tag":"PTNTHTM","time":1270938096.843,
    "heading":14223.00,"mag_st":"N",
    "pitch":169.00,"pitch_st":"N", "roll":-43.00,"roll_st":"N",
    "dip":13641.000,"mag_x":2454.000,"temperature":0.000,"depth":0.000}
    
 * @author emibre
 *
 */
public class ATT extends GPSdMessage {
	
	@JsonProperty(value="class")
	public String className = "ATT";
	public String tag;
	public String device;
	public double heading;
	@JsonProperty(value="mag_st")
	public ATTDeviceStatus magStatus;
	public double pitch;
	@JsonProperty(value="pitch_st")
	public ATTDeviceStatus pitchStatus;
	public double yaw;
	@JsonProperty(value="yaw_st")
	public ATTDeviceStatus yawStatus;
	public double roll;
	@JsonProperty(value="roll_st")
	public ATTDeviceStatus rollStatus;
	public double dip;
	public double mag_len;
	public double mag_x;
	public double mag_y;
	public double mag_z;
	public double acc_len;
	public double acc_x;
	public double acc_z;
	public double gyro_x;
	public double gyro_y;
	public double depth;
	public double temperature;
	
}
