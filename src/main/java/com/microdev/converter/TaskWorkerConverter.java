package com.microdev.converter;

import com.microdev.model.Company;
import com.microdev.model.TaskWorker;
import com.microdev.param.WorkTaskViewModel;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * @author yinbaoxin
 */
@Component
public class TaskWorkerConverter {
    public WorkTaskViewModel toViewModel(TaskWorker taskWorker) {
        if(taskWorker==null){
            return null;
        }
        WorkTaskViewModel viewModel=new  WorkTaskViewModel();
        viewModel.setWorkTaskId(taskWorker.getPid());
        viewModel.setFromDate(taskWorker.getFromDate());
        viewModel.setToDate(taskWorker.getToDate());
        viewModel.setStatus(taskWorker.getStatus());
        viewModel.setDayStartTime (viewModel.getFromDate ().toOffsetTime ());
        viewModel.setDayEndTime (viewModel.getToDate ().toOffsetTime ());
        viewModel.setRefusedReason(taskWorker.getRefusedReason());
        viewModel.setHavePayMoney(taskWorker.getHavePayMoney());
        viewModel.setShouldPayMoney(taskWorker.getShouldPayMoney());
        viewModel.setWaitPayMoney(taskWorker.getShouldPayMoney()-taskWorker.getHavePayMoney());
        viewModel.setMinutes(taskWorker.getMinutes());
        viewModel.setRepastTimes(taskWorker.getRepastTimes());

        viewModel.setPayStatus("未结算");
        if(taskWorker.getHavePayMoney()>0){
            viewModel.setPayStatus("结算中");
        }
        if(taskWorker.getShouldPayMoney()-taskWorker.getHavePayMoney()<=0 && taskWorker.getShouldPayMoney()>0){
            viewModel.setPayStatus("已结算");
        }
        if(taskWorker.getToDate ().isAfter (OffsetDateTime.now ()) && taskWorker.getFromDate ().isBefore (OffsetDateTime.now ()) && taskWorker.getStatus () == 1){
            viewModel.setStatus (4);
        }
        if(taskWorker.getToDate ().isBefore (OffsetDateTime.now ()) && taskWorker.getStatus () == 1 ){
            viewModel.setStatus (5);
        }
        viewModel.setHourlyPay(taskWorker.getHourlyPay());
        viewModel.setHrCompanyName(taskWorker.getHrCompanyName());
        viewModel.setTaskTypeText(taskWorker.getTaskTypeText());
        viewModel.setTaskContent(taskWorker.getTaskContent());
        viewModel.setHotelName(taskWorker.getHotelName());
        viewModel.setUnConfirmedPay (taskWorker.getUnConfirmedPay ());
        viewModel.setSettlementPeriod (taskWorker.getSettlementPeriod ());
        viewModel.setSettlementNum (taskWorker.getSettlementNum ());
        viewModel.setHotelLeader (taskWorker.getHotel ().getLeader ());
        viewModel.setHotelMobile (taskWorker.getHotel ().getLeaderMobile ());
        viewModel.setHrLeader (taskWorker.getHrCompany ().getLeader ());
        viewModel.setHrMobile (taskWorker.getHrCompany ().getLeaderMobile ());

        return viewModel;
    }
}
