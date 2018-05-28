package com.microdev.param;

import java.time.OffsetDateTime;

public class WorkerSupplementRequest {

	//请求设置的时间
	private OffsetDateTime time;
	//申请理由
	private String reason;
	//taskWorkId
	private String taskWorkerId;
	//被请求者的id
	private String applicateId;

	private OffsetDateTime endTime;
	
	private Integer minutes;
	
	private String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getMinutes() {
		return minutes;
	}
	public void setMinutes(Integer minutes) {
		this.minutes = minutes;
	}
	public OffsetDateTime getEndTime() {
		return endTime;
	}
	public void setEndTime(OffsetDateTime endTime) {
		this.endTime = endTime;
	}
	
	public String getApplicateId() {
		return applicateId;
	}
	public void setApplicateId(String applicateId) {
		this.applicateId = applicateId;
	}
	public OffsetDateTime getTime() {
		return time;
	}
	public void setTime(OffsetDateTime time) {
		this.time = time;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getTaskWorkerId() {
		return taskWorkerId;
	}
	public void setTaskWorkerId(String taskWorkerId) {
		this.taskWorkerId = taskWorkerId;
	}
	
}
