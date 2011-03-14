package org.gpsd.client.message;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;


/**
 * ?POLL; The POLL command requests data from the last-seen fixes on all active
 * GPS devices. Devices must previously have been activated by ?WATCH to be
 * pollable, or have been specified on the GPSD command line together with an -n
 * option.
 * 
 * Polling can lead to possibly surprising results when it is used on a device
 * such as an NMEA GPS for which a complete fix has to be accumulated from
 * several sentences. If you poll while those sentences are being emitted, the
 * response will contain the last complete fix data and may be as much as one
 * cycle time (typically 1 second) stale.
 * 
 * The POLL response will contain a timestamped list of TPV objects describing
 * cached data, and a timestamped list of SKY objects describing satellite
 * configuration. If a device has not seen fixes, it will be reported with a
 * mode field of zero.
 * 
 * 
 * Here's an example of a POLL response:

{"class":"POLL","timestamp":1270517274.846,"active":1,
    "fixes":[{"class":"TPV","tag":"MID41","device":"/dev/ttyUSB0",
              "time":"2010-09-08T13:33:06.09Z",
	      "ept":0.005,"lat":40.035093060,
              "lon":-75.519748733,"track":99.4319,"speed":0.123,"mode":2}],
    "skyviews":[{"class":"SKY","tag":"MID41","device":"/dev/ttyUSB0",
                 "time":1270517264.240,"hdop":9.20,
                 "satellites":[{"PRN":16,"el":55,"az":42,"ss":36,"used":true},
                               {"PRN":19,"el":25,"az":177,"ss":0,"used":false},
                               {"PRN":7,"el":13,"az":295,"ss":0,"used":false},
                               {"PRN":6,"el":56,"az":135,"ss":32,"used":true},
                               {"PRN":13,"el":47,"az":304,"ss":0,"used":false},
                               {"PRN":23,"el":66,"az":259,"ss":0,"used":false},
                               {"PRN":20,"el":7,"az":226,"ss":0,"used":false},
                               {"PRN":3,"el":52,"az":163,"ss":32,"used":true},
                               {"PRN":31,"el":16,"az":102,"ss":0,"used":false}
]}]}
 *
 * @author emibre
 * 
 */
public class Poll extends GPSdMessage {

	@JsonProperty(value="class")
	public String className = "POLL";
	public int active;
	public List<TPV> fixes = new ArrayList<TPV>();
	public List<SKY> skyviews = new ArrayList<SKY>();
}
