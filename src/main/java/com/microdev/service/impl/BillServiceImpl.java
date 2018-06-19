package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.paging.Paginator;
import com.microdev.mapper.BillMapper;
import com.microdev.model.Bill;
import com.microdev.model.TaskHrCompany;
import com.microdev.param.HotelPayHrCompanyRequest;
import com.microdev.param.HrCompanyPayWorkerRequest;
import com.microdev.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class BillServiceImpl extends ServiceImpl<BillMapper,Bill> implements BillService{
    @Autowired
    BillMapper billMapper;
    /**
     * 酒店按人力公司查询支付记录
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
}
