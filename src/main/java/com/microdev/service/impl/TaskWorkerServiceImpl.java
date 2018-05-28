package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.converter.TaskWorkerConverter;
import com.microdev.mapper.TaskHrCompanyMapper;
import com.microdev.mapper.TaskMapper;
import com.microdev.mapper.TaskWorkerMapper;
import com.microdev.mapper.UserCompanyMapper;
import com.microdev.model.Task;
import com.microdev.model.TaskHrCompany;
import com.microdev.model.TaskWorker;
import com.microdev.param.RefusedTaskRequest;
import com.microdev.param.TaskWorkerQuery;
import com.microdev.service.TaskHrCompanyService;
import com.microdev.service.TaskWorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
     * 领取任务
     */
    @Override
    public ResultDO receivedTask(String workerId, String workerTaskId) {
        TaskWorker taskWorker = taskWorkerMapper.findFirstById(workerTaskId);
        if (taskWorker.getStatus() > 0) {
            throw new BusinessException("任务状态不是新派发,无法接受任务");
        }
        if (taskWorker.getFromDate().isBefore(OffsetDateTime.now())) {
            throw new BusinessException("任务已过期，无法接受");
        }
        //TODO 人数判断
        TaskHrCompany taskHr = taskHrCompanyMapper.queryByTaskId(taskWorker.getTaskHrId());
        Integer confirmedWorkers = taskHr.getConfirmedWorkers();
        if (confirmedWorkers == null) {
            confirmedWorkers = 0;
        }
        if (confirmedWorkers + 1 > taskHr.getNeedWorkers()) {
            throw new BusinessException("人数已满,无法接受任务");
        }

        //TODO 是否和已有任务时间冲突

        taskWorker.setStatus(1);
        taskWorker.setConfirmedDate(OffsetDateTime.now());
        //TODO 酒店人数加1
        Task hotelTask=taskMapper.getFirstById(taskHr.getHotelId());
        Integer hotelConfirmedWorkers=hotelTask.getConfirmedWorkers();
        if(hotelConfirmedWorkers==null){
            hotelConfirmedWorkers=0;
        }
        hotelTask.setConfirmedWorkers(hotelConfirmedWorkers+1);
        if(hotelTask.getConfirmedWorkers() == hotelTask.getNeedWorkers()){
            taskMapper.updateStatus(hotelTask.getPid(),4);
        }
        //TODO 人力公司人数加1
        taskHr.setConfirmedWorkers(confirmedWorkers+1);
        if(taskHr.getConfirmedWorkers() == taskHr.getNeedWorkers()){
            taskHrCompanyMapper.updateStatus(taskHr.getPid(),6);
        }
        taskWorker.setRefusedReason("");
        taskWorkerMapper.updateById(taskWorker);
        taskMapper.updateById(hotelTask);
        taskHrCompanyMapper.updateById(taskHr);

        return ResultDO.buildSuccess("任务领取成功");
    }
    /**
     * 拒绝任务
     */
    @Override
    public ResultDO refusedTask(RefusedTaskRequest refusedTaskReq) {
        if (!StringUtils.hasLength(refusedTaskReq.getRefusedReason())) {
            throw new ParamsException("拒绝理由不能为空");
        }
        if (!StringUtils.hasLength(refusedTaskReq.getWorkerId())) {
            throw new ParamsException("小时工不能为空");
        }
        if (!StringUtils.hasLength(refusedTaskReq.getWorkerTaskId())) {
            throw new ParamsException("任务不能为空");
        }
        TaskWorker taskWorker = taskWorkerMapper.findFirstById(refusedTaskReq.getWorkerTaskId());
        if(taskWorker.getStatus()>0){
            throw new BusinessException("任务状态不是新派发,无法接受任务");
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
        Task hotelTask=taskMapper.getFirstById(taskHr.getHotelId());
        Integer hotelRefuseWorkers=hotelTask.getRefusedWorkers();
        if(hotelRefuseWorkers==null){
            hotelRefuseWorkers=0;
        }
        hotelTask.setRefusedWorkers(hotelRefuseWorkers+1);
        taskWorkerMapper.updateById(taskWorker);
        taskMapper.updateById(hotelTask);
        taskHrCompanyMapper.updateById(taskHr);
        return ResultDO.buildSuccess("拒绝任务成功");
    }
    /**
     * 分页查询任务
     */
    @Override
    public ResultDO pagesTaskWorkers(Paginator paginator, TaskWorkerQuery taskQueryDTO) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<TaskWorker> list = taskWorkerMapper.findAll(taskQueryDTO);
        PageInfo<TaskWorker> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess(result);
    }
}
