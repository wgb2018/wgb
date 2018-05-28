package com.microdev.param;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HotelDeployInfoRequest {

	private String taskId;
	private String hotelId;
	private String messageCode;
	private String taskContent;
	/**
     * 开始时间
     */
    private OffsetDateTime fromDate;
    /**
     * 截止时间
     */
    private OffsetDateTime toDate;
    private double hourlyPay;
    
    private String name;
    
    Set<Map<String, Object>> hrCompany = new HashSet<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getHotelId() {
		return hotelId;
	}

	public void setHotelId(String hotelId) {
		this.hotelId = hotelId;
	}

	public String getMessageCode() {
		return messageCode;
	}

	public void setMessageCode(String messageCode) {
		this.messageCode = messageCode;
	}

	public String getTaskContent() {
		return taskContent;
	}

	public void setTaskContent(String taskContent) {
		this.taskContent = taskContent;
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

	public double getHourlyPay() {
		return hourlyPay;
	}

	public void setHourlyPay(double hourlyPay) {
		this.hourlyPay = hourlyPay;
	}

	public Set<Map<String, Object>> getHrCompany() {
		return hrCompany;
	}

	public void setHrCompany(Set<Map<String, Object>> hrCompany) {
		this.hrCompany = hrCompany;
	}
    
}
