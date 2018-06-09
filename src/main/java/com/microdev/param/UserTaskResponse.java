package com.microdev.param;

import com.microdev.model.WorkLog;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
public class UserTaskResponse {

	private List<WorkerDetail> list = new ArrayList<>();
}
