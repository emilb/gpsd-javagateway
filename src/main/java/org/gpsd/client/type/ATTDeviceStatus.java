package org.gpsd.client.type;

import org.codehaus.jackson.annotate.JsonCreator;

/**
 * ATT Device flags
 * 
 * Code	Description
 * C	magnetometer calibration alarm
 * L	low alarm
 * M	low warning
 * N	normal
 * O	high warning
 * P	high alarm
 * V	magnetometer voltage level alarm
 * 
 **/
public enum ATTDeviceStatus {
	MAGNETOMETER_CALIBRATION_ALARM("C"),
	LOW_ALARM("L"),
	LOW_WARNING("M"),
	NORMAL("N"),
	HIGH_WARNING("O"),
	HIGH_ALARM("P"),
	MAGNETOMETER_VOLTAGE_LEVEL_ALARM("V");
	
	private String value;
	
	private ATTDeviceStatus(String value) {
		this.value = value;
	}

	@JsonCreator
	public static ATTDeviceStatus getATTDeviceStatus(String value) {
		for (ATTDeviceStatus status : ATTDeviceStatus.values()) {
			if (status.value.equals(value))
				return status;
		}
		throw new IllegalArgumentException("Value: [" + value + "] not valid for ATTDeviceStatus.");
	}
	
	@Override
	public String toString() {
		return value;
	}
}
