package se.panamach.util.map;

import java.util.Date;

import org.gpsd.client.connector.GPSdSimulationConnection;
import org.junit.Test;

import se.panamach.services.gps.type.Location;
import junit.framework.TestCase;

public class MapUtilsTest extends TestCase {

	@Test
	public void testGetDirectionBetween() {
		Location london = new Location(51.61802, -0.17578);
		Location stockholm = new Location(59.22093, 18.10547);
		Location southOfStockholm = new Location(50.22093, 18.10547);
		double degrees = MapUtils.getStartBearing(london, stockholm);
		System.out.println("start bearing: " + degrees);
		System.out.println("end bearing: " + MapUtils.getEndBearing(london, stockholm));
		
//		System.out.println(GPSdSimulationConnection.invertTrackInLine(tpv));
//		System.out.println(System.currentTimeMillis());
//		System.out.println(System.currentTimeMillis()/1000);
//		
//		System.out.println(GPSdSimulationConnection.adjustTimeInLine(tpv));
//		System.out.println("from log: " + new Date(1301134049000L));
//		System.out.println("adjusted: " + new Date(1301216588000L));
	}
	
	private static String tpv = "{\"class\":\"TPV\",\"tag\":\"MID2\",\"device\":\"/dev/ttyUSB0\",\"time\":1301134038.000,\"ept\":0.005,\"lat\":59.497349023,\"lon\":17.925195235,\"alt\":17.932,\"epx\":67.706,\"epy\":46.968,\"epv\":171.529,\"track\":350.2878,\"speed\":1.949,\"climb\":0.464,\"eps\":135.41,\"mode\":3}";
}
