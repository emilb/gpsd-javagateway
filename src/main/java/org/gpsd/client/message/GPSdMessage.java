package org.gpsd.client.message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public abstract class GPSdMessage {
	
	private long time;
	
	private static String IDENTIFIER = "class";
	private static JsonFactory factory = new JsonFactory();
	
	@JsonIgnore
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time*1000;
	}

	private static Map<String, Class<? extends GPSdMessage>> identifierToClass = new HashMap<String, Class<? extends GPSdMessage>>();
	static {
		identifierToClass.put("TPV", TPV.class);
		identifierToClass.put("SKY", SKY.class);
		identifierToClass.put("GST", GST.class);
		identifierToClass.put("ATT", ATT.class);
		identifierToClass.put("VERSION", Version.class);
		identifierToClass.put("DEVICES", Devices.class);
		identifierToClass.put("WATCH", Watch.class);
		identifierToClass.put("POLL", Poll.class);
		identifierToClass.put("DEVICE", Device.class);
		identifierToClass.put("ERROR", GPSdError.class);
	}
	
	public static Class<? extends GPSdMessage> getClassForIdentifier(String identifier) {
		if (identifierToClass.containsKey(identifier))
			return identifierToClass.get(identifier);
		
		throw new IllegalArgumentException("Unknown identifier: " + identifier);
	}
	
	public static Class<? extends GPSdMessage> parseAndGetClassForMessage(String message) {
		JsonParser parser = null;
		try {
			parser = factory.createJsonParser(message);
			parser.nextToken();
			while (parser.nextToken() != JsonToken.END_OBJECT) {
				String nameField = parser.getCurrentName();
				parser.nextToken();
				if (nameField.equals(IDENTIFIER))
					return getClassForIdentifier(parser.getText());
			}
		} catch (Exception e) {
			// JSON exception
			throw new IllegalArgumentException("Could not parse message: " + message, e);
		} finally {
			if (parser != null)
				try {
					parser.close();
				} catch (IOException e) {}
		}
		
		// Nothing found
		throw new IllegalArgumentException("Could not find [" + IDENTIFIER + "] in message: " + message);
	}
}
