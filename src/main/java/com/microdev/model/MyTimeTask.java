package com.microdev.model;

import com.microdev.param.RefusedTaskRequest;
import com.microdev.service.TaskWorkerService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Data
@Component
@Scope("prototype")
public class MyTimeTask extends java.util.TimerTask{
    private RefusedTaskRequest refusedReq;
    @Autowired
    TaskWorkerService taskWorkerService;
    @Override
    public void run() {
        System.out.println ("任务领取时间超时");
        pl();
    }
    public void pl(){
        System.out.println ("service:"+taskWorkerService);
        taskWorkerService.refusedTask(refusedReq);
        this.cancel();
    }
}
