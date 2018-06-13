package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class WorkerSupplementRequest {

	//请求设置的时间
	private String time;
	//申请理由
	private String reason;
	//taskWorkId
	private String taskWorkerId;
	//被请求者的id
	private String applicateId;

	private String endTime;
	
	private Integer minutes;
	
	private String name;
}
