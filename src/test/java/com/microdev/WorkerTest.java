package com.microdev;

import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.context.ServiceContext;
import com.microdev.common.context.ServiceContextHolder;
import com.microdev.common.context.User;
import com.microdev.model.Company;
import com.microdev.param.*;
import com.microdev.param.api.response.GetBalanceResponse;
import com.microdev.param.api.response.GetCurrentTaskResponse;
import com.microdev.service.WorkerService;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkerTest {

    @Autowired
    private WorkerService workerService;

   /* @Test
    public void oneTest() {
        String userId = "1f1fe691-9fd4-4197-986c-a75904993bd8";
        UserTaskResponse response = workerService.selectWorkerInfo(userId);
        if (response != null) {
            System.out.println(response.toString());
        }
    }*/

    @Test
    public void twoTest() {
        String workerId = "e3905e35-8e4a-4c05-9870-6c2f80a144f2";
        GetCurrentTaskResponse response = workerService.getCurrentTask(workerId);
        if (response != null) {
            System.out.println(response.toString());
        }
    }

    @Test
    public void threeTest() {
        com.microdev.common.context.User user = new User();
        user.setId("bb7d27e1-2d17-4868-bb1c-738d87a5fa50");
        ServiceContext context = new ServiceContext();
        context.setUser(user);
        ServiceContextHolder.setServiceContext(context);
        String taskWorkerId = "4cadc3dee17646febfccb59bbfe26aea";
        PunchType punchType = PunchType.PUNCHIN;
        OffsetDateTime punchTime = OffsetDateTime.now();
        /*workerService.punch(taskWorkerId, punchType, punchTime);
        //下班打卡
        punchType = PunchType.PUNCHOUT;
        punchTime = punchTime.plusHours(5);
        workerService.punch(taskWorkerId, punchType, punchTime);*/
    }

    @Test
    public void fourTest() {
        String userId = "1f1fe691-9fd4-4197-986c-a75904993bd8";
        List<Company> list = workerService.getHrCompanyPartners(userId);
        if (list != null) {
            System.out.println(list.toString());
        }
    }

    @Test
    public void fiveTest() {
        String userId = "bb7d27e1-2d17-4868-bb1c-738d87a5fa50";
        GetBalanceResponse response = workerService.getBalance(userId);
        if (response != null) {
            System.out.println(response.toString());
        }
    }

    @Test
    public void tenTest() {
        WorkerSupplementRequest info = new WorkerSupplementRequest();
        info.setReason("测试取消任务");
        info.setTaskWorkerId("018f3e28-0526-44dd-8600-3dd8d5ade53c");
        workerService.applyCancelTask(info);
    }


    @Test
    public void twelveTest() {
        WorkerSupplementRequest info = new WorkerSupplementRequest();
        info.setReason("测试补签");
        info.setTaskWorkerId("018f3e28-0526-44dd-8600-3dd8d5ade53c");
        ResultDO r = ResultDO.buildSuccess(workerService.supplementWork(info));
        JSONObject json = JSONObject.fromObject(r);
        System.out.println(json.toString());
    }

    @Test
    public void thirteenTest() {
        long start = System.currentTimeMillis();
        String taskWorkerId = "140da358-6e4b-4499-9369-8d9536b82d35";
        String userId = "8c03e4ae-524b-48d9-bdbd-2ae019429689";
        UserTaskResponse response = workerService.selectUserTaskInfo(taskWorkerId, userId);
        if (response != null) {
            JSONObject json = JSONObject.fromObject(response);
            System.out.println(json.toString());
        }
        long end = System.currentTimeMillis();
        System.out.println("花费时间：" + (end - start));
    }
}
