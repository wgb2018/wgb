package com.microdev.converter;

import com.microdev.model.WorkLog;
import com.microdev.param.api.response.WorkLogResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WorkLogConverter {
    public WorkLogResponse toResponse(WorkLog workLog){
        WorkLogResponse result = new WorkLogResponse();

        result.setPunchIn(workLog.getFromDate());
        result.setPunchOut(workLog.getToDate());
        result.setRepastTimes(workLog.getRepastTimes());

        return result;
    }

    public List<WorkLogResponse> toResponse(List<WorkLog> workLogs) {
        List<WorkLogResponse> result = new ArrayList<>();

        for (WorkLog item : workLogs) {
            result.add(toResponse(item));
        }

        return result;
    }
}
