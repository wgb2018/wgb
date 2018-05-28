package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("holiday")
public class Holiday extends BaseEntity {

    private OffsetDateTime fromDate;

    private OffsetDateTime toDate;

    private String taskWorkerId;

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

    public String getTaskWorkerId() {
        return taskWorkerId;
    }

    public void setTaskWorkerId(String taskWorkerId) {
        this.taskWorkerId = taskWorkerId == null ? null : taskWorkerId.trim();
    }
}