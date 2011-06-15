package se.panamach.web.rest.types.gpx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "trk")
public class Track {

	@XmlElement(name = "name")
	public String name;
	
	@XmlElement(name = "number")
	public int number;
	
	@XmlElement(name = "trkseg")
	public TrackSegment trackSegment;
}
