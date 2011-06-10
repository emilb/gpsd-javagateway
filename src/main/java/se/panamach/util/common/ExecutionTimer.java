package se.panamach.util.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExecutionTimer {

	private long start;
	private long end;
	private String name;
	private SimpleDateFormat sdf = new SimpleDateFormat();
	
	private Map<String, TimedAction> timedActions = new HashMap<String, TimedAction>();
	
	public ExecutionTimer(String name) {
		this.name = name;
	}
	
	public void start() {
		start = System.currentTimeMillis();
	}
	
	public void end() {
		end = System.currentTimeMillis();
	}
	
	public String registerStartOfTimedAction(String action) {
		TimedAction ta = new TimedAction(action);
		String key = UUID.randomUUID().toString();
		timedActions.put(key, ta);
		return key;
	}
	
	public long registerEndOfTimedAction(String key) {
		if (!timedActions.containsKey(key))
			return 0;
		
		timedActions.get(key).finished();
		return timedActions.get(key).getExectionTimeInMs();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("** Execution report for ").append(name).append(" **\n");
		sb.append("Started at: ").append(sdf.format(new Date(start))).append("\n");
		sb.append("Ended at: ").append(sdf.format(new Date(end))).append("\n");
		sb.append("\t** Timed Actions **\n");
		for (String key : timedActions.keySet()) {
			TimedAction ta = timedActions.get(key);
			sb.append("\t").append(ta.action).append(" took: ").append(ta.getExectionTimeInMs()).append("ms\n");
		}
		sb.append("Total execution time: ").append(end - start).append("ms");
		return sb.toString();
	}
	
	
	private class TimedAction {
		private String action;
		private long start;
		private long end;
		
		TimedAction(String action) {
			this.action = action;
			start = System.currentTimeMillis();
		}
		
		public void finished() {
			end = System.currentTimeMillis();
		}
		
		public long getExectionTimeInMs() {
			return end - start;
		}
	}
}
