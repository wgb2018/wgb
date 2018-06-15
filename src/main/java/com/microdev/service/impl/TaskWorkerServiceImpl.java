package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.Maths;
import com.microdev.common.utils.StringKit;
import com.microdev.converter.TaskWorkerConverter;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.ApplyParamDTO;
import com.microdev.param.RefusedTaskRequest;
import com.microdev.param.TaskWorkerQuery;
import com.microdev.service.InformService;
import com.microdev.service.TaskWorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class TaskWorkerServiceImpl extends ServiceImpl<TaskWorkerMapper,TaskWorker> implements TaskWorkerService{

    @Autowired
    TaskWorkerMapper taskWorkerMapper;
    @Autowired
    UserCompanyMapper userCompanyMapper;
    @Autowired
    private TaskWorkerConverter taskWorkerConverter;
    @Autowired
    TaskHrCompanyMapper taskHrCompanyMapper;
    @Autowired
    TaskMapper  taskMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private InformMapper informMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    CompanyMapper companyMapper;
    @Autowired
    MessageTemplateMapper messageTemplateMapper;

    /**
     * 设置违约的任务
     */
    @Override
    public ResultDO noPromise(String taskWorkerId) {
        TaskWorker taskWorker =taskWorkerMapper.findFirstById(taskWorkerId);
        if(!taskWorker.isNoPromise()){
            taskWorker.setNoPromise(true);
            String userId=taskWorker.getUserId();
            String hrId=taskWorker.getTaskHrId();
            userCompanyMapper.addNoPromiseTasks(userId,hrId);
            taskWorkerMapper.updateById(taskWorker);
        }
        return ResultDO.buildSuccess("操作成功");
    }
    /**
     * 任务详情
     */
    @Override
    public ResultDO findWorkTaskById(String workerTaskId) {
        TaskWorker taskWorker=  taskWorkerMapper.findFirstById(workerTaskId);
        return ResultDO.buildSuccess(taskWorkerConverter.toViewModel(taskWorker));
    }
    /**
     * 小时工领取任务
     */
    @Override
    public ResultDO receivedTask(String messageId) {
        if (StringUtils.isEmpty(messageId)) {
            throw new ParamsException("参数messageId不能为空");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new BusinessException("消息已处理");
        }
        messageMapper.updateStatus (message.getPid ());
        TaskWorker taskWorker = taskWorkerMapper.findFirstById(message.getWorkerTaskId());
        if (taskWorker.getFromDate().isBefore(OffsetDateTime.now())) {
            System.out.println ("now:"+OffsetDateTime.now()+"AAA:"+taskWorker.getFromDate());
            taskWorker.setStatus (2);
            taskWorker.setRefusedReason ("任务已过期，无法接受");
            taskWorkerMapper.updateById (taskWorker);
            return ResultDO.buildSuccess("任务已过期，无法接受");
        }
        //TODO 人数判断
        TaskHrCompany taskHr = taskHrCompanyMapper.queryByTaskId(taskWorker.getTaskHrId());
        Integer confirmedWorkers = taskHr.getConfirmedWorkers();
        if (confirmedWorkers == null) {
            confirmedWorkers = 0;
        }
        taskWorker.setStatus(1);
        taskWorker.setConfirmedDate(OffsetDateTime.now());
        //TODO 酒店人数加1
        Task hotelTask=taskMapper.getFirstById(taskHr.getTaskId());
        Integer hotelConfirmedWorkers=hotelTask.getConfirmedWorkers();
        if(hotelConfirmedWorkers==null){
            hotelConfirmedWorkers=0;
        }
        if(hotelTask.getConfirmedWorkers ()+1<=hotelTask.getNeedWorkers ()){
            hotelTask.setConfirmedWorkers(hotelConfirmedWorkers+1);
        }
        if(hotelTask.getConfirmedWorkers() == hotelTask.getNeedWorkers()){
            taskMapper.updateStatus(hotelTask.getPid(),4);
        }else{
            taskMapper.updateStatus(hotelTask.getPid(),3);
        }
        //TODO 人力公司人数加1
        if (confirmedWorkers + 1 <= taskHr.getNeedWorkers()) {
             taskHr.setConfirmedWorkers(confirmedWorkers+1);
        }
        if(taskHr.getConfirmedWorkers() == taskHr.getNeedWorkers()){
            taskHrCompanyMapper.updateStatus(taskHr.getPid(),5);
        }
        taskWorker.setRefusedReason("");
        taskWorkerMapper.updateById(taskWorker);
        taskMapper.updateById(hotelTask);
        taskHrCompanyMapper.updateById(taskHr);

        //添加一个通知消息
        Inform notice = new Inform();
        notice.setReceiveId(taskHr.getHrCompanyId());
        notice.setAcceptType(2);
        notice.setSendType(1);
        notice.setTitle("任务已接受");
        notice.setContent("小时工" + taskWorker.getUserName() + "接受了你派发的任务");
        informMapper.insertInform(notice);
        return ResultDO.buildSuccess("任务领取成功");
    }
    /**
     * 小时工拒绝任务
     */
    @Override
    public ResultDO refusedTask(RefusedTaskRequest refusedTaskReq) {
        if (!StringUtils.hasLength(refusedTaskReq.getRefusedReason())) {
            throw new ParamsException("拒绝理由不能为空");
        }
        if (StringUtils.isEmpty(refusedTaskReq.getMessageId())) {
            throw new ParamsException("消息id不能为空");
        }
        Message message = messageMapper.selectById(refusedTaskReq.getMessageId());
        if (message == null || message.getStatus() == 1) {
            throw new BusinessException("消息已处理");
        }
        message.setStatus(1);
        messageMapper.updateAllColumnById(message);

        TaskWorker taskWorker = taskWorkerMapper.findFirstById(message.getWorkerTaskId());
        if(taskWorker.getStatus()>0){
            throw new BusinessException("任务状态不是新派发,无法拒绝任务");
        }
        //TODO 人力公司人数判断
        TaskHrCompany taskHr= taskHrCompanyMapper.queryByTaskId(taskWorker.getTaskHrId());
        Integer refuseWorkers=taskHr.getRefusedWorkers();
        if(refuseWorkers==null){
            refuseWorkers=0;
        }
        taskHr.setRefusedWorkers(refuseWorkers+1);
        taskWorker.setStatus(2);
        taskWorker.setConfirmedDate(OffsetDateTime.now());
        taskWorker.setRefusedReason(refusedTaskReq.getRefusedReason());
        //TODO 酒店人数
        Task hotelTask=taskMapper.getFirstById(taskHr.getTaskId ());
        Integer hotelRefuseWorkers=hotelTask.getRefusedWorkers();
        if(hotelRefuseWorkers==null){
            hotelRefuseWorkers=0;
        }
        hotelTask.setRefusedWorkers(hotelRefuseWorkers+1);
        taskWorkerMapper.updateById(taskWorker);
        taskMapper.updateById(hotelTask);
        taskHrCompanyMapper.updateById(taskHr);

        //发送消息
        Message m = new Message();
        m.setContent(refusedTaskReq.getRefusedReason());
        MessageTemplate mess = messageTemplateMapper.findFirstByCode ("refuseTaskMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageType(10);
        m.setMessageTitle(mess.getTitle());
        m.setWorkerId (taskWorker.getWorkerId ());
        m.setWorkerTaskId (taskWorker.getPid ());
        m.setHrCompanyId (taskHr.getHrCompanyId());
        m.setTaskId(hotelTask.getPid());
        m.setHotelId(hotelTask.getHotelId());
        Map<String, String> param = new HashMap<>();
        param.put("userName", userMapper.selectByWorkerId (taskWorker.getWorkerId ()).getNickname ());
        param.put("content", refusedTaskReq.getRefusedReason());
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setHrTaskId(message.getHrTaskId());
        m.setApplyType(2);
        m.setApplicantType(1);
        m.setStatus(0);
        m.setIsTask(0);
        messageMapper.insert(m);

        return ResultDO.buildSuccess("拒绝任务成功");
    }
    /**
     * 分页查询任务
     */
    @Override
    public ResultDO pagesTaskWorkers(Paginator paginator, TaskWorkerQuery taskQueryDTO) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        String date = taskQueryDTO.getOfDate();
        if (date != null) {
            Integer year = Integer.parseInt(date.split("-")[0]);
            Integer month = Integer.parseInt(date.split("-")[1]);
            if (month == 12) {
                taskQueryDTO.setFromDate(OffsetDateTime.of
                        (year, 12, 1, 0, 0, 0, 0,
                                ZoneOffset.UTC));
                taskQueryDTO.setToDate(OffsetDateTime.of
                        (year, 1, 1, 0, 0, 0, 0,
                                ZoneOffset.UTC));
            } else {
                taskQueryDTO.setFromDate(OffsetDateTime.of
                        (year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC));
                taskQueryDTO.setToDate(OffsetDateTime.of
                        (year, month + 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
            }
        }
        //查询数据集合
        List<TaskWorker> list = null;
        if(taskQueryDTO.getHotelName () == null || taskQueryDTO.getHotelName ().equals ("")){
            list = taskWorkerMapper.findAll(taskQueryDTO);
        }else{
            list = taskWorkerMapper.findAllh(taskQueryDTO);
        }
        HashMap<String,Object> extra = new HashMap<>();
        Double shouldPayMoney = 0.0;
        Double havePayMoney=0.0;
        for (TaskWorker t:list) {
            if(t.getToDate ().isAfter (OffsetDateTime.now ()) && t.getFromDate ().isBefore (OffsetDateTime.now ()) && t.getStatus () == 1){
                t.setStatus (4);
            }
            if(t.getToDate ().isBefore (OffsetDateTime.now ()) && t.getStatus () == 1 ){
                t.setStatus (4);
            }
            t.setUser (userMapper.queryByUserId (t.getUserId ()));
            t.setHotel (companyMapper.selectById (t.getHotelId ()));
            t.setHrCompany (companyMapper.selectById (t.getHrCompanyId ()));
            shouldPayMoney += t.getShouldPayMoney();
            havePayMoney += t.getHavePayMoney();
        }
        extra.put("shouldPayMoney",shouldPayMoney);
        extra.put("havePayMoney",havePayMoney);
        extra.put("paidPayMoney", Maths.sub(shouldPayMoney, havePayMoney));
        PageInfo<TaskWorker> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess(null,result,extra,null);
    }

    /**
     * 查询小时工当前任务数量
     * @param applyParamDTO
     * @return
     */
    @Override
    public int selectWorkerCurTaskCount(ApplyParamDTO applyParamDTO) {

        if (StringUtils.isEmpty(applyParamDTO.getId())) {
            return 0;
        }
        TaskWorkerQuery query = new TaskWorkerQuery();
        query.setWorkerId(applyParamDTO.getId());
        query.setTaskStatus(1);
        return taskWorkerMapper.selectCurTasCount(query);
    }

}
