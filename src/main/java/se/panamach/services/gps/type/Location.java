package se.panamach.services.gps.type;

public class Location {

	// Latitude in degrees: +/- signifies West/East
	public double latitude;
	
	// Longitude in degrees: +/- signifies North/South
	public double longitude;
	
	public void setLocation(Location loc) {
		this.latitude = loc.latitude;
		this.longitude = loc.longitude;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (Double.doubleToLongBits(latitude) != Double
				.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double
				.doubleToLongBits(other.longitude))
			return false;
		return true;
	}
	
}
