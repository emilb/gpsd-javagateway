package org.gpsd.client.type;

/**
 * 0 means NMEA mode and 1 means alternate mode (binary if it has one, for SiRF
 * and Evermore chipsets in particular). Attempting to set this mode on a
 * non-GPS device will yield an error
 * 
 * @author emibre
 * 
 */
public enum DeviceMode {
	NMEA, ALTERNATE
}
