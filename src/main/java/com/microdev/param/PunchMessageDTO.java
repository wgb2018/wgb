package com.microdev.param;

import java.time.OffsetDateTime;

public class PunchMessageDTO {

	private OffsetDateTime supplement;
	private OffsetDateTime fromDate;
	private OffsetDateTime toDate;
	private String id;
	private OffsetDateTime punchDate;
	private String taskId;
	private String taskWorkerId;
	private String taskHrId;
	public OffsetDateTime getSupplement() {
		return supplement;
	}
	public void setSupplement(OffsetDateTime supplement) {
		this.supplement = supplement;
	}
	public OffsetDateTime getFromDate() {
		return fromDate;
	}
	public void setFromDate(OffsetDateTime fromDate) {
		this.fromDate = fromDate;
	}
	public OffsetDateTime getToDate() {
		return toDate;
	}
	public void setToDate(OffsetDateTime toDate) {
		this.toDate = toDate;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public OffsetDateTime getPunchDate() {
		return punchDate;
	}
	public void setPunchDate(OffsetDateTime punchDate) {
		this.punchDate = punchDate;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getTaskWorkerId() {
		return taskWorkerId;
	}
	public void setTaskWorkerId(String taskWorkerId) {
		this.taskWorkerId = taskWorkerId;
	}
	public String getTaskHrId() {
		return taskHrId;
	}
	public void setTaskHrId(String taskHrId) {
		this.taskHrId = taskHrId;
	}
	@Override
	public String toString() {
		return "PunchMessageDTO [supplement=" + supplement + ", fromDate=" + fromDate + ", toDate=" + toDate + ", id="
				+ id + ", punchDate=" + punchDate + ", taskId=" + taskId + ", taskWorkerId=" + taskWorkerId
				+ ", taskHrId=" + taskHrId + "]";
	}
	
}
