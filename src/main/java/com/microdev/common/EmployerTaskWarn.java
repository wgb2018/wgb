package com.microdev.common;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import com.github.pagehelper.PageHelper;
import com.microdev.common.utils.JPushManage;
import com.microdev.mapper.CompanyMapper;
import com.microdev.mapper.TaskMapper;
import com.microdev.model.Company;
import com.microdev.model.JpushClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class EmployerTaskWarn {

    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    JpushClient jpushClient;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void taskWarn() {
        int count = taskMapper.selectStartTaskCount();
        if (count == 0) return;
        PageHelper.startPage(1, count, true);
        List<Map<String, Object>> list = taskMapper.selectStartTask();
        if (list != null && list.size() > 0) {
            Company c = null;
            try {
                for (Map<String, Object> m : list) {
                    c = companyMapper.selectById(m.get("hotelId").toString());
                    String content = m.get("taskTypeText") + "还差" + m.get("num") + "人未报名";
                    jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (c.getLeaderMobile (), content));

                }
            } catch (APIConnectionException e) {
                e.printStackTrace();
            } catch (APIRequestException e) {
                e.printStackTrace();
            }

        }
    }
}
