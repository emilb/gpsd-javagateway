package org.gpsd.client.connector;

import java.io.IOException;

public interface GPSdConnection {

	public abstract void connect(String host, int port) throws IOException;

	public abstract void disconnect();

	public abstract boolean isRunning();
	
	public abstract boolean isConnected();

	public abstract void send(String msg) throws IOException;

}