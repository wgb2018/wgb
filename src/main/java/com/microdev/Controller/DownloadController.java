package com.microdev.Controller;

import com.microdev.common.utils.ExcelUtil;
import com.microdev.model.Company;
import com.microdev.param.DownLoadAccount;
import com.microdev.param.HotelPayHrCompanyRequest;
import com.microdev.param.PayRecord;
import com.microdev.param.TaskWorkerQuery;
import com.microdev.service.BillService;
import com.microdev.service.CompanyService;
import com.microdev.service.TaskWorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
public class DownloadController {

    @Autowired
    private TaskWorkerService taskWorkerService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private BillService billService;

    /**
     * 用人单位下载小时工结账
     * @param taskQueryDTO
     * @param response
     */
    @GetMapping("/hotel/download/worker/pay")
    public void downloadHotelPayWorker(@ModelAttribute TaskWorkerQuery taskQueryDTO, HttpServletResponse response) {
        List<DownLoadAccount> list = taskWorkerService.queryWorkerAccount(taskQueryDTO);
        String name = "";
        Company c = companyService.selectById(taskQueryDTO.getHotelId());
        if (c != null) {
            name = c.getName();
        }
        ExcelUtil.download(response, list, ExcelUtil.workerAccount, "结账详情", name + "结账单");
    }

    /**
     * 用人单位查询支付小时工记录
     * @param request
     * @param response
     */
    @GetMapping("/hotel/pay/worker/account")
    public void downloadHotelPayWorkerAccount(@ModelAttribute HotelPayHrCompanyRequest request, HttpServletResponse response) {
        Map<String, Object> result = billService.queryHotelPayWorkerRecord(request);
        String name = (String)result.get("name");
        if (name == null) {
            name = "";
        }
        ExcelUtil.download(response, (List<PayRecord>)result.get("list"), ExcelUtil.payRecord, "支付记录", name + "付款记录");
    }
}
