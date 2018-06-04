package com.microdev.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
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
import com.microdev.service.MessageService;
import com.microdev.service.UserCompanyService;
import com.microdev.type.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
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
    private MessageService messageService;
    @Autowired
    private MessageMapper messageMapper;
	@Autowired
    TaskConverter taskConverter;
    @Autowired
    TaskWorkerMapper taskWorkerMapper;
    @Autowired
    private InformMapper informMapper;
    /**
     * 小时工绑定人力公司
     */
    @Override
    public ResultDO workerBindHr(String messageId, String status) {
        if (StringUtils.isEmpty(messageId) || StringUtils.isEmpty(status)) {
            throw new ParamsException("参数messageId不能为空");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new ParamsException("消息已被处理");
        }

        boolean flag = true;
        Message newMess = new Message();
        User user=  userMapper.queryByWorkerId(message.getWorkerId());
        if(user==null){
            throw new ParamsException("未找到匹配的员工信息");
        }
        Inform inform = new Inform();
        inform.setReceiveId(message.getHrCompanyId());
        inform.setAcceptType(2);
        inform.setSendType(1);
        if ("0".equals(status)) {
            inform.setTitle("绑定被拒绝");
            inform.setContent("小时工" + user.getUsername() + "拒绝了你的绑定申请。");
        } else if ("1".equals(status)) {
            UserCompany userCompany= userCompanyMapper.findOneUserCompany(message.getHrCompanyId(),message.getWorkerId());
            if(userCompany==null){
                userCompany=new UserCompany();

                Company company= companyMapper.findCompanyById(message.getWorkerId());
                if(company==null){
                    throw new ParamsException("未找到匹配的公司信息");
                }
                userCompany.setCompanyId(company.getPid());
                userCompany.setCompanyType(company.getCompanyType());
                userCompany.setUserId(user.getPid());
                userCompany.setUserType(user.getUserType());
                flag = false;
            }
            //TODO 绑定上限设置
            DictDTO dict = dictMapper.findByNameAndCode("WorkerBindHrMaxNum","1");
            Integer maxNum = Integer.parseInt(dict.getText());
            Wrapper<UserCompany>  wrapper = new EntityWrapper<>();
            wrapper.and("user_id", userCompany.getUserId());
            wrapper.and("company_id", message.getHrCompanyId());
            wrapper.in("status", new Integer[]{0, 1, 3});
            Integer hasBindNum = userCompanyMapper.selectCount(wrapper);
            if (hasBindNum >= maxNum) {
                throw new BusinessException("已达到可绑定人力公司的个数上限");
            }
            //TODO 已绑定是否返回重复绑定提示
            userCompany.setStatus(1);
            if (flag) {
                // 修改
                userCompany.setStatus(1);
                userCompanyMapper.updateAllColumnById(userCompany);

            } else {
                // 新增
                userCompany.setStatus(1);
                userCompany.setDeleted(false);
                userCompanyMapper.insert(userCompany);
            }
            inform.setTitle("绑定成功");
            inform.setContent("小时工" + user.getUsername() + "同意了你的绑定申请，成功添加为合作伙伴，添加合作人力公司即代表同意劳务合作协议。你可以接受合作的人力公司派发的任务，确保能够及时完美的完成任务，可以获得相应的酬劳。");
        } else {
            return ResultDO.buildSuccess("失败");
        }
        informMapper.insert(inform);
        return    ResultDO.buildSuccess("添加成功");
    }
    /**
     * 小时工解绑人力公司
     */
    @Override
    public ResultDO workerUnbindHr(String workerId, String hrId) {

        UserCompany userCompany= userCompanyMapper.selectByWorkerIdHrId(hrId,workerId);
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
        User user = userMapper.selectById(userCompany.getUserId());
        if (user == null) {
            throw new ParamsException("用户不存在");
        }
        Set<String> set = new HashSet<>();
        set.add(hrId);
        messageService.bindHrCompany(user.getWorkerId(), set, user.getUsername(), "applyUnbindMessage");
        return    ResultDO.buildSuccess("解绑已提交");
    }

    /**
     * 根据人力公司获取小时工
     */
    @Override
    public ResultDO getHrWorkers(Paginator paginator, HrQueryWorkerDTO queryDTO) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        Map<String,Object> map = new HashMap<>();
        /*map.put("company_id",queryDTO.getHrId());
        map.put("status","1 or status = 3");*/
        Wrapper<UserCompany> et = new EntityWrapper<UserCompany> ().where("company_id={0}",queryDTO.getHrId()).in("status","1,3");
        List<User> list = null;
        User us = null;
        //List<String> ids = new ArrayList<>();
        HashMap<String,Object> result = new HashMap<>();
        if(queryDTO.getTaskId()==null) {
            List<UserCompany> list1 = userCompanyMapper.selectList (et);
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
        if (StringUtils.isEmpty(queryDTO.getWorkerId())) {
            throw new ParamsException("参数错误");
        }
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合

        List<WorkerBindCompany> list = userCompanyMapper.selectHrCompanyByUserId(queryDTO.getWorkerId());
        OffsetDateTime nowTime = OffsetDateTime.now();
        OffsetDateTime applyTime = null;
        for (WorkerBindCompany work : list) {
            if (work.getStatus() == 3) {
                applyTime = work.getCreateTime();
                long leaveMinute = nowTime.getLong(ChronoField.MINUTE_OF_DAY) - applyTime.getLong(ChronoField.MINUTE_OF_DAY);
                int hour = (int)(leaveMinute % 60 == 0 ? leaveMinute / 60 : (leaveMinute / 60) + 1);
                DictDTO dict = dictMapper.findByNameAndCode("MaxUnbindDay","22");
                Integer maxNum = Integer.parseInt(dict.getText());
                hour = maxNum * 24 - hour <= 0 ? 0 : maxNum * 24 - hour;
                work.setHour(hour/24 + "天" + hour%24 + "小时");
            }
        }
        PageInfo<WorkerBindCompany> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",list);
        result.put("page",paginator.getPage());
        Map<String, Object> extra = new HashMap<>();
        String total = dictMapper.findByNameAndCode ("WorkerBindHrMaxNum","1").getText ();
        int num = userCompanyMapper.selectBindCountByWorkerId(queryDTO.getWorkerId());
        extra.put("bindTotalNum",Integer.parseInt (total));
        extra.put("bindNum",num);
        return ResultDO.buildSuccess(null, result, extra, null);
    }

    @Override
    public ResultDO removeHrWorkers(List<String> request) {
        taskWorkerMapper.deleteBatchIds(request);
        return ResultDO.buildSuccess("移除成功");
    }

    /**
     * 人力申请绑定小时工
     * @param hrId      人力公司id
     * @param set       小时工workerId集合
     * @return
     */
    @Override
    public String hrApplyBindWorker(String hrId, List<String> set) {
        if (StringUtils.isEmpty(hrId) || set == null || set.size() == 0) {
            throw new ParamsException("参数错误");
        }
        List<String> userList = userMapper.selectIdByWorkerId(set);
        if (set.size() != userList.size()) {
            throw new BusinessException("set包含无效数据");
        }
        Company c = companyMapper.selectById(hrId);
        if (c == null) {
            throw new ParamsException("人力公司查询不到");
        }
        System.out.println ("hrId:"+hrId);
        System.out.println ("userList:"+userList);
        int count = userCompanyMapper.selectIsbind(hrId, userList);
        if (count > 0) {
            throw new BusinessException("提交过申请");
        }
        UserCompany userCompany = null;
        List<UserCompany> userCompanyList = new ArrayList<>();
        for (String str : userList) {
            userCompany = new UserCompany();
            userCompany.setCompanyId(hrId);
            userCompany.setUserType(UserType.worker);
            userCompany.setCompanyType(2);
            userCompany.setUserId(str);
            userCompany.setStatus(0);
            userCompanyList.add(userCompany);
        }
        userCompanyMapper.saveBatch(userCompanyList);
        messageService.bindUserHrCompany(c.getName(), hrId, set, 2);
        return "绑定成功";
    }

    /**
     * 查询对人力发出申请的小时工的待审核信息
     * @param hrCompanyId
     * @param page
     * @param pageNum
     * @return
     */
    @Override
    public ResultDO selectUserByHrId(String hrCompanyId, Integer page, Integer pageNum) {
        if (StringUtils.isEmpty(hrCompanyId)) {
            throw new ParamsException("参数错误");
        }
        PageHelper.startPage(page, pageNum, true);
        List<Map<String, Object>> list = userCompanyMapper.selectUserByHrId(hrCompanyId);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
        Map<String, Object> param = new HashMap<>();
        param.put("page", page);
        param.put("total", pageInfo.getTotal());
        param.put("result", list);
        return ResultDO.buildSuccess(param);
    }

    /**
     * 人力查询合作的小时工
     * @param map
     * @param page
     * @param pageNum
     * @return
     */
    @Override
    public ResultDO selectWorkerCooperate(QueryCooperateRequest map, Integer page, Integer pageNum) {
        if (StringUtils.isEmpty(map.getId())) {
            throw new ParamsException("参数错误");
        }
        PageHelper.startPage(page, pageNum, true);
        List<Map<String, Object>> list = companyMapper.selectCooperateWorker(map);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
        Map<String, Object> param = new HashMap<>();
        param.put("page", page);
        param.put("total", pageInfo.getTotal());
        param.put("result", list);
        return ResultDO.buildSuccess(param);
    }

    /**
     * 人力公司处理小时工绑定申请
     * @param messageId
     * @param status        0拒绝1同意
     * @return
     */
    @Override
    public ResultDO hrRespondWorkerBind(String messageId, String status) {
        if (StringUtils.isEmpty(messageId) || !"0".equals(status) || "1".equals(status)) {
            return ResultDO.buildError("参数错误");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            return ResultDO.buildError("消息id错误");
        }
        message.setStatus(1);
        messageMapper.updateById(message);

        if ("1".equals(status)) {

            UserCompany userCompany = userCompanyMapper.selectByWorkerIdHrId(message.getHrCompanyId(), message.getWorkerId());
            userCompany.setStatus(1);
            userCompanyMapper.update(userCompany);
        }
        return ResultDO.buildSuccess("绑定成功");
    }
}
