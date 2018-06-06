package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.DateUtil;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.converter.TaskConverter;
import com.microdev.service.MessageService;
import com.microdev.service.TaskService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
    /**
     * 创建酒店任务
     */
    @Override
    public ResultDO createTask(CreateTaskRequest request) {
        System.out.println ("param:"+request);
        if(request.getFromDateL ()!=null){
            request.setFromDate (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getFromDateL ()),ZoneOffset.systemDefault ()));
            System.out.println (request.getFromDate ());
        }
        if(request.getToDateL ()!=null){
            request.setToDate (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getToDateL ()),ZoneOffset.systemDefault ()));
            System.out.println (request.getToDate ());
        }
        if(request.getDayStartTimeL ()!=null){
            request.setDayStartTime (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getDayStartTimeL ()),ZoneOffset.systemDefault ()).toOffsetTime ());
            System.out.println (request.getDayStartTime ());
        }
        if(request.getDayEndTimeL ()!=null){
            request.setDayEndTime (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getDayEndTimeL ()),ZoneOffset.systemDefault ()).toOffsetTime ());
            System.out.println (request.getDayEndTime ());
        }
        Company hotel=companyMapper.findCompanyById(request.getHotelId());
        if (hotel == null || !StringUtils.hasLength(hotel.getPid()) ) {
            throw new ParamsException("酒店不存在");
        }
        //TODO 判断酒店状态
        if(hotel.getStatus()==null ||hotel.getStatus()!=1){
            throw new ParamsException("酒店状态不是已审核,无法发布任务");
        }

        int needAllWorkers=0;
        for (TaskHrCompanyDTO item : request.getHrCompanySet()) {
            needAllWorkers=needAllWorkers+item.getNeedWorkers();
        }
        Task task=taskConverter.toNewTask(request);
        task.setStatus(1);
        task.setConfirmedWorkers(0);
        task.setRefusedWorkers(0);
        task.setHotelId(hotel.getPid());
        task.setHotelName(hotel.getName());
        task.setNeedWorkers(needAllWorkers);
        task.setTaskTypeText(request.getTaskTypeText());
        task.setTaskTypeCode(request.getTaskTypeCode());
        task.setTaskContent(request.getTaskContent());
        task.setHourlyPay(request.getHourlyPay());

        AddHrTask(task,request);
        taskMapper.insert(task);
        messageService.hotelDistributeTask(request, hotel, "workTaskMessage", task.getPid());
        TaskViewDTO taskDto= taskConverter.toViewDTOWithOutSet(task);
        return ResultDO.buildSuccess("任务发布成功",taskDto);
    }
    /**
     * 获取单条任务
     */
    @Override
    public ResultDO getTaskById(String id) {
        TaskViewDTO taskViewDTO = taskMapper.findTaskAndHrInfoById(id);
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
        List<TaskHrCompanyViewDTO> taskHrList = taskViewDTO.getListTaskHr();
        if (taskHrList != null) {
            Iterator<TaskHrCompanyViewDTO> it = taskHrList.iterator();
            TaskHrCompanyViewDTO dto;
            List<Map<String, Object>> list = null;
            while (it.hasNext()) {
                dto = it.next();
                list = taskWorkerMapper.selectTaskWorkById(dto.getPid());
                List<Map<String, Object>> confirmedList = new ArrayList<>();
                List<Map<String, Object>> refusedList = new ArrayList<>();
                List<Map<String, Object>> distributedList = new ArrayList<>();
                for (Map<String, Object> m : list) {
                    m.put("Age", DateUtil.CaculateAge((OffsetDateTime) m.get("birthday")));
                    distributedList.add(m);
                    if (m.get("taskStatus") == null)
                        continue;
                    if ((Integer) m.get("taskStatus") == 1) {
                        confirmedList.add(m);
                    } else if ((Integer) m.get("taskStatus") == 2) {
                        refusedList.add(m);
                    }

                }
                dto.setRefusedList(refusedList);
                dto.setConfirmedList(confirmedList);
                dto.setDistributedList(distributedList);
            }
        }


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
        extra.put("havePayMoney",havePayMoney);*/
        return ResultDO.buildSuccess(null,result,null,null);

    }
    /**
     * 酒店支付人力公司
     */
    @Override
    public ResultDO hotelPayHr(HotelPayHrRequest payHrRequest) {
        Set<HrPayDetailRequest>  paySet= payHrRequest.getPayHrSet();
        if(paySet.size()==0){
            throw new ParamsException("支付的人力公司列表不能为空");
        }
        Task task=  taskMapper.getFirstById(payHrRequest.getTaskId());

        //List<TaskHrCompany> listHrTask = taskHrCompanyMapper.queryByHotelTaskId(payHrRequest.getTaskId());
            double thisPayMoney=0.0;
            for (HrPayDetailRequest payHr:paySet){
                thisPayMoney=payHr.getThisPayMoney();
                if(thisPayMoney<=0){
                    throw new ParamsException("付款金额不能小于0");
                }
                TaskHrCompany taskHrCompany = taskHrCompanyMapper.queryByTaskId(payHr.getTaskHrId());
                taskHrCompany.setHavePayMoney(taskHrCompany.getHavePayMoney()+thisPayMoney);
                task.setHavePayMoney(task.getHavePayMoney()+ thisPayMoney);
                //记录详情
                /*hotelPayHrDetails = new HotelPayHrDetails();
                hotelPayHrDetails.setTask_hr_id(payHr.getTaskHrId());
                hotelPayHrDetails.setThis_pay_money(thisPayMoney);
                hotelPayHrDetails.setPid(UUID.randomUUID().toString());
                hotelPayHrDetails.setCreate_time(OffsetDateTime.now());
                taskHrCompanyMapper.update(taskHrCompany);*/
                //废弃方法
                //hotelPayDetailsMapper.save(hotelPayHrDetails);
                //支付记录
                //记录详情
                Bill bill = new Bill();
                bill.setTaskId(task.getPid());
                bill.setHotelId(task.getHotelId());
                bill.setHotelName(task.getHotelName());
                bill.setPayMoney(thisPayMoney);
                bill.setHrCompanyId(taskHrCompany.getHrCompanyId());
                bill.setHrCompanyName(taskHrCompany.getHrCompanyName());
                bill.setDeleted(false);
                bill.setPayType(1);
                billMapper.insert(bill);
            }
        taskMapper.updateById(task);
        return ResultDO.buildSuccess("结算成功");
    }

    /**
     * 查询酒店未读任务
     * @param hotelId
     * @return
     */
    @Override
    public int selectUnReadAmount(String hotelId) {
        if (StringUtils.isEmpty(hotelId)) {
            throw new ParamsException("参数不能为空");
        }
        return taskMapper.selectUnReadCount(hotelId);
    }

    /**
     * 查询已完成任务数量
     * @param hotelId
     * @return
     */
    @Override
    public int selectCompleteAmount(String hotelId) {
        if (StringUtils.isEmpty(hotelId)) {
            throw new ParamsException("参数不能为空");
        }
        return taskMapper.selectCompleteCount(hotelId);
    }

    /**
     * 更新任务的状态
     * @param taskId        任务id
     * @param status        1未完成已读3已完成已读
     */
    @Override
    public String updateTaskStatus(String taskId, Integer status) {
        if (StringUtils.isEmpty(taskId) || status == null) {
            throw new ParamsException("参数错误");
        }
        taskMapper.updateTaskCheckSign(taskId, status);
        return "成功";
    }

    /**
     * 酒店再次派发任务
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
        Company hotel=companyMapper.findCompanyById(task.getHotelId());
        if (hotel == null || !StringUtils.hasLength(hotel.getPid()) ) {
            throw new ParamsException("酒店不存在");
        }
        // 判断酒店状态
        if(hotel.getStatus()==null ||hotel.getStatus()!=1){
            throw new ParamsException("酒店状态不是已审核,无法发布任务");
        }
        int needAllWorkers=0;
        for (TaskHrCompanyDTO item : request.getHrCompanySet()) {
            needAllWorkers=needAllWorkers+item.getNeedWorkers();
        }

        AddHrTask(task,request);
        messageService.hotelDistributeTask(request, hotel, "workTaskMessage", request.getTaskId());
        //TaskViewDTO taskDto= taskConverter.toViewDTOWithOutSet(task);
        return ResultDO.buildSuccess("任务发布成功");
    }

    //循环添加人力资源任务
    private  Set<TaskHrCompany> AddHrTask(Task task,CreateTaskRequest createTaskRequest ){
        Set setHrTask= new HashSet<TaskHrCompany>();
        for(TaskHrCompanyDTO hrCompanyDTO:createTaskRequest.getHrCompanySet()){
            if (!StringUtils.hasLength(String.valueOf(hrCompanyDTO.getNeedWorkers())) ||hrCompanyDTO.getNeedWorkers()==0) {
                throw new ParamsException("任务需要的人数不能为空");
            }
            Company hotel=companyMapper.findCompanyById(createTaskRequest.getHotelId());
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
            taskHrCompany.setStatus(1);
            taskHrCompany.setRefusedWorkers(0);
            taskHrCompany.setConfirmedWorkers(0);
            taskHrCompany.setNeedWorkers(hrCompanyDTO.getNeedWorkers());
            taskHrCompany.setDeleted(false);
            taskHrCompany.setTaskTypeText(createTaskRequest.getTaskTypeText());
            taskHrCompany.setTaskTypeCode(createTaskRequest.getTaskTypeCode());
            taskHrCompany.setTaskContent(createTaskRequest.getTaskContent());
			taskHrCompany.setHourlyPayHotel(task.getHourlyPay());
            taskHrCompanyMapper.insert(taskHrCompany);
            setHrTask.add(taskHrCompany);
        }
        return setHrTask;
    }
}
