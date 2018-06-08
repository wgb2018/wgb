package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Message;
import com.microdev.param.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
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

    int updateByMapId(Message mess);

    List<MessageResponse> selectAllMessage(Map<String, Object> map);

    List<Message> selectUnReadMessage(Map<String, Object> param);

    int selectUnReadCount(Map<String, Object> param);

    List<Map<String, Object>> selectMessageDetails(String id);

    List<Message> selectByParam(Map<String, Object> param);

    @Update("update message set check_sign = 1 where id = #{id} ")
    int updateMessageCheckSign(String id);

    List<Message> selectWorkerUnbindMessage(int start, int end);

    List<AwaitHandleInfo> selectWorkerAwaitHandleInfo(@Param("workerId") String workerId);

    List<AwaitHandleInfo> selectHrAwaitHandleInfo(@Param("hrId") String hrId);

    List<AwaitHandleInfo> selectHotelAwaitHandleInfo(@Param("hotelId") String hotelId);

    int selectWorkerUnbindCount();

    MessageDetailsResponse selectWorkerApply(@Param("messageId") String messageId);

    MessageDetailsResponse selectCompanyApply(@Param("messageId") String messageId);

    MessageDetailsResponse selectHotelApply(@Param("messageId") String messageId);

    MessageDetailsResponse selectPayConfirm(@Param("messageId") String messageId,@Param("type") String type);

    MessageDetailsResponse selectApplyAllocate(@Param("messageId") String messageId);

    String selectCompanyNameByMessageId(String messageId);

    Map<String, Object> selectNeedWorkers(String messageId);

    AwaitTaskResponse selectWorkerAwaitHandleTask(@Param("messageId") String messageId);

    AwaitTaskResponse selectHrAwaitHandleTask(@Param("messageId") String messageId,@Param("taskHrId") String taskHrId);

    MessageDetailsResponse selectSupplementApply(@Param("messageId") String messageId);

    MessageDetailsResponse selectOvertimeApply(@Param("messageId") String messageId);

    MessageDetailsResponse selectLeaveApply(@Param("messageId") String messageId);

    AwaitTaskResponse selectCancelApply(@Param("messageId") String messageId);

    MessageDetailsResponse hotelHrApplyCooperate(@Param("messageId") String messageId);

    AwaitTaskResponse selectHrHotelDetails(@Param("messageId") String messageId);
}
