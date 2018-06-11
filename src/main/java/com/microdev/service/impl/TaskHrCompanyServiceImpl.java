package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.DateUtil;
import com.microdev.common.utils.Maths;
import com.microdev.common.utils.StringKit;
import com.microdev.converter.TaskHrCompanyConverter;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.service.HotelPayHrDetailsService;
import com.microdev.service.InformService;
import com.microdev.service.MessageService;
import com.microdev.service.TaskHrCompanyService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Transactional
@Service
public class TaskHrCompanyServiceImpl extends ServiceImpl<TaskHrCompanyMapper,TaskHrCompany> implements TaskHrCompanyService{

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

    /**
     * 查看人力资源公司的任务
     * @param id     人力任务id
     */
    @Override
    public ResultDO getTaskHrCompanyById(String id) {
        Map<String, Object> map = taskHrCompanyMapper.selectByTaskId(id);
        if (map == null) {
            return ResultDO.buildSuccess(new HashMap<String, Object>());
        }
        map.put("payStatus", "未结算");
        if ((Double) map.get("workersHavePay") > 0) {
            map.put("payStatus", "结算中");
        }
        if ((Double) map.get("workersShouldPay") > 0
                && ((Double) map.get("workersShouldPay") - (Double) map.get("workersHavePay") <= 0)) {
            map.put("payStatus", "已结算");
        }
        List<Map<String, Object>> list = taskWorkerMapper.selectTaskWorkById((String) map.get("pid"));
		List<Map<String, Object>> confirmedList = new ArrayList<>();
        List<Map<String, Object>> refusedList = new ArrayList<>();
        List<Map<String, Object>> distributedList = new ArrayList<>();
        for (Map<String, Object> m : list) {
            m.put("age", DateUtil.caculateAge((Timestamp) m.get("birthday")));
            distributedList.add(m);
            if (m.get("taskStatus") == null)
                continue;
            if ((Integer) m.get("taskStatus") == 1 || (Integer) m.get("taskStatus") == 3) {
                confirmedList.add(m);
            } else if ((Integer) m.get("taskStatus") == 2) {
                refusedList.add(m);
            }

        }
        map.put("distributeWorkers", list.size());
        map.put("confirmedSet", confirmedList);
        map.put("refusedSet", refusedList);
        map.put("distributedSet", distributedList);
        return ResultDO.buildSuccess(map);
    }
    /**
     * 任务分发
     */
    @Override
    public ResultDO TaskHrDistribute(HrTaskDistributeRequest hrTaskDis) {
        if (!StringUtils.hasLength(String.valueOf(hrTaskDis.getHourlyPay()))) {
            throw new ParamsException("人力公司每小时工钱不能为空");
        }
        /*if (!StringUtils.hasLength(hrTaskDis.getMessageId())) {
            throw new ParamsException("messageId不能为空");
        }*/
        if (hrTaskDis.getWorkerIds().size()==0) {
            throw new ParamsException("请选择派发的员工");
        }
        Message message = null;
        TaskHrCompany hrTask= null;
        if(StringUtils.hasLength(hrTaskDis.getMessageId())){
            message = messageMapper.selectById(hrTaskDis.getMessageId());
            message.setStatus(1);
            messageMapper.updateAllColumnById(message);
            hrTask = taskHrCompanyMapper.queryByTaskId(message.getHrTaskId());
            hrTask.setHourlyPay(hrTaskDis.getHourlyPay());
        }else{
            hrTask = taskHrCompanyMapper.queryByTaskId(hrTaskDis.getHrTaskId ());
        }
       /* if (message == null) {
            throw new ParamsException("查询不到消息");
        }*/

        // 获取人力公司任务和酒店任务信息
        if(hrTask==null){
            throw new ParamsException("人力公司参数有误");
        }
        //Company hrCompany=companyMapper.findCompanyById(hrTask.getHrCompanyId());
        taskMapper.updateStatus(hrTask.getTaskId(),3);
        Company hrCompany=companyMapper.findCompanyById(hrTask.getHrCompanyId());
        hrTask.setStatus(4);
        Task hotelTask=taskMapper.getFirstById(hrTask.getTaskId());
        if(hotelTask==null){
            throw new BusinessException("任务派发失败：未获取酒店到任务");
        }
       // Company hotel=companyMapper.findCompanyById(hrTask.getHotelId());
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> m = null;
        for (String id:hrTaskDis.getWorkerIds()){
            System.out.println (id);
            m = new HashMap<>();
            TaskWorker taskWorker=new TaskWorker();
            taskWorker.setTaskHrId(hrTask.getPid());;
            userMapper.queryByWorkerId(id);
            User user = userMapper.queryByWorkerId(id);
            taskWorker.setUserId(user.getPid());
            taskWorker.setWorkerId (user.getWorkerId ());
            taskWorker.setUserName(user.getUsername());
            taskWorker.setStatus(0);
            taskWorker.setFromDate(hotelTask.getFromDate());
            taskWorker.setToDate(hotelTask.getToDate());
            taskWorker.setHourlyPay(hrTask.getHourlyPay());
            taskWorker.setTaskTypeCode(hrTask.getTaskTypeCode());
            taskWorker.setTaskContent(hrTask.getTaskContent());
            taskWorker.setTaskTypeText(hrTask.getTaskTypeText());
            taskWorker.setHrCompanyName (hrTask.getHrCompanyName ());
            taskWorker.setHrCompanyId (hrTask.getHrCompanyId ());
            taskWorker.setHotelName(hrTask.getHotelName());
            taskWorker.setHotelId (hotelTask.getHotelId ());
            taskWorker.setDayStartTime(hotelTask.getDayStartTime());
            taskWorker.setDayEndTime(hotelTask.getDayEndTime());
            taskWorker.setHotelTaskId (hotelTask.getPid ());
            taskWorker.setHrCompanyId(hrTask.getHrCompanyId());
            taskWorkerMapper.insert(taskWorker);
            m.put("workerId", id);
            m.put("workerTaskId", taskWorker.getPid());
            m.put("hotelId", taskWorker.getHotelId());
            list.add(m);
        }
        hrTask.setDistributeWorkers (hrTask.getDistributeWorkers ()+hrTaskDis.getWorkerIds ().size ());
        taskHrCompanyMapper.updateById(hrTask);
        messageService.hrDistributeTask(list, hrTask.getHrCompanyId(), hrTask.getHrCompanyName(), "workTaskMessage", hotelTask.getPid(), hrTask.getPid());
        //短信发送
        /*CreateMessageDTO createMessageDTO =new CreateMessageDTO();
        createMessageDTO.setHotelName(hotel.getName());
        createMessageDTO.setHrCompanyName(hrCompany.getName());
        createMessageDTO.setFromDate(hotelTask.getFromDate());
        createMessageDTO.setToDate(hotelTask.getToDate());
        createMessageDTO.setTaskType(hotelTask.getTaskTypeText());
        createMessageDTO.setTaskContent(hotelTask.getTaskContent());
        SendMessage(hrTask.getListWorkerTask(),createMessageDTO,setTaskWorkId);*/

        return ResultDO.buildSuccess("分发成功");
    }
    /**
     * 分页查询人力资源公司任务
     */
    @Override
    public ResultDO getPageTasks(Paginator paginator, TaskHrQueryDTO taskHrQueryDTO) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        String date = taskHrQueryDTO.getOfDate();
        if(date!=null) {
            Integer year = Integer.parseInt(date.split("-")[0]);
            Integer month = Integer.parseInt(date.split("-")[1]);
            if (month == 12) {
                taskHrQueryDTO.setFromDate(OffsetDateTime.of
                        (year, 12, 1, 0, 0, 0, 0,
                                ZoneOffset.UTC));
                taskHrQueryDTO.setToDate(OffsetDateTime.of
                        (year, 1, 1, 0, 0, 0, 0,
                                ZoneOffset.UTC));
            } else {
                taskHrQueryDTO.setFromDate(OffsetDateTime.of
                        (year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC));
                taskHrQueryDTO.setToDate(OffsetDateTime.of
                        (year, month + 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
            }
        }
        //查询数据集合
        List<TaskHrCompany> list = taskHrCompanyMapper.queryHrCompanyTasks(taskHrQueryDTO);
        HashMap<String,Object> extra = new HashMap<>();
        Double shouldPayMoney = 0.0;
        Double havePayMoney=0.0;
        Double workersShouldPay=0.0;
        Double workersHavePay=0.0;
        for (TaskHrCompany task:list) {
			task.setHrCompany(companyMapper.findCompanyById(task.getHrCompanyId()));
            shouldPayMoney += task.getShouldPayMoney();
            havePayMoney += task.getHavePayMoney();
            workersShouldPay += task.getWorkersShouldPay();
            workersHavePay += task.getWorkersHavePay();
            task.setHotel (companyMapper.findCompanyById (task.getHotelId ()));
            List<Map<String, Object>> lis = taskWorkerMapper.selectTaskWorkCById (task.getPid ());
            task.setListWorkerTask (lis);
        }
        PageInfo<TaskHrCompany> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        extra.put("shouldPayMoney",shouldPayMoney);
        extra.put("havePayMoney",havePayMoney);
        extra.put("workersShouldPay",workersShouldPay);
        extra.put("workersHavePay",workersHavePay);
        return ResultDO.buildSuccess(null,result,extra,null);
    }
    /**
     * 人力公司支付小时工
     */
    @Override
    public ResultDO hrPayWorkers(HrPayWorkerRequest payWorkerRequest) {
        TaskHrCompany taskHr=  taskHrCompanyMapper.queryByTaskId(payWorkerRequest.getHrTaskId());
        Set<WorkerPayDetailRequest>  paySet= payWorkerRequest.getPayWorkerSet();
        if(paySet.size()==0){
            throw new ParamsException("支付的小时工列表不能为空");
        }
        double thisPayMoneySum=0.0;
        for (WorkerPayDetailRequest payWorker:paySet){
            TaskWorker taskWorker=taskWorkerMapper.findFirstById(payWorker.getTaskWorkerId());
            if(taskWorker.getSettled()==null ||taskWorker.getSettled()==false ){
                thisPayMoneySum+=payWorker.getPayMoney();
                taskWorker.setSettled(true);
                taskWorker.setSettledDate(OffsetDateTime.now());
            }
            taskWorker.setHavePayMoney(taskWorker.getHavePayMoney()+payWorker.getPayMoney());
            taskWorkerMapper.updateById(taskWorker);
            //支付记录
            //记录详情
            Bill bill = new Bill();
            bill.setTaskId(taskHr.getPid());
            bill.setWorkerId(userMapper.queryByUserId(taskWorker.getUserId()).getWorkerId());
            bill.setPayMoney(payWorker.getPayMoney());
            bill.setHrCompanyId(taskHr.getHrCompanyId());
            bill.setHrCompanyName(taskHr.getHrCompanyName());
            bill.setDeleted(false);
            bill.setPayType(1);
            billMapper.insert(bill);
        }
        taskHr.setWorkersHavePay(taskHr
                .getWorkersHavePay()+thisPayMoneySum);
        taskHrCompanyMapper.updateById(taskHr);
        return ResultDO.buildSuccess("结算成功");
    }
    /**
     * 人力公司接受任务
     */
    @Override
    public void TaskHraccept(String messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new BusinessException("消息已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        TaskHrCompany taskHrCompany = taskHrCompanyMapper.queryByTaskId(message.getHrTaskId ());
        Task task = taskMapper.selectById(taskHrCompany.getTaskId());
        if (task == null) {
            throw new ParamsException("查询不到不到酒店任务");
        }
        if (task.getStatus() == 1) {
            taskMapper.updateStatus(taskHrCompany.getTaskId(),2);
        }

        taskHrCompanyMapper.updateStatus(message.getHrTaskId (),2);

        Inform inform = new Inform();
        inform.setTitle("任务已接受");
        inform.setContent(taskHrCompany.getHrCompanyName() + "接受了派发的任务。");
        inform.setReceiveId(taskHrCompany.getHotelId());
        inform.setAcceptType(3);
        inform.setSendType(2);
        informMapper.insertInform(inform);

        //生成一个待派发的消息
        Map<String, Object> param = new HashMap<>();
        param.put("hrCompanyId", taskHrCompany.getHrCompanyId());
        param.put("hotelId", taskHrCompany.getHotelId());
        param.put("applicantType", 2);
        param.put("applyType", 2);
        param.put("hrTaskId", taskHrCompany.getPid());
        param.put("taskId", taskHrCompany.getTaskId());
        param.put("messageType", 11);
        param.put("messageCode", "awaitSendMessage");
        messageService.sendMessageInfo(param);
    }
    /**
     * 人力公司拒绝任务
     */
    @Override
    public void TaskHrrefuse(String messageId, String reason) {

        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new BusinessException("消息已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        TaskHrCompany taskHrCompany = taskHrCompanyMapper.queryByTaskId(message.getHrTaskId ());
        if (taskHrCompany == null) {
            throw new BusinessException("查询不到人力任务数据");
        }
        taskHrCompany.setStatus (3);
        taskHrCompany.setRefusedReason (reason);
        taskHrCompanyMapper.updateById (taskHrCompany);

		//发送拒绝通知
        InformTemplate inf = informTemplateMapper.selectByCode (InformType.refuse_hotel_task.name ());
        Map<String,String> map = new HashMap<>();
        map.put("hr",taskHrCompany.getHrCompanyName ());
        map.put ("reason",reason);
        String content = StringKit.templateReplace(inf.getContent (), map);
        informService.sendInformInfo (inf.getSendType (),3,content,taskHrCompany.getHotelId (),inf.getTitle ());
        //发送拒绝消息

        Message m = new Message();
        m.setContent(reason);
        MessageTemplate mess = messageTemplateMapper.findFirstByCode ("refuseTaskMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageType(10);
        m.setMessageTitle(mess.getTitle());
        m.setHrCompanyId (taskHrCompany.getHrCompanyId ());
        m.setHrTaskId (taskHrCompany.getPid ());
        m.setHotelId(taskHrCompany.getHotelId ());
        Map<String, String> param = new HashMap<>();
        param.put("userName", taskHrCompany.getHrCompanyName ());
        param.put("content", reason);
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setApplyType(2);
        m.setStatus(0);
        m.setIsTask(0);
        messageMapper.insert(m);

    }
    /**
     * 人力公司任务调配
     */
    @Override
    public String TaskHrallocate(String id, String reason, Integer number) {
        if (number == null || number < 1 || StringUtils.isEmpty(id)) {
            throw new ParamsException("参数错误");
        }
        TaskHrCompany t = taskHrCompanyMapper.selectById(id);
        if (t == null) {
            throw new BusinessException("查找不到人力资源任务");
        }
        //t.setStatus(5);
        Map map = new HashMap<String,Object> ();
        map.put("message_type",4);
        map.put("hr_task_id",t.getPid ());
        map.put("status",0);
        if(messageMapper.selectByMap (map).size ()>0){
            return "你已提交过申请";
        }
        taskHrCompanyMapper.update(t);
        messageService.sendMessage(t, reason, String.valueOf(number), "applyChangeMessage");
        return "发送成功";

    }
    /**
     * 酒店查询账目
     */
    @Override
    public ResultDO getHotelBill(Paginator paginator, BillRequest request) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<TaskHrCompany> list = taskHrCompanyMapper.queryHotelBill(request);
        PageInfo<TaskHrCompany> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        Map<String,Object> map = new HashMap<>();
        Double should_pay_money = 0.0;
        Double have_pay_money  = 0.0;
        for (TaskHrCompany item:list) {
            should_pay_money = Maths.add (should_pay_money,item.getShouldPayMoney());
            have_pay_money = Maths.add (item.getHavePayMoney(),have_pay_money);
            item.setHrCompany (companyMapper.selectById (item.getHrCompanyId ()));
        }
        map.put("shouldPayMoney", should_pay_money);
        map.put("havePayMoney",have_pay_money);
        map.put("paidPayMoney",Maths.sub (should_pay_money,have_pay_money));
        return ResultDO.buildSuccess(null,result,map,null);
    }
    /**
     * 人力公司按酒店查询账目
     */
    @Override
    public ResultDO getCompanyBillHotel(Paginator paginator,BillRequest request) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<TaskHrCompany> list = taskHrCompanyMapper.queryHrCompanyBill(request);
        Map<String,Object> map = new HashMap<>();
        Double should_pay_money = 0.0;
        Double have_pay_money  = 0.0;
        for (TaskHrCompany item:list) {
            should_pay_money = Maths.add (should_pay_money,item.getShouldPayMoney());
            have_pay_money = Maths.add (item.getHavePayMoney(),have_pay_money);
            System.out.println ("horelID:"+item.getHotelId ());
            item.setHotel (companyMapper.selectById (item.getHotelId ()));
        }
        map.put("shouldPayMoney",should_pay_money);
        map.put("havePayMoney",have_pay_money);
        map.put("paidPayMoney",Maths.sub (should_pay_money,have_pay_money));
        PageInfo<TaskHrCompany> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess(null,result,map,null);
    }
    /**
     * 人力公司按小时工查询账目
     */
    @Override
    public ResultDO getCompanyBillWorker(Paginator paginator,BillRequest request) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<TaskWorker> list = taskWorkerMapper.queryHrCompanyBill(request);
        PageInfo<TaskWorker> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        Map<String,Object> map = new HashMap<>();
        Double should_pay_money = 0.0;
        Double have_pay_money  = 0.0;
        for (TaskWorker item:list) {
            should_pay_money = Maths.add (should_pay_money,item.getShouldPayMoney());
            have_pay_money = Maths.add (item.getHavePayMoney(),have_pay_money);
            item.setUser (userMapper.queryByUserId (item.getUserId ()));
        }
        map.put("shouldPayMoney",should_pay_money);
        map.put("havePayMoney",have_pay_money);
        map.put("paidPayMoney",Maths.sub (should_pay_money,have_pay_money));
        return ResultDO.buildSuccess(null,result,map,null);
    }
    /**
     * 小时工按人力公司工查询账目
     *
     */
    @Override
    public ResultDO getWorkerBill(Paginator paginator,BillRequest request) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<TaskHrCompany> list = taskWorkerMapper.queryWorkerBill(request);
        PageInfo<TaskHrCompany> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        Map<String,Object> map = new HashMap<>();
        Double should_pay_money = 0.0;
        Double have_pay_money  = 0.0;
        for (TaskHrCompany item:list) {
            should_pay_money = Maths.add (should_pay_money,item.getShouldPayMoney());
            have_pay_money = Maths.add (item.getHavePayMoney(),have_pay_money);
            item.setHrCompany (companyMapper.selectById (item.getHrCompanyId ()));
        }
        map.put("shouldPayMoney",should_pay_money);
        map.put("havePayMoney",have_pay_money);
        map.put("paidPayMoney",Maths.sub (should_pay_money,have_pay_money));
        return ResultDO.buildSuccess(null,result,map,null);
    }
    /**
     * 人力公司申请调配.
     */
    @Override
    public ResultDO hrApplyChangeWorker(Map<String, Object> map) {
        if (StringUtils.isEmpty(map.get("hotelId"))) {
            throw new ParamsException("hotelId不能为空");
        }
        if (StringUtils.isEmpty(map.get("hrCompanyId"))) {
            throw new ParamsException("hrCompanyId不能为空");
        }
        if (StringUtils.isEmpty(map.get("reason"))) {
            throw new ParamsException("reason不能为空");
        }
        if(StringUtils.isEmpty(map.get("hrTaskId"))) {
            throw new ParamsException("hrTaskId不能为空");
        }
        Message m = new Message();
        m.setContent((String)map.get("reason"));
        Company company = companyMapper.selectById(map.get("hrCompanyId").toString());
        MessageTemplate mess = messageTemplateMapper.findFirstByCode("applyChangeMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageTitle(mess.getTitle());
        m.setHotelId ((String)map.get("hotelId"));
        m.setHrCompanyId ((String)map.get("hrCompanyId"));
        m.setHrTaskId ((String)map.get("hrTaskId"));
        Map<String, String> param = new HashMap<>();
        param.put("hrCompanyName", company.getName());
        param.put("reason", (String)map.get("reason"));
        param.put("number", (String)map.get("number"));
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setMessageType(4);
        m.setApplicantType (2);
        m.setApplyType(3);
		m.setMinutes ((String)map.get("number"));
		m.setStatus(0);
        m.setIsTask(0);
        m.setHotelId((String)map.get("hotelId"));
        m.setHrCompanyId((String)map.get("hrCompanyId"));
        m.setTaskId (taskHrCompanyMapper.queryByTaskId ((String)map.get("hrTaskId")).getTaskId ());
        messageMapper.insert(m);
        return ResultDO.buildSuccess("成功");
    }

    /**
     * 统计人力公司待处理未读数据
     * @param hrCompanyId
     * @return
     */
    @Override
    public int selectUnreadCount(String hrCompanyId) {
        if (StringUtils.isEmpty(hrCompanyId)) {
            throw new ParamsException("参数不能为空");
        }
        return taskHrCompanyMapper.selectUnreadCount(hrCompanyId);
    }

    /**
     * 统计人力已完成未读数据
     * @param hrCompanyId
     * @return
     */
    @Override
    public int selectCompleteCount(String hrCompanyId) {
        if (StringUtils.isEmpty(hrCompanyId)) {
            throw new ParamsException("参数不能为空");
        }

        return taskHrCompanyMapper.selectCompleteCount(hrCompanyId);
    }

    /**
     * 更新人力任务查看标识
     * @param taskHrCompanyId
     * @param status            1未完成已读 3已完成已读
     * @return
     */
    @Override
    public String updateTaskHrStatus(String taskHrCompanyId, Integer status) {
        if (StringUtils.isEmpty(taskHrCompanyId) || status == null) {
            throw new ParamsException("参数错误");
        }
        taskHrCompanyMapper.updateStatusById(taskHrCompanyId, status);
        return "成功";
    }

    /**
     * PC端人力接受任务
     * @param id
     * @return
     */
    @Override
    public String taskHracceptPC(String id) {
        if (StringUtils.isEmpty(id) ) {
            throw new ParamsException("参数不能为空");
        }
        Message message = messageMapper.selectByHrId(id);
        messageMapper.updateStatus (message.getPid ());
        TaskHrCompany taskHrCompany = taskHrCompanyMapper.queryByTaskId(id);
        taskMapper.updateStatus(taskHrCompany.getTaskId(),2);
        taskHrCompanyMapper.updateStatus(id,2);
        Inform inform = new Inform();
        inform.setTitle("任务已接受");
        inform.setContent(taskHrCompany.getHrCompanyName() + "接受了派发的任务。");
        inform.setReceiveId(taskHrCompany.getHotelId());
        inform.setAcceptType(3);
        inform.setSendType(2);
        informMapper.insertInform(inform);
        return "成功";
    }

    /**
     * PC端人力拒绝任务
     * @param id
     * @return
     */
    @Override
    public String TaskHrrefusePC(String id,String reason) {

        if (StringUtils.isEmpty(id) ) {
            throw new ParamsException("参数不能为空");
        }
        Message message = messageMapper.selectByHrId(id);
        if (message == null || message.getStatus() == 1) {
            throw new BusinessException("消息已处理");
        }
        messageMapper.updateStatus (message.getPid ());
        TaskHrCompany taskHrCompany = taskHrCompanyMapper.queryByTaskId(id);
        if (taskHrCompany == null) {
            throw new BusinessException("查询不到人力任务数据");
        }
        taskHrCompany.setRefusedReason (reason);
        taskHrCompany.setStatus (3);
        taskHrCompanyMapper.updateById (taskHrCompany);
        //taskMapper.updateStatus (taskHrCompanyMapper.queryByTaskId (id).getTaskId (),8);

        //发送拒绝通知
        InformTemplate inf = informTemplateMapper.selectByCode (InformType.refuse_hotel_task.name ());
        Map<String,String> map = new HashMap<>();
        map.put("hr",taskHrCompany.getHrCompanyName ());
        map.put ("reason",reason);
        String content = StringKit.templateReplace(inf.getContent (), map);
        informService.sendInformInfo (inf.getSendType (),3,content,taskHrCompany.getHotelId (),inf.getTitle ());
        //发送拒绝消息

        Message m = new Message();
        m.setContent(reason);
        MessageTemplate mess = messageTemplateMapper.findFirstByCode ("refuseTaskMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageType(10);
        m.setMessageTitle(mess.getTitle());
        m.setHrCompanyId (taskHrCompany.getHrCompanyId ());
        m.setHrTaskId (taskHrCompany.getPid ());
        m.setHotelId(taskHrCompany.getHotelId ());
        Map<String, String> param = new HashMap<>();
        param.put("userName", taskHrCompany.getHrCompanyName ());
        param.put("content", reason);
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setApplyType(2);
        m.setStatus(0);
        m.setIsTask(0);
        messageMapper.insert(m);

        return "成功";
    }

    /**
     * 人力再派发任务
     * @param request  包含id(消息id,set小时工id集合)
     * @return
     */
    @Override
    public ResultDO hrAssignmentTask(AssignmentRequest request) {

        if (request == null || StringUtils.isEmpty(request.getMessageId()) || request.getSet() == null
                || request.getSet().size() == 0) {
            throw new ParamsException("参数错误");
        }

        Message message = messageMapper.selectById(request.getMessageId());
        if (message == null) {
            throw new BusinessException("查询不到消息");
        }
        message.setStatus(1);
        //消息发送者是酒店，将小时工任务状态设置为3终止，如果是小时工，将状态置为2
        TaskWorker taskWorker = taskWorkerMapper.selectById(message.getWorkerTaskId());
        if (taskWorker == null) {
            throw new BusinessException("查询不到小时工工作任务");
        }
        if (message.getApplicantType() == 3) {
            taskWorker.setStatus(3);
        } else if (message.getApplicantType() == 1) {
            taskWorker.setStatus(2);
        } else {
            throw new ParamsException("参数错误");
        }
        taskWorker.setRefusedReason(message.getContent());
        taskWorkerMapper.updateAllColumnById(taskWorker);

        //更新人力任务信息
        TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById(message.getHrTaskId());
        if (taskHrCompany == null) {
            throw new BusinessException("查询不到人力公司任务");
        }
        taskHrCompany.setConfirmedWorkers(taskHrCompany.getConfirmedWorkers() - 1);
        if (message.getApplicantType() == 1) {
            taskHrCompany.setRefusedWorkers(taskHrCompany.getRefusedWorkers() + 1);
        }
        taskHrCompanyMapper.updateAllColumnById(taskHrCompany);

        //插入小时工任务信息
        TaskWorker workerTask = null;
        List<TaskWorker> list = new ArrayList<>();
        for (String str : request.getSet()) {
            workerTask = new TaskWorker();
            workerTask.setStatus(0);
            workerTask.setDayEndTime(taskWorker.getDayEndTime());
            workerTask.setDayStartTime(taskWorker.getDayStartTime());
            workerTask.setToDate(taskWorker.getToDate());
            workerTask.setFromDate(taskWorker.getFromDate());
            workerTask.setHotelName(taskHrCompany.getHotelName());
            workerTask.setHourlyPay(taskHrCompany.getHourlyPay());
            workerTask.setTaskContent(taskHrCompany.getTaskContent());
            workerTask.setTaskTypeCode(taskHrCompany.getTaskTypeCode());
            workerTask.setTaskTypeText(taskHrCompany.getTaskTypeText());
            workerTask.setTaskHrId(taskHrCompany.getPid());
            workerTask.setWorkerId(message.getWorkerId());
            taskWorkerMapper.insert(workerTask);
            list.add(workerTask);
        }

        //给小时工发送消息
        messageService.hrDistributeWorkerTask(list, taskHrCompany);

        if (message.getApplicantType() == 3) {
            //给酒店发送通知
            //被替换的小时工
            User oldUser = userMapper.selectByWorkerId(taskWorker.getWorkerId());
            taskWorkerMapper.updateStatus(taskWorker.getWorkerId(),3);
            User newUser = userMapper.selectByWorkerId(list.get(0).getWorkerId());
            String content = taskHrCompany.getHrCompanyName() + "同意了你的换人申请，将任务里的小时工" + oldUser.getNickname() + "换成了" + newUser.getNickname();
            informService.sendInformInfo(2, 3, content, message.getHotelId(), "换人成功");

            //给被替换的小时工发通知
            content = message.getMessageContent() + " 。任务重新调配了一个小时工，希望你下次能认真对待工作，这会影响你的信用。";
            informService.sendInformInfo(2, 1, content, message.getWorkerId(), "调换通知");
        }
        return ResultDO.buildSuccess("派发完成");
    }

    /**
     * 人力拒绝酒店调换小时工的申请
     * @param messageId
     * @return
     */
    @Override
    public ResultDO hrRefuseHotelSwapWorker(String messageId) {
        if (StringUtils.isEmpty(messageId)) {
            throw new ParamsException("参数错误");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new ParamsException("查找不到消息");
        }
        message.setStatus(1);
        messageMapper.updateById(message);

        //给酒店发送一条通知
        Company company = companyMapper.findCompanyById(message.getHrCompanyId());
        if (company == null) {
            throw new BusinessException("人力公司查询不到");
        }
        String content = company.getName() + "拒绝了你的换人申请。";
        informService.sendInformInfo(2, 3, content, message.getHotelId(), "换人被拒绝");
        return ResultDO.buildSuccess("成功");
    }

    /**
     * 人力拒绝小时工取消任务
     * @param messageId
     * @return
     */
    @Override
    public ResultDO hrHandleWorkerTaskCancel(String messageId) {
        if (StringUtils.isEmpty(messageId)) {
            throw new ParamsException("参数错误");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new ParamsException("参数错误");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        TaskWorker taskWorker = taskWorkerMapper.selectById(message.getWorkerTaskId());
        if (taskWorker == null) {
            throw new ParamsException("参数错误");
        }

        String content = taskWorker.getHrCompanyName() + "拒绝了你的取消任务申请，希望你能完成该任务。";
        informService.sendInformInfo(2, 1, content, message.getWorkerId(), "申请取消被拒绝");
        return ResultDO.buildSuccess("成功");
    }

    /**
     * 人力同意小时工取消任务并派发新任务
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
        if (message == null) {
            throw new ParamsException("参数错误");
        }
        message.setStatus(1);
        messageMapper.updateById(message);

        TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById(message.getHrTaskId());
        if (taskHrCompany == null) {
            throw new ParamsException("人力任务查询不到.");
        }
        //更新人力任务
        taskHrCompany.setRefusedWorkers(taskHrCompany.getRefusedWorkers() + 1);
        taskHrCompany.setConfirmedWorkers(taskHrCompany.getConfirmedWorkers() - 1);
        if (taskHrCompany.getStatus() == 5) {
            taskHrCompany.setStatus(4);
        }
        taskHrCompanyMapper.updateAllColumnById(taskHrCompany);
        //更新小时工任务
        TaskWorker taskWorker = taskWorkerMapper.selectById(message.getWorkerTaskId());
        if (taskWorker == null) {
            throw new ParamsException("小时工任务查询不到");
        }
        taskWorker.setStatus(2);
        taskWorker.setRefusedReason(message.getContent());
        taskWorkerMapper.updateById (taskWorker);

        //给取消任务的小时工发送通知
        DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm");
        String content = taskWorker.getHrCompanyName() + "同意了你的取消任务申请，申请取消的任务日期是"
                + date.format(message.getSupplementTime()) + ",时间为" + time.format(message.getSupplementTime()) + "-" + time.format(message.getSupplementTimeEnd()) + ",申请理由" + message.getMessageContent();
        informService.sendInformInfo(2, 1, content, message.getWorkerId(), "申请取消成功");

        //给新的小时工派发任务
        TaskWorker workerTask = new TaskWorker();
        workerTask.setStatus(0);
        workerTask.setDayEndTime(taskWorker.getDayEndTime());
        workerTask.setDayStartTime(taskWorker.getDayStartTime());
        workerTask.setToDate(taskWorker.getToDate());
        workerTask.setFromDate(taskWorker.getFromDate());
        workerTask.setHotelName(taskHrCompany.getHotelName());
        workerTask.setHourlyPay(taskHrCompany.getHourlyPay());
        workerTask.setTaskContent(taskHrCompany.getTaskContent());
        workerTask.setTaskTypeCode(taskHrCompany.getTaskTypeCode());
        workerTask.setTaskTypeText(taskHrCompany.getTaskTypeText());
        workerTask.setTaskHrId(taskHrCompany.getPid());
        workerTask.setWorkerId(workerId);
        User us = userMapper.selectByWorkerId (workerId);
        workerTask.setUserId (us.getPid ());
        workerTask.setUserName (us.getNickname ());
        workerTask.setHrCompanyName (taskHrCompany.getHrCompanyName ());
        workerTask.setHotelId (taskHrCompany.getHotelId ());
        workerTask.setHrCompanyId (taskHrCompany.getHrCompanyId ());
        workerTask.setHotelTaskId (taskHrCompany.getTaskId ());
        taskWorkerMapper.insert(workerTask);
        //发送消息
        Map<String, String> param = new HashMap<>();
        param.put("hrCompanyName", taskWorker.getHrCompanyName());
        String notice = messageService.installContent(param, "workTaskMessage");
        Map<String, Object> result = new HashMap<>();
        result.put("hrCompanyId", taskHrCompany.getHrCompanyId());
        result.put("taskHrId", taskHrCompany.getTaskId());
        result.put("workerId", workerId);
        result.put("workerTaskId", workerTask.getPid());
        result.put("hotelId", taskHrCompany.getHotelId());
        result.put("applicantType", 2);
        result.put("applyType", 0);
        result.put("messageContent", notice);
        result.put("messageType", 6);
        result.put("hrTaskId", taskHrCompany.getPid());
        messageService.sendMessageInfo(result);
        return ResultDO.buildSuccess("成功");
    }

    /**
     * 人力公司处理酒店支付
     * @param messageId
     * @param status        0拒绝1同意
     * @param reason
     * @return
     */
    @Override
    public ResultDO hrHandleIncome(String messageId, String status, String reason) {
        if (StringUtils.isEmpty(messageId) || StringUtils.isEmpty(status)) {
            throw new ParamsException("参数不能为空");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new ParamsException("查询不到消息");
        }
        message.setStatus(1);
        messageMapper.updateAllColumnById(message);

        TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById(message.getHrTaskId());
        if (taskHrCompany == null) {
            throw new ParamsException("查询不到人力任务信息");
        }
        if ("0".equals(status)) {
            String content = taskHrCompany.getHrCompanyName() + "拒绝了你发起的一笔支付信息，金额为" + message.getMinutes() + "，拒绝理由为" + message.getContent();
            informService.sendInformInfo(2, 3, content, message.getHotelId(), "账目被拒绝");
        } else if ("1".equals(status)) {
            //确认收入
            taskHrCompany.setHavePayMoney(taskHrCompany.getHavePayMoney() + Double.valueOf(message.getMinutes()));
            taskHrCompanyMapper.updateAllColumnById(taskHrCompany);
            Task task = taskMapper.selectById(taskHrCompany.getTaskId());
            if (task == null) {
                throw new ParamsException("找不到任务信息");
            }
            task.setHavePayMoney(task.getHavePayMoney() + Double.valueOf(message.getMinutes()));
            taskMapper.updateAllColumnById(task);
            //新增酒店支付人力明细
            HotelPayHrDetails details = new HotelPayHrDetails();
            details.setTaskHrId(taskHrCompany.getPid());
            details.setThisPayMoney(Double.valueOf(message.getMinutes()));
            hotelPayHrDetailsService.saveBean(details);
            String content = taskHrCompany.getHrCompanyName() + "同意了你发起的一笔支付信息，金额为" + Double.valueOf(message.getMinutes());
            informService.sendInformInfo(2, 3, content, message.getHotelId(), "账目已同意");
        } else {
            throw new ParamsException("参数错误");
        }
        return ResultDO.buildSuccess("成功");
    }

    /**
     * 人力同意小时工拒绝任务并派发任务
     * @param messageParamDTO
     * @return
     */
    @Override
    public ResultDO hrAgreeWorkerRefuseAndPost(MessageParamDTO messageParamDTO) {
        if (messageParamDTO == null || StringUtils.isEmpty(messageParamDTO.getMessgeId())
                || messageParamDTO.getSet() == null || messageParamDTO.getSet().size() == 0) {
            throw new ParamsException("参数不能为空");
        }

        Message message = messageMapper.selectById(messageParamDTO.getMessgeId());
        if (message == null) {
            throw new ParamsException("查询不到消息");
        }
        message.setStatus(1);
        messageMapper.updateAllColumnById(message);

        TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById(message.getHrTaskId());
        if (taskHrCompany == null) {
            throw new ParamsException("查询不到人力任务信息");
        }
        //更新人力任务的拒绝人数
        taskHrCompany.setRefusedWorkers(taskHrCompany.getRefusedWorkers() + 1);
        taskHrCompanyMapper.updateAllColumnById(taskHrCompany);

        Task task = taskMapper.selectById(taskHrCompany.getTaskId());
        if (task == null) {
            throw new ParamsException("查询不到酒店任务");
        }

        //发送通知给拒绝任务的小时工
        String content = taskHrCompany.getHrCompanyName() + "同意了你的拒绝任务申请,拒绝原因：" + message.getContent();
        informService.sendInformInfo(2, 1, content, message.getWorkerId(), "拒绝任务");

        //派发小时工任务
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> m = null;
        for (String id : messageParamDTO.getSet()){
            System.out.println (id);
            m = new HashMap<>();
            TaskWorker taskWorker=new TaskWorker();
            taskWorker.setTaskHrId(taskHrCompany.getPid());;

            User user = userMapper.queryByWorkerId(id);
            taskWorker.setUserId(user.getPid());
            taskWorker.setWorkerId (user.getWorkerId ());
            taskWorker.setUserName(user.getUsername());
            taskWorker.setStatus(0);
            taskWorker.setFromDate(taskHrCompany.getFromDate());
            taskWorker.setToDate(taskHrCompany.getToDate());
            taskWorker.setHourlyPay(taskHrCompany.getHourlyPay());
            taskWorker.setTaskTypeCode(taskHrCompany.getTaskTypeCode());
            taskWorker.setTaskContent(taskHrCompany.getTaskContent());
            taskWorker.setTaskTypeText(taskHrCompany.getTaskTypeText());
            taskWorker.setHrCompanyName (taskHrCompany.getHrCompanyName ());
            taskWorker.setHrCompanyId (taskHrCompany.getHrCompanyId ());
            taskWorker.setHotelName(taskHrCompany.getHotelName());
            taskWorker.setHotelId (task.getHotelId());
            taskWorker.setDayStartTime(task.getDayStartTime());
            taskWorker.setDayEndTime(task.getDayEndTime());
            taskWorker.setHotelTaskId (task.getPid());
            taskWorker.setHrCompanyId(taskHrCompany.getHrCompanyId());
            taskWorkerMapper.insert(taskWorker);
            m.put("workerId", id);
            m.put("workerTaskId", taskWorker.getPid());
            m.put("hotelId", taskWorker.getHotelId());
            list.add(m);
        }

        //发送消息
        messageService.hrDistributeTask(list, taskHrCompany.getHrCompanyId(), taskHrCompany.getHrCompanyName(), "workTaskMessage", task.getPid(), taskHrCompany.getPid());
        return ResultDO.buildSuccess("派发成功");

    }
}
