package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.fasterxml.jackson.annotation.JsonFormat;
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
import com.microdev.service.MessageService;
import com.microdev.service.TaskHrCompanyService;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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

    /**
     * 查看人力资源公司的任务
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
        List<Map<String, Object>> list = taskWorkerMapper.selectTaskWorkById((String) map.get("id"));
        List<Map<String, Object>> confirmedList = new ArrayList<>();
        List<Map<String, Object>> refusedList = new ArrayList<>();
        List<Map<String, Object>> distributedList = new ArrayList<>();
        for (Map<String, Object> m : list) {
            m.put("Age", DateUtil.caculateAge((Timestamp) m.get("birthday")));
            distributedList.add(m);
            if (m.get("taskStatus") == null)
                continue;
            if ((Integer) m.get("taskStatus") == 1) {
                confirmedList.add(m);
            } else if ((Integer) m.get("taskStatus") == 2) {
                refusedList.add(m);
            }

        }
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
        if (!StringUtils.hasLength(hrTaskDis.getId())) {
            throw new ParamsException("人力公司id不能为空");
        }
        if (hrTaskDis.getWorkerIds().size()==0) {
            throw new ParamsException("请选择派发的员工");
        }

        // 获取人力公司任务和酒店任务信息
        TaskHrCompany hrTask= taskHrCompanyMapper.queryByTaskId(hrTaskDis.getId());
        if(hrTask==null){
            throw new ParamsException("人力公司参数有误");
        }
        //Company hrCompany=companyMapper.findCompanyById(hrTask.getHrCompanyId());
        taskMapper.updateStatus(hrTask.getTaskId(),3);
        Company hrCompany=companyMapper.findCompanyById(hrTask.getHrCompanyId());
        hrTask.setStatus(4);
        hrTask.setHourlyPay(hrTaskDis.getHourlyPay());
        Task hotelTask=taskMapper.getFirstById(hrTask.getTaskId());
        if(hotelTask==null){
            throw new BusinessException("任务派发失败：未获取酒店到任务");
        }
       // Company hotel=companyMapper.findCompanyById(hrTask.getHotelId());
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> m = null;
        for (String id:hrTaskDis.getWorkerIds()){
            m = new HashMap<>();
            TaskWorker taskWorker=new TaskWorker();
            String pid = UUID.randomUUID().toString();
            taskWorker.setPid(pid);
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
            taskWorker.setHotelName(hrTask.getHotelName());
            taskWorker.setHotelId (hotelTask.getHotelId ());
            taskWorker.setDayStartTime(hotelTask.getDayStartTime());
            taskWorker.setDayEndTime(hotelTask.getDayEndTime());
            taskWorkerMapper.insert(taskWorker);
            m.put("workerId", id);
            m.put("workerTaskId", pid);
            list.add(m);
        }
        taskHrCompanyMapper.updateById(hrTask);
        messageService.hrDistributeTask(list, hrTask.getHrCompanyId(), hrTask.getHrCompanyName(), "workTaskMessage", hotelTask.getPid());
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
            taskWorkerMapper.selectTaskWorkById (task.getPid ());
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
    public void TaskHraccept(String id, String messageId) {
        System.out.println ("id:"+id+"messageId:"+messageId);
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(messageId)) {
            throw new ParamsException("参数不能为空");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new BusinessException("消息已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
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
    }
    /**
     * 人力公司拒绝任务
     */
    @Override
    public void TaskHrrefuse(String id, String messageId, String reason) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(messageId)) {
            throw new ParamsException("参数不能为空");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new BusinessException("消息已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        TaskHrCompany taskHrCompany = taskHrCompanyMapper.queryByTaskId(id);
        if (taskHrCompany == null) {
            throw new BusinessException("查询不到人力任务数据");
        }
        taskHrCompanyMapper.updateStatus(id,3);
		taskMapper.updateStatus (taskHrCompanyMapper.queryByTaskId (id).getTaskId (),8);

		//发送拒绝消息
        Map<String, String> param = new HashMap<>();
        param.put("userName", taskHrCompany.getHrCompanyName());
        param.put("startId", taskHrCompany.getHrCompanyId());
        param.put("endId", taskHrCompany.getHotelId());
        param.put("type", "1");
        param.put("reason", reason);
        messageService.refuseTask(param);
    }
    /**
     * 人力公司任务调配
     */
    @Override
    public void TaskHrallocate(String id, String reason, Integer number) {
        if (number == null || number < 1 || StringUtils.isEmpty(id)) {
            throw new ParamsException("参数错误");
        }
        TaskHrCompany t = taskHrCompanyMapper.selectById(id);
        if (t == null) {
            throw new BusinessException("查找不到人力资源任务");
        }
        t.setStatus(5);
        taskHrCompanyMapper.update(t);
        messageService.sendMessage(t, reason, String.valueOf(number), "applyChangeMessage");

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
            throw new ParamsException("参数不能为空");
        }
        if (StringUtils.isEmpty(map.get("hrCompanyId"))) {
            throw new ParamsException("参数不能为空");
        }
        if (StringUtils.isEmpty(map.get("number")) || (Integer) map.get("number") < 0) {
            throw new ParamsException("参数不能小于0");
        }
        if (StringUtils.isEmpty(map.get("reason"))) {
            throw new ParamsException("参数不能为空");
        }

        Message m = new Message();
        m.setContent((String)map.get("reason"));
        Company company = companyMapper.selectById(map.get("hrCompanyId").toString());
        MessageTemplate mess = messageTemplateMapper.findFirstByCode("applyChangeMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageTitle(mess.getTitle());
        Map<String, String> param = new HashMap<>();
        param.put("hrCompanyName", company.getName());
        param.put("reason", (String)map.get("reason"));
        param.put("number", (String)map.get("number"));
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setMessageType(4);
        m.setApplyType(3);
        m.setStatus(0);

        messageMapper.insert(m);
        return ResultDO.buildSuccess(true);
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

        TaskHrCompany taskHrCompany = taskHrCompanyMapper.queryByTaskId(id);
        taskMapper.updateStatus(taskHrCompany.getTaskId(),2);
        taskHrCompanyMapper.updateStatus(id,2);
        return "成功";
    }

    /**
     * PC端人力拒绝任务
     * @param id
     * @return
     */
    @Override
    public String TaskHrrefusePC(String id) {

        if (StringUtils.isEmpty(id) ) {
            throw new ParamsException("参数不能为空");
        }

        TaskHrCompany taskHrCompany = taskHrCompanyMapper.queryByTaskId(id);
        if (taskHrCompany == null) {
            throw new BusinessException("查询不到人力任务数据");
        }
        taskHrCompanyMapper.updateStatus(id,3);
        taskMapper.updateStatus (taskHrCompanyMapper.queryByTaskId (id).getTaskId (),8);
        return "成功";
    }

    /**
     * 人力再派发任务
     * @param request  包含id(消息id,set小时工id集合)
     * @return
     */
    @Override
    public ResultDO hrAssignmentTask(AssignmentRequest request) {
        if (request == null || StringUtils.isEmpty(request.getId()) || request.getSet() == null
                || request.getSet().size() == 0) {
            throw new ParamsException("参数错误");
        }
        Message message = messageMapper.selectById(request.getId());
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
            taskWorkerMapper.insertAllColumn(workerTask);
        }

        //给小时工发送消息
        messageService.hrDistributeWorkerTask(new ArrayList<String>(request.getSet()), taskHrCompany);
        return ResultDO.buildSuccess("派发完成");
    }
}
