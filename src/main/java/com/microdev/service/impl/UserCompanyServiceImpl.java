package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.DateUtil;
import com.microdev.converter.TaskConverter;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.service.UserCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.*;

@Transactional
@Service
public class UserCompanyServiceImpl extends ServiceImpl<UserCompanyMapper,UserCompany> implements UserCompanyService {
    @Autowired
    UserCompanyMapper userCompanyMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    CompanyMapper companyMapper;
    @Autowired
    DictMapper dictMapper;
    @Autowired
    TaskConverter taskConverter;
    @Autowired
    TaskWorkerMapper taskWorkerMapper;
    /**
     * 小时工绑定人力公司
     */
    @Override
    public ResultDO workerBindHr(String workerId, String hrId) {
        UserCompany userCompany= userCompanyMapper.findOneUserCompany(hrId,workerId);
        if(userCompany==null){
            userCompany=new UserCompany();
            User user=  userMapper.queryByWorkerId(workerId);
            if(user==null){
                throw new ParamsException("未找到匹配的员工信息");
            }
            Company company= companyMapper.findCompanyById(hrId);
            if(company==null){
                throw new ParamsException("未找到匹配的公司信息");
            }
            userCompany.setCompanyId(hrId);
            userCompany.setCompanyType(company.getCompanyType());
            userCompany.setUserId(user.getPid());
            userCompany.setUserType(user.getUserType());
        }
        //TODO 绑定上限设置
        //TODO 已绑定是否返回重复绑定提示
        userCompany.setStatus(1);
        userCompanyMapper.insert(userCompany);
        return    ResultDO.buildSuccess("添加成功");
    }
    /**
     * 小时工解绑人力公司
     */
    @Override
    public ResultDO workerUnbindHr(String workerId, String hrId) {
        UserCompany userCompany= userCompanyMapper.findOneUserCompany(hrId,workerId);
        if(userCompany==null){
            throw new ParamsException("未找到匹配的信息");
        }
        //TODO 已绑定是否返回重复绑定提示WorkerRelieveHrDays
        DictDTO dict= dictMapper.findByNameAndCode("WorkerRelieveHrDays","1");
        Integer days=Integer.parseInt(dict.getText());
        userCompany.setStatus(3);
        OffsetDateTime releaseTime=OffsetDateTime.now().plusDays(days);
        userCompany.setRelieveTime(releaseTime);
        userCompanyMapper.updateById(userCompany);
        return    ResultDO.buildSuccess("解绑已提交");
    }

    /**
     * 根据人力公司获取小时工
     */
    @Override
    public ResultDO getHrWorkers(Paginator paginator, HrQueryWorkerDTO queryDTO) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        Map map = new HashMap<String,Object>();
        map.put("company_id",queryDTO.getHrId());
        map.put("status",1);
        List<User> list = null;
        User us = null;
        //List<String> ids = new ArrayList<>();
        HashMap<String,Object> result = new HashMap<>();
        if(queryDTO.getTaskId()==null) {
            List<UserCompany> list1 = userCompanyMapper.selectByMap(map);
            for (UserCompany u:list1) {
                us = userMapper.selectById(u.getUserId());
                us.setAge(DateUtil.CaculateAge(us.getBirthday()));
                u.setUser(us);
            }
            PageInfo<UserCompany> pageInfo = new PageInfo<UserCompany>(list1);
            //设置获取到的总记录数total：
            result.put("total",pageInfo.getTotal());
            //设置数据集合rows：
            result.put("result",pageInfo.getList());
            result.put("page",paginator.getPage());
            //list = userMapper.selectBatchIds(ids);
        }else{
            list = userCompanyMapper.getSelectableWorker(queryDTO);
            PageInfo<User> pageInfo = new PageInfo<User>(list);
            //设置获取到的总记录数total：
            result.put("total",pageInfo.getTotal());
            //设置数据集合rows：
            result.put("result",pageInfo.getList());
            result.put("page",paginator.getPage());
        }
        return ResultDO.buildSuccess(result);
    }

    @Override
    public ResultDO getHrwWorkers(Paginator paginator, HrQueryWorkerDTO queryDTO) {
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize());
        List<TaskWorker> list1 = userCompanyMapper.getUnSelectableWorker(queryDTO);
        PageInfo<TaskWorker> pageInfo = new PageInfo<TaskWorker>(list1);
        List<HrTaskWorkersResponse> list = new ArrayList<>();
        for (TaskWorker taskWorker:pageInfo.getList()) {
            list.add(taskConverter.toWorkerReponse(taskWorker));
        }
        HashMap<String, Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total", pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result", list);
        result.put("page", paginator.getPage());
        return ResultDO.buildSuccess(result);
    }

    /**
     * 根据小时工获取人力公司
     */
    @Override
    public ResultDO getWorkerHrs(Paginator paginator, WokerQueryHrDTO queryDTO) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        Map map = new HashMap<String,Object>();
        map.put("user_id",queryDTO.getWorkerId());
        map.put("status",1);
        List<UserCompany> list = userCompanyMapper.selectByMap(map);
        PageInfo<UserCompany> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",list);
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess(result);
    }

    @Override
    public ResultDO removeHrWorkers(List<String> request) {
        taskWorkerMapper.deleteBatchIds(request);
        return ResultDO.buildSuccess("移除成功");
    }
}
