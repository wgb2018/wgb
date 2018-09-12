package com.microdev.common;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import com.github.pagehelper.PageHelper;
import com.microdev.common.utils.JPushManage;
import com.microdev.mapper.CompanyMapper;
import com.microdev.mapper.TaskMapper;
import com.microdev.model.Company;
import com.microdev.model.JpushClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
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

    private static final Logger logger = LoggerFactory.getLogger(EmployerTaskWarn.class);

    @Scheduled(cron = "0 0/60 * * * ?")    public void taskWarn() {
        int count = taskMapper.selectStartTaskCount();
        if (count == 0) return;
        PageHelper.startPage(1, count, true);
        List<Map<String, Object>> list = taskMapper.selectStartTask();
        if (list != null && list.size() > 0) {
            try {
                DecimalFormat decimal = new DecimalFormat("0");
                for (Map<String, Object> m : list) {
                    String content = m.get("taskTypeText") + "还差" + decimal.format(m.get("num")) + "人未报名";
                    logger.info(m.get("name") + ":" + content);
                    String mobile = (String)m.get("leaderMobile");
                    if (StringUtils.isEmpty(mobile)) {
                        logger.error("hotelId=" + m.get("hotelId") + ";用人单位名称：" + m.get("name") + ";负责人的手机号没有设置");
                    } else {
                        jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (mobile, content));
                    }
                }
            } catch (APIConnectionException e) {
                e.printStackTrace();
            } catch (APIRequestException e) {
                e.printStackTrace();
            }

        }
    }
}
