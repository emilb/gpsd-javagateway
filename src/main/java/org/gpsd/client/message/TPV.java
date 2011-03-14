package org.gpsd.client.message;

import org.codehaus.jackson.annotate.JsonProperty;
import org.gpsd.client.type.TPVMode;

/**
 * A TPV object is a time-position-velocity report. The "class" and "mode" fields will reliably be present. Others may be reported or not depending on the fix quality.

Table 1. TPV object

Name	Always?	Type	Description
class	Yes	string	Fixed: "TPV"
tag	No	string	Type tag associated with this GPS sentence; from an NMEA device this is just the NMEA sentence type..
device	No	string	Name of originating device
time	No	string	Time/date stamp in ISO8601 format, UTC. May have a fractional part of up to .01sec precision.
ept	No	numeric	Estimated timestamp error (%f, seconds, 95% confidence).
lat	No	numeric	Latitude in degrees: +/- signifies West/East
lon	No	numeric	Longitude in degrees: +/- signifies North/South.
alt	No	numeric	Altitude in meters.
epx	No	numeric	Longitude error estimate in meters, 95% confidence.
epy	No	numeric	Latitude error estimate in meters, 95% confidence.
epv	No	numeric	Estimated vertical error in meters, 95% confidence.
track	No	numeric	Course over ground, degrees from true north.
speed	No	numeric	Speed over ground, meters per second.
climb	No	numeric	Climb (positive) or sink (negative) rate, meters per second.
epd	No	numeric	Direction error estimate in degrees, 95% confifdence.
eps	No	numeric	Speed error estinmate in meters/sec, 95% confifdence.
epc	No	numeric	Climb/sink error estinmate in meters/sec, 95% confifdence.
mode	Yes	numeric	NMEA mode: %d, 0=no mode value yet seen, 1=no fix, 2=2D, 3=3D.

When the C client library parses a response of this kind, it will assert validity bits in the top-level set member for each field actually received; see gps.h for bitmask names and values.

Here's an example:

{"class":"TPV","tag":"MID2","device":"/dev/pts/1",
    "time":"2005-06-08T10:34:48.28Z,"ept":0.005,
    "lat":46.498293369,"lon":7.567411672,"alt":1343.127, 
    "eph":36.000,"epv":32.321,
    "track":10.3788,"speed":0.091,"climb":-0.085,"mode":3}
    
 * @author emibre
 *
 */
public class TPV extends GPSdMessage {
	
	@JsonProperty(value="class")
	public String className = "TPV";
	public String tag;
	public String device;
	public double ept;
	public double lat;
	public double lon;
	public double alt;
	public double epx;
	public double epv;
	public double track;
	public double speed;
	public double climb;
	public double epd;
	public double eps;
	public double epc;
	public TPVMode mode;
		
}
