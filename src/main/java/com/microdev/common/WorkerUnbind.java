package com.microdev.common;

import com.microdev.mapper.DictMapper;
import com.microdev.mapper.MessageMapper;
import com.microdev.model.Message;
import com.microdev.param.DictDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.util.List;

@Component
public class WorkerUnbind {

    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private DictMapper dictMapper;

    //@Scheduled(cron = "* * 0/1 * * ?")
    public void scanUnbindMessage() {
        DictDTO dict = dictMapper.findByNameAndCode("MaxUnbindDay","22");
        if (dict != null) {
            Integer maxNum = Integer.parseInt(dict.getText());
            List<Message> list = messageMapper.selectWorkerUnbindMessage();
            if (list != null || list.size() > 0) {

                for (Message message : list) {
                    boolean flag = comparaTime(message.getCreateTime(), maxNum);
                    if (flag) {

                    }
                }
            }
        }

    }

    private boolean comparaTime(OffsetDateTime createTime, int maxNum) {
        OffsetDateTime nowTime = OffsetDateTime.now();
        long leaveTime = nowTime.getLong(ChronoField.MINUTE_OF_DAY) - createTime.getLong(ChronoField.MINUTE_OF_DAY);
        return (leaveTime / 60) >= (maxNum * 24);
    }
}
