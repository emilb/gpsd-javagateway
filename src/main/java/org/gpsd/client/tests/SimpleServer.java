package org.gpsd.client.tests;

import java.net.ServerSocket;
import java.net.Socket;

public class SimpleServer {

	public static void main(String[] args) throws Exception {
		ServerSocket server = new ServerSocket(1234);
		
		while (true) {
			Socket socket = server.accept();
			doIt(socket);
		}
	}
	
	private static void doIt(Socket socket) throws Exception {
		String msg = "line1\nline2";
		socket.getOutputStream().write(msg.getBytes());
		
		Thread.sleep(500);
		
		socket.getOutputStream().write("\nline3 is ".getBytes());
		Thread.sleep(500);
		socket.getOutputStream().write("pretty ".getBytes());
		Thread.sleep(500);
		socket.getOutputStream().write("long ".getBytes());
		Thread.sleep(500);
		socket.getOutputStream().write("haha !\n".getBytes());
	}
}
