package com.microdev.param;

import java.time.OffsetDateTime;

public class HotelInfo {

	private String messageTitle;
	private String messageCode;
	private double hourlyPay;
	private OffsetDateTime fromDate;
	private OffsetDateTime toDate;
	private OffsetDateTime dayStartTime;
	private OffsetDateTime dayEndTime;
	private String hotelName;
	private String address;
	private String leader;
	private String leaderMobile;
	private Integer needWorkers;
	private Integer confirmedWorkers;
	private String taskContent;
	private String taskTypeText;
	private String messageId;
	private String taskId;
	
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getMessageTitle() {
		return messageTitle;
	}
	public void setMessageTitle(String messageTitle) {
		this.messageTitle = messageTitle;
	}
	public String getMessageCode() {
		return messageCode;
	}
	public void setMessageCode(String messageCode) {
		this.messageCode = messageCode;
	}
	public double getHourlyPay() {
		return hourlyPay;
	}
	public void setHourlyPay(double hourlyPay) {
		this.hourlyPay = hourlyPay;
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
	public OffsetDateTime getDayStartTime() {
		return dayStartTime;
	}
	public void setDayStartTime(OffsetDateTime dayStartTime) {
		this.dayStartTime = dayStartTime;
	}
	public OffsetDateTime getDayEndTime() {
		return dayEndTime;
	}
	public void setDayEndTime(OffsetDateTime dayEndTime) {
		this.dayEndTime = dayEndTime;
	}
	public String getHotelName() {
		return hotelName;
	}
	public void setHotelName(String hotelName) {
		this.hotelName = hotelName;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
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
	public Integer getNeedWorkers() {
		return needWorkers;
	}
	public void setNeedWorkers(Integer needWorkers) {
		this.needWorkers = needWorkers;
	}
	public Integer getConfirmedWorkers() {
		return confirmedWorkers;
	}
	public void setConfirmedWorkers(Integer confirmedWorkers) {
		this.confirmedWorkers = confirmedWorkers;
	}
	public String getTaskContent() {
		return taskContent;
	}
	public void setTaskContent(String taskContent) {
		this.taskContent = taskContent;
	}
	public String getTaskTypeText() {
		return taskTypeText;
	}
	public void setTaskTypeText(String taskTypeText) {
		this.taskTypeText = taskTypeText;
	}
	
}
