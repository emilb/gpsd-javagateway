package se.panamach.web.rest.types.gpx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "gpx")
public class Gpx {

	@XmlAttribute(name = "version")
	String version = "1.0";
	
	@XmlElement(name = "name")
	public String name;
	
	@XmlElement(name = "wpt")
	public WayPoint waypoint;
	
	@XmlElement(name = "trk")
	public Track track;
}
