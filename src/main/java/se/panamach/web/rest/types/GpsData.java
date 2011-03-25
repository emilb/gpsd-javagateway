package se.panamach.web.rest.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "gpsdata")
public class GpsData {
	
	@XmlElement(name = "lat")
	public double latitude;
	
	@XmlElement(name = "long")
	public double longitude;
	
	public GpsData() {}
	
	public GpsData(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
}
