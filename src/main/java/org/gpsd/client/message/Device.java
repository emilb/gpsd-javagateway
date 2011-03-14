package org.gpsd.client.message;

import org.codehaus.jackson.annotate.JsonProperty;
import org.gpsd.client.type.DeviceMode;

/**
 * ?DEVICE This command reports (when followed by ';') the state of a device, or
 * sets (when followed by '=' and a DEVICE object) device-specific control bits,
 * notably the device's speed and serial mode and the native-mode bit. The
 * parameter-setting form will be rejected if more than one client is attached
 * to the channel.
 * 
 * Pay attention to the response, because it is possible for this command to
 * fail if the GPS does not support a speed-switching command or only supports
 * some combinations of serial modes. In case of failure, the daemon and GPS
 * will continue to communicate at the old speed.
 * 
 * Use the parameter-setting form with caution. On USB and Bluetooth GPSes it is
 * also possible for serial mode setting to fail either because the serial
 * adaptor chip does not support non-8N1 modes or because the device firmware
 * does not properly synchronize the serial adaptor chip with the UART on the
 * GPS chipset whjen the speed changes. These failures can hang your device,
 * possibly requiring a GPS power cycle or (in extreme cases) physically
 * disconnecting the NVRAM backup battery.
 * 
 * 
 * Here's an example:
 * 
 * {"class":"DEVICE", "speed":4800,"serialmode":"8N1","native":0}
 * 
 * @author emibre
 * 
 */
public class Device extends GPSdMessage {

	public String path;
	public double activated;
	//public List<DeviceFlag> flags = new ArrayList<DeviceFlag>();
	public String driver;
	public String subtype;
	public int bps;
	public String parity;
	public String stopbits;
	@JsonProperty(value="native")
	public DeviceMode deviceMode;
	public double cycle;
	public double mincycle;
	
	public double proto_minor;
	
}
