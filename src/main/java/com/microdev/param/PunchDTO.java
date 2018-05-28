package com.microdev.param;

import java.time.OffsetDateTime;
import java.util.Map;

public class PunchDTO {

	private OffsetDateTime startTime;
	private OffsetDateTime endTime;
	private Map<String, String> log;

	public OffsetDateTime getStartTime() {
		return startTime;
	}
	public void setStartTime(OffsetDateTime startTime) {
		this.startTime = startTime;
	}
	public OffsetDateTime getEndTime() {
		return endTime;
	}
	public void setEndTime(OffsetDateTime endTime) {
		this.endTime = endTime;
	}

	public Map<String, String> getLog() {
		return log;
	}

	public void setLog(Map<String, String> log) {
		this.log = log;
	}
}
