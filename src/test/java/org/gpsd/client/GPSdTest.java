package org.gpsd.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

import junit.framework.Assert;
import junit.framework.TestCase;

public class GPSdTest extends TestCase {

	@Test
	public void testMultiThreadedRequestResponse() throws IOException {
		
		int noofThreads = 2;
		int noofRequestsPerThread = 50;
		GPSd gpsd = new GPSd(null, 1233);
		gpsd.connect();
		
		List<Requester> threads = new ArrayList<Requester>();
		for (int i = 0; i < noofThreads; i++) {
			threads.add(new Requester(noofRequestsPerThread, gpsd));
		}
		
		for (Requester r : threads) {
			Thread t = new Thread(r);
			t.start();
		}
		
		boolean allDone = false;
		while (!allDone) {
			boolean foundNotDone = false;
			for (Requester r : threads) {
				if (!r.done) {
					foundNotDone = true;
					break;
				}
			}
			allDone = !foundNotDone;
			
			try {
				Thread.sleep(15);
			} catch (InterruptedException ie) {
			}
		}
		
	}
	
	private class Requester implements Runnable {

		int noofRequests;
		GPSd gpsd;
		boolean done = false;
		
		public Requester(int noofRequests, GPSd gpsd) {
			this.noofRequests = noofRequests;
			this.gpsd = gpsd;
		}
		
		@Override
		public void run() {
			int counter = 0;
			
			
			while (counter < noofRequests) {
				int operation = RandomUtils.nextInt(4);
				
				try {
					switch (operation) {
					case 0:
						
						break;

					default:
						break;
					}
					Assert.assertNotNull(gpsd.getGPSdVersion());
				} catch (Exception e) {
					e.printStackTrace();
				} 
				counter++;
				tryToSleep((long)(RandomUtils.nextDouble() * 150L));
			}
			done = true;
		}
		
		private void tryToSleep(long ms) {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException ie) {
			}
		}
	}
}
