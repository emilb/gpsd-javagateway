package se.panamach.util.converter;

import java.util.ArrayList;
import java.util.List;

import se.panamach.services.gps.type.Location;

public class LocationListConverter {

	public static List<Location> toLocation(@SuppressWarnings("rawtypes") List l) {
		List<Location> r = new ArrayList<Location>();
		for (Object o : l) {
			if (o instanceof Location)
				r.add((Location)o);
		}
		return r;
	}
}
