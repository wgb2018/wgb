package com.microdev.service.impl;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.FilePush;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.*;
import com.microdev.converter.TaskHrCompanyConverter;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.service.HotelPayHrDetailsService;
import com.microdev.service.InformService;
import com.microdev.service.MessageService;
import com.microdev.service.TaskHrCompanyService;
import com.microdev.type.ConstantData;
import com.microdev.type.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.xml.transform.dom.DOMSource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ParameterMetaData;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;

@Transactional
@Service
public class TaskHrCompanyServiceImpl extends ServiceImpl<TaskHrCompanyMapper, TaskHrCompany> implements TaskHrCompanyService {

    @Autowired
    TaskHrCompanyMapper taskHrCompanyMapper;
    @Autowired
    private TaskHrCompanyConverter taskHrCompanyConverter;
    @Autowired
    CompanyMapper companyMapper;
    @Autowired
    TaskMapper taskMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    TaskWorkerMapper taskWorkerMapper;
    @Autowired
    BillMapper billMapper;
    @Autowired
    MessageTemplateMapper messageTemplateMapper;
    @Autowired
    MessageMapper messageMapper;
    @Autowired
    private MessageService messageService;
    @Autowired
    private InformMapper informMapper;
    @Autowired
    private InformService informService;
    @Autowired
    private HotelPayHrDetailsService hotelPayHrDetailsService;
    @Autowired
    InformTemplateMapper informTemplateMapper;
    @Autowired
    MyTimeTask myTimeTask;
    @Autowired
    JpushClient jpushClient;
    @Autowired
    NoticeMapper noticeMapper;
    @Autowired
    TaskHrCompanyService taskHrCompanyService;
    @Autowired
    UserCompanyMapper userCompanyMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    FilePush filePush;
    @Autowired
    NoticeServiceMapper noticeServiceMapper;


    /**
     * 查看人力资源公司的任务
     *
     * @param id 人力任务id
     */
    @Override
    public ResultDO getTaskHrCompanyById(String id) {
        HrTaskDetails map = taskHrCompanyMapper.selectByTaskId (id);
        //map.setUnConfirmedPay (messageMapper.selectUnConfirmePay (0,map.getHotelTaskId (),map.getPid ()));
        if (map == null) {
            return ResultDO.buildSuccess (new HashMap <String, Object> ( ));
        }
        if (map.getStatus ( ) >= 4) {
            if (map.getToDate ( ).isAfter (OffsetDateTime.now ( )) && (map.getFromDate ( )).isBefore (OffsetDateTime.now ( ))) {
                map.setStatus (6);
            } else if ((map.getFromDate ( )).isBefore (OffsetDateTime.now ( ))) {
                map.setStatus (7);
            }
        }
        map.setPayStatus ("未结算");
        if (map.getWorkersHavePay ( ) > 0) {
            map.setPayStatus ("结算中");
        }
        if (map.getWorkersShouldPay ( ) > 0
                && (map.getWorkersShouldPay ( ) - map.getWorkersHavePay ( ) <= 0)) {
            map.setPayStatus ("已结算");
        }
        List <Map <String, Object>> list = taskWorkerMapper.selectTaskWorkById ((String) map.getPid ( ));
        List <Map <String, Object>> confirmedList = new ArrayList <> ( );
        List <Map <String, Object>> refusedList = new ArrayList <> ( );
        List <Map <String, Object>> distributedList = new ArrayList <> ( );
        for (Map <String, Object> m : list) {
            m.put ("age", DateUtil.caculateAge ((Timestamp) m.get ("birthday")));
            distributedList.add (m);
            if (m.get ("taskStatus") == null)
                continue;
            if ((Integer) m.get ("taskStatus") == 1 || (Integer) m.get ("taskStatus") == 3) {
                confirmedList.add (m);
            } else if ((Integer) m.get ("taskStatus") == 2) {
                refusedList.add (m);
            }

        }
        map.setConfirmedSet (confirmedList);
        map.setRefusedSet (refusedList);
        map.setDistributedSet (distributedList);
        return ResultDO.buildSuccess (map);
    }

    /**
     * 任务分发
     */
    @Override
    public ResultDO TaskHrDistribute(HrTaskDistributeRequest hrTaskDis) {
        /*if (!StringUtils.hasLength (String.valueOf (hrTaskDis.getHourlyPay ( )))) {
            throw new ParamsException ("人力公司每小时工钱不能为空");
        }*/
        /*if (!StringUtils.hasLength(hrTaskDis.getMessageId())) {
            throw new ParamsException("messageId不能为空");
        }*/
        /*if (hrTaskDis.getWorkerIds ( ).size ( ) == 0) {
            throw new ParamsException ("请选择派发的员工");
        }*/
        Message message = null;
        TaskHrCompany hrTask = null;
        if (StringUtils.hasLength (hrTaskDis.getMessageId ( ))) {
            System.out.println ("param:"+hrTaskDis.getMessageId ( ));
            message = messageMapper.selectById (hrTaskDis.getMessageId ( ));
            if (message == null || message.getStatus() == 1) {
                return ResultDO.buildSuccess ("已处理");
            }
            message.setStatus (1);
            messageMapper.updateAllColumnById (message);
            hrTask = taskHrCompanyMapper.queryByTaskId (message.getHrTaskId ( ));
            hrTask.setHourlyPay (hrTaskDis.getHourlyPay ( ));
            hrTask.setWorkerSettlementPeriod (hrTaskDis.getSettlementPeriod ());
            hrTask.setWorkerSettlementNum (hrTaskDis.getSettlementNum ());
        } else {
            hrTask = taskHrCompanyMapper.queryByTaskId(hrTaskDis.getHrTaskId());
            if(hrTask.getHourlyPay () == 0){
                if(hrTaskDis.getHourlyPay () == 0){
                    return ResultDO.buildError ("人力公司每小时工钱不能为空");
                }else{
                    hrTask.setHourlyPay (hrTaskDis.getHourlyPay ());
                }
            }
            if(hrTask.getWorkerSettlementPeriod () == null){
                if(hrTaskDis.getSettlementPeriod () == null){
                    return ResultDO.buildError  ("结算周期不能为空");
                }else{
                    hrTask.setWorkerSettlementPeriod (hrTaskDis.getSettlementPeriod ());
                }
            }
            if(hrTask.getWorkerSettlementNum () == null || hrTask.getWorkerSettlementNum () == 0){
                if(hrTaskDis.getSettlementNum () == null || hrTaskDis.getSettlementNum () == 0){
                    return ResultDO.buildError  ("结算时间不能为空");
                }else{
                    hrTask.setWorkerSettlementNum (hrTaskDis.getSettlementNum ());
                }
            }
        }
       /* if (message == null) {
            throw new ParamsException("查询不到消息");
        }*/

        // 获取人力公司任务和用人单位任务信息
        if (hrTask == null) {
            throw new ParamsException("人力公司参数有误");
        }
        if(hrTask.getFromDate ().isBefore (OffsetDateTime.now ())){
            hrTask.setStatus (3);
            hrTask.setRefusedReason ("派单超时，以为您拒绝此任务");
            taskHrCompanyMapper.updateById (hrTask);
            return ResultDO.buildError ("派单超时");
        }
        //Company hrCompany=companyMapper.findCompanyById(hrTask.getHrCompanyId());
        taskMapper.updateStatus (hrTask.getTaskId ( ), 3);
        Company hrCompany = companyMapper.findCompanyById (hrTask.getHrCompanyId ( ));
        hrTask.setStatus (4);
        Task hotelTask = taskMapper.getFirstById (hrTask.getTaskId ( ));
        if (hotelTask == null) {
            return ResultDO.buildError  ("任务派发失败：未获取用人单位到任务");
        }
        hotelTask.setStatus (3);
        // Company hotel=companyMapper.findCompanyById(hrTask.getHotelId());
        List <Map <String, String>> list = new ArrayList <> ( );
        Map <String, String> m = null;
        for (String id : hrTaskDis.getWorkerIds ( )) {
            System.out.println (id);
            m = new HashMap <> ( );
            TaskWorker taskWorker = new TaskWorker ( );
            taskWorker.setTaskHrId (hrTask.getPid ( ));
            userMapper.queryByWorkerId (id);
            User user = userMapper.queryByWorkerId (id);
            taskWorker.setUserId (user.getPid ( ));
            taskWorker.setWorkerId (user.getWorkerId ( ));
            taskWorker.setUserName (user.getUsername ( ));
            if(hrTaskDis.isNoticeTask ()){
                taskWorker.setStatus (1);
                hotelTask.setConfirmedWorkers (hotelTask.getConfirmedWorkers ()+1);
                hrTask.setConfirmedWorkers (hrTask.getConfirmedWorkers ()+1);
            }else{
                taskWorker.setStatus (0);
            }
            taskWorker.setFromDate (hotelTask.getFromDate ( ));
            taskWorker.setToDate (hotelTask.getToDate ( ));
            taskWorker.setHourlyPay (hrTask.getHourlyPay ( ));
            taskWorker.setTaskTypeCode (hrTask.getTaskTypeCode ( ));
            taskWorker.setTaskContent (hrTask.getTaskContent ( ));
            taskWorker.setTaskTypeText (hrTask.getTaskTypeText ( ));
            taskWorker.setHrCompanyName (hrTask.getHrCompanyName ( ));
            taskWorker.setHrCompanyId (hrTask.getHrCompanyId ( ));
            taskWorker.setHotelName (hrTask.getHotelName ( ));
            taskWorker.setHotelId (hotelTask.getHotelId ( ));
            taskWorker.setDayStartTime (hotelTask.getDayStartTime ( ));
            taskWorker.setDayEndTime (hotelTask.getDayEndTime ( ));
            taskWorker.setHotelTaskId (hotelTask.getPid ( ));
            taskWorker.setHrCompanyId (hrTask.getHrCompanyId ( ));
            taskWorker.setSettlementPeriod (hrTask.getWorkerSettlementPeriod ());
            taskWorker.setSettlementNum (hrTask.getWorkerSettlementNum ());
            taskWorkerMapper.insert (taskWorker);
            m.put ("workerId", id);
            m.put ("workerTaskId", taskWorker.getPid ( ));
            m.put ("hotelId", taskWorker.getHotelId ( ));
            if(!hrTaskDis.isNoticeTask ()){
                list.add (m);
            }
        }
        hrTask.setDistributeWorkers (hrTask.getDistributeWorkers ( ) + hrTaskDis.getWorkerIds ( ).size ( ));
        taskMapper.updateById (hotelTask);
        taskHrCompanyMapper.updateById (hrTask);
        messageService.hrDistributeTask (list, hrTask.getHrCompanyId ( ), hrTask.getHrCompanyName ( ), "workTaskMessage", hotelTask.getPid ( ), hrTask.getPid ( ), false);
        //发布公告
        if(hrTask.getNeedWorkers () - hrTask.getDistributeWorkers () > 0){
            Notice notice = new Notice ();
            notice.setCreateTime (OffsetDateTime.now ());
            notice.setFromDate (hrTask.getFromDate ());
            notice.setToDate (hrTask.getToDate ());
            notice.setHotelId (hrTask.getHotelId ());
            notice.setHrCompanyId (hrTask.getHrCompanyId ());
            notice.setNeedWorkers (hrTask.getNeedWorkers ());
            notice.setType (3);
            notice.setStatus (0);
            notice.setContent (hrTask.getTaskTypeText ());
            notice.setTaskId (hrTask.getPid ());
            noticeMapper.insert (notice);
            noticeServiceMapper.insert (notice.getPid ( ), hotelTask.getTaskTypeCode ( ));
        }

        return ResultDO.buildSuccess ("分发成功");
    }

    /**
     * 分页查询人力资源公司任务
     */
    @Override
    public ResultDO getPageTasks(Paginator paginator, TaskHrQueryDTO taskHrQueryDTO) {
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize());
        String date = taskHrQueryDTO.getOfDate();
        if (date != null) {
            Integer year = Integer.parseInt(date.split("-")[0]);
            Integer month = Integer.parseInt(date.split("-")[1]);
            if (month == 12) {
                taskHrQueryDTO.setFromDate (OffsetDateTime.of
                        (year, 12, 1, 0, 0, 0, 0,
                                ZoneOffset.UTC));
                taskHrQueryDTO.setToDate (OffsetDateTime.of
                        (year, 1, 1, 0, 0, 0, 0,
                                ZoneOffset.UTC));
            } else {
                taskHrQueryDTO.setFromDate (OffsetDateTime.of
                        (year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC));
                taskHrQueryDTO.setToDate (OffsetDateTime.of
                        (year, month + 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
            }
        }
        //查询数据集合
        List <TaskHrCompany> list = taskHrCompanyMapper.queryHrCompanyTasks (taskHrQueryDTO);
        HashMap <String, Object> extra = new HashMap <> ( );
        Double shouldPayMoney = 0.0;
        Double havePayMoney = 0.0;
        Double unConfirmedPay = 0.0;
        Double workersShouldPay = 0.0;
        Double workersHavePay = 0.0;
        Double workersUnConfirmed = 0.0;
        for (TaskHrCompany task : list) {
            task.setHrCompany(companyMapper.findCompanyById(task.getHrCompanyId()));
            task.setPaidPayMoney(Maths.getTwoDecimal (task.getShouldPayMoney() - task.getHavePayMoney() - task.getUnConfirmedPay(),2));
            task.setWorkersPaidPay (Maths.getTwoDecimal (task.getWorkersShouldPay () - task.getWorkersHavePay () - task.getWorkerUnConfirmed (),2));
            shouldPayMoney = Maths.getTwoDecimal (task.getShouldPayMoney()+shouldPayMoney,2);
            havePayMoney = Maths.getTwoDecimal (task.getHavePayMoney()+havePayMoney,2);
            workersShouldPay = Maths.getTwoDecimal (task.getWorkersShouldPay()+workersShouldPay,2);
            unConfirmedPay = Maths.getTwoDecimal (task.getUnConfirmedPay ()+unConfirmedPay,2);
            workersUnConfirmed = Maths.getTwoDecimal (task.getWorkerUnConfirmed ()+workersUnConfirmed,2);
            workersHavePay = Maths.getTwoDecimal (task.getWorkersHavePay()+workersHavePay,2);
            task.setHotel (companyMapper.findCompanyById (task.getHotelId ( )));
            List <Map <String, Object>> lis = taskWorkerMapper.selectTaskWorkCById (task.getPid ( ));
            task.setListWorkerTask (lis);
            if (task.getStatus () >= 4 &&  task.getStatus () != 8) {
                if (task.getToDate ( ).isAfter (OffsetDateTime.now ( )) && task.getFromDate ( ).isBefore (OffsetDateTime.now ( ))) {
                    task.setStatus (6);
                } else if (task.getToDate ( ).isBefore (OffsetDateTime.now ( ))) {
                    task.setStatus (7);
                }
            }
        }
        PageInfo <TaskHrCompany> pageInfo = new PageInfo <> (list);
        HashMap <String, Object> result = new HashMap <> ( );
        //设置获取到的总记录数total：
        result.put ("total", pageInfo.getTotal ( ));
        //设置数据集合rows：
        result.put ("result", pageInfo.getList ( ));
        result.put ("page", paginator.getPage ( ));
        extra.put ("shouldPayMoney", shouldPayMoney);
        extra.put ("havePayMoney", havePayMoney);
        extra.put ("paidPayMoney", Maths.getTwoDecimal (shouldPayMoney - havePayMoney - unConfirmedPay, 2));
        extra.put ("workersShouldPay", workersShouldPay);
        extra.put ("workersHavePay", workersHavePay);
        extra.put ("workerPaidPay", Maths.getTwoDecimal (workersShouldPay - workersHavePay - workersUnConfirmed,2));
        return ResultDO.buildSuccess (null, result, extra, null);
    }

    /**
     * 人力公司支付小时工
     */
    @Override
    public ResultDO hrPayWorkers(PayParam PayHrParam) throws Exception {
        if (PayHrParam == null || !StringUtils.hasLength (PayHrParam.getTaskWorkerId ( )) || StringUtils.isEmpty (PayHrParam.getPayMoney ( ))) {
            throw new ParamsException ("参数错误");
        }
        TaskWorker taskWorker = taskWorkerMapper.findFirstById (PayHrParam.getTaskWorkerId ( ));
        //插入支付记录
        Bill bill = new Bill ( );
        bill.setTaskHrId (taskWorker.getTaskHrId ( ));
        bill.setHotelId (taskWorker.getHotelId ( ));
        bill.setPayMoney (PayHrParam.getPayMoney ( ));
        bill.setHrCompanyId (taskWorker.getHrCompanyId ( ));
        bill.setWorkerId (taskWorker.getWorkerId ());
        bill.setDeleted (false);
        bill.setPayType (2);
        bill.setStatus (0);
		bill.setTaskId (taskWorker.getHotelTaskId ());        billMapper.insert (bill);
        //发送支付待确认消息
        MessageTemplate mess = messageTemplateMapper.findFirstByCode ("hrPayWorkerMessage");
        Message m = new Message ( );
        m.setTaskId (taskWorker.getHotelTaskId ( ));
        m.setMessageCode ("hotelPayHrMessage");
        m.setMessageType (8);
        m.setWorkerId (taskWorker.getWorkerId ( ));
        m.setWorkerTaskId (taskWorker.getPid ( ));
        m.setMessageTitle ("人力公司支付小时工");
        m.setStatus (0);
        m.setHotelId (taskWorker.getHotelId ( ));
        m.setHrCompanyId (taskWorker.getHrCompanyId ( ));
        m.setApplicantType (2);
        m.setApplyType (1);
        m.setIsTask (0);
        m.setHrTaskId (taskWorker.getTaskHrId ( ));
        m.setMinutes (PayHrParam.getPayMoney ( )+"");
        m.setRequestId(bill.getPid());
        Map <String, String> param = new HashMap <> ( );
        param.put ("hrName", companyMapper.findCompanyById (taskWorker.getHrCompanyId ( )).getName ( ));
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
        taskWorker.setUnConfirmedPay (taskWorker.getUnConfirmedPay ( ) + PayHrParam.getPayMoney ( ));
        taskWorkerMapper.updateById (taskWorker);
        TaskHrCompany taskHr = taskHrCompanyMapper.selectById (taskWorker.getTaskHrId ( ));
        if (taskHr == null) {
            throw new Exception ("小时工任务数据异常");
        }
        taskHr.setWorkerUnConfirmed (taskHr.getWorkerUnConfirmed ( ) + PayHrParam.getPayMoney ( ));
        taskHrCompanyMapper.updateById (taskHr);
        return ResultDO.buildSuccess ("消息发送成功");
    }

    /**
     * 人力公司接受任务
     */
    @Override
    public String TaskHraccept(String messageId) {
        Message message = messageMapper.selectById (messageId);
        if (message == null || message.getStatus ( ) == 1) {
            return "消息已处理";
        }
        message.setStatus (1);
        messageMapper.updateById (message);
        TaskHrCompany taskHrCompany = taskHrCompanyMapper.queryByTaskId (message.getHrTaskId ( ));
        if (taskHrCompany.getFromDate ().isBefore (OffsetDateTime.now ( ))) {
            taskHrCompany.setRefusedReason ("任务已开始，接受任务超时");
            taskHrCompany.setStatus (3);
            taskHrCompanyMapper.updateById (taskHrCompany);
            return "任务领取超时";
        }
        Task task = taskMapper.selectById (taskHrCompany.getTaskId ( ));
        if (task == null) {
            throw new ParamsException ("查询不到不到用人单位任务");
        }
        if (task.getStatus ( ) == 1) {
            taskMapper.updateStatus (taskHrCompany.getTaskId ( ), 2);
        }
        taskHrCompanyMapper.updateStatus (message.getHrTaskId ( ), 2);

        Inform inform = new Inform ( );
        inform.setTitle ("任务已接受");
        inform.setContent (taskHrCompany.getHrCompanyName ( ) + "接受了派发的任务。");
        inform.setReceiveId (taskHrCompany.getHotelId ( ));
        inform.setAcceptType (3);
        inform.setSendType (2);
        informMapper.insertInform (inform);
        Company hotel = companyMapper.findCompanyById (taskHrCompany.getHotelId ( ));
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (hotel.getLeaderMobile (), inform.getContent ()));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {
            e.printStackTrace ( );
        }
        //生成一个待派发的消息
        Map <String, Object> param = new HashMap <> ( );
        param.put ("hrCompanyId", taskHrCompany.getHrCompanyId ( ));
        param.put ("hotelId", taskHrCompany.getHotelId ( ));
        param.put ("applicantType", 2);
        param.put ("applyType", 2);
        param.put ("hrTaskId", taskHrCompany.getPid ( ));
        param.put ("taskId", taskHrCompany.getTaskId ( ));
        param.put ("messageType", 11);
        param.put ("messageCode", "awaitSendMessage");
        messageService.sendMessageInfo (param);
        return "任务领取成功";
    }

    /**
     * 人力公司拒绝任务
     */
    @Override
    public void TaskHrrefuse(String messageId, String reason) {

        Message message = messageMapper.selectById (messageId);
        if (message == null || message.getStatus ( ) == 1) {
            return;
        }
        message.setStatus (1);
        messageMapper.updateById (message);
        TaskHrCompany taskHrCompany = taskHrCompanyMapper.queryByTaskId (message.getHrTaskId ( ));
        if (taskHrCompany == null) {
            throw new BusinessException ("查询不到人力任务数据");
        }
        taskHrCompany.setStatus (3);
        taskHrCompany.setRefusedReason (reason);
        taskHrCompanyMapper.updateById (taskHrCompany);

        //发送拒绝通知
        InformTemplate inf = informTemplateMapper.selectByCode (InformType.refuse_hotel_task.name ( ));
        Map <String, String> map = new HashMap <> ( );
        map.put ("hr", taskHrCompany.getHrCompanyName ( ));
        map.put ("reason", reason);
        String content = StringKit.templateReplace (inf.getContent ( ), map);
        informService.sendInformInfo (inf.getSendType ( ), 3, content, taskHrCompany.getHotelId ( ), inf.getTitle ( ));
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (taskHrCompany.getHotelId ( )).getLeaderMobile ( ), content));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {
            e.printStackTrace ( );
        }
        //发送拒绝消息

        Message m = new Message ( );
        m.setContent (reason);
        MessageTemplate mess = messageTemplateMapper.findFirstByCode ("refuseTaskMessage");
        m.setMessageCode (mess.getCode ( ));
        m.setMessageType (10);
        m.setMessageTitle (mess.getTitle ( ));
        m.setHrCompanyId (taskHrCompany.getHrCompanyId ( ));
        m.setHrTaskId (taskHrCompany.getPid ( ));
        m.setHotelId (taskHrCompany.getHotelId ( ));
        m.setTaskId (taskHrCompany.getTaskId ( ));
        Map <String, String> param = new HashMap <> ( );
        param.put ("userName", taskHrCompany.getHrCompanyName ( ));
        param.put ("content", reason);
        String c = StringKit.templateReplace (mess.getContent ( ), param);
        m.setMessageContent (c);
        m.setApplyType (3);
        m.setApplicantType (2);
        m.setStatus (0);
        m.setIsTask (0);
        messageMapper.insert (m);

    }

    /**
     * 用人单位查询账目
     */
    @Override
    public ResultDO getHotelBill(Paginator paginator, BillRequest request) {
        PageHelper.startPage (paginator.getPage ( ), paginator.getPageSize ( ));
        //查询数据集合
        List <TaskHrCompany> list = taskHrCompanyMapper.queryHotelBill (request);
        for (TaskHrCompany taskHr : list) {
            taskHr.setPaidPayMoney(Maths.getTwoDecimal (taskHr.getShouldPayMoney() - taskHr.getHavePayMoney() - taskHr.getUnConfirmedPay (),2));
            taskHr.setWorkersPaidPay (Maths.getTwoDecimal (taskHr.getWorkersShouldPay () - taskHr.getWorkersHavePay () - taskHr.getWorkerUnConfirmed (),2));
        }
        PageInfo <TaskHrCompany> pageInfo = new PageInfo <> (list);
        HashMap <String, Object> result = new HashMap <> ( );
        //设置获取到的总记录数total：
        result.put ("total", pageInfo.getTotal ( ));
        //设置数据集合rows：
        result.put ("result", pageInfo.getList ( ));
        result.put ("page", paginator.getPage ( ));
        Map <String, Object> map = new HashMap <> ( );
        Double should_pay_money = 0.0;
        Double have_pay_money = 0.0;
        Double un_confirmed_pay = 0.0;
        Double workers_should_pay = 0.0;
        Double workers_have_pay = 0.0;
        Double worker_un_confirmed = 0.0;
        for (TaskHrCompany item : list) {
            should_pay_money = Maths.add (should_pay_money, item.getShouldPayMoney ( ));
            have_pay_money = Maths.add (item.getHavePayMoney ( ), have_pay_money);
            un_confirmed_pay = Maths.add (item.getUnConfirmedPay (), un_confirmed_pay);
            workers_should_pay = Maths.add (item.getWorkersShouldPay (), workers_should_pay);
            workers_have_pay = Maths.add (item.getWorkersHavePay (), workers_have_pay);
            worker_un_confirmed = Maths.add (item.getWorkerUnConfirmed (), worker_un_confirmed);
            item.setHrCompany (companyMapper.selectById (item.getHrCompanyId ( )));
        }
        map.put ("shouldPayMoney", should_pay_money);
        map.put ("havePayMoney", have_pay_money);
        map.put ("workersShouldPay", workers_should_pay);
        map.put ("workersHavePay", workers_have_pay);
        map.put ("paidPayMoney", Maths.getTwoDecimal (should_pay_money - have_pay_money - un_confirmed_pay,2));
        map.put ("workerPaidPay", Maths.getTwoDecimal (workers_should_pay - workers_have_pay - worker_un_confirmed,2));
        return ResultDO.buildSuccess (null, result, map, null);
    }

    @Override
    public ResultDO getHotelBillWorker(Paginator paginator, BillRequest request) {
        PageHelper.startPage (paginator.getPage ( ), paginator.getPageSize ( ));
        //查询数据集合
        List <TaskWorker> list = taskHrCompanyMapper.queryHotelBillWorker (request);
        for (TaskWorker taskHr : list) {
            taskHr.setPaidPayMoney(Maths.getTwoDecimal (taskHr.getShouldPayMoney() - taskHr.getHavePayMoney() - taskHr.getUnConfirmedPay (),2));
        }
        PageInfo <TaskWorker> pageInfo = new PageInfo <> (list);
        HashMap <String, Object> result = new HashMap <> ( );
        //设置获取到的总记录数total：
        result.put ("total", pageInfo.getTotal ( ));
        //设置数据集合rows：
        result.put ("result", pageInfo.getList ( ));
        result.put ("page", paginator.getPage ( ));
        Map <String, Object> map = new HashMap <> ( );
        Double should_pay_money = 0.0;
        Double have_pay_money = 0.0;
        Double un_confirmed_pay = 0.0;
        for (TaskWorker item : list) {
            should_pay_money = Maths.add (should_pay_money, item.getShouldPayMoney ( ));
            have_pay_money = Maths.add (item.getHavePayMoney ( ), have_pay_money);
            un_confirmed_pay = Maths.add (item.getUnConfirmedPay (), un_confirmed_pay);
            item.setUser (userMapper.selectByWorkerId (item.getWorkerId ()));
        }
        map.put ("shouldPayMoney", should_pay_money);
        map.put ("havePayMoney", have_pay_money);
        map.put ("paidPayMoney", Maths.getTwoDecimal (should_pay_money - have_pay_money - un_confirmed_pay,2));
        return ResultDO.buildSuccess (null, result, map, null);
    }

    /**
     * 人力公司按用人单位查询账目
     */
    @Override
    public ResultDO getCompanyBillHotel(Paginator paginator, BillRequest request) {
        PageHelper.startPage (paginator.getPage ( ), paginator.getPageSize ( ));
        //查询数据集合
        List <TaskHrCompany> list = taskHrCompanyMapper.queryHrCompanyBill (request);
        Map <String, Object> map = new HashMap <> ( );
        Double should_pay_money = 0.0;
        Double have_pay_money = 0.0;
        Double Un_confirmed_pay = 0.0;
        Double workers_should_pay = 0.0;
        Double workers_have_pay = 0.0;
        Double worker_un_confirmed = 0.0;
        for (TaskHrCompany item : list) {
            should_pay_money = Maths.add (should_pay_money, item.getShouldPayMoney ( ));
            have_pay_money = Maths.add (item.getHavePayMoney ( ), have_pay_money);
            Un_confirmed_pay = Maths.add (item.getUnConfirmedPay (), Un_confirmed_pay);
            workers_should_pay = Maths.add (item.getWorkersShouldPay (), workers_should_pay);
            workers_have_pay = Maths.add (item.getWorkersHavePay (), workers_have_pay);
            worker_un_confirmed = Maths.add (item.getWorkerUnConfirmed (), worker_un_confirmed);
            System.out.println ("horelID:" + item.getHotelId ( ));
            item.setHotel (companyMapper.selectById (item.getHotelId ( )));
        }
        map.put ("shouldPayMoney", should_pay_money);
        map.put ("havePayMoney", have_pay_money);
        map.put ("workersShouldPay", workers_should_pay);
        map.put ("workersHavePay", workers_have_pay);
        map.put ("paidPayMoney", Maths.getTwoDecimal (should_pay_money - have_pay_money - Un_confirmed_pay,2));
        map.put ("workerUnConfirmed",Maths.getTwoDecimal (workers_should_pay - workers_have_pay - worker_un_confirmed,2));
        PageInfo <TaskHrCompany> pageInfo = new PageInfo <> (list);
        HashMap <String, Object> result = new HashMap <> ( );
        //设置获取到的总记录数total：
        result.put ("total", pageInfo.getTotal ( ));
        //设置数据集合rows：
        result.put ("result", pageInfo.getList ( ));
        result.put ("page", paginator.getPage ( ));
        return ResultDO.buildSuccess (null, result, map, null);
    }

    /**
     * 人力公司按小时工查询账目
     */
    @Override
    public ResultDO getCompanyBillWorker(Paginator paginator, BillRequest request) {
        PageHelper.startPage (paginator.getPage ( ), paginator.getPageSize ( ));
        //查询数据集合
        List <TaskWorker> list = taskWorkerMapper.queryHrCompanyBill (request);
        PageInfo <TaskWorker> pageInfo = new PageInfo <> (list);
        HashMap <String, Object> result = new HashMap <> ( );
        //设置获取到的总记录数total：
        result.put ("total", pageInfo.getTotal ( ));
        //设置数据集合rows：
        result.put ("result", pageInfo.getList ( ));
        result.put ("page", paginator.getPage ( ));
        Map <String, Object> map = new HashMap <> ( );
        Double should_pay_money = 0.0;
        Double have_pay_money = 0.0;
        Double Un_confirmed_pay = 0.0;
        for (TaskWorker item : list) {
            should_pay_money = Maths.add (should_pay_money, item.getShouldPayMoney ( ));
            have_pay_money = Maths.add (item.getHavePayMoney ( ), have_pay_money);
            Un_confirmed_pay = Maths.add (item.getUnConfirmedPay (), Un_confirmed_pay);
            item.setUser (userMapper.queryByUserId (item.getUserId ( )));
        }
        map.put ("shouldPayMoney", should_pay_money);
        map.put ("havePayMoney", have_pay_money);
        map.put ("paidPayMoney", Maths.getTwoDecimal (should_pay_money - have_pay_money - Un_confirmed_pay,2));
        return ResultDO.buildSuccess (null, result, map, null);
    }

    /**
     * 小时工按人力公司工查询账目
     */
    @Override
    public ResultDO getWorkerBill(Paginator paginator, BillRequest request) {
        PageHelper.startPage (paginator.getPage ( ), paginator.getPageSize ( ));
        //查询数据集合
        List <TaskHrCompany> list = taskWorkerMapper.queryWorkerBill (request);
        PageInfo <TaskHrCompany> pageInfo = new PageInfo <> (list);
        HashMap <String, Object> result = new HashMap <> ( );
        //设置获取到的总记录数total：
        result.put ("total", pageInfo.getTotal ( ));
        //设置数据集合rows：
        result.put ("result", pageInfo.getList ( ));
        result.put ("page", paginator.getPage ( ));
        Map <String, Object> map = new HashMap <> ( );
        Double should_pay_money = 0.0;
        Double have_pay_money = 0.0;
        Double Un_confirmed_pay = 0.0;
        for (TaskHrCompany item : list) {
            should_pay_money = Maths.add (should_pay_money, item.getShouldPayMoney ( ));
            have_pay_money = Maths.add (item.getHavePayMoney ( ), have_pay_money);
            Un_confirmed_pay = Maths.add (item.getUnConfirmedPay (), Un_confirmed_pay);
            item.setHrCompany (companyMapper.selectById (item.getHrCompanyId ( )));
        }
        map.put ("shouldPayMoney", should_pay_money);
        map.put ("havePayMoney", have_pay_money);
        map.put ("unConfirmed", Un_confirmed_pay);
        map.put ("paidPayMoney", Maths.getTwoDecimal (should_pay_money - have_pay_money - Un_confirmed_pay,2));
        return ResultDO.buildSuccess (null, result, map, null);
    }

    @Override
    public ResultDO getWorkerBillHotel(Paginator paginator, BillRequest request) {
        PageHelper.startPage (paginator.getPage ( ), paginator.getPageSize ( ));
        //查询数据集合
        List <TaskWorker> list = taskWorkerMapper.queryWorkerBillHotel (request);
        PageInfo <TaskWorker> pageInfo = new PageInfo <> (list);
        HashMap <String, Object> result = new HashMap <> ( );
        //设置获取到的总记录数total：
        result.put ("total", pageInfo.getTotal ( ));
        //设置数据集合rows：
        result.put ("result", pageInfo.getList ( ));
        result.put ("page", paginator.getPage ( ));
        Map <String, Object> map = new HashMap <> ( );
        Double should_pay_money = 0.0;
        Double have_pay_money = 0.0;
        Double Un_confirmed_pay = 0.0;
        for (TaskWorker item : list) {
            should_pay_money = Maths.add (should_pay_money, item.getShouldPayMoney ( ));
            have_pay_money = Maths.add (item.getHavePayMoney ( ), have_pay_money);
            Un_confirmed_pay = Maths.add (item.getUnConfirmedPay (), Un_confirmed_pay);
            item.setHrCompany (companyMapper.selectById (item.getHrCompanyId ( )));
            item.setHotel (companyMapper.selectById (item.getHotelId ()));
        }
        map.put ("shouldPayMoney", should_pay_money);
        map.put ("havePayMoney", have_pay_money);
        map.put ("UnConfirmedPay", Un_confirmed_pay);
        map.put ("paidPayMoney", Maths.getTwoDecimal (should_pay_money - have_pay_money - Un_confirmed_pay,2));
        return ResultDO.buildSuccess (null, result, map, null);
    }

    /**
     * 人力公司申请调配.
     */
    @Override
    public ResultDO hrApplyChangeWorker(Map <String, Object> map) {
        if (StringUtils.isEmpty (map.get ("hotelId"))) {
            throw new ParamsException ("hotelId不能为空");
        }
        if (StringUtils.isEmpty (map.get ("hrCompanyId"))) {
            throw new ParamsException ("hrCompanyId不能为空");
        }
        if (StringUtils.isEmpty (map.get ("reason"))) {
            throw new ParamsException ("reason不能为空");
        }
        if (StringUtils.isEmpty (map.get ("hrTaskId"))) {
            throw new ParamsException ("hrTaskId不能为空");
        }
        Message m = new Message ( );
        m.setContent ((String) map.get ("reason"));
        Company company = companyMapper.selectById (map.get ("hrCompanyId").toString ( ));
        MessageTemplate mess = messageTemplateMapper.findFirstByCode ("applyChangeMessage");
        m.setMessageCode (mess.getCode ( ));
        m.setMessageTitle (mess.getTitle ( ));
        m.setHotelId ((String) map.get ("hotelId"));
        m.setHrCompanyId ((String) map.get ("hrCompanyId"));
        m.setHrTaskId ((String) map.get ("hrTaskId"));
        Map <String, String> param = new HashMap <> ( );
        param.put ("hrCompanyName", company.getName ( ));
        param.put ("reason", (String) map.get ("reason"));
        param.put ("number", (String) map.get ("number"));
        String c = StringKit.templateReplace (mess.getContent ( ), param);

        m.setMessageContent (c);
        m.setMessageType (4);
        m.setApplicantType (2);
        m.setApplyType (3);
        m.setMinutes ((String) map.get ("number"));
        m.setStatus (0);
        m.setIsTask (0);
        m.setHotelId ((String) map.get ("hotelId"));
        m.setHrCompanyId ((String) map.get ("hrCompanyId"));
        m.setTaskId (taskHrCompanyMapper.queryByTaskId ((String) map.get ("hrTaskId")).getTaskId ( ));
        Map map1 = new HashMap <String, Object> ( );
        map1.put ("message_type", 4);
        map1.put ("hr_task_id", (String) map.get ("hrTaskId"));
        map1.put ("status", 0);
        if (messageMapper.selectByMap (map1).size ( ) > 0) {
            return ResultDO.buildSuccess ("你已提交过申请");
        }
        messageMapper.insert (m);
        return ResultDO.buildSuccess ("操作成功");
    }

    /**
     * PC端人力接受任务
     *
     * @param id
     * @return
     */
    @Override
    public String taskHracceptPC(String id) {
        if (StringUtils.isEmpty (id)) {
            throw new ParamsException ("参数不能为空");
        }
        Message message = messageMapper.selectByHrId (id);
        if (message == null || message.getStatus() == 1) {
            return "已处理";
        }
        messageMapper.updateStatus (message.getPid ( ));
        TaskHrCompany taskHrCompany = taskHrCompanyMapper.queryByTaskId (id);
        taskMapper.updateStatus (taskHrCompany.getTaskId ( ), 2);
        taskHrCompanyMapper.updateStatus (id, 2);
        Inform inform = new Inform ( );
        inform.setTitle ("任务已接受");
        inform.setContent (taskHrCompany.getHrCompanyName ( ) + "接受了派发的任务。");
        inform.setReceiveId (taskHrCompany.getHotelId ( ));
        inform.setAcceptType (3);
        inform.setSendType (2);
        informMapper.insertInform (inform);
        return "操作成功";
    }

    /**
     * PC端人力拒绝任务
     *
     * @param id
     * @return
     */
    @Override
    public String TaskHrrefusePC(String id, String reason) {

        if (StringUtils.isEmpty (id)) {
            throw new ParamsException ("参数不能为空");
        }
        Message message = messageMapper.selectByHrId (id);
        if (message == null || message.getStatus ( ) == 1) {
            return  "消息已处理";
        }
        messageMapper.updateStatus (message.getPid ( ));
        TaskHrCompany taskHrCompany = taskHrCompanyMapper.queryByTaskId (id);
        if (taskHrCompany == null) {
            throw new BusinessException ("查询不到人力任务数据");
        }
        taskHrCompany.setRefusedReason (reason);
        taskHrCompany.setStatus (3);
        taskHrCompanyMapper.updateById (taskHrCompany);
        //taskMapper.updateStatus (taskHrCompanyMapper.queryByTaskId (id).getTaskId (),8);

        //发送拒绝通知
        InformTemplate inf = informTemplateMapper.selectByCode (InformType.refuse_hotel_task.name ( ));
        Map <String, String> map = new HashMap <> ( );
        map.put ("hr", taskHrCompany.getHrCompanyName ( ));
        map.put ("reason", reason);
        String content = StringKit.templateReplace (inf.getContent ( ), map);
        informService.sendInformInfo (inf.getSendType ( ), 3, content, taskHrCompany.getHotelId ( ), inf.getTitle ( ));
        //发送拒绝消息
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (taskHrCompany.getHotelId ( )).getLeaderMobile ( ), content));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {
            e.printStackTrace ( );
        }
        Message m = new Message ( );
        m.setContent (reason);
        MessageTemplate mess = messageTemplateMapper.findFirstByCode ("refuseTaskMessage");
        m.setMessageCode (mess.getCode ( ));
        m.setMessageType (10);
        m.setMessageTitle (mess.getTitle ( ));
        m.setHrCompanyId (taskHrCompany.getHrCompanyId ( ));
        m.setHrTaskId (taskHrCompany.getPid ( ));
        m.setHotelId (taskHrCompany.getHotelId ( ));
        Map <String, String> param = new HashMap <> ( );
        param.put ("userName", taskHrCompany.getHrCompanyName ( ));
        param.put ("content", reason);
        String c = StringKit.templateReplace (mess.getContent ( ), param);
        m.setMessageContent (c);
        m.setApplicantType (2);
        m.setApplyType (3);
        m.setStatus (0);
        m.setIsTask (0);
        messageMapper.insert (m);

        return "操作成功";
    }

    /**
     * 人力再派发任务
     *
     * @param request 包含id(消息id,set小时工id集合)
     * @return
     */
    @Override
    public ResultDO hrAssignmentTask(AssignmentRequest request) {

        if (request == null || StringUtils.isEmpty (request.getMessageId ( )) || request.getSet ( ) == null
                || request.getSet ( ).size ( ) == 0) {
            throw new ParamsException ("参数错误");
        }

        Message message = messageMapper.selectById (request.getMessageId ( ));
        if (message == null) {
            throw new BusinessException ("查询不到消息");
        }
        message.setStatus (1);
        messageMapper.updateById (message);
        //消息发送者是用人单位，将小时工任务状态设置为3终止，如果是小时工，将状态置为2
        TaskWorker taskWorker = taskWorkerMapper.selectById (message.getWorkerTaskId ( ));
        if (taskWorker == null) {
            throw new BusinessException ("查询不到小时工工作任务");
        }
        if (message.getApplicantType ( ) == 3) {
            taskWorker.setStatus (3);
        } else if (message.getApplicantType ( ) == 1) {
            taskWorker.setStatus (2);
        } else {
            throw new ParamsException ("参数错误");
        }
        taskWorker.setRefusedReason (message.getContent ( ));
        taskWorkerMapper.updateAllColumnById (taskWorker);

        //更新人力任务信息
        TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById (message.getHrTaskId ( ));
        if (taskHrCompany == null) {
            throw new BusinessException ("查询不到人力公司任务");
        }
        taskHrCompany.setConfirmedWorkers (taskHrCompany.getConfirmedWorkers ( ) - 1);
        Task task = taskMapper.getFirstById (message.getTaskId ( ));
        task.setConfirmedWorkers (task.getConfirmedWorkers ( ) - 1);
        taskMapper.updateById (task);
        if (message.getApplicantType ( ) == 1) {
            taskHrCompany.setRefusedWorkers (taskHrCompany.getRefusedWorkers ( ) + 1);
        }
        taskHrCompanyMapper.updateAllColumnById (taskHrCompany);

        //插入小时工任务信息
        TaskWorker workerTask = null;
        boolean flag = true;
        List <TaskWorker> list = new ArrayList <> ( );
        for (String str : request.getSet ( )) {
            workerTask = new TaskWorker ( );
            workerTask.setStatus (0);
            workerTask.setDayEndTime (taskWorker.getDayEndTime ( ));
            workerTask.setDayStartTime (taskWorker.getDayStartTime ( ));
            workerTask.setUserName (userMapper.queryByWorkerId (str).getUsername ());
            workerTask.setToDate (taskWorker.getToDate ( ));
            if(task.getFromDate ().isAfter (OffsetDateTime.now ())){
                workerTask.setFromDate (taskWorker.getFromDate ());
                flag = false;
            }else{
                workerTask.setFromDate (taskWorker.getToDate ( ));
            }
            workerTask.setHotelName (taskHrCompany.getHotelName ( ));
            workerTask.setHourlyPay (taskHrCompany.getHourlyPay ( ));
            workerTask.setTaskContent (taskHrCompany.getTaskContent ( ));
            workerTask.setTaskTypeCode (taskHrCompany.getTaskTypeCode ( ));
            workerTask.setTaskTypeText (taskHrCompany.getTaskTypeText ( ));
            workerTask.setTaskHrId (taskHrCompany.getPid ( ));
            workerTask.setUserId (userMapper.selectByWorkerId (str).getPid ( ));
            workerTask.setHrCompanyId (taskHrCompany.getHrCompanyId ( ));
            workerTask.setHrCompanyName (taskHrCompany.getHrCompanyName ());
            workerTask.setHotelId (taskHrCompany.getHotelId ( ));
            workerTask.setHotelTaskId (taskHrCompany.getTaskId ( ));
            workerTask.setWorkerId (str);
            workerTask.setSettlementPeriod (taskHrCompany.getWorkerSettlementPeriod ());
            workerTask.setSettlementNum (taskHrCompany.getWorkerSettlementNum ());
            taskWorkerMapper.insert (workerTask);
            list.add (workerTask);
        }


        //给小时工发送消息
        Message ms = messageService.hrDistributeWorkerTask (list, taskHrCompany, flag);
        if (message.getApplicantType ( ) == 3) {
            if(flag){
                RefusedTaskRequest ref = new RefusedTaskRequest ( );
                ref.setRefusedReason ("小时工未在规定时间内领取任务，请重新派发");
                ref.setMessageId (ms.getPid ( ));
                ref.setWorkerTaskId (workerTask.getPid ( ));
                myTimeTask.setRefusedReq (ref);
                java.util.Timer timer = new Timer (true);
                Field field;
                try {
                    field = TimerTask.class.getDeclaredField ("state");
                    field.setAccessible (true);
                    field.set (myTimeTask, 0);
                } catch (NoSuchFieldException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace ( );
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace ( );
                }
                timer.schedule (myTimeTask, 86400 * 1000);
            }
            //给用人单位发送通知
            //被替换的小时工
            User oldUser = userMapper.selectByWorkerId (taskWorker.getWorkerId ( ));
            taskWorkerMapper.updateStatus (taskWorker.getWorkerId ( ), 3);
            User newUser = userMapper.selectByWorkerId (list.get (0).getWorkerId ( ));
            String content = taskHrCompany.getHrCompanyName ( ) + "同意了你的换人申请，将任务里的小时工" + oldUser.getNickname ( ) + "换成了" + newUser.getNickname ( );
            informService.sendInformInfo (2, 3, content, message.getHotelId ( ), "换人成功");
            try {
                jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (taskHrCompany.getHotelId ( )).getLeaderMobile ( ), content));
            } catch (APIConnectionException e) {
                e.printStackTrace ( );
            } catch (APIRequestException e) {
                e.printStackTrace ( );
            }
            //给被替换的小时工发通知
            content = message.getMessageContent ( ) + " 。任务重新调配了一个小时工，希望你下次能认真对待工作，这会影响你的信用。";
            informService.sendInformInfo (2, 1, content, message.getWorkerId ( ), "调换通知");
            try {
                jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (message.getWorkerId ( )).getMobile ( ), content));
            } catch (APIConnectionException e) {
                e.printStackTrace ( );
            } catch (APIRequestException e) {

            }

        }
        return ResultDO.buildSuccess ("派发完成");
    }

        @Override
        public ResultDO exchangeWorker (String taskWorkerId, String workerId){
            TaskWorker taskWorker = taskWorkerMapper.selectById (taskWorkerId);
            if (taskWorker == null) {
                throw new BusinessException ("查询不到小时工工作任务");
            }
            if(taskWorker.getStatus () == 3){
                return ResultDO.buildSuccess ("已替换成功");
            }
            taskWorker.setStatus (3);
            taskWorker.setRefusedReason ("小时工有事不能工作，另换小时工接替工作");
            taskWorkerMapper.updateAllColumnById (taskWorker);

            //更新人力任务信息
            TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById (taskWorker.getTaskHrId ( ));
            if (taskHrCompany == null) {
                throw new BusinessException ("查询不到人力公司任务");
            }
            taskHrCompany.setConfirmedWorkers (taskHrCompany.getConfirmedWorkers ( ) - 1);
            taskHrCompany.setRefusedWorkers (taskHrCompany.getRefusedWorkers ( ) + 1);
            Task task = taskMapper.getFirstById (taskHrCompany.getTaskId ( ));
            task.setConfirmedWorkers (task.getConfirmedWorkers ( ) - 1);
            taskMapper.updateById (task);
            taskHrCompanyMapper.updateAllColumnById (taskHrCompany);

            //插入小时工任务信息
            TaskWorker workerTask = null;

            workerTask = new TaskWorker ( );
            workerTask.setStatus (0);
            workerTask.setDayEndTime (taskWorker.getDayEndTime ( ));
            workerTask.setDayStartTime (taskWorker.getDayStartTime ( ));
            workerTask.setToDate (taskWorker.getToDate ( ));
            workerTask.setFromDate (taskWorker.getToDate ( ));
            workerTask.setHotelName (taskHrCompany.getHotelName ( ));
            workerTask.setHourlyPay (taskHrCompany.getHourlyPay ( ));
            workerTask.setTaskContent (taskHrCompany.getTaskContent ( ));
            workerTask.setTaskTypeCode (taskHrCompany.getTaskTypeCode ( ));
            workerTask.setTaskTypeText (taskHrCompany.getTaskTypeText ( ));
            workerTask.setTaskHrId (taskHrCompany.getPid ( ));
            workerTask.setWorkerId (workerId);
            workerTask.setTaskHrId (taskHrCompany.getPid ( ));
            workerTask.setHotelTaskId (taskHrCompany.getTaskId ( ));
            workerTask.setHrCompanyId (taskHrCompany.getHrCompanyId ( ));
            workerTask.setHotelId (taskHrCompany.getHotelId ( ));
            workerTask.setUserId (userMapper.selectByWorkerId (workerId).getPid ( ));
            workerTask.setSettlementPeriod (taskHrCompany.getWorkerSettlementPeriod ());
            workerTask.setSettlementNum (taskHrCompany.getWorkerSettlementNum ());
            taskWorkerMapper.insert (workerTask);

            List <TaskWorker> list = new ArrayList <TaskWorker> ( );
            list.add (workerTask);
            //给小时工发送消息
            RefusedTaskRequest ref = new RefusedTaskRequest ( );
            ref.setRefusedReason ("小时工未在规定时间内领取任务，请重新派发");
            ref.setMessageId (messageService.hrDistributeWorkerTask (list, taskHrCompany, true).getPid ( ));
            ref.setWorkerTaskId ("");
            myTimeTask.setRefusedReq (ref);
            java.util.Timer timer = new Timer (true);
            Field field;
            try {
                field = TimerTask.class.getDeclaredField ("state");
                field.setAccessible (true);
                field.set (myTimeTask, 0);
            } catch (NoSuchFieldException e) {
                // TODO Auto-generated catch block
                e.printStackTrace ( );
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace ( );
            }
            timer.schedule (myTimeTask, 86400 * 1000);

            User oldUser = userMapper.selectByWorkerId (taskWorker.getWorkerId ( ));
            taskWorkerMapper.updateStatus (taskWorker.getWorkerId ( ), 3);
            User newUser = userMapper.selectByWorkerId (list.get (0).getWorkerId ( ));
            String content = companyMapper.findCompanyById (taskHrCompany.getHrCompanyId ( )).getName ( ) + "人力公司终止了您在" + companyMapper.findCompanyById (task.getHotelId ( )).getName ( ) + "的任务，如有疑问请咨询相关人力公司";
            informService.sendInformInfo (2, 1, content, taskWorker.getWorkerId (), "任务被终止");
            try {
                jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (taskWorker.getWorkerId ()).getMobile ( ), content));
            } catch (APIConnectionException e) {
                e.printStackTrace ( );
            } catch (APIRequestException e) {

            }

            return ResultDO.buildSuccess ("操作完成");
        }

        /**
         * 人力拒绝用人单位调换小时工的申请
         *
         * @param messageId
         * @return
         */
            @Override
            public ResultDO hrRefuseHotelSwapWorker (String messageId){
                if (StringUtils.isEmpty (messageId)) {
                    throw new ParamsException ("参数错误");
                }
                Message message = messageMapper.selectById (messageId);
                if (message == null || message.getStatus ( ) == 1) {
                    return ResultDO.buildError ("已处理");
                }
                message.setStatus (1);
                messageMapper.updateById (message);

                //给用人单位发送一条通知
                Company company = companyMapper.findCompanyById (message.getHrCompanyId ( ));
                if (company == null) {
                    throw new BusinessException ("人力公司查询不到");
                }
                String content = company.getName ( ) + "拒绝了你的换人申请。";
                informService.sendInformInfo (2, 3, content, message.getHotelId ( ), "换人被拒绝");
                try {
                    jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (message.getHotelId ( )).getLeaderMobile ( ), content));
                } catch (APIConnectionException e) {
                    e.printStackTrace ( );
                } catch (APIRequestException e) {

                }
                return ResultDO.buildSuccess ("操作成功");
            }

    /**
     * 人力拒绝小时工取消任务
     *
     * @param messageId
     * @return
     */
    @Override
    public ResultDO hrHandleWorkerTaskCancel(String messageId) {
        if (StringUtils.isEmpty(messageId)) {
            throw new ParamsException("参数错误");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            return ResultDO.buildError ("已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        TaskWorker taskWorker = taskWorkerMapper.selectById(message.getWorkerTaskId());
        if (taskWorker == null) {
            throw new ParamsException("参数错误");
        }

            String content = taskWorker.getHrCompanyName ( ) + "拒绝了你的取消任务申请，希望你能完成该任务。";
            informService.sendInformInfo (2, 1, content, message.getWorkerId ( ), "申请取消被拒绝");
            try {
                jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (message.getWorkerId ( )).getMobile ( ), content));
            } catch (APIConnectionException e) {
                e.printStackTrace ( );
            } catch (APIRequestException e) {

            }
            return ResultDO.buildSuccess ("操作成功");
        }

    /**
     * 人力同意小时工取消任务并派发新任务
     *
     * @param messageId
     * @param workerId
     * @return
     */
    @Override
    public ResultDO hrAgrreWorkerTaskCancel(String messageId, String workerId) {
        if (StringUtils.isEmpty(messageId) || StringUtils.isEmpty(workerId)) {
            throw new ParamsException("参数错误");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            return ResultDO.buildError ("已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);

        TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById (message.getHrTaskId ( ));
        if (taskHrCompany == null) {
            throw new ParamsException ("人力任务查询不到.");
        }
        //更新人力任务
        taskHrCompany.setRefusedWorkers (taskHrCompany.getRefusedWorkers ( ) + 1);
        taskHrCompany.setConfirmedWorkers (taskHrCompany.getConfirmedWorkers ( ) - 1);
        if (taskHrCompany.getStatus ( ) == 5) {
            taskHrCompany.setStatus (4);
        }
        taskHrCompanyMapper.updateAllColumnById (taskHrCompany);

        //更新用人单位任务的已确认小时工人数和拒绝小时工人数
        Task task = taskMapper.selectById (taskHrCompany.getTaskId ( ));
        if (task == null) {
            throw new ParamsException ("查询不到用人单位任务");
        }
        task.setConfirmedWorkers (task.getConfirmedWorkers ( ) - 1);
        task.setRefusedWorkers (task.getRefusedWorkers ( ) + 1);
        if (task.getStatus ( ) == 4) {
            task.setStatus (3);
        }
        taskMapper.updateAllColumnById (task);
        //更新小时工任务
        TaskWorker taskWorker = taskWorkerMapper.selectById (message.getWorkerTaskId ( ));
        if (taskWorker == null) {
            throw new ParamsException ("小时工任务查询不到");
        }
        taskWorker.setStatus (2);
        taskWorker.setRefusedReason (message.getContent ( ));
        taskWorkerMapper.updateById (taskWorker);

        //给取消任务的小时工发送通知

        String content = taskWorker.getHrCompanyName ( ) + "同意了你的取消任务申请。";
        informService.sendInformInfo (2, 1, content, message.getWorkerId ( ), "申请取消成功");
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (message.getWorkerId ( )).getMobile ( ), content));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {

        }
        if (task.getFromDate ( ).isBefore (OffsetDateTime.now ( ))) {
            return ResultDO.buildSuccess ("任务已执行，派发失败");
        }
        //给新的小时工派发任务
        TaskWorker workerTask = new TaskWorker ( );
        workerTask.setStatus (0);
        workerTask.setDayEndTime (taskWorker.getDayEndTime ( ));
        workerTask.setDayStartTime (taskWorker.getDayStartTime ( ));
        workerTask.setToDate (taskWorker.getToDate ( ));
        workerTask.setFromDate (taskWorker.getFromDate ( ));
        workerTask.setHotelName (taskHrCompany.getHotelName ( ));
        workerTask.setHourlyPay (taskHrCompany.getHourlyPay ( ));
        workerTask.setTaskContent (taskHrCompany.getTaskContent ( ));
        workerTask.setTaskTypeCode (taskHrCompany.getTaskTypeCode ( ));
        workerTask.setTaskTypeText (taskHrCompany.getTaskTypeText ( ));
        workerTask.setTaskHrId (taskHrCompany.getPid ( ));
        workerTask.setWorkerId (workerId);
        User us = userMapper.selectByWorkerId (workerId);
        workerTask.setUserId (us.getPid ( ));
        workerTask.setUserName (us.getNickname ( ));
        workerTask.setHrCompanyName (taskHrCompany.getHrCompanyName ( ));
        workerTask.setHotelId (taskHrCompany.getHotelId ( ));
        workerTask.setHrCompanyId (taskHrCompany.getHrCompanyId ( ));
        workerTask.setHotelTaskId (taskHrCompany.getTaskId ( ));
        workerTask.setSettlementPeriod (taskHrCompany.getWorkerSettlementPeriod ());
        workerTask.setSettlementNum (taskHrCompany.getWorkerSettlementNum ());
        taskWorkerMapper.insert (workerTask);
        //发送消息
        Map <String, String> param = new HashMap <> ( );
        param.put ("hrCompanyName", taskWorker.getHrCompanyName ( ));
        String notice = messageService.installContent (param, "workTaskMessage");
        Map <String, Object> result = new HashMap <> ( );
        result.put ("hrCompanyId", taskHrCompany.getHrCompanyId ( ));
        result.put ("taskHrId", taskHrCompany.getTaskId ( ));
        result.put ("workerId", workerId);
        result.put ("workerTaskId", workerTask.getPid ( ));
        result.put ("hotelId", taskHrCompany.getHotelId ( ));
        result.put ("applicantType", 2);
        result.put ("applyType", 0);
        result.put ("messageContent", notice);
        result.put ("messageType", 6);
        result.put ("workTaskMessage", "workTaskMessage");
        result.put ("messageTitle", "人力公司派发任务通知书");
        result.put ("taskId", taskHrCompany.getTaskId ( ));
        result.put ("hrTaskId", taskHrCompany.getPid ( ));
        messageService.sendMessageInfo (result);
        return ResultDO.buildSuccess ("操作成功");
        }

    /**
     * 人力公司处理用人单位支付
     *
     * @param messageId
     * @param status    0拒绝1同意
     * @return
     */
    @Override
    public ResultDO hrHandleIncome(String messageId, String status) {
        if (StringUtils.isEmpty(messageId) || StringUtils.isEmpty(status)) {
            throw new ParamsException("参数不能为空");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new ParamsException("已处理");
        }
        message.setStatus(1);
        messageMapper.updateAllColumnById(message);

            TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById (message.getHrTaskId ( ));
            if (taskHrCompany == null) {
                throw new ParamsException ("查询不到人力任务信息");
            }
            Bill bill = billMapper.selectById (message.getRequestId ( ));
            if (bill == null) {
                return ResultDO.buildError ("未查到相关支付记录");
            }
            if ("0".equals (status)) {
                String content = taskHrCompany.getHrCompanyName ( ) + "拒绝了你发起的一笔支付信息，金额为" + message.getMinutes ( ) + "，拒绝理由为" + (message.getContent() == null ? "" : message.getContent());
                taskHrCompany.setUnConfirmedPay (taskHrCompany.getUnConfirmedPay ( ) - bill.getPayMoney ( ));
                taskHrCompanyMapper.updateAllColumnById (taskHrCompany);
                //确认收入
                Task task = taskMapper.selectById (taskHrCompany.getTaskId ( ));
                if (task == null) {
                    throw new ParamsException ("找不到任务信息");
                }
                task.setUnConfirmedPay (task.getUnConfirmedPay ( ) - bill.getPayMoney ( ));
                taskMapper.updateAllColumnById (task);
                informService.sendInformInfo (2, 3, content, message.getHotelId ( ), "账目被拒绝");
                try {
                    jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (message.getHotelId ( )).getLeaderMobile ( ), content));
                } catch (APIConnectionException e) {
                    e.printStackTrace ( );
                } catch (APIRequestException e) {

                }
                bill.setStatus (2);
                billMapper.updateById (bill);
            } else if ("1".equals (status)) {
                taskHrCompany.setHavePayMoney (taskHrCompany.getHavePayMoney ( ) + bill.getPayMoney ( ));
                taskHrCompany.setUnConfirmedPay (taskHrCompany.getUnConfirmedPay ( ) - bill.getPayMoney ( ));
                taskHrCompanyMapper.updateAllColumnById (taskHrCompany);
                //确认收入
                Task task = taskMapper.selectById (taskHrCompany.getTaskId ( ));
                if (task == null) {
                    throw new ParamsException ("找不到任务信息");
                }
                task.setHavePayMoney (task.getHavePayMoney ( ) + bill.getPayMoney ( ));
                task.setUnConfirmedPay (task.getUnConfirmedPay ( ) - bill.getPayMoney ( ));
                taskMapper.updateAllColumnById (task);
                //新增用人单位支付人力明细
                HotelPayHrDetails details = new HotelPayHrDetails ( );
                details.setTaskHrId (taskHrCompany.getPid ( ));
                details.setThisPayMoney (Double.valueOf (message.getMinutes ( )));
                hotelPayHrDetailsService.saveBean (details);
                String content = taskHrCompany.getHrCompanyName ( ) + "同意了你发起的一笔支付信息，金额为" + Double.valueOf (message.getMinutes ( ));
                informService.sendInformInfo (2, 3, content, message.getHotelId ( ), "账目已同意");
                try {
                    jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (message.getHotelId ( )).getLeaderMobile ( ), content));
                } catch (APIConnectionException e) {
                    e.printStackTrace ( );
                } catch (APIRequestException e) {

                }
                bill.setStatus (1);
                billMapper.updateById (bill);
            } else {
                throw new ParamsException ("参数错误");
            }
            return ResultDO.buildSuccess ("操作成功");
        }

        /**
         * 人力同意小时工拒绝任务并派发任务
         *
         * @param messageId
         * @param workerId
         * @return
         */
        @Override
        public ResultDO hrAgreeWorkerRefuseAndPost (String messageId, String workerId){
            if (StringUtils.isEmpty (workerId) || StringUtils.isEmpty (messageId)) {
                throw new ParamsException ("参数不能为空");
            }

        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new ParamsException("已处理");
        }
        message.setStatus(1);
        messageMapper.updateAllColumnById(message);

            TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById (message.getHrTaskId ( ));
            if (taskHrCompany == null) {
                throw new ParamsException ("查询不到人力任务信息");
            }
            //更新人力任务的拒绝人数
            //taskHrCompany.setRefusedWorkers(taskHrCompany.getRefusedWorkers() + 1);
            taskHrCompanyMapper.updateAllColumnById (taskHrCompany);

            Task task = taskMapper.selectById (taskHrCompany.getTaskId ( ));
            if (task == null) {
                throw new ParamsException ("查询不到用人单位任务");
            }
            //小时工任务更新
            TaskWorker taskWorker = taskWorkerMapper.selectById (message.getWorkerTaskId ( ));
            if (taskWorker == null) {
                throw new ParamsException ("查询不到小时工任务信息");
            }
            taskWorker.setStatus (2);
            taskWorker.setRefusedReason (message.getContent ( ));
            taskWorkerMapper.updateAllColumnById (taskWorker);
            //发送通知给拒绝任务的小时工
            String content = taskHrCompany.getHrCompanyName ( ) + "同意了你的拒绝任务申请,拒绝原因：" + message.getContent ( );
            informService.sendInformInfo (2, 1, content, message.getWorkerId ( ), "拒绝任务");
            try {
                jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (message.getWorkerId ( )).getMobile ( ), content));
            } catch (APIConnectionException e) {
                e.printStackTrace ( );
            } catch (APIRequestException e) {

            }
            if (!message.isStop ( )) {
                if (task.getFromDate ( ).isBefore (OffsetDateTime.now ( ))) {
                    return ResultDO.buildSuccess ("任务已开始，处理超时");
                }
            }
            //派发小时工任务
            List <Map <String, String>> list = new ArrayList <> ( );
            Map <String, String> m = new HashMap <> ( );
            TaskWorker taskWork = new TaskWorker ( );
            taskWork.setTaskHrId (taskHrCompany.getPid ( ));

            User user = userMapper.queryByWorkerId (workerId);
            taskWork.setUserId (user.getPid ( ));
            taskWork.setWorkerId (user.getWorkerId ( ));
            taskWork.setUserName (user.getUsername ( ));
            taskWork.setStatus (0);
            taskWork.setFromDate (taskWorker.getFromDate ( ));
            taskWork.setToDate (taskWorker.getToDate ( ));
            taskWork.setHourlyPay (taskHrCompany.getHourlyPay ( ));
            taskWork.setTaskTypeCode (taskHrCompany.getTaskTypeCode ( ));
            taskWork.setTaskContent (taskHrCompany.getTaskContent ( ));
            taskWork.setTaskTypeText (taskHrCompany.getTaskTypeText ( ));
            taskWork.setHrCompanyName (taskHrCompany.getHrCompanyName ( ));
            taskWork.setHrCompanyId (taskHrCompany.getHrCompanyId ( ));
            taskWork.setHotelName (taskHrCompany.getHotelName ( ));
            taskWork.setHotelId (task.getHotelId ( ));
            taskWork.setDayStartTime (task.getDayStartTime ( ));
            taskWork.setDayEndTime (task.getDayEndTime ( ));
            taskWork.setHotelTaskId (task.getPid ( ));
            taskWork.setSettlementPeriod (taskHrCompany.getWorkerSettlementPeriod ());
            taskWork.setSettlementNum (taskHrCompany.getWorkerSettlementNum ());
            taskWorkerMapper.insert (taskWork);
            m.put ("workerId", workerId);
            m.put ("workerTaskId", taskWork.getPid ( ));
            m.put ("hotelId", taskWork.getHotelId ( ));
            list.add (m);
            List <Message> ms = null;
            if (message.isStop ( )) {
                ms = messageService.hrDistributeTask (list, taskHrCompany.getHrCompanyId ( ), taskHrCompany.getHrCompanyName ( ), "workTaskMessage", task.getPid ( ), taskHrCompany.getPid ( ), true);
            } else {
                ms = messageService.hrDistributeTask (list, taskHrCompany.getHrCompanyId ( ), taskHrCompany.getHrCompanyName ( ), "workTaskMessage", task.getPid ( ), taskHrCompany.getPid ( ), false);
            }
            if (message.isStop ( )) {
                if (ms.size ( ) != 1) {
                    throw new ParamsException ("数据异常");
                }
                RefusedTaskRequest ref = new RefusedTaskRequest ( );
                ref.setRefusedReason ("小时工未在规定时间内领取任务，请重新派发");
                ref.setMessageId (ms.get (0).getPid ( ));
                ref.setWorkerTaskId ("");
                myTimeTask.setRefusedReq (ref);
                java.util.Timer timer = new Timer (true);
                Field field;
                try {
                    field = TimerTask.class.getDeclaredField ("state");
                    field.setAccessible (true);
                    field.set (myTimeTask, 0);
                } catch (NoSuchFieldException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace ( );
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace ( );
                }
                timer.schedule (myTimeTask, 86400 * 1000);

            }
            return ResultDO.buildSuccess ("派发成功");

        }

        /**
         * 查询人力当前任务数量
         * @param applyParamDTO
         * @return
         */
        @Override
        public int selectHrCurTaskCount (ApplyParamDTO applyParamDTO){
            if (StringUtils.isEmpty (applyParamDTO.getId ( ))) {
                return 0;
            }
            TaskHrQueryDTO queryDTO = new TaskHrQueryDTO ( );
            queryDTO.setHrCompanyId (applyParamDTO.getId ( ));
            queryDTO.setStatus (8);
            return taskHrCompanyMapper.queryHrCurTaskCount (queryDTO);
        }

        /**
         * pc端人力处理小时工任务取消
         * @param messageId
         * @param status
         * @return
         */
        @Override
        public ResultDO hrHandleWorkerCancel (String messageId, String status, String workerId){

        if (StringUtils.isEmpty(messageId) || StringUtils.isEmpty(status)) {
            throw new ParamsException("参数不能为空");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new ParamsException("已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);

            TaskWorker taskWorker = taskWorkerMapper.selectById (message.getWorkerTaskId ( ));
            if (taskWorker == null) {
                throw new ParamsException ("参数错误");
            }
            if ("0".equals (status)) {
                String content = taskWorker.getHrCompanyName ( ) + "拒绝了你的取消任务申请，希望你能完成该任务。";
                informService.sendInformInfo (2, 1, content, message.getWorkerId ( ), "申请取消被拒绝");
                try {
                    jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (message.getWorkerId ( )).getMobile ( ), content));
                } catch (APIConnectionException e) {
                    e.printStackTrace ( );
                } catch (APIRequestException e) {

                }
            } else if ("1".equals (status)) {
                TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById (message.getHrTaskId ( ));
                if (taskHrCompany == null) {
                    throw new ParamsException ("人力任务查询不到.");
                }
                //更新人力任务
                taskHrCompany.setRefusedWorkers (taskHrCompany.getRefusedWorkers ( ) + 1);
                taskHrCompany.setConfirmedWorkers (taskHrCompany.getConfirmedWorkers ( ) - 1);
                if (taskHrCompany.getStatus ( ) == 5) {
                    taskHrCompany.setStatus (4);
                }
                taskHrCompanyMapper.updateAllColumnById (taskHrCompany);

                //更新用人单位任务的已确认小时工人数和拒绝小时工人数
                Task task = taskMapper.selectById (taskHrCompany.getTaskId ( ));
                if (task == null) {
                    throw new ParamsException ("查询不到用人单位任务");
                }
                task.setConfirmedWorkers (task.getConfirmedWorkers ( ) - 1);
                task.setRefusedWorkers (task.getRefusedWorkers ( ) + 1);
                if (task.getStatus ( ) == 4) {
                    task.setStatus (3);
                }
                taskMapper.updateAllColumnById (task);
                //更新小时工任务

                taskWorker.setStatus (2);
                taskWorker.setRefusedReason (message.getContent ( ));
                taskWorkerMapper.updateById (taskWorker);

                //给取消任务的小时工发送通知

                String content = taskWorker.getHrCompanyName ( ) + "同意了你的取消任务申请。";
                informService.sendInformInfo (2, 1, content, message.getWorkerId ( ), "申请取消成功");
                try {
                    jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (message.getWorkerId ( )).getMobile ( ), content));
                } catch (APIConnectionException e) {
                    e.printStackTrace ( );
                } catch (APIRequestException e) {

                }

                //给新的小时工派发任务
                TaskWorker workerTask = new TaskWorker ( );
                workerTask.setStatus (0);
                workerTask.setDayEndTime (taskWorker.getDayEndTime ( ));
                workerTask.setDayStartTime (taskWorker.getDayStartTime ( ));
                workerTask.setToDate (taskWorker.getToDate ( ));
                workerTask.setFromDate (taskWorker.getFromDate ( ));
                workerTask.setHotelName (taskHrCompany.getHotelName ( ));
                workerTask.setHourlyPay (taskHrCompany.getHourlyPay ( ));
                workerTask.setTaskContent (taskHrCompany.getTaskContent ( ));
                workerTask.setTaskTypeCode (taskHrCompany.getTaskTypeCode ( ));
                workerTask.setTaskTypeText (taskHrCompany.getTaskTypeText ( ));
                workerTask.setTaskHrId (workerId);
                workerTask.setWorkerId (workerId);
                User us = userMapper.selectByWorkerId (workerId);
                workerTask.setUserId (us.getPid ( ));
                workerTask.setUserName (us.getNickname ( ));
                workerTask.setHrCompanyName (taskHrCompany.getHrCompanyName ( ));
                workerTask.setHotelId (taskHrCompany.getHotelId ( ));
                workerTask.setHrCompanyId (taskHrCompany.getHrCompanyId ( ));
                workerTask.setHotelTaskId (taskHrCompany.getTaskId ( ));
                taskWorkerMapper.insert (workerTask);
                //发送消息
                Map <String, String> param = new HashMap <> ( );
                param.put ("hrCompanyName", taskWorker.getHrCompanyName ( ));
                String notice = messageService.installContent (param, "workTaskMessage");
                Map <String, Object> result = new HashMap <> ( );
                result.put ("hrCompanyId", taskHrCompany.getHrCompanyId ( ));
                result.put ("taskHrId", taskHrCompany.getTaskId ( ));
                result.put ("workerId", workerId);
                result.put ("workerTaskId", workerTask.getPid ( ));
                result.put ("hotelId", taskHrCompany.getHotelId ( ));
                result.put ("applicantType", 2);
                result.put ("applyType", 0);
                result.put ("messageContent", notice);
                result.put ("messageType", 6);
                result.put ("workTaskMessage", "workTaskMessage");
                result.put ("messageTitle", "人力公司派发任务通知书");
                result.put ("taskId", taskHrCompany.getTaskId ( ));
                result.put ("hrTaskId", taskHrCompany.getPid ( ));
                Message ms = messageService.sendMessageInfo (result);
                //设置定时
                RefusedTaskRequest ref = new RefusedTaskRequest ( );
                ref.setRefusedReason ("小时工未在规定时间内领取任务，请重新派发");
                ref.setMessageId (ms.getPid ( ));
                ref.setWorkerTaskId ("");
                myTimeTask.setRefusedReq (ref);
                java.util.Timer timer = new Timer (true);
                timer.schedule (myTimeTask, OffsetDateTime.now ( ).plusSeconds (15).getLong (ChronoField.SECOND_OF_DAY));
            } else {
                throw new ParamsException ("参数值错误");
            }
            return ResultDO.buildSuccess ("操作成功");
        }

        /**
         * pc端人力处理用人单位申请替换小时工
         * @param messageId
         * @param status
         * @param workerId
         * @return
         */
        @Override
        public ResultDO hrHandleHotelReplace (String messageId, String status, String workerId){

            if (StringUtils.isEmpty (messageId) || StringUtils.isEmpty (status)) {
                throw new ParamsException ("参数错误");
            }
            Message message = messageMapper.selectById (messageId);
            if (message == null || message.getStatus() == 1) {
                throw new ParamsException ("查找不到消息");
            }
            message.setStatus (1);
            messageMapper.updateById (message);
            Company company = companyMapper.findCompanyById (message.getHrCompanyId ( ));
            if (company == null) {
                throw new BusinessException ("人力公司查询不到");
            }
            if ("0".equals (status)) {
                String content = company.getName ( ) + "拒绝了你的换人申请。";
                informService.sendInformInfo (2, 3, content, message.getHotelId ( ), "换人被拒绝");
                try {
                    jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (message.getHotelId ( )).getLeaderMobile ( ), content));
                } catch (APIConnectionException e) {
                    e.printStackTrace ( );
                } catch (APIRequestException e) {

                }
            } else if ("1".equals (status)) {
                //消息发送者是用人单位，将小时工任务状态设置为3终止，如果是小时工，将状态置为2
                TaskWorker taskWorker = taskWorkerMapper.selectById (message.getWorkerTaskId ( ));
                if (taskWorker == null) {
                    throw new BusinessException ("查询不到小时工工作任务");
                }
                if (message.getApplicantType ( ) == 3) {
                    taskWorker.setStatus (3);
                } else if (message.getApplicantType ( ) == 1) {
                    taskWorker.setStatus (2);
                } else {
                    throw new ParamsException ("参数错误");
                }
                taskWorker.setRefusedReason (message.getContent ( ));
                taskWorkerMapper.updateAllColumnById (taskWorker);

                //更新人力任务信息
                TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById (message.getHrTaskId ( ));
                if (taskHrCompany == null) {
                    throw new BusinessException ("查询不到人力公司任务");
                }
                taskHrCompany.setConfirmedWorkers (taskHrCompany.getConfirmedWorkers ( ) - 1);
                Task task = taskMapper.getFirstById (message.getTaskId ( ));
                task.setConfirmedWorkers (task.getConfirmedWorkers ( ) - 1);
                taskMapper.updateById (task);
                if (message.getApplicantType ( ) == 1) {
                    taskHrCompany.setRefusedWorkers (taskHrCompany.getRefusedWorkers ( ) + 1);
                }
                taskHrCompanyMapper.updateAllColumnById (taskHrCompany);

                //插入小时工任务信息
                TaskWorker workerTask = null;
                List <TaskWorker> list = new ArrayList <> ( );
                workerTask = new TaskWorker ( );
                workerTask.setStatus (0);
                workerTask.setDayEndTime (taskWorker.getDayEndTime ( ));
                workerTask.setDayStartTime (taskWorker.getDayStartTime ( ));
                workerTask.setToDate (taskWorker.getToDate ( ));
                workerTask.setFromDate (taskWorker.getFromDate ( ));
                workerTask.setHotelName (taskHrCompany.getHotelName ( ));
                workerTask.setHourlyPay (taskHrCompany.getHourlyPay ( ));
                workerTask.setTaskContent (taskHrCompany.getTaskContent ( ));
                workerTask.setTaskTypeCode (taskHrCompany.getTaskTypeCode ( ));
                workerTask.setTaskTypeText (taskHrCompany.getTaskTypeText ( ));
                workerTask.setTaskHrId (taskHrCompany.getPid ( ));
                workerTask.setHotelTaskId (taskHrCompany.getTaskId ( ));
                workerTask.setHotelId (taskHrCompany.getHotelId ( ));
                workerTask.setHrCompanyId (taskHrCompany.getHrCompanyId ( ));
                workerTask.setWorkerId (workerId);
                workerTask.setUserId (userMapper.selectByWorkerId (workerId).getPid ( ));
                taskWorkerMapper.insert (workerTask);
                list.add (workerTask);

                //给小时工发送消息

                Message ms = messageService.hrDistributeWorkerTask (list, taskHrCompany, true);
                if (message.getApplicantType ( ) == 3) {
                    //给用人单位发送通知
                    //被替换的小时工
                    RefusedTaskRequest ref = new RefusedTaskRequest ( );
                    ref.setRefusedReason ("小时工未在规定时间内领取任务，请重新派发");
                    ref.setMessageId (ms.getPid ( ));
                    ref.setWorkerTaskId (workerTask.getPid ( ));
                    myTimeTask.setRefusedReq (ref);
                    java.util.Timer timer = new Timer (true);
                    timer.schedule (myTimeTask, OffsetDateTime.now ( ).plusSeconds (15).getLong (ChronoField.SECOND_OF_DAY));
                    User oldUser = userMapper.selectByWorkerId (taskWorker.getWorkerId ( ));
                    taskWorkerMapper.updateStatus (taskWorker.getWorkerId ( ), 3);
                    User newUser = userMapper.selectByWorkerId (list.get (0).getWorkerId ( ));
                    String content = taskHrCompany.getHrCompanyName ( ) + "同意了你的换人申请，将任务里的小时工" + oldUser.getNickname ( ) + "换成了" + newUser.getNickname ( );
                    informService.sendInformInfo (2, 3, content, message.getHotelId ( ), "换人成功");
                    try {
                        jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (message.getHotelId ( )).getLeaderMobile ( ), content));
                    } catch (APIConnectionException e) {
                        e.printStackTrace ( );
                    } catch (APIRequestException e) {

                    }

                    //给被替换的小时工发通知
                    content = message.getMessageContent ( ) + " 。任务重新调配了一个小时工，希望你下次能认真对待工作，这会影响你的信用。";
                    informService.sendInformInfo (2, 1, content, message.getWorkerId ( ), "调换通知");
                    try {
                        jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (message.getWorkerId ( )).getMobile ( ), content));
                    } catch (APIConnectionException e) {
                        e.printStackTrace ( );
                    } catch (APIRequestException e) {

                    }
                }
            } else {
                throw new ParamsException ("参数值错误");
            }
            return ResultDO.buildSuccess ("操作成功");
        }

    /**
     * 人力查询用人单位支付账单
     * @param taskHrQueryDTO
     * @return
     */
    @Override
    public List<DownLoadAccount> queryHrAccount(TaskHrQueryDTO taskHrQueryDTO) {

        int count = taskHrCompanyMapper.queryHrCompanyTasksCount(taskHrQueryDTO);
        PageHelper.startPage(1, count, true);
        List <TaskHrCompany> list = taskHrCompanyMapper.queryHrCompanyTasks (taskHrQueryDTO);
        List<DownLoadAccount> accountList = new ArrayList<>();
        if (list != null) {
            DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            DateTimeFormatter df2 = DateTimeFormatter.ofPattern("HH:mm");
            for (TaskHrCompany task : list) {
                DownLoadAccount account = new DownLoadAccount();
                account.setName(task.getHotelName());
                account.setTaskType(task.getTaskTypeText());
                account.setTaskContent(task.getTaskContent());
                account.setWorkDate(task.getFromDate().format(df1) + " / " + task.getToDate().format(df1));
                account.setStartEndTime(task.getDayStartTime().format(df2) + " / " + task.getDayEndTime().format(df2));
                account.setUnConfirmedPay(Maths.getTwoDecimal (task.getUnConfirmedPay () + 0.0 ,2));
                account.setHavePay(Maths.getTwoDecimal (task.getHavePayMoney() + 0.0,2));
                account.setShouldPay(Maths.getTwoDecimal (task.getShouldPayMoney() + 0.0,2));
                account.setPaidPayMoney(Maths.getTwoDecimal (task.getShouldPayMoney() - task.getHavePayMoney() - task.getUnConfirmedPay(),2));
                accountList.add(account);
            }
        }
        return accountList;
    }

    /**
     * 用人单位查询支付人力账单
     * @param taskHrQueryDTO
     * @return
     */
    @Override
    public List<DownLoadAccount> queryHotelAccount(TaskHrQueryDTO taskHrQueryDTO) {

        int count = taskHrCompanyMapper.queryHrCompanyTasksCount(taskHrQueryDTO);
        PageHelper.startPage(1, count, true);
        List <TaskHrCompany> list = taskHrCompanyMapper.queryHrCompanyTasks (taskHrQueryDTO);
        List<DownLoadAccount> accountList = new ArrayList<>();
        if (list != null) {
            DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            DateTimeFormatter df2 = DateTimeFormatter.ofPattern("HH:mm");
            for (TaskHrCompany task : list) {
                DownLoadAccount account = new DownLoadAccount();
                account.setName(task.getHrCompanyName());
                account.setTaskType(task.getTaskTypeText());
                account.setTaskContent(task.getTaskContent());
                account.setWorkDate(task.getFromDate().format(df1) + " / " + task.getToDate().format(df1));
                account.setStartEndTime(task.getDayStartTime().format(df2) + " / " + task.getDayEndTime().format(df2));
                account.setUnConfirmedPay(Maths.getTwoDecimal (task.getUnConfirmedPay () + 0.0 ,2));
                account.setHavePay(Maths.getTwoDecimal (task.getHavePayMoney() + 0.0,2));
                account.setShouldPay(Maths.getTwoDecimal (task.getShouldPayMoney() + 0.0,2));
                account.setPaidPayMoney(Maths.getTwoDecimal (task.getShouldPayMoney() - task.getHavePayMoney() - task.getUnConfirmedPay(),2));
                accountList.add(account);
            }
        }
        return accountList;
    }

    /**
     * 查询人力任务
     * @param taskHrQueryDTO
     * @return
     */
    @Override
    public List<HrTask> queryHrTask(TaskHrQueryDTO taskHrQueryDTO) {
        String date = taskHrQueryDTO.getOfDate();
        if (date != null) {
            Integer year = Integer.parseInt(date.split("-")[0]);
            Integer month = Integer.parseInt(date.split("-")[1]);
            if (month == 12) {
                taskHrQueryDTO.setFromDate (OffsetDateTime.of
                        (year, 12, 1, 0, 0, 0, 0,
                                ZoneOffset.UTC));
                taskHrQueryDTO.setToDate (OffsetDateTime.of
                        (year, 1, 1, 0, 0, 0, 0,
                                ZoneOffset.UTC));
            } else {
                taskHrQueryDTO.setFromDate (OffsetDateTime.of
                        (year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC));
                taskHrQueryDTO.setToDate (OffsetDateTime.of
                        (year, month + 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
            }
        }
        //查询数据集合
        int count = taskHrCompanyMapper.queryHrCompanyTasksCount(taskHrQueryDTO);
        PageHelper.startPage(1, count, true);
        List <TaskHrCompany> list = taskHrCompanyMapper.queryHrCompanyTasks (taskHrQueryDTO);
        List<HrTask> taskList = new ArrayList<>();
        if (list != null) {
            DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            DateTimeFormatter df2 = DateTimeFormatter.ofPattern("HH:mm");
            for (TaskHrCompany task : list) {
                HrTask t = new HrTask();
                t.setEmployerName(task.getHotelName());
                t.setTaskContent(task.getTaskContent());
                t.setType(task.getTaskTypeText());
                t.setHotelSalary(task.getHourlyPayHotel() + "");
                t.setSalary(task.getHourlyPay() + "");
                t.setWorkDate(task.getFromDate().format(df1) + " / " + task.getToDate().format(df1));
                t.setWorkTime(task.getDayStartTime().format(df2) + " / " + task.getDayEndTime().format(df2));
                t.setTotal(task.getConfirmedWorkers() + " / " + task.getNeedWorkers());
                int settlementNum = task.getSettlementNum();
                int workerSettlementNum = task.getWorkerSettlementNum();
                if (task.getSettlementPeriod() == 0) {
                    t.setSettlement(settlementNum + "天/次");
                } else if (task.getSettlementPeriod() == 1) {
                    t.setSettlement(settlementNum + "月/次");
                }
                if (task.getWorkerSettlementPeriod() == 0) {
                    t.setHrSettlement(workerSettlementNum + "天/次");
                } else if (task.getWorkerSettlementPeriod() == 1) {
                    t.setHrSettlement(workerSettlementNum + "天/次");
                }
                if (task.getStatus() == 1) {
                    t.setStatus("待接受");
                } else if (task.getStatus() == 2) {
                    t.setStatus("接受任务");
                } else if (task.getStatus() == 3) {
                    t.setStatus("拒绝任务");
                } else if (task.getStatus() == 4) {
                    t.setStatus("已派发");
                } else if (task.getStatus() == 5) {
                    t.setStatus("派单完成");
                } else if (task.getStatus() == 6) {
                    t.setStatus("进行中");
                } else if (task.getStatus() == 7) {
                    t.setStatus("已完成");
                } else if (task.getStatus() == 8) {
                    t.setStatus("拒绝接单");
                }
                taskList.add(t);
            }
        }
        return taskList;
    }

    /**
     * 人力同意小时工报名申请并派发
     * @param request
     * @return
     */
    @Override
    public ResultDO agreeApplySendTask(HrTaskDistributeRequest request) {
        System.out.println (request);
        if(StringUtils.isEmpty (request.getMessageId ())){
            throw new ParamsException ("messageId为空");
        }
        Message message = messageMapper.selectById (request.getMessageId ());
        if(message == null){
            throw new ParamsException ("messageId有误");
        }
        message.setStatus (1);
        messageMapper.updateById (message);
        Notice notice = noticeMapper.selectById (message.getRequestId ());
        if(notice == null){
            return ResultDO.buildError ("");
        }
        if(notice.getStatus () == 1){
            return ResultDO.buildError ("");
        }
        //添加合作伙伴
        UserCompany userCompany = userCompanyMapper.selectByWorkerIdHrId (notice.getHrCompanyId (),message.getWorkerId ());
        if(userCompany == null){
            List<UserCompany> userCompanyList = new ArrayList<>();
            //从redis中查询是否有协议
            String path = redisUtil.getString("defaultWorkerHrProtocol");
            if (StringUtils.isEmpty(path)) {
                try {
                    path = filePush.pushFileToServer(ConstantData.CATALOG.getName(), ConstantData.WORKHRPROTOCOL.getName());            } catch (Exception e) {
                    e.printStackTrace();
                    return ResultDO.buildError ("服务异常");
                }
                redisUtil.setString("defaultWorkerHrProtocol", path);
            }
            userCompany.setCompanyId(notice.getHrCompanyId ());
            userCompany.setUserType(UserType.worker);
            userCompany.setCompanyType(2);
            userCompany.setUserId(userMapper.selectByWorkerId (message.getWorkerId ()).getPid ());
            userCompany.setStatus(0);
            userCompany.setBindProtocol(path);
        }else if(userCompany.getStatus () == 0){
            userCompany.setStatus (1);
        }else if(userCompany.getStatus () == 2){
            userCompany.setStatus (1);
        }else if(userCompany.getStatus () == 4){
            userCompany.setStatus (1);
        }
        notice.setConfirmedWorkers (notice.getConfirmedWorkers () + 1);
        if(notice.getConfirmedWorkers () > notice.getNeedWorkers ()){
            return ResultDO.buildError ("");
        }
        if(notice.getNeedWorkers () == notice.getConfirmedWorkers ()){
            notice.setStatus (1);
        }
        noticeMapper.updateById (notice);
        //派发任务
        request.setMessageId (null);
        request.setHrTaskId (notice.getTaskId ());
        Set<String> set = new HashSet <> ();
        set.add (message.getWorkerId ());
        request.setWorkerIds (set);
        taskHrCompanyService.TaskHrDistribute(request);
        return null;
    }
}
