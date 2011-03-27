package se.panamach.web.rest.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "gpsStatus")
public class GPSStatus {
	
	@XmlElement(name = "lat")
	public double latitude;
	
	@XmlElement(name = "long")
	public double longitude;
	
	@XmlElement(name = "altitude")
	public double altitude;
	
	@XmlElement(name = "speed")
	public double speed;
	
	@XmlElement(name = "track")
	public double track;
	
	@XmlElement(name = "static")
	public boolean isStatic;
	
	public GPSStatus() {}
	
	public GPSStatus(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
}
