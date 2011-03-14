package org.gpsd.client.message;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * ?WATCH; This command sets watcher mode. It also sets or elicits a report of
 * per-subscriber policy and the raw bit. An argument WATCH object changes the
 * subscriber's policy. The responce describes the subscriber's policy. The
 * response will also include a DEVICES object.
 * 
 * There is an additional boolean "timing" attribute which is undocumented
 * because that portion of the interface is considered unstable and for
 * developer use only.
 * 
 * In watcher mode, GPS reports are dumped as TPV and SKY responses. AIS and
 * RTCM reporting is described in the next section.
 * 
 * When the C client library parses a response of this kind, it will assert the
 * POLICY_SET bit in the top-level set member.
 * 
 * Here's an example:
 * 
 * {"class":"WATCH", "raw":1,"scaled":true}
 * 
 * @author emibre
 * 
 */
public class Watch extends GPSdMessage {
	
	@JsonProperty(value="class")
	public String className = "WATCH";
	public boolean enable;
	public boolean json;
	public boolean nmea;
	public int raw;
	public boolean scaled;
	public String device;

	
}
