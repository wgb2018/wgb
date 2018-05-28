package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
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
}
