package com.microdev.param;

import lombok.Data;

import java.time.OffsetDateTime;
@Data
public class PunchMessageDTO {

	private OffsetDateTime supplement;
	private OffsetDateTime fromDate;
	private OffsetDateTime toDate;
	private String id;
	private OffsetDateTime punchDate;
	private String taskId;
	private String taskWorkerId;
	private int minutes;
}
