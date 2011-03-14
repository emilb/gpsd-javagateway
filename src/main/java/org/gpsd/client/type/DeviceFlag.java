package org.gpsd.client.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonCreator;

/**
 * Device flags
 * 
 * C #define	Value	Description
 * SEEN_GPS		0x01	GPS data has been seen on this device
 * SEEN_RTCM2	0x02	RTCM2 data has been seen on this device
 * SEEN_RTCM3	0x04	RTCM3 data has been seen on this device
 * SEEN_AIS		0x08	AIS data has been seen on this device
 * 
 **/
public enum DeviceFlag {
	SEEN_GPS(1), SEEN_RTCM2(2), SEEN_RTCM3(4), SEEN_AIS(8);
	
	private int value;
	
	private DeviceFlag(int value) {
		this.value = value;
	}
	
	@JsonCreator
	public static List<DeviceFlag> getDeviceFlag(String value) {
		String[] values = StringUtils.split(value, ',');
		
		List<DeviceFlag> flags = new ArrayList<DeviceFlag>();
		for (String strVal : values) {
			flags.add(findDeviceFlag(Integer.parseInt(strVal)));
		}
		return flags;
	}
	
	@JsonCreator
	public static List<DeviceFlag> getDeviceFlag(int value) {
		List<DeviceFlag> flags = new ArrayList<DeviceFlag>();
		for (DeviceFlag flag : DeviceFlag.values()) {
			if (flag.value == value)
				flags.add(flag);
		}
		return flags;
	}
	
	public static DeviceFlag findDeviceFlag(int value) {
		for (DeviceFlag flag : DeviceFlag.values()) {
			if (flag.value == value)
				return flag;
		}
		throw new IllegalArgumentException("Value: [" + value + "] not valid for DeviceFlag.");
	}
	
	@Override
	public String toString() {
		return value + "";
	}
}
