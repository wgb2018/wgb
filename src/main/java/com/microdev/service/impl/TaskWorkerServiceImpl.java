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
import com.microdev.param.*;
import com.microdev.service.TaskWorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;

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
        taskWorker.setHrCompany (companyMapper.findCompanyById (taskWorker.getHrCompanyId ()));
        taskWorker.setHotel (companyMapper.findCompanyById (taskWorker.getHotelId ()));
        taskWorker.setUnConfirmedPay(messageMapper.selectUnConfirmePay (1,taskWorker.getTaskHrId (),taskWorker.getPid ()));
        taskWorker.setHrCompanyName (taskWorker.getHrCompany ().getName ());
        taskWorker.setHotelName (taskWorker.getHotel ().getName ());
        if(taskWorker.getToDate ().isAfter (OffsetDateTime.now ()) && taskWorker.getFromDate ().isBefore (OffsetDateTime.now ()) && taskWorker.getStatus () == 1){
            taskWorker.setStatus (4);
        }
        if(taskWorker.getToDate ().isBefore (OffsetDateTime.now ()) && taskWorker.getStatus () == 1 ){
            taskWorker.setStatus (5);
        }
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
        if (taskWorker.getStatus() == 2 || taskWorker.getStatus() == 3) {
            return ResultDO.buildSuccess("任务已终止");
        }
        Task hotelTask=taskMapper.getFirstById(taskWorker.getHotelTaskId ());
        if(!taskWorker.getFromDate ().isEqual (taskWorker.getToDate ())){
            if (taskWorker.getFromDate().isBefore(OffsetDateTime.now())) {
                taskWorker.setStatus (2);
                taskWorker.setRefusedReason ("任务已过期，无法接受");
                taskWorkerMapper.updateById (taskWorker);
                return ResultDO.buildSuccess("任务已过期，无法接受");
            }
        }
        //TODO 人数判断
        if(taskWorker.getFromDate ().isEqual (taskWorker.getToDate ()))  {
            OffsetDateTime of = OffsetDateTime.now();
            if(of.toOffsetTime ().isBefore (taskWorker.getDayStartTime ())){
                taskWorker.setFromDate (OffsetDateTime.ofInstant (Instant.ofEpochSecond (of.toEpochSecond () - of.toOffsetTime ().getLong (ChronoField.SECOND_OF_DAY)+hotelTask.getDayStartTime ().getLong (ChronoField.SECOND_OF_DAY)),ZoneId.systemDefault ()));
            }else{
                taskWorker.setFromDate (OffsetDateTime.ofInstant (Instant.ofEpochSecond (of.toEpochSecond () - of.toOffsetTime ().getLong (ChronoField.SECOND_OF_DAY)+hotelTask.getDayStartTime ().getLong (ChronoField.SECOND_OF_DAY)),ZoneId.systemDefault ()).plusDays (1));
            }
            if(taskWorker.getToDate ().isBefore (taskWorker.getFromDate ())){
                taskWorker.setStatus (2);
                taskWorker.setRefusedReason ("任务即将结束，无法接受");
                taskWorkerMapper.updateById (taskWorker);
                return ResultDO.buildSuccess("任务即将结束，无法接受");
            }
        }
        if(taskWorker.getStatus() == 1){
            RefusedTaskRequest refusedTaskReq = new RefusedTaskRequest();
            refusedTaskReq.setMessageId (messageId);
            refusedTaskReq.setRefusedReason ("已存在相同任务，无法接受");
            refusedTask(refusedTaskReq);
            return ResultDO.buildSuccess("已存在相同任务，无法接受");
        }
                List<TaskWorker> li = taskWorkerMapper.findByUserId (userMapper.selectByWorkerId (taskWorker.getWorkerId ()).getPid ());
                for (TaskWorker ts:li) {
                    if(!(taskWorker.getDayStartTime ().isAfter (ts.getDayEndTime ()) || ts.getDayStartTime ().isAfter (taskWorker.getDayEndTime ()))){
                        if(!(taskWorker.getFromDate ().isAfter (ts.getToDate ()) || ts.getFromDate ().isAfter (taskWorker.getToDate ()))){
                            RefusedTaskRequest refusedTaskReq = new RefusedTaskRequest();
                            refusedTaskReq.setMessageId (messageId);
                            refusedTaskReq.setRefusedReason ("任务存在时间冲突，无法接受");
                            refusedTask(refusedTaskReq);
                            return ResultDO.buildSuccess("任务存在时间冲突，无法接受");
                        }
                    }
                }
        TaskHrCompany taskHr = taskHrCompanyMapper.queryByTaskId(taskWorker.getTaskHrId());
                if(taskHr!=null){
                    Integer confirmedWorkers = taskHr.getConfirmedWorkers();
                    if (confirmedWorkers == null) {
                        confirmedWorkers = 0;
                    }
                    //TODO 人力公司人数加1
                    if (confirmedWorkers + 1 <= taskHr.getNeedWorkers()) {
                        taskHr.setConfirmedWorkers(confirmedWorkers+1);
                    }
                    if(taskHr.getConfirmedWorkers() == taskHr.getNeedWorkers()){
                        taskHr.setStatus (5);
                    }
                    taskHrCompanyMapper.updateById(taskHr);
                }
        taskWorker.setStatus(1);
        taskWorker.setConfirmedDate(OffsetDateTime.now());
        //TODO 用人单位人数加1
        Integer hotelConfirmedWorkers=hotelTask.getConfirmedWorkers();
        if(hotelConfirmedWorkers==null){
            hotelConfirmedWorkers=0;
        }
        if(hotelTask.getConfirmedWorkers ()+1<=hotelTask.getNeedWorkers ()){
            hotelTask.setConfirmedWorkers(hotelConfirmedWorkers+1);
        }
        if(hotelTask.getConfirmedWorkers() == hotelTask.getNeedWorkers()){
            hotelTask.setStatus (4);
        }else{
            hotelTask.setStatus (3);
        }
        taskWorker.setRefusedReason("");
        taskWorkerMapper.updateById(taskWorker);
        taskMapper.updateById(hotelTask);
        //添加一个通知消息
        User user = userMapper.selectByWorkerId(taskWorker.getWorkerId());
        if (user == null) {
            return ResultDO.buildSuccess("查询不到小时工的信息");
        }
        Inform notice = new Inform();
        notice.setCreateTime(OffsetDateTime.now());
        notice.setModifyTime(OffsetDateTime.now());
        if(taskHr!=null){
            notice.setReceiveId(taskHr.getHrCompanyId());
            notice.setAcceptType(2);
        }else{
            notice.setReceiveId(hotelTask.getHotelId ());
            notice.setAcceptType(3);
        }
        notice.setSendType(1);
        notice.setTitle("任务已接受");
        notice.setContent("小时工" + user.getNickname() + "接受了你派发的任务");
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
        TaskWorker taskWorker = null;
        Message message = messageMapper.selectById(refusedTaskReq.getMessageId());
            if (message == null) {
                throw new BusinessException("消息不存在");
            }
            message.setStatus(1);
            messageMapper.updateAllColumnById(message);
            taskWorker = taskWorkerMapper.findFirstById(message.getWorkerTaskId());
        if(taskWorker.getStatus()>0){
            throw new BusinessException("任务状态不是新派发,无法拒绝任务");
        }
        //TODO 人力公司人数判断
        TaskHrCompany taskHr= taskHrCompanyMapper.queryByTaskId(taskWorker.getTaskHrId());
        if(taskHr!=null){
            Integer refuseWorkers=taskHr.getRefusedWorkers();
            if(refuseWorkers==null){
                refuseWorkers=0;
            }
            taskHr.setRefusedWorkers(refuseWorkers+1);
            taskHrCompanyMapper.updateById(taskHr);
        }
        taskWorker.setStatus(2);
        taskWorker.setConfirmedDate(OffsetDateTime.now());
        taskWorker.setRefusedReason(refusedTaskReq.getRefusedReason());
        //TODO 用人单位人数
        Task hotelTask=taskMapper.getFirstById(taskWorker.getHotelTaskId ());
        Integer hotelRefuseWorkers=hotelTask.getRefusedWorkers();
        if(hotelRefuseWorkers==null){
            hotelRefuseWorkers=0;
        }
        hotelTask.setRefusedWorkers(hotelRefuseWorkers+1);
        taskWorkerMapper.updateById(taskWorker);
        taskMapper.updateById(hotelTask);

        //发送消息
        Message m = new Message();
        m.setContent(refusedTaskReq.getRefusedReason());
        MessageTemplate mess = messageTemplateMapper.findFirstByCode ("refuseTaskMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageType(10);
        m.setMessageTitle(mess.getTitle());
        m.setWorkerId (taskWorker.getWorkerId ());
        m.setWorkerTaskId (taskWorker.getPid ());
        if(taskHr!=null){
            m.setHrCompanyId (taskHr.getHrCompanyId());
            m.setHrTaskId(taskHr.getPid ());
            m.setApplyType(2);
        }else{
            m.setApplyType(3);
        }
        m.setTaskId(hotelTask.getPid());
        m.setHotelId(hotelTask.getHotelId());
        Map<String, String> param = new HashMap<>();
        param.put("userName", userMapper.selectByWorkerId (taskWorker.getWorkerId ()).getNickname ());
        param.put("content", refusedTaskReq.getRefusedReason());
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setApplicantType(1);
        m.setStatus(0);
        m.setIsTask(0);
        m.setStop (message.isStop ());
        messageMapper.insert(m);
        return ResultDO.buildSuccess("拒绝任务成功");
    }
    /**
     * 分页查询任务
     */
    @Override
    public ResultDO pagesTaskWorkers(Paginator paginator, TaskWorkerQuery taskQueryDTO) {
        User u = userMapper.selectById(taskQueryDTO.getWorkerId ());
        if(u != null){
            taskQueryDTO.setWorkerId (u.getWorkerId ());
        }
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
        Double UnConfirmedPay = 0.0;
        for (TaskWorker t:list) {
            if(t.getToDate ().isAfter (OffsetDateTime.now ()) && t.getFromDate ().isBefore (OffsetDateTime.now ()) && t.getStatus () == 1){
                t.setStatus (4);
            }
            if(t.getToDate ().isBefore (OffsetDateTime.now ()) && t.getStatus () == 1 ){
                t.setStatus (5);
            }
            t.setUser (userMapper.queryByUserId (t.getUserId ()));
            t.setHotel (companyMapper.selectById (t.getHotelId ()));
            t.setHrCompany (companyMapper.selectById (t.getHrCompanyId ()));
            shouldPayMoney = Maths.getTwoDecimal (t.getShouldPayMoney() + shouldPayMoney,2);
            havePayMoney = Maths.getTwoDecimal (t.getHavePayMoney() + havePayMoney,2);
            UnConfirmedPay = Maths.getTwoDecimal (t.getUnConfirmedPay () + UnConfirmedPay,2);
            //t.setUnConfirmedPay(messageMapper.selectUnConfirmePay (1,t.getTaskHrId (),t.getPid ()));
            t.setShouldPayMoney (Maths.getTwoDecimal (t.getShouldPayMoney (),2));
            t.setHavePayMoney (Maths.getTwoDecimal (t.getHavePayMoney (),2));
            t.setUnConfirmedPay (Maths.getTwoDecimal (t.getUnConfirmedPay (),2));
            t.setPaidPayMoney (Maths.getTwoDecimal (t.getPaidPayMoney (),2));
        }
        extra.put("shouldPayMoney",shouldPayMoney);
        extra.put("havePayMoney",havePayMoney);
        extra.put("paidPayMoney", Maths.getTwoDecimal (shouldPayMoney - havePayMoney - UnConfirmedPay,2));
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
        return taskWorkerMapper.selectCurTasCount(query);
    }

    /**
     * 查询小时工账单
     * @param taskQueryDTO
     * @return
     */
    @Override
    public List<DownLoadAccount> queryWorkerAccount(TaskWorkerQuery taskQueryDTO) {

        //查询数据集合
        User u = userMapper.selectById(taskQueryDTO.getWorkerId ());
        if(u != null){
            taskQueryDTO.setWorkerId (u.getWorkerId ());
        }
        List<TaskWorker> list = null;
        if(taskQueryDTO.getHotelName () == null || taskQueryDTO.getHotelName ().equals ("")) {
            int count = taskWorkerMapper.findAllCount(taskQueryDTO);
            PageHelper.startPage(1, count, true);
            list = taskWorkerMapper.findAll(taskQueryDTO);
        }else{
            int count = taskWorkerMapper.findAllhCount(taskQueryDTO);
            PageHelper.startPage(1, count, true);
            list = taskWorkerMapper.findAllh(taskQueryDTO);
        }
        List<DownLoadAccount> accountList = new ArrayList<>();
        if (list != null) {
            DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            DateTimeFormatter df2 = DateTimeFormatter.ofPattern("HH:mm");
            for (TaskWorker t : list) {
                DownLoadAccount account = new DownLoadAccount();
                account.setTaskContent(t.getTaskContent());
                account.setTaskType(t.getTaskTypeText());
                account.setWorkDate(t.getFromDate().format(df1) + " / " + t.getToDate().format(df1));
                account.setStartEndTime(t.getDayStartTime().format(df2) + " / " + t.getDayEndTime().format(df2));
                account.setHavePay(Maths.getTwoDecimal (t.getHavePayMoney(),2));
                account.setPaidPayMoney(Maths.getTwoDecimal (t.getPaidPayMoney (),2));
                account.setShouldPay(Maths.getTwoDecimal (t.getShouldPayMoney (),2));
                account.setUnConfirmedPay(Maths.getTwoDecimal (t.getUnConfirmedPay (),2));
                account.setName(t.getHotelName());
                accountList.add(account);
            }
        }
        return accountList;
    }

    /**
     * 查询小时工任务
     * @param taskQueryDTO
     * @return
     */
    @Override
    public List<WorkerTask> queryWorkerTask(TaskWorkerQuery taskQueryDTO) {

        User u = userMapper.selectById(taskQueryDTO.getWorkerId ());
        if(u != null){
            taskQueryDTO.setWorkerId (u.getWorkerId ());
        }
        int count = taskWorkerMapper.findAllCount(taskQueryDTO);
        PageHelper.startPage(1, count, true);
        List<Map<String, Object>> list = taskWorkerMapper.findWorkerTask(taskQueryDTO);
        List<WorkerTask> taskList = new ArrayList<>();
        if (list != null) {
            for (Map<String, Object> task : list) {
                WorkerTask t = new WorkerTask();
                t.setName((String)task.get("hotelName"));
                t.setLeader((String)task.get("leader"));
                t.setLeaderMobile((String)task.get("leaderMobile"));
                t.setHrName((String)task.get("hrName"));
                t.setRefuseReason((String)task.get("refuseReason"));
                t.setSalary(task.get("hourlyPay") + "");
                t.setTaskContent(task.get("taskContent") + "");
                t.setType(task.get("taskTypeText") + "");
                t.setWorkDate(task.get("fromDate") + " / " + task.get("toDate"));
                t.setWorkTime(task.get("dayStartTime") + " / " + task.get("dayEndTime"));
                int status = (Integer)task.get("status");
                if (status == 0) {
                    t.setStatus("等待确认");
                } else if (status == 1) {
                    t.setStatus("接受任务");
                } else if (status == 2) {
                    t.setStatus("拒绝任务");
                } else if (status == 3) {
                    t.setStatus("终止任务");
                } else if (status == 4) {
                    t.setStatus("进行中");
                } else if (status == 5) {
                    t.setStatus("已完成");
                } else if (status == 11) {
                    t.setStatus("违约");
                }
                taskList.add(t);
            }
        }
        return taskList;
    }
}
