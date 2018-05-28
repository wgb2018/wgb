package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Message;
import com.microdev.param.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface MessageMapper extends BaseMapper<Message> {
    void updateStatus(String id);

    List<Message> findAll(MessageQuery message);

    PunchMessageDTO selectPunchMessage(String id);

    HrApply selectHrCooperateInfo(String id);

    LeaveApply selectHotelWorkerApply(Map<String, Object> map);

    HrDeployApply selectHrDeployInfo(Map<String, Object> map);

    int saveBatch(List<Message> list);

    List<Map<String, Object>> selectHrWaitInfo(String hrCompanyId);

    int updateByMapId(Map<String, Object> map);
}
