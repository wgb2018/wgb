package com.microdev.param;

import com.microdev.model.WorkLog;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
public class UserTaskResponse {

	private String username;
	private String sex;
	private Integer age;
	private String mobile;
	private OffsetDateTime birthday;
	private String healthCard;
	private List<String> areaList = new ArrayList<>();;
	private List<String> serviceList = new ArrayList<>();;
	private List<WorkerDetail> list = new ArrayList<>();
}
