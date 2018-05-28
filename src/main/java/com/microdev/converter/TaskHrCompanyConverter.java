package com.microdev.converter;
import com.microdev.mapper.TaskMapper;
import com.microdev.model.Task;
import com.microdev.model.TaskHrCompany;
import com.microdev.model.TaskWorker;
import com.microdev.param.HrTaskWorkersResponse;
import com.microdev.param.TaskHrCompanyViewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author yinbaoxin
 */
@Component
public class TaskHrCompanyConverter {
    @Autowired
    TaskMapper taskMapper;
    //包含小时工(已接受和已拒绝任务的人员)的数据
    public TaskHrCompanyViewDTO toViewDTO(TaskHrCompany taskHr) {
        TaskHrCompanyViewDTO viewDTO=this.toViewWithOutWorkerSet(taskHr);

        // 人力资源公司包含的小时工
       /* Set<HrTaskWorkersResponse> confirmedSet=viewDTO.getConfirmedSet();
        Set<HrTaskWorkersResponse> refusedSet=viewDTO.getRefusedSet();
        Set<HrTaskWorkersResponse> distributedSet=viewDTO.getDistributedSet();*/

        /*Set<TaskWorker> listWorkerTask = taskHr.getListWorkerTask();
        for (TaskWorker taskWorker:listWorkerTask){
            distributedSet.add(this.toWorkerReponse(taskWorker));
            if(taskWorker.getStatus()==null){
                continue;
            }
            if(taskWorker.getStatus()==1){
                confirmedSet.add(this.toWorkerReponse(taskWorker)) ;
            } else if(taskWorker.getStatus()==2){
                refusedSet.add(this.toWorkerReponse(taskWorker)) ;
            }
        }*/
        /*viewDTO.setDistributedSet(distributedSet);
        viewDTO.setConfirmedSet(confirmedSet);
        viewDTO.setRefusedSet(refusedSet);*/
        return viewDTO;
    }
    //私有方法 ：人力资源公司派发的任务中，人员列表数据
    private HrTaskWorkersResponse toWorkerReponse(TaskWorker taskWorker){
        HrTaskWorkersResponse  hrTaskWorkersResponse=new HrTaskWorkersResponse();
        hrTaskWorkersResponse.setRefusedReason(taskWorker.getRefusedReason());
        hrTaskWorkersResponse.setTaskStatus(taskWorker.getStatus());
        hrTaskWorkersResponse.setRepastTimes(taskWorker.getRepastTimes());
        hrTaskWorkersResponse.setMinutes(taskWorker.getMinutes());
        hrTaskWorkersResponse.setHavePayMoney(taskWorker.getHavePayMoney());
        hrTaskWorkersResponse.setShouldPayMoney(taskWorker.getShouldPayMoney());
        hrTaskWorkersResponse.setWaitPayMoney(taskWorker.getShouldPayMoney()-taskWorker.getHavePayMoney());
        hrTaskWorkersResponse.setTaskWorkerId(taskWorker.getPid());
        if(taskWorker.getUserName()!=null){
            //User worker=taskWorker.getUser();
            hrTaskWorkersResponse.setWorkerId(taskWorker.getUserId());
            hrTaskWorkersResponse.setWorkerName(taskWorker.getUserName());
            /*UserSex sex= worker.getSex();
            if(sex==null){
                hrTaskWorkersResponse.setGender("未知");
            }else if(sex==UserSex.MALE){
                hrTaskWorkersResponse.setGender("男");
            }else{
                hrTaskWorkersResponse.setGender("女");
            }
            if(worker.getAvatar()!=null){
                hrTaskWorkersResponse.setHeadImage(worker.getAvatar());
            }else{
                hrTaskWorkersResponse.setHeadImage("");
            }

            hrTaskWorkersResponse.setAge(DateUtil.CaculateAge(worker.getBirthday()));
            hrTaskWorkersResponse.setMobile(worker.getMobile());*/
        }

        return hrTaskWorkersResponse;
    }

    //不包含小时工任务的数据
    public TaskHrCompanyViewDTO toViewWithOutWorkerSet(TaskHrCompany taskHr) {
        TaskHrCompanyViewDTO viewDTO=new TaskHrCompanyViewDTO();
        if(taskHr==null){
            return null;
        }
        viewDTO.setPid(taskHr.getPid());
        viewDTO.setHourlyPay(taskHr.getHourlyPay());
        viewDTO.setNeedWorkers(taskHr.getNeedWorkers());
        viewDTO.setConfirmedWorkers(taskHr.getConfirmedWorkers());
        viewDTO.setRefusedWorkers(taskHr.getRefusedWorkers());
        viewDTO.setHrCompanyName(taskHr.getHrCompanyName());
        viewDTO.setStatus(taskHr.getStatus());
        viewDTO.setRepastTimes(taskHr.getRepastTimes());
        viewDTO.setMinutes(taskHr.getMinutes());
        viewDTO.setShouldPayMoney(taskHr.getShouldPayMoney());
        viewDTO.setHavePayMoney(taskHr.getHavePayMoney());
        viewDTO.setWaitPayMoney(taskHr.getShouldPayMoney()-taskHr.getHavePayMoney());
        viewDTO.setWorkersHavePay(taskHr.getWorkersHavePay());
        viewDTO.setWorkersShouldPay(taskHr.getWorkersShouldPay());
        viewDTO.setWorkersWaitPay(taskHr.getWorkersShouldPay()-taskHr.getWorkersHavePay());
        viewDTO.setPayStatus("未结算");
        if(taskHr.getWorkersHavePay()>0){
            viewDTO.setPayStatus("结算中");
        }
        if(taskHr.getWorkersShouldPay()-taskHr.getWorkersHavePay()<=0 && taskHr.getWorkersShouldPay()>0){
            viewDTO.setPayStatus("已结算");
        }
        Task task = taskMapper.getFirstById(taskHr.getTaskId());
        if(task!=null){
            viewDTO.setHotelTaskId(task.getPid());
            viewDTO.setTaskTypeText(task.getTaskTypeText());
            viewDTO.setTaskContent(task.getTaskContent());
            viewDTO.setFromDate(task.getFromDate());
            viewDTO.setToDate(task.getToDate());
            viewDTO.setHotelHourlyPay(task.getHourlyPay());
        }
        viewDTO.setHotelName(taskHr.getHotelName());
        //viewDTO.setAddress(hotel.getAddress());
        return viewDTO;
    }





}
