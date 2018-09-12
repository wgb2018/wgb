package com.microdev.converter;
import com.microdev.common.utils.DateUtil;
import com.microdev.mapper.UserMapper;
import com.microdev.model.Task;
import com.microdev.model.TaskHrCompany;
import com.microdev.model.TaskWorker;
import com.microdev.model.User;
import com.microdev.param.CreateTaskRequest;
import com.microdev.param.HrTaskWorkersResponse;
import com.microdev.param.TaskViewDTO;
import com.microdev.type.UserSex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * @author yinbaoxin
 */
@Component
public class TaskConverter {

    @Autowired
    private UserMapper userMapper;

    public Task toNewTask(CreateTaskRequest request) {
        Task task=new  Task();
        task.setTaskContent(request.getTaskContent());
        task.setToDate(request.getToDate());
        task.setFromDate(request.getFromDate());
        task.setHourlyPay(request.getHourlyPay());
        task.setDayStartTime (request.getDayStartTime ());
        task.setDayEndTime (request.getDayEndTime ());
        return task;
    }
    //包含人力公司任务集合同时包含人力公司派发任务人员的集合的查询
    public TaskViewDTO toTaskViewWithHrSet(Task task) {
        if(task==null){
            return null;
        }
        TaskViewDTO viewDTO=this.toViewDTOWithOutSet(task);
        /*Set<TaskHrCompanyViewDTO> listTaskHr=viewDTO.getListTaskHr();
        for (TaskHrCompany taskHr:task.getListHrTask()){
            TaskHrCompanyViewDTO TaskHrView=new TaskHrCompanyViewDTO();
            TaskHrView.setTaskHrId(taskHr.getId());
            TaskHrView.setNeedWorkers(taskHr.getNeedWorkers());
            TaskHrView.setConfirmedWorkers(taskHr.getConfirmedWorkers());
            TaskHrView.setRefusedWorkers(taskHr.getRefusedWorkers());
            TaskHrView.setRepastTimes(taskHr.getRepastTimes());
            TaskHrView.setMinutes(taskHr.getMinutes());
            TaskHrView.setShouldPayMoney(taskHr.getShouldPayMoney());
            TaskHrView.setWaitPayMoney(taskHr.getShouldPayMoney()-taskHr.getHavePayMoney());
            TaskHrView.setHavePayMoney(taskHr.getHavePayMoney());
            if(taskHr.getHrCompany()!=null){
                TaskHrView.setHrCompanyName(taskHr.getHrCompany().getName());
                TaskHrView.setHrCompanyId(taskHr.getHrCompany().getId());
            }
            // 人力资源公司包含的小时工
            Set<HrTaskWorkersResponse> confirmedSet=TaskHrView.getConfirmedSet();
            Set<HrTaskWorkersResponse> refusedSet=TaskHrView.getRefusedSet();

            Set<TaskWorker> listWorkerTask = taskHr.getListWorkerTask();
            for (TaskWorker taskWorker:listWorkerTask){
                if(taskWorker.getStatus()==null){
                    continue;
                }
                if(taskWorker.getStatus()==1){
                    confirmedSet.add(this.toWorkerReponse(taskWorker)) ;
                } else if(taskWorker.getStatus()==2){
                    refusedSet.add(this.toWorkerReponse(taskWorker)) ;
                }
            }
            TaskHrView.setConfirmedSet(confirmedSet);
            TaskHrView.setRefusedSet(refusedSet);
            listTaskHr.add(TaskHrView);
        }
        viewDTO.setListTaskHr(listTaskHr);*/
        return viewDTO;
    }

    public HrTaskWorkersResponse toWorkerReponse(TaskWorker taskWorker){
        HrTaskWorkersResponse  hrTaskWorkersResponse=new HrTaskWorkersResponse();
        hrTaskWorkersResponse.setTaskWorkerId(taskWorker.getPid());
        hrTaskWorkersResponse.setRefusedReason(taskWorker.getRefusedReason());
        hrTaskWorkersResponse.setTaskStatus(taskWorker.getStatus());
        hrTaskWorkersResponse.setRepastTimes(taskWorker.getRepastTimes());
        hrTaskWorkersResponse.setMinutes(taskWorker.getMinutes());
        hrTaskWorkersResponse.setHavePayMoney(taskWorker.getHavePayMoney());
        hrTaskWorkersResponse.setShouldPayMoney(taskWorker.getShouldPayMoney());
        hrTaskWorkersResponse.setWaitPayMoney(taskWorker.getShouldPayMoney()-taskWorker.getHavePayMoney());
        hrTaskWorkersResponse.setNoPromise(taskWorker.isNoPromise());
        if(taskWorker.getUserId()!=null){
            User worker = userMapper.selectById(taskWorker.getUserId());
            hrTaskWorkersResponse.setWorkerId(worker.getWorkerId());
            hrTaskWorkersResponse.setWorkerName(worker.getNickname());
            if(worker.getAvatar()!=null){
                hrTaskWorkersResponse.setHeadImage(worker.getAvatar());
            }else{
                hrTaskWorkersResponse.setHeadImage("");
            }
            UserSex sex= worker.getSex();
            if(sex==null){
                hrTaskWorkersResponse.setGender("未知");
            }else if(sex==UserSex.MALE){
                hrTaskWorkersResponse.setGender("男");
            }else{
                hrTaskWorkersResponse.setGender("女");
            }
            hrTaskWorkersResponse.setAge(DateUtil.CaculateAge(worker.getBirthday()));
            hrTaskWorkersResponse.setMobile(worker.getMobile());
        }

        return hrTaskWorkersResponse;
    }


    //不包含人力公司任务集合的查询
    public TaskViewDTO toViewDTOWithOutSet(Task task) {
        TaskViewDTO viewDTO=new TaskViewDTO();
        if(task==null){
            return null;
        }
        viewDTO.setPid(task.getPid());
        viewDTO.setTaskTypeText(task.getTaskTypeText());
        viewDTO.setTaskContent(task.getTaskContent());
        viewDTO.setFromDate(task.getFromDate());
        viewDTO.setToDate(task.getToDate());
        viewDTO.setHourlyPay(task.getHourlyPay());
        viewDTO.setNeedWorkers(task.getNeedWorkers());
        viewDTO.setHotelName(task.getHotelName());
        viewDTO.setConfirmedWorkers(task.getConfirmedWorkers());
        viewDTO.setRefusedWorkers(task.getRefusedWorkers());
        viewDTO.setRepastTimes(task.getRepastTimes());
        viewDTO.setMinutes(task.getMinutes());
        viewDTO.setShouldPayMoney(task.getShouldPayMoney());
        viewDTO.setHavePayMoney(task.getHavePayMoney());
        viewDTO.setWaitPayMoney(task.getShouldPayMoney()-task.getHavePayMoney());
        viewDTO.setDayEndTime (task.getDayEndTime ());
        viewDTO.setDayStartTime (task.getDayStartTime ());
        viewDTO.setPayStatus("未结算");
        viewDTO.setWorkerSettlementNum (task.getWorkerSettlementNum ());
        viewDTO.setWorkerSettlementPeriod (task.getWorkerSettlementPeriod ());
        if(task.getStatus()>=3){
            if(OffsetDateTime.now().isBefore(task.getToDate()) &&  OffsetDateTime.now().isAfter(task.getFromDate())){
                task.setStatus(5);
            }
        }
        if(OffsetDateTime.now().isAfter(task.getToDate())){
            task.setStatus(6);
        }
        viewDTO.setStatus(task.getStatus());
        if(task.getHavePayMoney()>0){
            viewDTO.setPayStatus("结算中");
        }
        if(task.getShouldPayMoney()-task.getHavePayMoney()<=0 && task.getShouldPayMoney()>0){
            viewDTO.setPayStatus("已结算");
        }
        return viewDTO;
    }

}
