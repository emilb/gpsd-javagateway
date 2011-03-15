package org.gpsd.client.type;

/*
 * 0=no mode value yet seen, 1=no fix, 2=2D, 3=3D
 */
public enum TPVMode {
	NO_MODE,
	NO_FIX,
	TWO_DIMENSIONAL,
	THREE_DIMENSIONAL;
	
	public String toString() {
		return this.ordinal() + "";
	}
}
