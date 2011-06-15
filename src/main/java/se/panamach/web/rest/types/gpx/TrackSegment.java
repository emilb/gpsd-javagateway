package se.panamach.web.rest.types.gpx;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "trkseg")
public class TrackSegment {

	@XmlElement(name = "trkpt")
	public List<TrackPoint> trackPoints = new ArrayList<TrackPoint>();
}
