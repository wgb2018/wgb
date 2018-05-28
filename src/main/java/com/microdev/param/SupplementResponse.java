package com.microdev.param;

import lombok.Data;

@Data
public class SupplementResponse {

	private String hotelName;
	private String taskTypeText;
	private String workFromDate;
	private String workToDate;
	private String dayStartTime;
	private String dayEndTime;
	private String fromDate;
	private String toDate;
	private String workLogId;
	private String hotelId;
	private String current;
	private String taskWorkerId;
	private String taskContent;
	private String leader;
	private String leaderMobile;
	private String address;
	public String getTaskWorkerId() {
		return taskWorkerId;
	}
	public void setTaskWorkerId(String taskWorkerId) {
		this.taskWorkerId = taskWorkerId;
	}
	public String getTaskContent() {
		return taskContent;
	}
	public void setTaskContent(String taskContent) {
		this.taskContent = taskContent;
	}
	public String getLeader() {
		return leader;
	}
	public void setLeader(String leader) {
		this.leader = leader;
	}
	public String getLeaderMobile() {
		return leaderMobile;
	}
	public void setLeaderMobile(String leaderMobile) {
		this.leaderMobile = leaderMobile;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCurrent() {
		return current;
	}
	public void setCurrent(String current) {
		this.current = current;
	}
	public String getWorkLogId() {
		return workLogId;
	}
	public void setWorkLogId(String workLogId) {
		this.workLogId = workLogId;
	}
	public String getHotelId() {
		return hotelId;
	}
	public void setHotelId(String hotelId) {
		this.hotelId = hotelId;
	}
	
	public String getHotelName() {
		return hotelName;
	}
	public void setHotelName(String hotelName) {
		this.hotelName = hotelName;
	}
	public String getTaskTypeText() {
		return taskTypeText;
	}
	public void setTaskTypeText(String taskTypeText) {
		this.taskTypeText = taskTypeText;
	}
	public String getWorkFromDate() {
		return workFromDate;
	}
	public void setWorkFromDate(String workFromDate) {
		this.workFromDate = workFromDate;
	}
	public String getWorkToDate() {
		return workToDate;
	}
	public void setWorkToDate(String workToDate) {
		this.workToDate = workToDate;
	}
	public String getDayStartTime() {
		return dayStartTime;
	}
	public void setDayStartTime(String dayStartTime) {
		this.dayStartTime = dayStartTime;
	}
	public String getDayEndTime() {
		return dayEndTime;
	}
	public void setDayEndTime(String dayEndTime) {
		this.dayEndTime = dayEndTime;
	}
	public String getFromDate() {
		return fromDate;
	}
	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}
	public String getToDate() {
		return toDate;
	}
	public void setToDate(String toDate) {
		this.toDate = toDate;
	}
	
}
