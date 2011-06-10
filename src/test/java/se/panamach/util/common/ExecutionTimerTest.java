package se.panamach.util.common;

import junit.framework.Assert;

import org.junit.Test;


public class ExecutionTimerTest {

	@Test
	public void testExecutionTimer() throws Exception {
		ExecutionTimer et = new ExecutionTimer("test-timer");
		et.start();
		String key = et.registerStartOfTimedAction("dummy action1");
		Thread.sleep(200);
		Assert.assertTrue(et.registerEndOfTimedAction(key) >= 200);
		et.end();
	}
}
