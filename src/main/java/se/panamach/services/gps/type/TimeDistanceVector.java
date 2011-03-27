package se.panamach.services.gps.type;


public class TimeDistanceVector extends Location {

	// The estimated time of arrival to destination
	public long estimatedArrivalTime;
	
	// The destination
	public Location destination;
	
	// Degrees from true north towards destination
	public double track;
	
	// Distance in meters to destination
	public double distance;
	
	// Are we getting closer or moving away
	public boolean closing;
}
