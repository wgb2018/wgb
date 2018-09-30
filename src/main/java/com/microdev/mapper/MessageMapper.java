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

    List<PunchMessageDTO> selectPunchMessage(String id);

    HrApply selectHrCooperateInfo(String id);

    LeaveApply selectHotelWorkerApply(Map<String, Object> map);

    HrDeployApply selectHrDeployInfo(Map<String, Object> map);

    int saveBatch(List<Message> list);

    List<Map<String, Object>> selectHrWaitInfo(String hrCompanyId);

    int updateByMapId(Message mess);

    int selectUnReadCount(Map<String, Object> param);

    List<Map<String, Object>> selectMessageDetails(String id);

    List<Message> selectByParam(Map<String, Object> param);

    List<Message> selectWorkerUnbindMessage(@Param("start") int start,@Param("end") int end);

    List<AwaitHandleInfo> selectWorkerAwaitHandleInfo(@Param("workerId") String workerId);

    List<AwaitHandleInfo> selectHrAwaitHandleInfo(@Param("hrId") String hrId,@Param("type") Integer type);

    List<AwaitHandleInfo> selectHotelAwaitHandleInfo(@Param("hotelId") String hotelId,@Param("type") Integer type);

    int selectWorkerUnbindCount();

    MessageDetailsResponse selectWorkerApply(@Param("messageId") String messageId);

    MessageDetailsResponse selectCompanyApply(@Param("messageId") String messageId);

    MessageDetailsResponse selectHotelApply(@Param("messageId") String messageId);

    MessageDetailsResponse selectHotelApplyWorker(@Param("messageId") String messageId);

    MessageDetailsResponse selectPayConfirm(@Param("messageId") String messageId,@Param("type") String type);

    MessageDetailsResponse selectApplyAllocate(@Param("messageId") String messageId);

    String selectCompanyNameByMessageId(String messageId);

    Map<String, Object> selectNeedWorkers(String messageId);

    AwaitTaskResponse selectWorkerAwaitHandleTask(@Param("messageId") String messageId, @Param("type") Integer type);

    AwaitTaskResponse selectHrAwaitHandleTask(@Param("messageId") String messageId);

    AwaitTaskResponse selectHrAwaitHandleHotelTask(@Param("messageId") String messageId);

    MessageDetailsResponse selectSupplementApply(@Param("messageId") String messageId);

    MessageDetailsResponse selectOvertimeApply(@Param("messageId") String messageId);

    MessageDetailsResponse selectLeaveApply(@Param("messageId") String messageId);

    AwaitTaskResponse selectCancelApply(@Param("messageId") String messageId);

    AwaitTaskResponse selectCancelApplyHotel(@Param("messageId") String messageId);

    MessageDetailsResponse hotelHrApplyCooperate(@Param("messageId") String messageId);

    MessageDetailsResponse hotelApplyCooperate(@Param("messageId") String messageId);

    AwaitTaskResponse selectHrHotelDetails(@Param("messageId") String messageId);

    Message selectByHrId(String id);

    List<ApplyResponseDTO> selectHotelDeploy(@Param("hotelId") String hotelId);

    List<ApplyResponseDTO> selectHrDeploy(@Param("hrId") String hrId);

    List<ApplySupplementRequest> selectPcLeaveApply(@Param("hotelId") String hotelId,@Param("messageType") String messageType);

    List<BindPcResponse> selectPcUnBindApply(@Param("hrId") String hrId);

    List<ApplyBindResponse> selectPcWorkerBindApply(@Param("workerId") String workerId);

    List<ApplyBindResponse> selectPcHotelBind(@Param("hotelId") String hotelId);

    List<ApplyBindResponse> selectPcHrBind(@Param("hrId") String hrId);

    int selectIsRepeat(@Param("workerTaskId") String workerTaskId,@Param("workerId") String workerId);

    Double selectUnConfirmePay(@Param("payType") int payType,@Param("taskId1") String taskId,@Param("taskId2") String taskId2);

    List<ApplyResponseDTO> selectPcHrRefuse(@Param("hotelId") String hotelId);

    List<ApplyResponseDTO> selectPcHotelReplace(@Param("hrId") String hrId);

    List<ApplyResponseDTO> selectPcHotelPay(@Param("hrId") String hrId);

    List<ApplyResponseDTO> selectPcworkerCancel(@Param("hrId") String hrId);

    List<ApplyResponseDTO> selectPcHrNewTask(@Param("hrId") String hrId);

    List<MessageResponse> selectPcHrApplyInfo(@Param("hrId") String hrId);

    List<MessageResponse> selectPcHotelApplyInfo(@Param("hotelId") String hotelId);

    int selectReplaceCount(@Param("taskWorkerId") String taskWorkerId);

    List<AwaitHandleInfoPc> selectHotelHandleInfoPc(@Param("hotelId") String hotelId,@Param("type") Integer type);

    List<AwaitHandleInfoPc> selectHrHandleInfoPc(@Param("hrId") String hrId,@Param("type") Integer type);

    int selectUnReadWorkerCount(@Param("workerId") String workerId);

    int selectUnreadHrCount(@Param("hrId") String hrId);

    int selectUnreadHotelCount(@Param("hotelId") String hotelId);

    MessageDetailsResponse selectHrHotelUnbind(@Param("messageId") String messageId,@Param("type") String type);

    List<AwaitHandleInfoPc> selectWorkerAwaitHandleInfoPc(@Param("workerId") String workerId);

    MessageDetailsResponse selectNoticeApply(String messageId);

    void updateAllBind(@Param("workerId") String workerId,@Param("hrId") String hrId);

    AwaitTaskResponse selectHotelTaskDetails(@Param("messageId") String messageId);
}
