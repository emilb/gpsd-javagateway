package se.panamach.util.configuration;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PanamaConfiguration {

	private static Logger log = LoggerFactory
			.getLogger(PanamaConfiguration.class);
	
	private static CompositeConfiguration config = new CompositeConfiguration();
	private static String[] configurationLocations = new String[] {
			"/etc/panama.properties", 
			"panama-default.properties" };

	static {
		for (String c : configurationLocations) {
			try {
				config.addConfiguration(new PropertiesConfiguration(c));
				System.out.println("Added " + c);
			} catch (Exception e) {
				log.info("Failed to load  configuration: " + c);
			}
		}
		config.addConfiguration(new SystemConfiguration());
	}

	public static Configuration get() {
		return config;
	}
	
}
