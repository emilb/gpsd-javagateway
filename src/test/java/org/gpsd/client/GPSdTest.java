package org.gpsd.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.gpsd.client.message.GPSdMessage;
import org.gpsd.client.message.Watch;
import org.junit.Test;

import junit.framework.Assert;
import junit.framework.TestCase;

public class GPSdTest extends TestCase {

	@Test
	public void testMultiThreadedRequestResponse() throws IOException {
		
		int noofThreads = 10;
		int noofRequestsPerThread = 100;
		GPSd gpsd = new GPSd(null, 1233);
		gpsd.connect();
		
		List<Requester> threads = new ArrayList<Requester>();
		for (int i = 0; i < noofThreads; i++) {
			threads.add(new Requester(noofRequestsPerThread, gpsd, i));
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
		
		
		for (Requester r : threads) {
			Assert.assertEquals(0, r.noofFailed);
		}
	}
	
	private class Requester implements Runnable {

		int noofRequests;
		int threadNo;
		GPSd gpsd;
		boolean done = false;
		int noofFailed = 0;
		
		public Requester(int noofRequests, GPSd gpsd, int threadNo) {
			this.noofRequests = noofRequests;
			this.threadNo = threadNo;
			this.gpsd = gpsd;
		}
		
		@Override
		public void run() {
			int counter = 0;
			
			
			while (counter < noofRequests) {
				int operation = RandomUtils.nextInt(4);
				GPSdMessage result = null;
				try {
					switch (operation) {
					case 0:
						System.out.println("Thread " + threadNo + " requesting version [" + counter + "]");
						result = gpsd.getGPSdVersion();
						System.out.println("Thread " + threadNo + " got version");
						break;

					case 1:
						System.out.println("Thread " + threadNo + " requesting poll [" + counter + "]");
						result = gpsd.getGPSdPoll();
						System.out.println("Thread " + threadNo + " got poll");
						break;
					
					case 2:
						System.out.println("Thread " + threadNo + " requesting devices [" + counter + "]");
						result = gpsd.listGPSdDevices();
						System.out.println("Thread " + threadNo + " got devices");
						break;
						
					case 3:
						Watch w = new Watch();
						w.enable = true;
						w.json = true;
						w.raw = 0;
						System.out.println("Thread " + threadNo + " requesting watch [" + counter + "]");
						result = gpsd.startGPSdWatch(w);
						System.out.println("Thread " + threadNo + " got watch");
						break;
					default:
						break;
					}
					
					if (result == null) {
						noofFailed++;
						System.out.println("Thread " + threadNo + " got a NULL response");
					}
				} catch (Exception e) {
					e.printStackTrace();
				} 
				counter++;
				tryToSleep((long)(RandomUtils.nextDouble() * 10L));
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
