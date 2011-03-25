package se.panamach.web.rest.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "distanceVector")
public class DistanceVector {
	
	@XmlElement(name = "currentPosition")
	public GpsData currentPosition;
	
	@XmlElement(name = "destination")
	public GpsData destination;
	
	@XmlElement(name = "isClosingOnDestination")
	public boolean closing;
	
	@XmlElement(name = "distanceToDestination")
	public double distanceToDestination;
	
	@XmlElement(name = "track")
	public double trackToDestination;
	
	public DistanceVector() {}
	
}
