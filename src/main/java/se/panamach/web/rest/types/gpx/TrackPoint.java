package se.panamach.web.rest.types.gpx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "trkpt")
public class TrackPoint {
	
	@XmlAttribute(name = "lat")
	public double latititude;
	
	@XmlAttribute(name = "lon")
	public double longitude;
	
	@XmlElement(name = "ele")
	public double elevation;
	
	@XmlElement(name = "time")
	public String time;
}
