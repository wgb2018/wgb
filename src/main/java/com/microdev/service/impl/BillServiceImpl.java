package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.mapper.BillMapper;
import com.microdev.mapper.TaskHrCompanyMapper;
import com.microdev.mapper.TaskMapper;
import com.microdev.mapper.UserMapper;
import com.microdev.model.Bill;
import com.microdev.model.Task;
import com.microdev.model.TaskHrCompany;
import com.microdev.model.User;
import com.microdev.param.HotelPayHrCompanyRequest;
import com.microdev.param.HrCompanyPayWorkerRequest;
import com.microdev.param.PayRecord;
import com.microdev.service.BillService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class BillServiceImpl extends ServiceImpl<BillMapper,Bill> implements BillService{
    @Autowired
    BillMapper billMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TaskHrCompanyMapper taskHrCompanyMapper;
    @Autowired
    private TaskMapper taskMapper;
    /**
     * 用人单位按人力公司查询支付记录
     */
    @Override
    public ResultDO queryHotelPayHrCompany(Paginator paginator, HotelPayHrCompanyRequest request) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<Bill> list = billMapper.selectHotelPayBill(request);
        PageInfo<Bill> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        //设置数据集合rows：
        return ResultDO.buildSuccess(result);
    }

    @Override
    public ResultDO queryHotelPayWorker(Paginator paginator, HotelPayHrCompanyRequest request) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<Bill> list = billMapper.selectHotelPayBillWorker(request);
        PageInfo<Bill> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        //设置数据集合rows：
        return ResultDO.buildSuccess(result);
    }

    @Override
    public ResultDO queryHrCompanyPayWorker(Paginator paginator,HrCompanyPayWorkerRequest request) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<Bill> list = billMapper.selectHrCompanyPayBill(request);
        PageInfo<Bill> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        //设置数据集合rows：
        return ResultDO.buildSuccess(result);
    }

    /**
     * 更新评论状态
     * @param id
     * @return
     */
    @Override
    public ResultDO updateCommentStatus(String id) {
        if (StringUtils.isEmpty(id)) {
            return ResultDO.buildError ("参数不能为空");
        }
        Bill bill = billMapper.selectById(id);
        if (bill == null) {
            return ResultDO.buildError ("查询不到支付信息");
        }
        bill.setStatus(1);
        billMapper.updateById(bill);
        return ResultDO.buildSuccess("操作成功");
    }

    /**
     * 查询小时工的收益记录
     * @return
     */
    @Override
    public Map<String, Object> queryWorkerMoneyRecord(HrCompanyPayWorkerRequest request) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(request.getWorkerId())  || StringUtils.isEmpty(request.getHrCompanyTaskId())) {
            result.put("list", new ArrayList<PayRecord>());
            return result;
        }
        User u = userMapper.selectByWorkerId(request.getWorkerId());
        int count = billMapper.selectHrCompanyPayBillCount(request);
        PageHelper.startPage(1, count, true);
        List<PayRecord> list = billMapper.selectHrCompanyPayBillRecord(request);
        if (list == null) {
            list = new ArrayList<>();
        }

        result.put("name", u.getNickname());
        result.put("list", list);
        return result;
    }

    /**
     * 查看人力收益记录
     * @param request
     * @return
     */
    @Override
    public Map<String, Object> queryHrMoneyRecord(HotelPayHrCompanyRequest request) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(request.getHotelTaskID()) || StringUtils.isEmpty(request.getHrTaskId())) {
            result.put("list", new ArrayList<PayRecord>());
            return result;
        }
        TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById(request.getHrTaskId());
        int count = billMapper.selectHotelPayBillCount(request);
        PageHelper.startPage(1, count, true);
        List<PayRecord> list = billMapper.selectHotelPayBillRecord(request);
        if (list == null) {
            list = new ArrayList<PayRecord>();
        }
        result.put("list", list);
        result.put("name", taskHrCompany.getHrCompanyName());
        return result;
    }

    @Override
    public ResultDO queryWorkerStatistics(String id) {
        //billMapper.select
        return null;
    }

    /**
     * 用人单位查询支付小时工记录
     * @param request
     * @return
     */
    @Override
    public Map<String, Object> queryHotelPayWorkerRecord(HotelPayHrCompanyRequest request) {

        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(request.getHotelTaskID()) || StringUtils.isEmpty(request.getWorkerId())) {
            result.put("list", new ArrayList<PayRecord>());
            return result;
        }
        Task task = taskMapper.selectById(request.getHotelTaskID());
        int count = billMapper.selectHotelPayBillWorkerCount(request);
        List<PayRecord> list = null;
        if (count > 0) {
            PageHelper.startPage(1, count, true);
            list = billMapper.selectHotelPayBillWorkerRecord(request);
        }
        if (list == null) {
            list = new ArrayList<PayRecord>();
        }
        result.put("list", list);
        result.put("name", task.getHotelName());
        return result;
    }
}
