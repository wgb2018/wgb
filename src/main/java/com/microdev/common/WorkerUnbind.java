package com.microdev.common;

import com.microdev.common.exception.BusinessException;
import com.microdev.mapper.DictMapper;
import com.microdev.mapper.MessageMapper;
import com.microdev.mapper.UserCompanyMapper;
import com.microdev.mapper.UserMapper;
import com.microdev.model.Message;
import com.microdev.model.User;
import com.microdev.model.UserCompany;
import com.microdev.param.DictDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.util.List;

@Component
public class WorkerUnbind {

    private static final Logger logger = LoggerFactory.getLogger(WorkerUnbind.class);
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private DictMapper dictMapper;
    @Autowired
    private UserCompanyMapper userCompanyMapper;

    @Scheduled(cron = "0 0 * * * ?")
    public void scanUnbindMessage() {
        DictDTO dict = dictMapper.findByNameAndCode("MaxUnbindDay","22");
        if (dict != null) {
            Integer maxNum = Integer.parseInt(dict.getText());
            int num = messageMapper.selectWorkerUnbindCount();
            List<Message> list = null;
            int end = 0;
            for (int i = 1; i <= num; ) {
                if (i + 500 > num) {
                    end = num;
                } else {
                    end = i + 500;
                }
                list = messageMapper.selectWorkerUnbindMessage(i, end);
                updateInfo(list, maxNum);
                i = i + 500;
            }
        } else {
            throw new BusinessException("查询不到最大解绑天数");
        }

    }

    private void updateInfo(List<Message> list, Integer maxNum) {
        if (list != null || list.size() > 0) {

            for (Message message : list) {
                boolean flag = comparaTime(message.getCreateTime(), maxNum);
                if (flag) {
                    UserCompany userCompany = userCompanyMapper.selectByWorkerIdHrId(message.getHrCompanyId(), message.getWorkerId());
                    if (userCompany == null) {
                        logger.error("小时工人力关系查询不到,workerId=" + message.getWorkerId() + ";hrId=" + message.getHrCompanyId());
                    } else {
                        //解绑小时工和人力
                        userCompany.setStatus(4);
                        userCompany.setRelieveTime(OffsetDateTime.now());
                        userCompanyMapper.updateById(userCompany);
                    }
                }
            }
        }
    }

    private boolean comparaTime(OffsetDateTime createTime, int maxNum) {
        OffsetDateTime nowTime = OffsetDateTime.now();
        long leaveTime = nowTime.getLong(ChronoField.INSTANT_SECONDS) - createTime.getLong(ChronoField.INSTANT_SECONDS);
        return (leaveTime / 3600) >= (maxNum * 24);
    }
}
