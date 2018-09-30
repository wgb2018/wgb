package com.microdev.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.FilePush;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.DateUtil;
import com.microdev.common.utils.RedisUtil;
import com.microdev.converter.TaskConverter;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.service.MessageService;
import com.microdev.service.UserCompanyService;
import com.microdev.type.ConstantData;
import com.microdev.type.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(UserCompanyServiceImpl.class);
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
    @Autowired
    TaskHrCompanyMapper taskHrCompanyMapper;
    @Autowired
    TaskMapper taskMapper;
    @Autowired
    WorkerMapper workerMapper;
    @Autowired
    private FilePush filePush;
    @Autowired
    private RedisUtil redisUtil;

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
            return ResultDO.buildError ("消息已被处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        boolean flag = true;

        User user=  userMapper.queryByWorkerId(message.getWorkerId());
        if(user==null){
            throw new ParamsException("未找到匹配的员工信息");
        }
        Inform inform = new Inform();
        inform.setReceiveId(message.getHrCompanyId());
        inform.setAcceptType(2);
        inform.setSendType(1);
        UserCompany userCompany;
        if(message.getHrCompanyId() == null){
            userCompany = userCompanyMapper.selectByWorkerIdHrId(message.getHotelId (),message.getWorkerId());
        }else{
            userCompany = userCompanyMapper.selectByWorkerIdHrId(message.getHrCompanyId(),message.getWorkerId());
        }
        if ("0".equals(status)) {
            inform.setTitle("绑定被拒绝");
            inform.setContent("小时工" + user.getNickname() + "拒绝了你的绑定申请。");
            if(userCompany==null){
                userCompany=new UserCompany();

                Company company= companyMapper.findCompanyById(message.getHrCompanyId());
                if(company==null){
                    throw new ParamsException("未找到匹配的公司信息");
                }
                userCompany.setCompanyId(company.getPid());
                userCompany.setCompanyType(company.getCompanyType());
                userCompany.setUserId(user.getPid());
                userCompany.setUserType(user.getUserType());
                userCompany.setStatus (2);
                userCompanyMapper.insert (userCompany);
            }else{
                userCompany.setRelieveTime(OffsetDateTime.now());
                userCompany.setStatus(2);
                userCompanyMapper.updateById (userCompany);
            }

        } else if ("1".equals(status)) {
            Company company;
            if(message.getHrCompanyId() != null){
                company = companyMapper.findCompanyById(message.getHrCompanyId());
            }else{
                company = companyMapper.findCompanyById(message.getHotelId ());
            }
            if(userCompany==null) {
                userCompany = new UserCompany ( );
                if (company == null) {
                    throw new ParamsException ("未找到匹配的公司信息");
                }
                userCompany.setCompanyId(company.getPid());
                userCompany.setCompanyType(company.getCompanyType());
                userCompany.setUserId(user.getPid());
                userCompany.setUserType(user.getUserType());
                flag = false;
            }
                Worker worker = workerMapper.queryById (user.getWorkerId ());
                if(worker.getActiveCompanys () == null){
                    worker.setActiveCompanys (0);
                }
                if(worker.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("WorkerBindHrMaxNum","7").getText ())){
                    userCompany.setStatus(2);
                    userCompanyMapper.updateAllColumnById(userCompany);
                    inform.setTitle("绑定被拒绝");
                    inform.setContent(user.getNickname ()+"已达到可绑定公司的个数上限");
                    informMapper.insertInform(inform);
                    return ResultDO.buildSuccess ("超出小时工绑定公司数目上限");
                }
                worker.setActiveCompanys (worker.getActiveCompanys ()+1);
                if(worker.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("WorkerBindHrMaxNum","7").getText ())){
                    worker.setBindCompanys (false);
                }
                workerMapper.updateById (worker);
                if(company.getActiveWorkers () == null){
                    company.setActiveWorkers (0);
                }
                if(company.getActiveWorkers () == Integer.parseInt (dictMapper.findByNameAndCode ("HrBindWorkerMaxNum","10").getText ())){
                    userCompany.setStatus(2);
                    userCompanyMapper.updateAllColumnById(userCompany);
                    inform.setTitle("绑定被拒绝");
                    inform.setContent("已达到可绑定小时工的个数上限");
                    informMapper.insertInform(inform);
                    return ResultDO.buildSuccess ("超出公司绑定小时工数目上限");
                }
                company.setActiveWorkers (company.getActiveWorkers ()+1);
                if(company.getActiveWorkers () == Integer.parseInt (dictMapper.findByNameAndCode ("HrBindWorkerMaxNum","10").getText ())){
                    company.setBindWorkers (false);
                }
                companyMapper.updateById (company);
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
                try{
                    userCompanyMapper.insert(userCompany);
                }catch (Exception e){
                    e.printStackTrace ();
                    return ResultDO.buildSuccess ("添加成功");
                }

            }
            inform.setTitle("绑定成功");
            if(company.getCompanyType () == 1){
                inform.setContent("小时工" + user.getNickname() + "同意了你的绑定申请，成功添加为合作伙伴，添加合作用人单位即代表同意劳务合作协议。你可以向小时工派发任务。");
            }else{
                inform.setContent("小时工" + user.getNickname() + "同意了你的绑定申请，成功添加为合作伙伴，添加合作人力公司即代表同意劳务合作协议。你可以向小时工派发任务。");
            }
        } else {
            return ResultDO.buildSuccess("失败");
        }
        inform.setModifyTime(OffsetDateTime.now());
        inform.setCreateTime(OffsetDateTime.now());
        informMapper.insertInform(inform);
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
        if(userCompany.getStatus () == 3){
            return  ResultDO.buildSuccess("解绑申请已提交过");
        }
        //TODO 已绑定是否返回重复绑定提示WorkerRelieveHrDays
        DictDTO dict= dictMapper.findByNameAndCode("WorkerRelieveHrDays","8");
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
        messageService.bindHrCompany(user.getWorkerId(), set, user.getNickname(), "applyUnbindMessage", "");
        return    ResultDO.buildSuccess("解绑已提交");
    }

    @Override
    public ResultDO workerUnbindHotel(String workerId, String hotelId,String reason) {
        System.out.println ("workerId:"+workerId);
        System.out.println ("hotelId:"+hotelId);
        System.out.println ("reason:"+reason);
        if (StringUtils.isEmpty(workerId) || StringUtils.isEmpty(hotelId) || StringUtils.isEmpty(reason)) {
            throw new ParamsException("参数不能为空");
        }
        UserCompany userCompany= userCompanyMapper.selectByWorkerIdHrId(hotelId,workerId);
        if(userCompany==null){
            throw new ParamsException("未找到匹配的信息");
        }
        if(userCompany.getStatus () == 3){
            return  ResultDO.buildSuccess("解绑申请已提交过");
        }
        //TODO 已绑定是否返回重复绑定提示WorkerRelieveHrDays
        DictDTO dict= dictMapper.findByNameAndCode("WorkerRelieveHrDays","8");
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
        set.add(hotelId);
        messageService.bindHotelCompany(user.getWorkerId(), set, user.getNickname(), "applyUnbindMessage", reason);
        return    ResultDO.buildSuccess("解绑已提交");
    }

    /**
     * 根据人力公司获取小时工
     */
    @Override
    public ResultDO getHrWorkers(Paginator paginator, HrQueryWorkerDTO queryDTO) {
        //查询数据集合
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> map1 = new HashMap <> ();
        Wrapper<UserCompany> et = null;
        if(queryDTO.getUserName ()!=null){
            et = new EntityWrapper<UserCompany> ().where("company_id={0}",queryDTO.getHrId()).in("status","1,3");
        }else{
            et = new EntityWrapper<UserCompany> ().where("company_id={0}",queryDTO.getHrId()).in("status","1,3");
        }
        List<User> list = null;
        User us = null;
        HashMap<String,Object> result = new HashMap<>();
        if(queryDTO.getTaskId()==null) {
            PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
            List<UserCompany> list1 = userCompanyMapper.selectAllWorker(queryDTO);
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

            Company company = companyMapper.findCompanyById (queryDTO.getHrId ());
            map1.put ("bindNum",company.getActiveWorkers ());
            map1.put("bindTotalNum",Integer.parseInt (dictMapper.findByNameAndCode ("HrBindWorkerMaxNum","10").getText ()));
            //list = userMapper.selectBatchIds(ids);
        }else{
            boolean ifTimeConflict = false;
            list = userCompanyMapper.getSelectableWorker(queryDTO);
            Task task = taskMapper.getFirstById (taskHrCompanyMapper.queryByTaskId (queryDTO.getTaskId()).getTaskId ());
            Iterator<User> it = list.iterator ();
            while(it.hasNext ()){
                List<TaskWorker> li = taskWorkerMapper.findByUserId (it.next ().getPid ());
                for (TaskWorker ts:li) {
                    if(!(task.getDayStartTime ().isAfter (ts.getDayEndTime ()) || ts.getDayStartTime ().isAfter (task.getDayEndTime ()))){
                        if(!(task.getFromDate ().isAfter (ts.getToDate ()) || ts.getFromDate ().isAfter (task.getToDate ()))){
                            ifTimeConflict = true;
                            break;
                        }
                    }
                }
                if(ifTimeConflict){
                    System.out.println ("去除时间冲突的小时工："+it);
                    it.remove ();
                    ifTimeConflict = false;
                }
            }
            result.put("total",list.size ());
            int size = list.size();
            int a = (paginator.getPage ()-1)*paginator.getPageSize ()<size?(paginator.getPage ()-1)*paginator.getPageSize ():size-size%paginator.getPageSize ();
            int b = paginator.getPage ()*paginator.getPageSize ()<size?paginator.getPage ()*paginator.getPageSize ():size;
            list = list.subList (a,b);
            //list = list.subList (0,2);
            //设置获取到的总记录数total：
            //设置数据集合rows：
            result.put("result",list);
            result.put("page",paginator.getPage());
        }
        if(map1.isEmpty ()){
            return ResultDO.buildSuccess(null,result,map1,null);
        }else{
            Integer total = Integer.parseInt (map1.get("bindTotalNum").toString ())-Integer.parseInt (map1.get("bindNum").toString ());
            return ResultDO.buildSuccess(
                    "您已经绑定"+map1.get("bindNum")+"个小时工，还可以绑定"+total+"个小时工",result,map1,null);
        }

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

        List<WorkerBindCompany> list = userCompanyMapper.selectHrCompanyByUserId(queryDTO.getWorkerId(),queryDTO.getType (),queryDTO.getName ());
        OffsetDateTime nowTime = OffsetDateTime.now();
        DictDTO dict = dictMapper.findByNameAndCode("MaxUnbindDay","9");
        Integer maxNum = Integer.parseInt(dict.getText());
        OffsetDateTime applyTime = null;
        for (WorkerBindCompany work : list) {
            if (work.getStatus() == 3) {
                applyTime = work.getModifyTime();
                long leaveMinute = (nowTime.toEpochSecond() - applyTime.toEpochSecond()) / 60;
                int hour = (int)(leaveMinute % 60 == 0 ? leaveMinute / 60 : (leaveMinute / 60) + 1);
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
        String total = dictMapper.findByNameAndCode ("WorkerBindHrMaxNum","7").getText ();
        int num = userCompanyMapper.selectBindCountByWorkerId(queryDTO.getWorkerId());
        extra.put("bindTotalNum",Integer.parseInt (total));
        extra.put("bindNum",num);
        return ResultDO.buildSuccess("", result, extra, null);
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
    public String hrApplyBindWorker(String hrId, Set<String> set) {
        if (StringUtils.isEmpty(hrId) || set == null || set.size() == 0) {
            throw new ParamsException("参数错误");
        }
        List<String> list = new ArrayList<>(set);
        List<String> userList = userMapper.selectIdByWorkerId(list);

        if (set.size() != userList.size()) {
            throw new BusinessException("set包含无效数据");
        }
        Company c = companyMapper.selectById(hrId);
        if (c == null) {
            throw new ParamsException("人力公司查询不到");
        }

        int count = userCompanyMapper.selectIsbind(hrId, userList);
        if (count > 0) {
            return "申请提交重复";
        }
        UserCompany userCompany = null;
        List<UserCompany> userCompanyList = new ArrayList<>();
        //从redis中查询是否有协议
        String path = redisUtil.getString("defaultWorkerHrProtocol");
        if (StringUtils.isEmpty(path)) {
            try {
                path = filePush.pushFileToServer(ConstantData.CATALOG.getName(), ConstantData.WORKHRPROTOCOL.getName());            } catch (Exception e) {
                e.printStackTrace();
                return "服务异常";
            }
            redisUtil.setString("defaultWorkerHrProtocol", path);
        }
        for (String str : userList) {
            userCompany = new UserCompany();
            userCompany.setCompanyId(hrId);
            userCompany.setUserType(UserType.worker);
            userCompany.setCompanyType(2);
            userCompany.setUserId(str);
            userCompany.setStatus(0);
            userCompany.setBindProtocol(path);
            UserCompany temp = userCompanyMapper.findOneUserCompany (hrId,str);
            if(temp == null){
                userCompanyList.add(userCompany);
            }else{
                temp.setStatus(0);
                temp.setBindProtocol(path);
                userCompanyMapper.updateById(temp);
                continue;
            }
        }
        if(userCompanyList.size ()>0){
            try {
                userCompanyMapper.saveBatch (userCompanyList);
            }catch (Exception e){
                return  "申请提交重复";
            }
        }
        messageService.bindUserHrCompany(c.getName(), hrId, list, 2);
        return "申请已发送";
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

        if (StringUtils.isEmpty(messageId) || (!"0".equals(status) && !"1".equals(status))) {
            return ResultDO.buildError("参数错误");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            return ResultDO.buildError("消息id错误");
        }
        if (message.getStatus() == 1) {
            return ResultDO.buildError("消息已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        Inform inform = new Inform();
        inform.setCreateTime(OffsetDateTime.now());
        inform.setModifyTime(OffsetDateTime.now());
        inform.setReceiveId(message.getWorkerId());
        inform.setSendType(2);
        inform.setAcceptType(1);
        UserCompany userCompany = userCompanyMapper.selectByWorkerIdHrId(message.getHrCompanyId(), message.getWorkerId());
        if (userCompany == null) {
            throw new ParamsException("查询不到小时工和人力的关联信息");
        }
        Company company = companyMapper.selectById(userCompany.getCompanyId());
        if ("1".equals(status)) {
            if(company.getActiveWorkers () == null){
                company.setActiveWorkers (0);
            }
            if(company.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("HrBindWorkerMaxNum","10").getText ())){
                userCompany.setStatus(2);
                userCompanyMapper.update(userCompany);
                inform.setTitle("绑定被拒绝");
                inform.setContent(company.getName() + "人力公司超出了绑定小时工数目上限");
                return ResultDO.buildSuccess ("超过人力公司绑定小时工数目上限");
            }
            Worker worker = workerMapper.queryById (userMapper.queryByUserId (userCompany.getUserId ()).getWorkerId ());
            if(worker.getActiveCompanys () == null){
                worker.setActiveCompanys (0);
            }
            if(worker.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("WorkerBindHrMaxNum","7").getText ())){
                userCompany.setStatus(2);
                userCompanyMapper.update(userCompany);
                inform.setTitle("绑定被拒绝");
                inform.setContent("超过小时工绑定人力公司数目上限");
                return ResultDO.buildSuccess ("超过小时工绑定人力公司数目上限");
            }
            worker.setActiveCompanys (worker.getActiveCompanys ()+ 1);
            if(worker.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("WorkerBindHrMaxNum","7").getText ())){
                worker.setBindCompanys (false);
            }
            company.setActiveWorkers (company.getActiveWorkers ()+1);
            if(company.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("HrBindWorkerMaxNum","10").getText ())){
                company.setBindWorkers (false);
            }
            workerMapper.updateById (worker);
            companyMapper.updateById (company);
            userCompany.setStatus(1);
            userCompanyMapper.update(userCompany);
            inform.setTitle("绑定成功");
            inform.setContent(company.getName() + "同意了你的绑定申请，成功添加为合作人力公司，添加人力公司代表同意劳务合作协议。合作的人力公司可以向你派发任务，认真完成能获得相应的报酬。");
        } else {
            userCompany.setStatus(2);
            userCompanyMapper.update(userCompany);
            inform.setTitle("绑定被拒绝");
            inform.setContent(company.getName() + "拒绝了你的绑定申请，等以后有机会希望可以再合作。");
        }
        informMapper.insertInform(inform);
        return ResultDO.buildSuccess("处理成功");
    }
    /**
     * 用人单位处理小时工绑定申请
     * @param messageId
     * @param status        0拒绝1同意
     * @return
     */
    @Override
    public ResultDO hotelRespondWorkerBind(String messageId, String status) {
        if (StringUtils.isEmpty(messageId) || (!"0".equals(status) && !"1".equals(status))) {
            return ResultDO.buildError("参数错误");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            return ResultDO.buildError("消息id错误");
        }
        if (message.getStatus() == 1) {
            return ResultDO.buildError("消息已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        Inform inform = new Inform();
        inform.setCreateTime(OffsetDateTime.now());
        inform.setModifyTime(OffsetDateTime.now());
        inform.setReceiveId(message.getWorkerId());
        inform.setSendType(3);
        inform.setAcceptType(1);
        UserCompany userCompany = userCompanyMapper.selectByWorkerIdHrId(message.getHotelId (), message.getWorkerId());
        if (userCompany == null) {
            throw new ParamsException("查询不到小时工和用人单位的关联信息");
        }
        Company company = companyMapper.selectById(userCompany.getCompanyId());
        if ("1".equals(status)) {
            if(company.getActiveWorkers () == null){
                company.setActiveWorkers (0);
            }
            if(company.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("HrBindWorkerMaxNum","10").getText ())){
                userCompany.setStatus(2);
                userCompanyMapper.update(userCompany);
                inform.setTitle("绑定被拒绝");
                inform.setContent(company.getName() + "用人单位超出了绑定小时工数目上限");
                return ResultDO.buildSuccess ("超过用人单位绑定小时工数目上限");
            }
            Worker worker = workerMapper.queryById (userMapper.queryByUserId (userCompany.getUserId ()).getWorkerId ());
            if(worker.getActiveCompanys () == null){
                worker.setActiveCompanys (0);
            }
            if(worker.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("WorkerBindHrMaxNum","7").getText ())){
                userCompany.setStatus(2);
                userCompanyMapper.update(userCompany);
                inform.setTitle("绑定被拒绝");
                inform.setContent("超过小时工绑定用人单位数目上限");
                return ResultDO.buildSuccess ("超过小时工绑定用人单位数目上限");
            }
            worker.setActiveCompanys (worker.getActiveCompanys ()+ 1);
            if(worker.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("WorkerBindHrMaxNum","7").getText ())){
                worker.setBindCompanys (false);
            }
            company.setActiveWorkers (company.getActiveWorkers ()+1);
            if(company.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("HrBindWorkerMaxNum","10").getText ())){
                company.setBindWorkers (false);
            }
            workerMapper.updateById (worker);
            companyMapper.updateById (company);
            userCompany.setStatus(1);
            userCompanyMapper.update(userCompany);
            inform.setTitle("绑定成功");
            inform.setContent(company.getName() + "同意了你的绑定申请，成功添加为合作用人单位，添加用人单位代表同意劳务合作协议。合作的用人单位可以向你派发任务，认真完成能获得相应的报酬。");
        } else {
            userCompany.setStatus(2);
            userCompanyMapper.update(userCompany);
            inform.setTitle("绑定被拒绝");
            inform.setContent(company.getName() + "拒绝了你的绑定申请，等以后有机会希望可以再合作。");
        }
        informMapper.insertInform(inform);
        return ResultDO.buildSuccess("处理成功");
    }

    /**
     * 工作者申请解绑人力
     * @param param
     * @return
     */
    @Override
    public ResultDO workerApplyUnbindHr(Map<String, String> param) {
        if (StringUtils.isEmpty(param.get("workerId")) || StringUtils.isEmpty(param.get("hrId"))
                || StringUtils.isEmpty(param.get("reason"))) {
            throw new ParamsException("参数不能为空");
        }
        UserCompany userCompany= userCompanyMapper.selectByWorkerIdHrId(param.get("hrId"), param.get("workerId"));
        if(userCompany==null){
            throw new ParamsException("未找到匹配的信息");
        }
        if(userCompany.getStatus () == 3){
            return  ResultDO.buildSuccess("解绑申请已提交过");
        }
        //TODO 已绑定是否返回重复绑定提示WorkerRelieveHrDays
        DictDTO dict= dictMapper.findByNameAndCode("WorkerRelieveHrDays","8");
        Integer days=Integer.parseInt(dict.getText());
        userCompany.setStatus(3);
        userCompany.setRefusedReason(param.get("reason"));
        OffsetDateTime releaseTime=OffsetDateTime.now().plusDays(days);
        userCompany.setRelieveTime(releaseTime);
        userCompanyMapper.updateById(userCompany);
        User user = userMapper.selectById(userCompany.getUserId());
        if (user == null) {
            throw new ParamsException("用户不存在");
        }
        Set<String> set = new HashSet<>();
        set.add(param.get("hrId"));
        messageService.bindHrCompany(user.getWorkerId(), set, user.getNickname(), "applyUnbindMessage", param.get("reason"));
        return    ResultDO.buildSuccess("解绑已提交");
    }

    /**
     * 查询小时工绑定的人力公司
     * @param queryDTO
     * @return
     */
    @Override
    public List<CompanyCooperate> queryWorkerBindHr(WokerQueryHrDTO queryDTO) {
        if (StringUtils.isEmpty(queryDTO.getWorkerId())) {
            throw new ParamsException("参数错误");
        }
        List<WorkerBindCompany> list = userCompanyMapper.selectHrCompanyByUserId(queryDTO.getWorkerId(),queryDTO.getType (),queryDTO.getName ());
        List<CompanyCooperate> cooperateList = new ArrayList<>();
        if (list != null) {
            for (WorkerBindCompany company : list) {
                CompanyCooperate op = new CompanyCooperate();
                op.setAddress(company.getArea() + company.getAddress());
                op.setLeader(company.getLeader());
                op.setMobile(company.getLeaderMobile());
                op.setLicense(company.getBusinessLicense());
                op.setLogo(company.getLogo());
                op.setName(company.getName());
                if (0 == company.getStatus()) {
                    op.setStatus("未审核");
                } else if (1 == company.getStatus()) {
                    op.setStatus("已审核");
                } else if (2 == company.getStatus()) {
                    op.setStatus("已冻结");
                } else if (-1 == company.getStatus()) {
                    op.setStatus("已注销");
                }
                cooperateList.add(op);
            }
        }
        return cooperateList;
    }

    /**
     * 人力查询关联的工作者
     * @param queryDTO
     * @return
     */
    @Override
    public List<WorkerCooperate> queryHrBindWorker(HrQueryWorkerDTO queryDTO) {

        if (StringUtils.isEmpty(queryDTO.getHrId())) {
            throw new ParamsException("参数不能为空");
        }
        List<WorkerCooperate> list = userCompanyMapper.selectHrBindWorker(queryDTO);
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }


}
