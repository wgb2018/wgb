package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.DateUtil;
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
            taskWorker.setUserName(user.getUsername());
            taskWorker.setStatus(0);
            taskWorker.setFromDate(hotelTask.getFromDate());
            taskWorker.setToDate(hotelTask.getToDate());
            taskWorker.setHourlyPay(hrTask.getHourlyPay());
            taskWorker.setTaskTypeCode(hrTask.getTaskTypeCode());
            taskWorker.setTaskContent(hrTask.getTaskContent());
            taskWorker.setTaskTypeText(hrTask.getTaskTypeText());
            taskWorker.setHotelName(hrTask.getHotelName());
            taskWorker.setDayStartTime(hotelTask.getDayStartTime());
            taskWorker.setDayEndTime(hotelTask.getDayEndTime());
            taskWorkerMapper.insert(taskWorker);
            m.put("workerId", id);
            m.put("workerTaskId", pid);
            list.add(m);
        }
        taskHrCompanyMapper.updateById(hrTask);
        messageService.hrDistributeTask(list, hrTask.getHrCompanyId(), hrTask.getHrCompanyName(), "workTaskMessage");
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
        PageInfo<TaskHrCompany> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
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
        }
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
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(messageId)) {
            throw new ParamsException("参数不能为空");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new BusinessException("消息已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        taskMapper.updateStatus(taskHrCompany.getTaskId(),2);
        taskHrCompanyMapper.updateStatus(id,2);
    }
    /**
     * 人力公司拒绝任务
     */
    @Override
    public void TaskHrrefuse(String id, String messageId) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(messageId)) {
            throw new ParamsException("参数不能为空");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new BusinessException("消息已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        taskHrCompanyMapper.updateStatus(id,3);
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
    public ResultDO getHotelBill(String hotelId) {
        List<TaskHrCompany> list = taskHrCompanyMapper.queryHotelBill(hotelId);
        Map<String,Object> map = new HashMap<>();
        Double should_pay_money = 0.0;
        Double have_pay_money  = 0.0;
        for (TaskHrCompany item:list) {
            should_pay_money += item.getShouldPayMoney();
            have_pay_money += item.getHavePayMoney();
        }
        map.put("shouldPayMoney",should_pay_money);
        map.put("havePayMoney",have_pay_money);
        return ResultDO.buildSuccess(null,list,map,null);
    }
    /**
     * 人力公司按酒店查询账目
     */
    @Override
    public ResultDO getCompanyBillHotel(String hrCompanyId) {
        List<TaskHrCompany> list = taskHrCompanyMapper.queryHrCompanyBill(hrCompanyId);
        Map<String,Object> map = new HashMap<>();
        Double should_pay_money = 0.0;
        Double have_pay_money  = 0.0;
        for (TaskHrCompany item:list) {
            should_pay_money += item.getShouldPayMoney();
            have_pay_money += item.getHavePayMoney();
        }
        map.put("shouldPayMoney",should_pay_money);
        map.put("havePayMoney",have_pay_money);
        return ResultDO.buildSuccess(null,list,map,null);
    }
    /**
     * 人力公司按小时工查询账目
     */
    @Override
    public ResultDO getCompanyBillWorker(String hrCompanyId) {
        List<TaskWorker> list = taskWorkerMapper.queryHrCompanyBill(hrCompanyId);
        Map<String,Object> map = new HashMap<>();
        Double should_pay_money = 0.0;
        Double have_pay_money  = 0.0;
        for (TaskWorker item:list) {
            should_pay_money += item.getShouldPayMoney();
            have_pay_money += item.getHavePayMoney();
        }
        map.put("shouldPayMoney",should_pay_money);
        map.put("havePayMoney",have_pay_money);
        return ResultDO.buildSuccess(null,list,map,null);
    }
    /**
     * 小时工按人力公司工查询账目
     *
     */
    @Override
    public ResultDO getWorkerBill(String workerId) {
        List<TaskWorker> list = taskWorkerMapper.queryWorkerBill(workerId);
        Map<String,Object> map = new HashMap<>();
        Double should_pay_money = 0.0;
        Double have_pay_money  = 0.0;
        for (TaskWorker item:list) {
            should_pay_money += item.getShouldPayMoney();
            have_pay_money += item.getHavePayMoney();
        }
        map.put("shouldPayMoney",should_pay_money);
        map.put("havePayMoney",have_pay_money);
        return ResultDO.buildSuccess(null,list,map,null);
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

}
