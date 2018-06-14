package com.microdev.param;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SupplementResponse {

	private String hotelName;
	private String taskTypeText;
	private String workFromDate;
	private String workToDate;
	private String dayStartTime;
	private String dayEndTime;
	private List<Map<String, String>> signDate = new ArrayList<>();
	private String hotelId;
	private String current;
	private String taskWorkerId;
	private String taskContent;
	private String leader;
	private String leaderMobile;
	private String address;
	private String workerId;
	private String area;
	private String price;
	private String startTime;//打卡开始时间
	private String endTime;//打卡结束时间
}
