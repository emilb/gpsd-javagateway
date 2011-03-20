package se.panamach.services.gps;


public class TimePositionVelocity {

	public double time;
	
	// Estimated timestamp error (%f, seconds, 95% confidence)
	public double estimatedTimestampError;
	
	// Latitude in degrees: +/- signifies West/East
	public double latitude;
	
	// Latitude error estimate in meters, 95% confidence
	public double estimatedLatitudeError;
	
	// Longitude in degrees: +/- signifies North/South
	public double longitude;
	
	// Longitude error estimate in meters, 95% confidence
	public double estimatedLongitudeError;
	
	// Altitude in meters
	public double altitude;
	
	// Climb/sink error estimate in meters/sec, 95% confidence
	public double estimatedAltitudeError;
	
	// Course over ground, degrees from true north
	public double track;
	
	// Speed over ground, meters per second
	public double speed;
	
	//	Climb (positive) or sink (negative) rate, meters per second
	public double climb;
	
	// Track error estimate in degrees, 95% confidence
	public double estimatedTrackError;
	
	// Speed error estimate in meters/sec, 95% confidence
	public double estimatedSpeedError;
	
	// Climb/sink error estimate in meters/sec, 95% confidence
	public double estimatedClimbError;
}
