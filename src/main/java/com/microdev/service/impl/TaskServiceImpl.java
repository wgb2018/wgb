package com.microdev.service.impl;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.FilePush;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.DateUtil;
import com.microdev.common.utils.JPushManage;
import com.microdev.common.utils.RedisUtil;
import com.microdev.common.utils.StringKit;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.converter.TaskConverter;
import com.microdev.service.InformService;
import com.microdev.service.MessageService;
import com.microdev.service.TaskService;
import com.microdev.type.ConstantData;
import com.microdev.type.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;

@Transactional
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper,Task> implements TaskService{

    @Autowired
    CompanyMapper companyMapper;
    @Autowired
    private TaskConverter taskConverter;
    @Autowired
    TaskMapper taskMapper;
    @Autowired
    TaskHrCompanyMapper taskHrCompanyMapper;
    @Autowired
    HotelPayDetailsMapper hotelPayDetailsMapper;
    @Autowired
    BillMapper billMapper;
    @Autowired
    TaskWorkerMapper taskWorkerMapper;
    @Autowired
    private MessageService messageService;
    @Autowired
    private InformTemplateMapper informTemplateMapper;
    @Autowired
    private InformService informService;
    @Autowired
    private MessageTemplateMapper messageTemplateMapper;
    @Autowired
    JpushClient jpushClient;
    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private HotelHrCompanyMapper hotelHrCompanyMapper;
    @Autowired
    private UserCompanyMapper userCompanyMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private FilePush filePush;
    /**
     * 创建用人单位任务
     */
    @Override
    public ResultDO createTask(CreateTaskRequest request) {

        if(request.getFromDateL ()!=null){
            request.setFromDate (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getFromDateL ()+OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getDayStartTimeL ()),ZoneOffset.systemDefault ()).toOffsetTime ().getLong (ChronoField.SECOND_OF_DAY )*1000),ZoneOffset.systemDefault ()));

        }
        if(request.getToDateL ()!=null){
            request.setToDate (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getToDateL ()+OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getDayEndTimeL ()),ZoneOffset.systemDefault ()).toOffsetTime ().getLong (ChronoField.SECOND_OF_DAY )*1000),ZoneOffset.systemDefault ()));

        }
        if(request.getDayStartTimeL ()!=null){
            request.setDayStartTime (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getDayStartTimeL ()),ZoneOffset.systemDefault ()).toOffsetTime ());

        }
        if(request.getDayEndTimeL ()!=null){
            request.setDayEndTime (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getDayEndTimeL ()),ZoneOffset.systemDefault ()).toOffsetTime ());

        }
        Company hotel=companyMapper.findCompanyById(request.getHotelId());
        if (hotel == null || !StringUtils.hasLength(hotel.getPid()) ) {
            throw new ParamsException("用人单位不存在");
        }
        //TODO 判断用人单位状态
        if(hotel.getStatus()==null ||hotel.getStatus()!=1){
            throw new ParamsException("用人单位状态不是已审核,无法发布任务");
        }

        int needAllWorkers=0;
        for (TaskHrCompanyDTO item : request.getHrCompanySet()) {
            needAllWorkers=needAllWorkers+item.getNeedWorkers();
        }
        Task task=taskConverter.toNewTask(request);
        task.setConfirmedWorkers(0);
        task.setRefusedWorkers(0);
        task.setHotelId(hotel.getPid());
        task.setHotelName(hotel.getName());
        if(request.getNeedhrCompanys () == null || request.getNeedhrCompanys () == 0){
            task.setStatus(3);
            task.setNeedWorkers(needAllWorkers);
        }else{
            task.setStatus(1);
            task.setNeedWorkers(request.getNeedhrCompanys ());
        }
        task.setTaskTypeText(request.getTaskTypeText());
        task.setTaskTypeCode(request.getTaskTypeCode());
        task.setTaskContent(request.getTaskContent());
        task.setHourlyPay(request.getHourlyPay());
        task.setSettlementPeriod (request.getSettlementPeriod ());
        task.setSettlementNum (request.getSettlementNum ());
        task.setWorkerSettlementNum (request.getWorkerSettlementNum ());
        task.setWorkerSettlementPeriod (request.getWorkerSettlementPeriod ());
        taskMapper.insert(task);
        Set<TaskHrCompany> set = AddHrTask(task,request);

        messageService.hotelDistributeTask(set, hotel, "workTaskMessage", request);
        TaskViewDTO taskDto= taskConverter.toViewDTOWithOutSet(task);
        return ResultDO.buildSuccess("任务发布成功",taskDto);
    }
    /**
     * 获取单条任务
     */
    @Override
    public ResultDO getTaskById(String id) {
        TaskViewDTO taskViewDTO = taskMapper.findTaskAndHrInfoById(id);
        taskViewDTO.setPid (id);
        if (taskViewDTO == null) {
            return ResultDO.buildSuccess(new TaskViewDTO());
        }
        taskViewDTO.setPayStatus("未结算");
        if (taskViewDTO.getHavePayMoney() > 0) {
            taskViewDTO.setPayStatus("结算中");
        }
        if (taskViewDTO.getShouldPayMoney() > 0
                && (taskViewDTO.getShouldPayMoney() - taskViewDTO.getHavePayMoney() <= 0)) {
            taskViewDTO.setPayStatus("已结算");
        }
        if(taskViewDTO.getStatus() >= 3 ){
            if(OffsetDateTime.now().isBefore(taskViewDTO.getToDate()) &&  OffsetDateTime.now().isAfter(taskViewDTO.getFromDate())){
                taskViewDTO.setStatus(5);
            }
        }
        if(OffsetDateTime.now().isAfter(taskViewDTO.getToDate())){
            taskViewDTO.setStatus(6);
        }
        List<TaskHrCompanyViewDTO> taskHrList = taskViewDTO.getListTaskHr();
        List<Map<String, Object>> list = null;
        if (taskHrList != null) {
            for(int i = 0;i<taskHrList.size ();i++){
                list = taskWorkerMapper.selectTaskWorkById(taskHrList.get (i).getPid());
                List<Map<String, Object>> confirmedList = new ArrayList<>();
                List<Map<String, Object>> refusedList = new ArrayList<>();
                List<Map<String, Object>> distributedList = new ArrayList<>();
                for (Map<String, Object> m : list) {
                    if(m.get("birthday") != null){
                        m.put("age", DateUtil.caculateAge((Timestamp) m.get("birthday")));
                    }
                    if((Integer) m.get("taskStatus") != 2){
                        distributedList.add(m);
                    }
                    if (m.get("taskStatus") == null)
                        continue;
                    if ((Integer) m.get("taskStatus") == 1 || (Integer) m.get("taskStatus") == 3) {
                        confirmedList.add(m);
                    } else if ((Integer) m.get("taskStatus") == 2) {
                        refusedList.add(m);
                    }
                }
                taskHrList.get (i).setRefusedList(refusedList);
                taskHrList.get (i).setConfirmedList(confirmedList);
                taskHrList.get (i).setDistributedList(distributedList);
            }
        }
        taskViewDTO.setListTaskHr (taskHrList);
        list = taskWorkerMapper.selectTaskWorkByHotelId (taskViewDTO.getPid ());
        List<Map<String, Object>> confirmedList = new ArrayList<>();
        List<Map<String, Object>> refusedList = new ArrayList<>();
        List<Map<String, Object>> distributedList = new ArrayList<>();
        for (Map<String, Object> m : list)  {
            if (m.get("taskStatus") == null)
                continue;
            if ((Integer) m.get("taskStatus") == 1 || (Integer) m.get("taskStatus") == 3) {
                confirmedList.add(m);
            } else if ((Integer) m.get("taskStatus") == 2) {
                refusedList.add(m);
            }            if(taskViewDTO.getToDate ().isAfter (OffsetDateTime.now ()) && taskViewDTO.getFromDate ().isBefore (OffsetDateTime.now ()) && ((Integer) m.get("taskStatus")).intValue () == 1){
                m.put ("taskStatus",4);
            }
            if(taskViewDTO.getToDate ().isBefore (OffsetDateTime.now ()) && ((Integer) m.get("taskStatus")).intValue () == 1 ){
                m.put ("taskStatus",5);
            }
            if(m.get("birthday") != null){
                System.out.println (m.get("birthday"));
                m.put("age", DateUtil.caculateAge((Timestamp) m.get("birthday")));
            }
			if((Integer) m.get("taskStatus") != 2){
                distributedList.add(m);
            }
        }
        TaskWorkerViewDTO listTaskWorker = taskViewDTO.getListTaskWorker ();
        listTaskWorker.setConfirmedList (confirmedList);
        listTaskWorker.setDistributedList (distributedList);
        listTaskWorker.setRefusedList (refusedList);
        taskViewDTO.setListTaskWorker (listTaskWorker);
        return ResultDO.buildSuccess(taskViewDTO);
    }

    @Override
    public ResultDO getPageTasks(Paginator paginator, TaskQueryDTO taskQueryDTO) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        String date = taskQueryDTO.getOfDate();
        if(date!=null) {
            Integer year = Integer.parseInt(date.split("-")[0]);
            Integer month = Integer.parseInt(date.split("-")[1]);
            if (month == 12) {
                taskQueryDTO.setStartTime(OffsetDateTime.of
                        (year, 12, 1, 0, 0, 0, 0,
                                ZoneOffset.UTC));
                taskQueryDTO.setEndTime(OffsetDateTime.of
                        (year, 1, 1, 0, 0, 0, 0,
                                ZoneOffset.UTC));
            } else {
                taskQueryDTO.setStartTime(OffsetDateTime.of
                        (year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC));
                taskQueryDTO.setEndTime(OffsetDateTime.of
                        (year, month + 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
            }
        }
        //查询数据集合
        List<Task> list = taskMapper.queryTasks(taskQueryDTO);

        List<TaskViewDTO> data = new ArrayList<>();
        for (Task task:list) {
            if(task.getConfirmedWorkers () == task.getNeedWorkers ()){
                task.setStatus (4);
            }
            data.add(taskConverter.toViewDTOWithOutSet(task));
        }
        PageInfo<Task> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        /*HashMap<String,Object> extra = new HashMap<>();
        Double shouldPayMoney = 0.0;
        Double havePayMoney=0.0;
        for (Task task:list) {
            shouldPayMoney += task.getShouldPayMoney();
            havePayMoney += task.getHavePayMoney();
        }
        extra.put("shouldPayMoney",shouldPayMoney);
        extra.put("havePayMoney",havePayMoney);
        extra.put("paidPayMoney", Maths.sub(shouldPayMoney, havePayMoney));*/
        return ResultDO.buildSuccess(null,result,null,null);

    }
    /**
     * 用人单位支付人力公司
     */
    @Override
    public ResultDO hotelPayHr(PayParam PayHrParam) {
        if (PayHrParam == null || !StringUtils.hasLength(PayHrParam.getTaskHrId ()) ||  StringUtils.isEmpty (PayHrParam.getPayMoney ())) {
            throw new ParamsException("参数错误");
        }
        TaskHrCompany taskHr =  taskHrCompanyMapper.queryByTaskId (PayHrParam.getTaskHrId ());

        //插入支付记录
                Bill bill = new Bill();
                bill.setTaskId(taskHr.getTaskId ());
                bill.setHotelId(taskHr.getHotelId());
                bill.setPayMoney(PayHrParam.getPayMoney ());
                bill.setHrCompanyId(taskHr.getHrCompanyId());
                bill.setTaskHrId (taskHr.getPid ());
                bill.setDeleted(false);
                bill.setPayType(1);
                bill.setStatus (0);
                billMapper.insert(bill);
         //发送支付待确认消息
        MessageTemplate mess = messageTemplateMapper.findFirstByCode("hotelPayHrMessage");
        Message m = new Message();
        m.setTaskId (taskHr.getTaskId ());
        m.setMessageCode ("hotelPayHrMessage");
        m.setMessageType(8);
        m.setMessageTitle ("用人单位支付人力公司");
        m.setStatus (0);
        m.setHotelId (taskHr.getHotelId ());
        m.setHrCompanyId (taskHr.getHrCompanyId ());
        m.setApplicantType (3);
        m.setApplyType (2);
        m.setIsTask (0);
        m.setHrTaskId (taskHr.getPid ());
        m.setRequestId (bill.getPid ());
        m.setMinutes (PayHrParam.getPayMoney ().toString ());
        Map<String, String> param = new HashMap<>();
        param.put("hotelName", companyMapper.findCompanyById (taskHr.getHotelId ()).getName ());
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent (c);
        messageService.insert (m);
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (taskHr.getHrCompanyId ()).getLeaderMobile ( ), c));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {
            e.printStackTrace ( );
        }
        taskHr.setUnConfirmedPay (taskHr.getUnConfirmedPay ()+PayHrParam.getPayMoney ());
        taskHrCompanyMapper.updateAllColumnById (taskHr);
        Task task = taskMapper.selectById (taskHr.getTaskId ());
        task.setUnConfirmedPay (task.getUnConfirmedPay ()+PayHrParam.getPayMoney ());
        taskMapper.updateAllColumnById (task);
        return ResultDO.buildSuccess("消息发送成功");
    }

    @Override
    public ResultDO hotelPayWoreker(PayParam payParam) {
        if (payParam == null || !StringUtils.hasLength (payParam.getTaskWorkerId ( )) || StringUtils.isEmpty (payParam.getPayMoney ( ))) {
            throw new ParamsException ("参数错误");
        }
        TaskWorker taskWorker = taskWorkerMapper.findFirstById (payParam.getTaskWorkerId ( ));
        //插入支付记录
        Bill bill = new Bill ( );
        bill.setTaskHrId (taskWorker.getTaskHrId ( ));
        bill.setHotelId (taskWorker.getHotelId ( ));
        bill.setPayMoney (payParam.getPayMoney ( ));
        bill.setHrCompanyId (taskWorker.getHrCompanyId ( ));
        bill.setWorkerId (taskWorker.getWorkerId ());
        bill.setDeleted (false);
        bill.setPayType (3);
        bill.setStatus (0);
        bill.setTaskId (taskWorker.getHotelTaskId ());        billMapper.insert (bill);
        //发送支付待确认消息
        MessageTemplate mess = messageTemplateMapper.findFirstByCode ("hrPayWorkerMessage");
        Message m = new Message ( );
        m.setTaskId (taskWorker.getHotelTaskId ( ));
        m.setMessageCode ("hotelPayWorkerMessage");
        m.setMessageType (8);
        m.setWorkerId (taskWorker.getWorkerId ( ));
        m.setWorkerTaskId (taskWorker.getPid ( ));
        m.setMessageTitle ("用人单位支付小时工");
        m.setStatus (0);
        m.setHotelId (taskWorker.getHotelId ( ));
        m.setApplicantType (3);
        m.setApplyType (1);
        m.setIsTask (0);
        m.setMinutes (payParam.getPayMoney ( )+"");
        m.setRequestId(bill.getPid());
        Map <String, String> param = new HashMap <> ( );
        param.put ("hrName", companyMapper.findCompanyById (taskWorker.getHotelId ()).getName ( ));
        String c = StringKit.templateReplace (mess.getContent ( ), param);
        m.setContent (c);
        messageService.insert (m);
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (taskWorker.getWorkerId ( )).getMobile ( ), m.getMessageContent ()));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {
            e.printStackTrace ( );
        }
        taskWorker.setUnConfirmedPay (taskWorker.getUnConfirmedPay ( ) + payParam.getPayMoney ( ));
        taskWorkerMapper.updateById (taskWorker);
        Task task = taskMapper.selectById (taskWorker.getHotelTaskId ());
        if (task == null) {
              throw new ParamsException ("小时工工任务错误");
        }
        task.setUnConfirmedPay (task.getUnConfirmedPay ( ) + payParam.getPayMoney ( ));
        taskMapper.updateById (task);
        return ResultDO.buildSuccess ("消息发送成功");
    }

    /**
     * 用人单位再次派发任务
     * @param request
     * @return
     */
    @Override
    public ResultDO hotelAgainSendTask(CreateTaskRequest request) {
        if (request == null) {
            throw new ParamsException("参数不能未空");
        }
        if (StringUtils.isEmpty(request.getTaskId())  || request.getHrCompanySet().size() == 0) {
            throw new ParamsException("参数错误");
        }
        Task task = taskMapper.selectById(request.getTaskId());

        if (task == null) {
            throw new BusinessException("任务找不到");
        }
        request.setHotelId(task.getHotelId());
        Company hotel=companyMapper.findCompanyById(task.getHotelId());
        if (hotel == null || !StringUtils.hasLength(hotel.getPid()) ) {
            throw new ParamsException("用人单位不存在");
        }
        // 判断用人单位状态
        if(hotel.getStatus()==null ||hotel.getStatus()!=1){
            throw new ParamsException("用人单位状态不是已审核,无法发布任务");
        }
        int needAllWorkers=0;
        for (TaskHrCompanyDTO item : request.getHrCompanySet()) {
            needAllWorkers=needAllWorkers+item.getNeedWorkers();
        }

        TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById(request.getTaskHrId());
        if (taskHrCompany == null) {
            throw new ParamsException("查询不到人力任务信息");
        }

        Set<TaskHrCompany> set = AddHrTask(task,request);
        //更新人力的状态为5: 派单完成
        taskHrCompany.setStatus(5);
        int number = 0;
        for (TaskHrCompanyDTO d : request.getHrCompanySet()) {
            number += d.getNeedWorkers();
        }
        taskHrCompany.setNeedWorkers(taskHrCompany.getNeedWorkers() - number);
        taskHrCompanyMapper.updateAllColumnById(taskHrCompany);
        //发送消息
        messageService.hotelDistributeTask(set, hotel, "workTaskMessage", request);
        //TaskViewDTO taskDto= taskConverter.toViewDTOWithOutSet(task);
        return ResultDO.buildSuccess("任务发布成功");
    }

    /**
     * 用人单位同意人力拒绝任务并再次派发
     * @param request
     * @return
     */
    @Override
    public ResultDO hotelAgreeAndSendTask(CreateTaskRequest request) {

        if (StringUtils.isEmpty(request.getMessageId())) {
            throw new ParamsException("参数不能为空");
        }
        Message message = messageService.selectById(request.getMessageId());
        if (message == null) {
            throw new ParamsException("消息查询不到数据");
        }
        message.setStatus(1);
        messageService.updateById(message);

        Task task = taskMapper.selectById(message.getTaskId());

        if (task == null) {
            throw new BusinessException("任务找不到");
        }
        if(task.getFromDate ().isBefore (OffsetDateTime.now ())){
            return ResultDO.buildSuccess("任务已执行，派发失败");
        }
        request.setHotelId(task.getHotelId());
        Company hotel=companyMapper.findCompanyById(task.getHotelId());
        if (hotel == null || !StringUtils.hasLength(hotel.getPid()) ) {
            throw new ParamsException("用人单位不存在");
        }
        // 判断用人单位状态
        if(hotel.getStatus()==null ||hotel.getStatus()!=1){
            throw new ParamsException("用人单位状态不是已审核,无法发布任务");
        }

        TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById(message.getHrTaskId());
        if (taskHrCompany == null) {
            throw new ParamsException("查询不到人力任务信息");
        }

        Set<TaskHrCompany> set = AddHrTask(task,request);
        //更新人力的状态为3: 拒绝任务
        taskHrCompany.setStatus(3);
        taskHrCompanyMapper.updateAllColumnById(taskHrCompany);
        //发送消息
        messageService.hotelDistributeTask(set, hotel, "workTaskMessage", request);

        //发送拒绝任务成功通知
        InformTemplate inf = informTemplateMapper.selectByCode (InformType.hotel_agree_apply.name());
        Map<String,String> map = new HashMap <> ();
        map.put("hotel",companyMapper.findCompanyById (message.getHotelId()).getName ());

        String content = StringKit.templateReplace(inf.getContent (), map);
        informService.sendInformInfo (3,2,content,message.getHrCompanyId (),"拒绝任务成功");
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (message.getHrCompanyId ()).getLeaderMobile ( ), content));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {

        }
        return ResultDO.buildSuccess("任务发布成功");
    }

    /**
     * 查询当前用人单位任务数量
     * @param applyParamDTO
     * @return
     */
    @Override
    public int selectCurHotelTaskCount(ApplyParamDTO applyParamDTO) {
        if (StringUtils.isEmpty(applyParamDTO.getId())) {
            throw new ParamsException("参数错误");
        }
        TaskQueryDTO queryDTO = new TaskQueryDTO();
        queryDTO.setHotelId(applyParamDTO.getId());
        queryDTO.setStatus(0);

        return taskMapper.queryHotelCurTaskCount(queryDTO);
    }

    /**
     *查询酒店任务
     * @param taskQueryDTO
     * @return
     */
    @Override
    public List<EmployerTask> queryHotelTask(TaskQueryDTO taskQueryDTO) {
        String date = taskQueryDTO.getOfDate();
        if(date!=null) {
            Integer year = Integer.parseInt(date.split("-")[0]);
            Integer month = Integer.parseInt(date.split("-")[1]);
            if (month == 12) {
                taskQueryDTO.setStartTime(OffsetDateTime.of
                        (year, 12, 1, 0, 0, 0, 0,
                                ZoneOffset.UTC));
                taskQueryDTO.setEndTime(OffsetDateTime.of
                        (year, 1, 1, 0, 0, 0, 0,
                                ZoneOffset.UTC));
            } else {
                taskQueryDTO.setStartTime(OffsetDateTime.of
                        (year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC));
                taskQueryDTO.setEndTime(OffsetDateTime.of
                        (year, month + 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
            }
        }
        //查询数据集合
        int count = taskMapper.queryTasksCount(taskQueryDTO);
        PageHelper.startPage(1, count, true);
        List<Task> list = taskMapper.queryTasks(taskQueryDTO);
        List<EmployerTask> taskList = new ArrayList<>();
        if (list != null) {
            DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            DateTimeFormatter df2 = DateTimeFormatter.ofPattern("HH:mm");
            for (Task task : list) {
                EmployerTask t = new EmployerTask();
                t.setEmployerName(task.getHotelName());
                t.setSalary(task.getHourlyPayHotel() + "");
                if (task.getSettlementPeriod() == 0) {
                    t.setSettlement(task.getSettlementNum() + "天/次");
                } else if (task.getSettlementPeriod() == 1) {
                    t.setSettlement(task.getSettlementNum() + "月/次");
                }
                t.setTaskContent(task.getTaskContent());
                t.setType(task.getTaskTypeText());
                t.setWorkDate(task.getFromDate().format(df1) + " / " + task.getToDate().format(df1));
                t.setWorkTime(task.getDayStartTime().format(df2) + " / " + task.getDayEndTime().format(df2));
                t.setTotal(task.getConfirmedWorkers() + " / " + task.getNeedWorkers());
                int status = task.getStatus();
                if(status >=3){
                    if(OffsetDateTime.now().isBefore(task.getToDate()) &&  OffsetDateTime.now().isAfter(task.getFromDate())){
                        status = 5;
                    }
                }
                if(OffsetDateTime.now().isAfter(task.getToDate())){
                    status = 6;
                }
                if (status == 0) {
                    t.setStatus("未完成");
                } else if (status == 1) {
                    t.setStatus("未接单");
                } else if (status == 2) {
                    t.setStatus("已接单");
                } else if (status == 3) {
                    t.setStatus("派单中");
                } else if (status == 4) {
                    t.setStatus("派单完成");
                } else if (status == 5) {
                    t.setStatus("进行中");
                } else if (status == 6) {
                    t.setStatus("已完成");
                }
                taskList.add(t);
            }
        }
        return taskList;
    }

    /**
     * 用人单位同意人力报名申请并派发
     * @param request
     * @return
     */
    @Override
    public ResultDO agreeApplySendTask(CreateTaskRequest request) {
        System.out.println (request);
        if (StringUtils.isEmpty(request.getMessageId())) {
            throw new ParamsException("参数不能为空");
        }
        if(request.getHrCompanySet ().size () == 0){
            throw new ParamsException("请指定需要派发的人力公司");
        }
        Message message = messageService.selectById (request.getMessageId ());
        if(message == null){
            throw new ParamsException("messageId不正确");
        }
        Notice notice = noticeMapper.selectById (message.getRequestId ());
        Task task = taskMapper.selectById (message.getTaskId ());
        if(notice == null){
            throw new ParamsException("message不正确");
        }
        if(task == null){
            throw new ParamsException("taskId不正确");
        }
        message.setStatus (1);
        messageService.updateById (message);
        if(notice.getStatus () == 1){
            return ResultDO.buildError ("任务报名已结束");
        }
        if(notice.getNeedWorkers () == notice.getConfirmedWorkers ()){
            return ResultDO.buildError ("任务报名人数已满");
        }
        for (TaskHrCompanyDTO req:request.getHrCompanySet ()) {
            HotelHrCompany hotelHrCompany = hotelHrCompanyMapper.selectByHrHotelId (req.getHrCompanyId (),notice.getHotelId ());
            if(hotelHrCompany == null){
                hotelHrCompany = new HotelHrCompany ();
                hotelHrCompany.setStatus (0);
                hotelHrCompany.setBindType (1);
                //从redis中取出默认协议地址
                String path = redisUtil.getString("defaultHrHotelProtocol");
                if (StringUtils.isEmpty(path)) {
                    try {
                        path = filePush.pushFileToServer(ConstantData.CATALOG.getName(), ConstantData.HRHOTELPROTOCOL.getName());
                        //path = filePush.pushFileToServer(ConstantData.CATALOG.getName(), ConstantData.TEST.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ResultDO.buildError("服务异常");
                    }
                    redisUtil.setString("defaultHrHotelProtocol", path);
                }
                hotelHrCompany.setBindProtocol (path);
                hotelHrCompany.setHotelId (notice.getHotelId ());
                hotelHrCompany.setHrId (req.getHrCompanyId ());
                hotelHrCompany.setBindTime (OffsetDateTime.now ());
                hotelHrCompanyMapper.insert (hotelHrCompany);
            }else if(hotelHrCompany.getStatus () == 1){//合作过 关系变成合作
                hotelHrCompany.setStatus (0);
            }else if(hotelHrCompany.getStatus () == 3){//待审核 关系变成合作
                hotelHrCompany.setStatus (0);
                Map<String,Object> map = new HashMap <> ();
                map.put("messageType",13);
                map.put("hrCompanyId",req.getHrCompanyId ());
                map.put("hotelId",notice.getHotelId ());
                List<Message> mes1 = messageService.selectByMap (map);
                if(mes1.size ()!=0){
                    for (Message ms:mes1) {
                        ms.setStatus (1);
                    }
                }
                messageService.updateBatchById (mes1);
            }else if(hotelHrCompany.getStatus () == 4){
                hotelHrCompany.setStatus (0);
            }
            notice.setConfirmedWorkers (notice.getConfirmedWorkers ()+req.getNeedWorkers ());
            if(notice.getConfirmedWorkers () > notice.getNeedWorkers ()){
                return ResultDO.buildError ("报名人数超过剩余所需人数");
            }
            hotelHrCompanyMapper.updateById (hotelHrCompany);
        }
        if(notice.getConfirmedWorkers () == notice.getNeedWorkers ()){
            notice.setStatus (1);
            Map<String,Object> map = new HashMap <> ();
            map.put ("requestId",notice.getPid ());
            List<Message> list = messageService.selectByMap (map);
            for (Message ms:list) {
                ms.setStatus (1);
            }
            messageService.updateBatchById (list);
        }
        noticeMapper.updateById (notice);
        request.setNoticeTask (true);
        Set<TaskHrCompany> set = AddHrTask(task,request);
        return ResultDO.buildSuccess ("发送成功");
    }

    /**
     * 用人单位同意人力报名申请并派发
     * @param request
     * @return
     */
    @Override
    public ResultDO agreeApplyWorker(CreateTaskRequest request) {
        System.out.println (request);
        if(StringUtils.isEmpty(request.getMessageId ())){
            throw new ParamsException ("参数不正确");
        }
        Message message = messageService.selectById (request.getMessageId ());
        if(message == null){
            throw new ParamsException("messageId不正确");
        }
        Notice notice = noticeMapper.selectById (message.getRequestId ());
        message.setStatus (1);
        messageService.updateById (message);
        if(notice.getNeedWorkers () == notice.getConfirmedWorkers ()){
            return ResultDO.buildError ("招聘人数已达到上限");
        }
        if(notice.getStatus () == 1){
            return ResultDO.buildError ("招聘已结束");
        }
        UserCompany userCompany = userCompanyMapper.selectByWorkerIdHrId (message.getHotelId (),message.getWorkerId ());
        if(userCompany == null){
            userCompany = new UserCompany ();
            userCompany.setCompanyId (message.getHotelId ());
            userCompany.setUserId (userMapper.selectByWorkerId (message.getWorkerId ()).getPid ());
            userCompany.setStatus (1);
            userCompany.setBindProtocol ("");
            userCompany.setCompanyType (1);
            userCompany.setUserType (UserType.worker);
            userCompanyMapper.insert (userCompany);
        }else{
            if(userCompany.getStatus () == 2 || userCompany.getStatus () == 4){
                userCompany.setStatus (1);
            }else{
                throw new ParamsException("用人单位与小时工关系存在异常");
            }
            userCompanyMapper.updateById(userCompany);
        }
        notice.setConfirmedWorkers (notice.getConfirmedWorkers () + 1);
        if(notice.getConfirmedWorkers () == notice.getNeedWorkers ()){
            notice.setStatus (1);
            noticeMapper.updateById (notice);
            Map<String,Object> map = new HashMap <> ();
            map.put ("requestId",notice.getPid ());
            List<Message> list = messageService.selectByMap (map);
            for (Message ms:list) {
                ms.setStatus (1);
            }
            messageService.updateBatchById (list);
        }
        return ResultDO.buildSuccess ("添加成功");
    }

    //循环添加人力资源任务
    private  Set<TaskHrCompany> AddHrTask(Task task,CreateTaskRequest createTaskRequest ){
        Set setHrTask= new HashSet<TaskHrCompany>();
        for(TaskHrCompanyDTO hrCompanyDTO:createTaskRequest.getHrCompanySet()){
            if (!StringUtils.hasLength(String.valueOf(hrCompanyDTO.getNeedWorkers())) ||hrCompanyDTO.getNeedWorkers()==0) {
                throw new ParamsException("任务需要的人数不能为空");
            }
            Company hotel=companyMapper.findCompanyById(task.getHotelId());
            Company hrCompany=companyMapper.findCompanyById(hrCompanyDTO.getHrCompanyId());
            if(hrCompany==null){
                throw new ParamsException("编号："+hrCompanyDTO.getHrCompanyId()+"人力资源公司不存在");
            }
            if(hrCompany.getStatus()==null ||hrCompany.getStatus()!=1){
                throw new ParamsException(hrCompany.getName()+":人力资源公司状态不是已审核");
            }

            TaskHrCompany taskHrCompany=new TaskHrCompany();
            taskHrCompany.setHrCompanyId(hrCompany.getPid());
            taskHrCompany.setHrCompanyName(hrCompany.getName());
            taskHrCompany.setHotelId(hotel.getPid());
            taskHrCompany.setHotelName(hotel.getName());
            taskHrCompany.setTaskId(task.getPid());
            if(createTaskRequest.isNoticeTask ()){
                taskHrCompany.setStatus(2);
            }else{
                taskHrCompany.setStatus(1);
            }
            taskHrCompany.setRefusedWorkers(0);
            taskHrCompany.setConfirmedWorkers(0);
            taskHrCompany.setNeedWorkers(hrCompanyDTO.getNeedWorkers());
            taskHrCompany.setDeleted(false);
            taskHrCompany.setTaskTypeText(task.getTaskTypeText());
            taskHrCompany.setTaskTypeCode(task.getTaskTypeCode());
            taskHrCompany.setTaskContent(task.getTaskContent());
			taskHrCompany.setHourlyPayHotel(task.getHourlyPay());
			taskHrCompany.setFromDate (task.getFromDate ());
			taskHrCompany.setToDate (task.getToDate ());
			taskHrCompany.setDayStartTime (task.getDayStartTime ());
			taskHrCompany.setDayEndTime (task.getDayEndTime ());
			taskHrCompany.setDistributeWorkers (0);
			taskHrCompany.setSettlementNum (task.getSettlementNum ());
			taskHrCompany.setSettlementPeriod (task.getSettlementPeriod ());
			taskHrCompany.setWorkerSettlementPeriod (0);
			taskHrCompany.setWorkerSettlementNum (0);
            taskHrCompanyMapper.insert(taskHrCompany);


            setHrTask.add(taskHrCompany);
        }
        return setHrTask;
    }
}
