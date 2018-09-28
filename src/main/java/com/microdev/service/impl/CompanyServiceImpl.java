package com.microdev.service.impl;


import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.FilePush;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.PagedList;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.DateUtil;
import com.microdev.common.utils.JPushManage;
import com.microdev.common.utils.RedisUtil;
import com.microdev.common.utils.StringKit;
import com.microdev.converter.TaskConverter;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.service.*;
import com.microdev.type.ConstantData;
import com.microdev.type.UserType;
import org.apache.ibatis.annotations.Param;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.logging.Logger;

@Transactional
@Service
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper,Company> implements CompanyService{


    @Autowired
    CompanyMapper companyMapper;
    @Autowired
    HotelHrCompanyMapper hotelHrCompanyMapper;
    @Autowired
    MessageMapper messageMapper;
    @Autowired
    WorkerLogMapper workLogMapper;
    @Autowired
    TaskMapper taskMapper;
    @Autowired
    TaskWorkerMapper taskWorkerMapper;
    @Autowired
    TaskHrCompanyMapper taskHrCompanyMapper;
    @Autowired
    MessageTemplateMapper messageTemplateMapper;
    @Autowired
    MessageService messageService;
	@Autowired
    DictMapper dictMapper;
    @Autowired
    TaskTypeRelationMapper taskTypeRelationMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    DictService dictService;
    @Autowired
    private InformMapper informMapper;
    @Autowired
    private UserCompanyMapper userCompanyMapper;
    @Autowired
    private HolidayMapper holidayMapper;
    @Autowired
    InformTemplateMapper informTemplateMapper;
    @Autowired
    InformService informService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private WorkerMapper workerMapper;
    @Autowired
    JpushClient jpushClient;
    @Autowired
    private FilePush filePush;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private EvaluteGradeMapper evaluteGradeMapper;
    @Autowired
    private MyTimeTask myTimeTask;


    @Override
    public ResultDO pagingCompanys(Paginator paginator, CompanyQueryDTO queryDTO) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<Company> list = companyMapper.queryCompanys(queryDTO);
        PageInfo<Company> pageInfo = new PageInfo<>(list);

        HashMap<String,Object> result = new HashMap<>();
         //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
         //设置数据集合rows：
        result.put("result",list);
        result.put("page",paginator.getPage());
        if(queryDTO.getObservertype () != null){
            if(queryDTO.getObservertype () == 0){
                String total = dictMapper.findByNameAndCode ("WorkerBindHrMaxNum","7").getText ();
                Map<String,Object> map = new HashMap <> ();
            /*map.put("user_id",userMapper.queryByWorkerId (queryDTO.getObserverId ()).getPid ());
            System.out.println ("userId:"+userMapper.queryByWorkerId (queryDTO.getObserverId ()).getPid ());
            map.put("status","1 or status = 3");*/
                Wrapper<UserCompany> et = new EntityWrapper<UserCompany> ().where("user_id={0}",userMapper.queryByWorkerId (queryDTO.getObserverId ()).getPid ()).in("status","1,3");
                //userCompanyMapper.selectList (et);
                Worker worker = workerMapper.queryById (queryDTO.getObserverId ());
                int num = userCompanyMapper.selectBindCountByWorkerId(queryDTO.getObserverId ());
                map.clear ();
                map.put("bindTotalNum",Integer.parseInt (total));
                map.put("bindNum",num);
                return ResultDO.buildSuccess("您已经绑定"+num+"家人力公司，还可以绑定"+(Integer.parseInt (total)-num)+"家人力公司",result,map,null);
            }else if(queryDTO.getObservertype () == 1){
                String total = dictMapper.findByNameAndCode ("HotelBindHrMaxNum","4").getText ();
                Map<String,Object> map = new HashMap <> ();
                Company company = companyMapper.selectById (queryDTO.getObserverId ());
                Integer num = company.getActiveCompanys ();
                map.clear ();
                map.put("bindTotalNum",Integer.parseInt (total));
                map.put("bindNum",num);
                return ResultDO.buildSuccess("您已经绑定"+num+"家人力公司，还可以绑定"+(Integer.parseInt (total)-num)+"家人力公司",result,map,null);
            }else if(queryDTO.getObservertype () == 2){
                String total = dictMapper.findByNameAndCode ("HrBindHotelMaxNum","5").getText ();
                Map<String,Object> map = new HashMap <> ();
                Integer num = companyMapper.selectById (queryDTO.getObserverId ()).getActiveCompanys ();
                map.clear ();
                map.put("bindTotalNum",Integer.parseInt (total));
                map.put("bindNum",num);
                return ResultDO.buildSuccess("您已经绑定"+num+"家用人单位，还可以绑定"+(Integer.parseInt (total)-num)+"家用人单位",result,map,null);
            }
        }
        return ResultDO.buildSuccess(result);
    }

    @Override
    public ResultDO hrCompanyHotels(Paginator paginator, CompanyQueryDTO request) {
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize());
        List<Map<String, Object>> list = companyMapper.queryHotelsByHrId(request);
        OffsetDateTime applyTime = null;
        OffsetDateTime nowTime = OffsetDateTime.now();
        DictDTO dict = dictMapper.findByNameAndCode("MaxUnbindDay","9");
        Integer maxNum = Integer.parseInt(dict.getText());
        for (Map<String, Object> obj:list) {
            if(Integer.parseInt (obj.get ("relationStatus").toString ()) == 5){
                applyTime = hotelHrCompanyMapper.selectByHrHotelId (request.getId (),obj.get("pid").toString ()).getModifyTime ();
                long leaveMinute = (nowTime.toEpochSecond() - applyTime.toEpochSecond()) / 60;
                int hour = (int)(leaveMinute % 60 == 0 ? leaveMinute / 60 : (leaveMinute / 60) + 1);
                hour = maxNum * 24 - hour <= 0 ? 0 : maxNum * 24 - hour;
                obj.put("hour",hour/24 + "天" + hour%24 + "小时");
            }
        }
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",list);
        result.put("page",paginator.getPage());
        Map<String,Object> map = new HashMap <> ();
        Company company = companyMapper.findCompanyById (request.getId ());
        Integer num = company.getActiveCompanys ();
        Integer total = Integer.parseInt (dictMapper.findByNameAndCode ("HrBindHotelMaxNum","5").getText ());
        map.put ("bindNum",num);
        map.put("bindTotalNum",total);
        return ResultDO.buildSuccess("您已经绑定"+num+"家用人单位，还可以绑定"+ (total-num) +"家用人单位",result,map,null);
    }

    @Override
    public ResultDO getCompanyById(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new ParamsException("参数错误");
        }
        Company company = companyMapper.findCompanyById(id);
        if (company == null) {
            company = new Company();
        }else{
            List l1 = dictService.findServiceArea (company.getPid ());
            List l2 = dictMapper.queryTypeByUserId (company.getPid ());
            company.setAreaCode (l1==null?new ArrayList<>():l1);
            company.setServiceType (l2==null?new ArrayList<>():l2);
            EvaluteGrade evaluteGrade = evaluteGradeMapper.selectByRoleId(company.getPid());
            if (evaluteGrade != null) {
                company.setGrade(evaluteGrade.getGrade() + "");
            } else {
                company.setGrade("3");
            }
            User u = userMapper.findByMobile(company.getLeaderMobile());
            company.setUserId (u.getPid ());
        }
        return ResultDO.buildSuccess(company);
    }
    /**
     * 人力资源公司和用人单位关系绑定 根据ID绑定
     */
    @Override
    public ResultDO hotelAddHrCompanyById(String messageId, String status, Integer type) {
        if (StringUtils.isEmpty(messageId) || (type != 1 && type != 2) || StringUtils.isEmpty(status)) {
            throw new ParamsException("参数不能为空");
        }

        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息查询不到数据。");
        }
        message.setStatus(1);
        messageMapper.updateById(message);

        Company company = null;
        Inform inform = new Inform();
        inform.setCreateTime(OffsetDateTime.now());
        inform.setModifyTime(OffsetDateTime.now());
        if (type == 1) {//用人单位处理

            inform.setSendType(3);
            inform.setAcceptType(2);
            inform.setReceiveId(message.getHrCompanyId());
            company = companyMapper.selectById(message.getHotelId());
            if ("0".equals(status)) {
                inform.setTitle("绑定被拒绝");
                inform.setContent(company.getName() + "拒绝了你的绑定申请，等以后有机会希望可以再合作。");
            } else if ("1".equals(status)) {
                Map<String, Object> columnMap = new HashMap<>();
                columnMap.put("hotel_id", message.getHotelId());
                columnMap.put("hr_id", message.getHrCompanyId());
                List<HotelHrCompany> list = hotelHrCompanyMapper.selectByMap(columnMap);
                if (list == null || list.size() == 0) {
                    throw new BusinessException("用人单位人力关系数据查询不到");
                } else {
                    HotelHrCompany h = list.get(0);
                    h.setDeleted(false);
                    h.setStatus(0);
                    h.setBindType(type);
                    h.setBindTime(OffsetDateTime.now());
                    hotelHrCompanyMapper.updateAllColumnById(h);
                }
                inform.setTitle("绑定成功");
                inform.setContent(company.getName() + "同意了你的绑定申请，成功添加为合作用人单位,添加合作用人单位代表同意劳务合作协议。你可以接受合作用人单位派发的任务，选择小时工，确保能够及时完美的完成任务，可以获得和支出相应的酬劳。");
            } else {
                throw new BusinessException("参数错误");
            }
            informMapper.insertInform(inform);
        } else {//人力处理
            inform.setSendType(2);
            inform.setAcceptType(3);
            inform.setReceiveId(message.getHotelId());
            company = companyMapper.selectById(message.getHotelId());
            if ("0".equals(status)) {
                inform.setTitle("绑定被拒绝");
                inform.setContent(company.getName() + "拒绝了你的绑定申请，等以后有机会希望可以再合作。");
            } else if ("1".equals(status)) {
                Map<String, Object> columnMap = new HashMap<>();
                columnMap.put("hotel_id", message.getHotelId());
                columnMap.put("hr_id", message.getHrCompanyId());
                List<HotelHrCompany> list = hotelHrCompanyMapper.selectByMap(columnMap);
                if (list == null || list.size() == 0) {
                    throw new BusinessException("用人单位人力关系数据查询不到");
                } else {
                    HotelHrCompany h = list.get(0);
                    h.setDeleted(false);
                    h.setStatus(0);
                    h.setBindType(type);
                    h.setBindTime(OffsetDateTime.now());
                    hotelHrCompanyMapper.updateAllColumnById(h);
                }
                inform.setTitle("绑定成功");
                inform.setContent(company.getName() + "接受了你的绑定申请，成功添加为合作人力公司。添加人力公司代表同意劳务合作协议，你可以向合作的人力公司派发任务，由合作的的人力公司选择小时工，并支出相应的酬劳，确保能及时完美的完成任务。");
            } else {
                throw new BusinessException("参数错误");
            }
        }

        return ResultDO.buildSuccess("添加成功");
    }


    @Override
    public ResultDO createCompany(Company companyDTO) {
        Company company = companyMapper.findFirstByLeaderMobile(companyDTO.getLeaderMobile());
        if(company == null){
            companyDTO.setStatus(0);
            companyMapper.insert(companyDTO);
        }else{
            return ResultDO.buildError ("该负责人已创建过公司");
        }
        return ResultDO.buildSuccess(companyDTO);
    }

    @Override
    public ResultDO updateCompany(Company companyDTO) {
        companyMapper.updateById(companyDTO);
        return ResultDO.buildSuccess(companyDTO);
    }

    @Override
    public ResultDO confirmCompany(String id, Integer status) {
        Company company =companyMapper.findCompanyById(id);
        company.setStatus(status);
        company.setConfirmedTime(OffsetDateTime.now());
        companyMapper.updateById(company);
        //TODO 审核成功，创建默认账户
        if(status==1){
            System.out.println("创建默认账户");
            System.out.println("发送短信通知");
        }
        return ResultDO.buildSuccess(company);
    }
    /**
     * 人力资源公司和用人单位关系移除
     */
    @Override
    public ResultDO hotelRemoveHrCompany(HotelHrIdBindDTO hotelHrDTO) {
        HotelHrCompany hotelHr = hotelHrCompanyMapper.findOneHotelHr(hotelHrDTO.getHotelId(), hotelHrDTO.getHrId());
        if (hotelHr == null) {
            throw new ParamsException("没有发现用人单位和人力公司的绑定记录");
        }
        hotelHr.setRelieveType(hotelHrDTO.getRelieveType());
        hotelHr.setRelieveTime(OffsetDateTime.now());
        hotelHr.setStatus(1);
        hotelHrCompanyMapper.updateById(hotelHr);
        
        return  ResultDO.buildSuccess("移除成功");
    }

    @Override
    public ResultDO hotelHrCompanies(Paginator paginator, CompanyQueryDTO request) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        List<Map<String, Object>> list=  companyMapper.queryCompanysByHotelId(request);
        OffsetDateTime applyTime = null;
        OffsetDateTime nowTime = OffsetDateTime.now();
        DictDTO dict = dictMapper.findByNameAndCode("MaxUnbindDay","9");
        Integer maxNum = Integer.parseInt(dict.getText());
        for (Map<String, Object> obj:list) {
            if(Integer.parseInt (obj.get ("relationStatus").toString ()) == 5){
                applyTime = hotelHrCompanyMapper.selectByHrHotelId (obj.get("pid").toString (),request.getId ()).getModifyTime ();
                long leaveMinute = (nowTime.toEpochSecond() - applyTime.toEpochSecond()) / 60;
                int hour = (int)(leaveMinute % 60 == 0 ? leaveMinute / 60 : (leaveMinute / 60) + 1);
                hour = maxNum * 24 - hour <= 0 ? 0 : maxNum * 24 - hour;
                obj.put("hour",hour/24 + "天" + hour%24 + "小时");
            }
        }
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
       
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
       
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        
        result.put("result",list);
        result.put("page",paginator.getPage());
        Map<String,Object> map = new HashMap <> ();
        Company company = companyMapper.findCompanyById (request.getId ());
        Integer num = company.getActiveCompanys ();
        Integer total = Integer.parseInt (dictMapper.findByNameAndCode ("HotelBindHrMaxNum","4").getText ());
        map.put ("bindNum",num);
        map.put("bindTotalNum",total);
        return ResultDO.buildSuccess("您已经绑定"+num+"家人力公司，还可以绑定"+(total-num)+"家人力公司",result,map,null);
    }

    @Override
    public ResultDO hotelWorkers(Paginator paginator, HrQueryWorkerDTO queryDTO) {
        //查询数据集合
        List<User> list;
        User us;
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
        }else{
            boolean ifTimeConflict = false;
            list = userCompanyMapper.getSelectableWorkerH(queryDTO);
            Task task = taskMapper.getFirstById (queryDTO.getTaskId ());
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
            int a = (paginator.getPage ()-1)*paginator.getPageSize ();
            int b = paginator.getPage ()*paginator.getPageSize ()<size?paginator.getPage ()*paginator.getPageSize ():size;
            list = list.subList (a,b);
            //list = list.subList (0,2);
            //设置获取到的总记录数total：
            //设置数据集合rows：
            result.put("result",list);
            result.put("page",paginator.getPage());
        }
            return ResultDO.buildSuccess(result);

    }

    @Override
    public ResultDO hotelNotHrCompanies(String id) {
        List<Company> list = companyMapper.queryNotCompanysByHotelId(id);
        return ResultDO.buildSuccess(list);
    }

    @Override
    public ResultDO hrCompanyNotHotels(String id) {
        List<Company> list = companyMapper.queryNotHotelsByHrId(id);
        return ResultDO.buildSuccess(list);
    }

    /**
     * @return
     */
    @Override
    public String supplementResponse(String id, String status) {
        if (!"0".equals(status) && !"1".equals(status)) {
            throw new ParamsException("参数错误");
        }
        Inform inform = new Inform();
        Message oldMsg = messageMapper.selectById(id);
        if (oldMsg == null || oldMsg.getStatus() == 1) {
            return "已处理";
        }
        oldMsg.setStatus(1);
        messageMapper.updateById(oldMsg);

        Company c = companyMapper.selectById(oldMsg.getHotelId());
        inform.setAcceptType(1);
        inform.setCreateTime(OffsetDateTime.now());
        inform.setModifyTime(OffsetDateTime.now());
        inform.setSendType(3);
        inform.setStatus(0);
        inform.setReceiveId(oldMsg.getWorkerId());
        Task task = taskMapper.selectById (oldMsg.getTaskId());
        if ("1".equals(status)) {
            List<PunchMessageDTO> punchList = messageMapper.selectPunchMessage(id);
            PunchMessageDTO punch = null;
            if (punchList == null || punchList.size() == 0) {
                return "查询不到数据";
            } else {
                punch = punchList.get(0);
            }
            Map<String, Object> param = new HashMap<>();
            param.put("id", punch.getId());
            param.put("punchDate", OffsetDateTime.now());
            param.put("modifyTime", OffsetDateTime.now(Clock.system(ZoneId.of("Asia/Shanghai"))));
            Long minutes = 0L;
            Duration duration = null;
            if (punch.getToDate() == null) {
                if (punch.getFromDate().compareTo(punch.getSupplement()) < 0) {
                    param.put("toDate", punch.getSupplement());
                    duration = Duration.between(punch.getFromDate().toOffsetTime().isBefore(task.getDayStartTime()) ? task.getDayStartTime() : punch.getFromDate().toOffsetTime(), punch.getSupplement().toOffsetTime().isBefore(task.getDayEndTime()) ? punch.getSupplement().toOffsetTime() : task.getDayEndTime());
                } else {
                    param.put("toDate", punch.getFromDate());
                    param.put("fromDate", punch.getSupplement());
                    duration = Duration.between(punch.getSupplement().toOffsetTime().isBefore(task.getDayStartTime()) ? task.getDayStartTime() : punch.getSupplement().toOffsetTime(), punch.getFromDate().toOffsetTime().isBefore(task.getDayEndTime()) ? punch.getFromDate().toOffsetTime() : task.getDayEndTime());
                }
                long seconds = duration.getSeconds();
                minutes = seconds / 60;
                param.put("punchDate", OffsetDateTime.now());
                param.put("minutes", minutes + punch.getMinutes());
                workLogMapper.updateByMapId(param);
            }

            if (minutes > 0) {
                Double shouldPayMoney_hrtoworker = 0D;
                Double shouldPayMoney_hoteltohr = 0D;
                if(oldMsg.getHrTaskId() != null){
                    TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById(oldMsg.getHrTaskId());
                    shouldPayMoney_hrtoworker = (minutes / 60.00) * taskHrCompany.getHourlyPay();
                    shouldPayMoney_hoteltohr = (minutes / 60.00) * taskHrCompany.getHourlyPayHotel();
                }else{
                    if(task ==null){
                        throw new ParamsException ("查询不到用人单位任务");
                    }
                    shouldPayMoney_hoteltohr = (minutes / 60.00) * task.getHourlyPay ();
                }


                //小数点后保留两位(第三位四舍五入)
                shouldPayMoney_hrtoworker = new BigDecimal(shouldPayMoney_hrtoworker).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                shouldPayMoney_hoteltohr = new BigDecimal(shouldPayMoney_hoteltohr).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                if(oldMsg.getHrTaskId() != null){
                    taskWorkerMapper.addMinutes(punch.getTaskWorkerId(),minutes,shouldPayMoney_hrtoworker);
                    taskHrCompanyMapper.addMinutes(oldMsg.getHrTaskId(),minutes,shouldPayMoney_hrtoworker,shouldPayMoney_hoteltohr);
                }else{
                    taskWorkerMapper.addMinutes(punch.getTaskWorkerId(),minutes,shouldPayMoney_hoteltohr);
                }
                taskMapper.addMinutes(oldMsg.getTaskId() ,minutes,shouldPayMoney_hoteltohr);
            }

            inform.setTitle("申请补签成功");
            inform.setContent(c.getName() + "同意了你的补签申请。");
            try {
                jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (oldMsg.getWorkerId()).getMobile (), c.getName() + "同意了你的补签申请。"));
            } catch (APIConnectionException e) {
                e.printStackTrace ( );
            } catch (APIRequestException e) {

            }
        } else if ("0".equals(status)) {

            inform.setTitle("申请补签被拒绝");
            inform.setContent(c.getName() + "拒绝了你的补签申请。");
            try {
                jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (oldMsg.getWorkerId()).getMobile (), c.getName() + "拒绝了你的补签申请。"));
            } catch (APIConnectionException e) {
                e.printStackTrace ( );
            } catch (APIRequestException e) {

            }
        } else {
            throw new ParamsException("参数错误");
        }
        informMapper.insertInform(inform);
        return "提交成功";
    }
    /**
     * 用人单位申请替换小时工
     */
    @Override
    public String changeWorker(Map<String, Object> map) {
        if (map == null) {
            throw new ParamsException("参数不能为空");
        }
        if (StringUtils.isEmpty(map.get("taskWorkerId").toString())) {
            throw new ParamsException("参数taskWorkerId不能为空");
        }
        /*if (StringUtils.isEmpty(map.get("hotelId").toString())) {
            throw new ParamsException("参数hotelId不能为空");
        }*/
        if ( StringUtils.isEmpty(map.get("reason").toString())) {
            throw new ParamsException("参数reason不能为空");
        }
        /*if ( StringUtils.isEmpty(map.get("hrCompanyId").toString())) {
            throw new ParamsException("参数hrCompanyId不能为空");
        }*/

        //查询是否已经有申请替换的消息
        int count = messageMapper.selectReplaceCount((String) map.get("taskWorkerId"));
        if (count > 0) {
            return "已经提交过替换申请，请勿重复提交";
        }
        Message m = new Message();
        Map<String, Object> tp = taskWorkerMapper.selectHrId((String) map.get("taskWorkerId"));
        MessageTemplate mess = messageTemplateMapper.findFirstByCode("applyChangeMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageTitle((String) tp.get("taskTypeText"));
        m.setContent((String) map.get("reason"));
        m.setWorkerId((String) tp.get("workerId"));
        m.setHrCompanyId((String) tp.get("hrCompanyId"));
        m.setHotelId((String) tp.get("hotelId"));
        m.setWorkerTaskId((String) map.get("taskWorkerId"));
        Map<String, String> param = new HashMap<>();
        param.put("userName", (String) tp.get("hotelName"));
        param.put("taskContent", (String) map.get("reason"));
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setApplyType(2);
        m.setApplicantType(3);
        m.setStatus(0);
        m.setIsTask(0);
        m.setMessageType(9);
        m.setHrTaskId((String) tp.get("hrTaskId"));
        m.setTaskId((String) tp.get("taskId"));
        m.setMessageTitle("用人单位发起更换小时工的申请通知");
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById ((String) tp.get("hrCompanyId")).getLeaderMobile (), "用人单位发起更换小时工的申请通知"));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {

        }        messageMapper.insert(m);
        return "提交成功";
    }
    /**

     * 用人单位主动替换小时工
     */
    @Override
    public ResultDO changeOwnWorker(ChangeWorkerParam request) {
        TaskWorker taskWorker = taskWorkerMapper.selectById (request.getTaskWorkerId());
        if (taskWorker == null) {
            throw new BusinessException ("查询不到小时工工作任务");
        }
        if(taskWorker.getStatus () == 3){
            return ResultDO.buildSuccess ("已替换成功");
        }
        taskWorker.setStatus (3);
        taskWorker.setRefusedReason ("小时工有事不能工作，另换小时工接替工作");
        taskWorkerMapper.updateAllColumnById (taskWorker);

        //更新人力任务信息
        Task task = taskMapper.getFirstById (taskWorker.getHotelTaskId ());
        task.setConfirmedWorkers (task.getConfirmedWorkers ( ) - 1);
        taskMapper.updateById (task);

        //插入小时工任务信息
        TaskWorker workerTask = null;
        workerTask = new TaskWorker ( );
        workerTask.setStatus (0);
        workerTask.setDayEndTime (taskWorker.getDayEndTime ( ));
        workerTask.setDayStartTime (taskWorker.getDayStartTime ( ));
        workerTask.setToDate (taskWorker.getToDate ( ));
        workerTask.setFromDate (taskWorker.getToDate ( ));
        workerTask.setHotelName (taskWorker.getHotelName ( ));
        workerTask.setHourlyPay (taskWorker.getHourlyPay ( ));
        workerTask.setTaskContent (taskWorker.getTaskContent ( ));
        workerTask.setTaskTypeCode (taskWorker.getTaskTypeCode ( ));
        workerTask.setTaskTypeText (taskWorker.getTaskTypeText ( ));
        workerTask.setWorkerId (request.getChangeWorkerId ());
        workerTask.setHotelTaskId (taskWorker.getHotelTaskId ());
        workerTask.setHotelId (taskWorker.getHotelId ( ));
        workerTask.setUserId (userMapper.selectByWorkerId (request.getChangeWorkerId ()).getPid ( ));
        workerTask.setSettlementPeriod (taskWorker.getSettlementPeriod ());
        workerTask.setSettlementNum (taskWorker.getSettlementNum ());
        workerTask.setType (1);
        taskWorkerMapper.insert (workerTask);

        List <TaskWorker> list = new ArrayList <TaskWorker> ( );
        list.add (workerTask);
        //给小时工发送消息
        RefusedTaskRequest ref = new RefusedTaskRequest ( );
        ref.setRefusedReason ("小时工未在规定时间内领取任务，请重新派发");
        ref.setMessageId (messageService.hotelDistributeWorkerTask (list, task, true).getPid ( ));
        ref.setWorkerTaskId ("");
        myTimeTask.setRefusedReq (ref);
        java.util.Timer timer = new Timer (true);
        Field field;
        try {
            field = TimerTask.class.getDeclaredField ("state");
            field.setAccessible (true);
            field.set (myTimeTask, 0);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ( );
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace ( );
        }
        timer.schedule (myTimeTask, 86400 * 1000);
        User oldUser = userMapper.selectByWorkerId (taskWorker.getWorkerId ( ));
        taskWorkerMapper.updateStatus (taskWorker.getWorkerId ( ), 3);
        User newUser = userMapper.selectByWorkerId (list.get (0).getWorkerId ( ));
        String content = companyMapper.findCompanyById (task.getHotelId ()).getName ( ) + "用人单位终止了您的任务，如有疑问请咨询相关用人单位";
        informService.sendInformInfo (2, 1, content, taskWorker.getWorkerId (), "任务被终止");
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (taskWorker.getWorkerId ()).getMobile ( ), content));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {

        }
        return ResultDO.buildSuccess ("操作完成");
    }

    /**
     * 用人单位账目明细
     */
    @Override
    public Map<String, Object> accountDetail(String hotelId, Integer page, Integer pageSize) {
        Map<String, Object> result = taskMapper.selectPayMoneyInfo(hotelId);
        if (result == null) {
            result = new HashMap<>();
            result.put("list", new ArrayList<>());
            return result;
        }
        PageHelper.startPage(page, pageSize, true);
        List<Map<String, Object>> list = taskHrCompanyMapper.selectPayHrInfo(hotelId);
        PageInfo<Map<String, Object>> info = new PageInfo<>(list);
        if (list == null) {
            info.setList(new ArrayList<>());
        } else {
            info.setList(list);
        }
        result.put("list", info);
        return result;
    }

    /**

     * 分页查询用人单位事务
     *
     * @param
     * @return
     */
    @Override
    public PageInfo<Map<String, Object>> hotelWaitTaskDetails(MessageRequest request) {
        if (request == null || StringUtils.isEmpty(request.getHotelId())
                || request.getStatus() == null) {
            throw new ParamsException("参数不能为空");
        }
        PageHelper.startPage(request.getPage(), request.getPageSize(), true);
        Map<String, Object> map = new HashMap<>();
        map.put("hotelId", request.getHotelId());
        map.put("status", request.getStatus());
        map.put("deleted", false);
        List<Map<String, Object>> list = taskMapper.selectHotelWaitDetails(map);
        PageInfo<Map<String, Object>> info = new PageInfo<>(list);
        if (list == null) {
            info.setList(new ArrayList<>());
        } else {
            info.setList(list);
        }
        return info;
    }

    /**

     * 展示用人单位待处理的信息详情
     */
    @Override
    public ResultDO showWaitInfo(PendRequest request) {
        String code = request.getMessageCode();
        Map<String, Object> param = new HashMap<>();
        param.put("id", request.getMessageId());
        if (StringUtils.hasLength(request.getTaskId())) {
            param.put("taskId", request.getTaskId());
        }

        //人力申请合作
        if ("1".equals(code)) {
            HrApply apply = messageMapper.selectHrCooperateInfo(request.getMessageId());
            return ResultDO.buildSuccess(apply);
        } else if ("2".equals(code)) {
            //小时工申请加时
            LeaveApply leave = messageMapper.selectHotelWorkerApply(param);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            leave.setLeaveTime(leave.getSupplementTime().format(formatter));
            return ResultDO.buildSuccess(leave);
        } else if ("3".equals(code)) {
            //小时工申请请假
            LeaveApply leave = messageMapper.selectHotelWorkerApply(param);
            return ResultDO.buildSuccess(leave);
        } else if ("4".equals(code)) {
            //小时工申请补签
            LeaveApply leave = messageMapper.selectHotelWorkerApply(param);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            leave.setLeaveTime(leave.getSupplementTime().format(formatter));
            formatter = DateTimeFormatter.ofPattern("HH:mm");
            leave.setSpecificTime(leave.getSupplementTime().format(formatter));
            return ResultDO.buildSuccess(leave);
        } else if ("5".equals(code)) {
            //人力申请调配
            HrDeployApply apply = messageMapper.selectHrDeployInfo(param);
            return ResultDO.buildSuccess(apply);
        }
        return ResultDO.buildSuccess("提交成功");
    }

    /**

     * 用人单位再发布
     */
    @Override
    public String hotelPublish(HotelDeployInfoRequest request) {
        if (request == null) {
            throw new ParamsException("参数不能为空");
        }
        if (!StringUtils.hasLength(request.getHotelId())) {
            throw new ParamsException("参数hotelId不能为空");
        }
        if (StringUtils.isEmpty(request.getName())) {

            throw new ParamsException("用人单位名称不能为空");
        }
        if (!StringUtils.hasLength(request.getTaskContent())) {
            throw new ParamsException("工作内容不能为空");
        }
        if (request.getFromDate() == null || request.getToDate() == null) {
            throw new ParamsException("工作内容不能为空");
        }
        if (request.getHourlyPay() <= 0) {
            throw new ParamsException("定价不能低于0元");
        }
        if (request.getHrCompany() == null || request.getHrCompany().size() == 0) {
            throw new ParamsException("人力公司没有指定");
        }

        List<Message> list = new ArrayList<>();
        Map<String, Object> param = null;
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Map<String, String> map = new HashMap<>();
        map.put("hotelName", request.getName());
        map.put("taskContent", request.getTaskContent());
        map.put("fromDate", request.getFromDate().format(format));
        map.put("toDate", request.getToDate().format(format));
        map.put("price", Double.toString(request.getHourlyPay()));

        MessageTemplate mess = messageTemplateMapper.findFirstByCode("hotelDeployHrMessage");
        Iterator<Map<String, Object>> it = request.getHrCompany().iterator();
        while (it.hasNext()) {
            param = it.next();
            Message message = new Message();
            message.setMessageCode(mess.getCode());
            message.setApplyType(2);
            message.setContent(request.getTaskContent());
            message.setSupplementTime(request.getFromDate());
            message.setSupplementTimeEnd(request.getToDate());
            message.setHotelId(request.getHotelId());
            message.setHrCompanyId((String) param.get("id"));
            message.setMessageCode(mess.getCode());
            message.setMessageTitle(mess.getTitle());
            message.setStatus(0);
            message.setIsTask(0);
            message.setMessageType(6);
            message.setStatus((Integer) param.get("number"));

            map.put("number", String.valueOf(param.get("number")));
            String c = StringKit.templateReplace(mess.getContent(), map);
            message.setMessageContent(c);
            list.add(message);
        }
        messageMapper.saveBatch(list);
        return "操作成功";
    }

    /**

     * 用人单位处理小时工加时
     *
     * @param id     消息id
     * @param status 0拒绝1同意
     * @return
     */
    @Override
    public String workExpand(String id, String status) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(status)) {
            throw new ParamsException("参数不能为空");
        }
        Message m = messageMapper.selectById(id);
        if (m == null || m.getStatus() == 1) {
            return "已处理";
        }
        m.setStatus(1);
        messageMapper.updateById(m);

        Inform inform = new Inform();
        inform.setCreateTime(OffsetDateTime.now());
        inform.setModifyTime(OffsetDateTime.now());
        inform.setReceiveId(m.getWorkerId());
        inform.setSendType(3);
        inform.setAcceptType(1);
        Company c = companyMapper.selectById(m.getHotelId());
        Task task = taskMapper.selectById (m.getTaskId ());
        if ("1".equals(status)) {
            Integer minutes = m.getMinutes() == null ? 0 : Integer.valueOf(m.getMinutes());

            Map<String, Object> map = new HashMap<>();
            map.put("taskWorkerId", m.getWorkerTaskId());
            map.put("time", m.getSupplementTime());
            List<WorkLog> list = workLogMapper.selectWorkLogByTime(map);
            if (list != null && list.size() > 0) {
                WorkLog log = list.get(0);
                Integer logMinutes = log.getMinutes() == null ? 0 : log.getMinutes();
                log.setMinutes(logMinutes + minutes);
                workLogMapper.updateById(log);
            }

            TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById(m.getHrTaskId());
            Double shouldPayMoney_hrtoworker = 0d;
            Double shouldPayMoney_hoteltohr = 0d;
            if(taskHrCompany != null){
                shouldPayMoney_hrtoworker = (minutes / 60.00) * taskHrCompany.getHourlyPay();
                shouldPayMoney_hoteltohr = (minutes / 60.00) * taskHrCompany.getHourlyPayHotel();
            }else{
                shouldPayMoney_hoteltohr = (minutes / 60.00) * task.getHourlyPay ();
            }
            shouldPayMoney_hrtoworker = new BigDecimal(shouldPayMoney_hrtoworker).
                    setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            shouldPayMoney_hoteltohr = new BigDecimal(shouldPayMoney_hoteltohr).
                    setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if(taskHrCompany != null){
                taskWorkerMapper.addMinutes(m.getWorkerTaskId(), minutes.longValue(), shouldPayMoney_hrtoworker);
                TaskWorker taskWorker = taskWorkerMapper.selectById(m.getWorkerTaskId());
                taskHrCompanyMapper.addMinutes(taskWorker.getTaskHrId(), minutes.longValue(), shouldPayMoney_hrtoworker, shouldPayMoney_hoteltohr);
                taskMapper.addMinutes(taskHrCompany.getTaskId(), minutes.longValue(), shouldPayMoney_hoteltohr);
            }else{
                taskWorkerMapper.addMinutes(m.getWorkerTaskId(), minutes.longValue(), shouldPayMoney_hoteltohr);
            }
            inform.setTitle("申请加时成功");
            inform.setContent(c.getName() + "同意了你的加时请求。" + m.getContent());
            try {
                jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (m.getWorkerId()).getMobile (), c.getName() + "同意了你的加时请求。" + m.getContent()));
            } catch (APIConnectionException e) {
                e.printStackTrace ( );
            } catch (APIRequestException e) {

            }
        } else if ("0".equals(status)) {

            inform.setTitle("申请加时被拒绝");
            inform.setContent(c.getName() + "拒绝了你的加时请求。");
            try {
                jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (m.getWorkerId()).getMobile (), c.getName() + "拒绝了你的加时请求。"));
            } catch (APIConnectionException e) {
                e.printStackTrace ( );
            } catch (APIRequestException e) {

            }
        } else {
            throw new ParamsException("参数错误");
        }

        informMapper.insertInform(inform);
        return "操作成功";
    }

    /**

     * 用人单位申请绑定人力资源公司或人力公司申请绑定用人单位
     */
    @Override
    public ResultDO hotelAddHrCompanySet(HotelHrIdBindDTO dto) {
        if (dto == null) {
            throw new ParamsException("参数不能为空");
        }

        if (dto.getSet().size() == 0) {
            throw new ParamsException("添加的公司不能为空");
        }
        Integer type = dto.getBindType();

        Set<String> hrSet = dto.getSet();

        List<HotelHrCompany> list = new ArrayList<>();
        HotelHrCompany hotelHr = null;
        Company company = null;
        //从redis中取出默认协议地址
        String path = redisUtil.getString("defaultHrHotelProtocol");
        if (StringUtils.isEmpty(path)) {
            try {
                path = filePush.pushFileToServer(ConstantData.CATALOG.getName(), ConstantData.HRHOTELPROTOCOL.getName());
                //path = filePush.pushFileToServer(ConstantData.CATALOG.getName(), ConstantData.TEST.getName());
            } catch (Exception e) {
                e.printStackTrace();
                return ResultDO.buildError("服务异常");
            }
            redisUtil.setString("defaultHrHotelProtocol", path);
        }

        if (type == 1) {
 
            //用人单位加人力
            if (StringUtils.isEmpty(dto.getHotelId())) {
                throw new ParamsException("参数hotelId为空");
            }
            int num = hotelHrCompanyMapper.selectBindCountByHotelId(dto);
            if (num > 0) {
                throw new BusinessException("已绑定,请勿重复");
            }
            num = hotelHrCompanyMapper.selectIsBind(dto);

            if (num > 0) {
                for (String hrId : hrSet) {
     
                    hotelHr = hotelHrCompanyMapper.selectByHrHotelId(hrId,dto.getHotelId());
                    if (hotelHr == null) {
                        hotelHr = new HotelHrCompany();
                        hotelHr.setStatus(3);
                        hotelHr.setBindType(type);
                        hotelHr.setHotelId(dto.getHotelId());
                        hotelHr.setHrId(hrId);
                        hotelHr.setBindProtocol(path);
                        list.add(hotelHr);
                    } else {
                        hotelHr.setBindProtocol(path);
                        hotelHr.setStatus(3);
                        hotelHr.setBindType (1);
                        hotelHrCompanyMapper.update(hotelHr);
                    }
                }
            } else {
                for (String hrId : hrSet) {
                    hotelHr = new HotelHrCompany();
                    hotelHr.setStatus(3);
                    hotelHr.setBindType(type);
                    hotelHr.setHotelId(dto.getHotelId());
                    hotelHr.setHrId(hrId);
                    hotelHr.setBindProtocol(path);
                    list.add(hotelHr);
                }
            }
            company = companyMapper.selectById(dto.getHotelId());
            if(company.getStatus () == 0)
                return ResultDO.buildError ("公司状态为未审核，添加失败");
        } else if (type == 2) {

            //人力加用人单位
            if (StringUtils.isEmpty(dto.getHrId())) {
                return ResultDO.buildError ("参数hrId为空");
            }
            int num = hotelHrCompanyMapper.selectBindCountByHrId(dto);
            if (num > 0) {
                return ResultDO.buildError ("您已提交过绑定申请,请勿重复申请");
            }
            num = hotelHrCompanyMapper.selectIsBIndByCompanyId(dto);
            if (num > 0) {
                for (String hotelId : hrSet) {

                    hotelHr = hotelHrCompanyMapper.selectByHrHotelId(dto.getHrId(),hotelId);
                    if (hotelHr == null) {
                        hotelHr = new HotelHrCompany();
                        hotelHr.setBindType(2);
                        hotelHr.setStatus(3);
                        hotelHr.setHotelId(hotelId);
                        hotelHr.setHrId(dto.getHrId());
                        hotelHr.setBindProtocol(path);
                        list.add(hotelHr);
                    } else {
                        hotelHr.setStatus(3);
                        hotelHr.setBindType (2);
                        hotelHr.setBindProtocol(path);
                        hotelHrCompanyMapper.update(hotelHr);
                    }
                }
            } else {
                for (String hotelId : hrSet) {
                    hotelHr = new HotelHrCompany();
                    hotelHr.setBindType(2);
                    hotelHr.setStatus(3);
                    hotelHr.setHotelId(hotelId);
                    hotelHr.setHrId(dto.getHrId());
                    hotelHr.setBindProtocol(path);
                    list.add(hotelHr);
                }
            }
            company = companyMapper.selectById(dto.getHrId());
            if(company.getStatus () == 0)
                return ResultDO.buildError ("公司状态为未审核，添加失败");
        }
        if (list.size() > 0) {
            try{
                hotelHrCompanyMapper.saveBatch(list);
            }catch(Exception e){
                HotelHrCompany hh = hotelHrCompanyMapper.selectByHrHotelId (list.get (0).getHrId (),list.get (0).getHotelId ());
                if(hh.getStatus () == 0){
                    return ResultDO.buildError ("绑定申请已提交，请勿重复提交");
                }else{
                    return ResultDO.buildError ("您已合作，请勿重复申请");
                }

            }

        }

        messageService.hotelBindHrCompany(dto.getSet(), company, "applyBindMessage", type,null);
        return ResultDO.buildSuccess("操作成功");
    }

    @Override
    public ResultDO hotelAddWorkerSet(HotelHrIdBindDTO dto) {
        if (dto == null) {
            throw new ParamsException ("参数不能为空");
        }

        if (dto.getSet ( ).size ( ) == 0) {
            throw new ParamsException ("添加的小时工不能为空");
        }
        Integer type = dto.getBindType ( );

        Set <String> hrSet = dto.getSet ( );

        List <UserCompany> list = new ArrayList <> ( );
        UserCompany hotelWorker = null;
        Company company = null;
        //从redis中取出默认协议地址
        String path = redisUtil.getString ("defaultHrHotelProtocol");
        if (StringUtils.isEmpty (path)) {
            try {
                path = filePush.pushFileToServer (ConstantData.CATALOG.getName ( ), ConstantData.HRHOTELPROTOCOL.getName ( ));
                //path = filePush.pushFileToServer(ConstantData.CATALOG.getName(), ConstantData.TEST.getName());
            } catch (Exception e) {
                e.printStackTrace ( );
                return ResultDO.buildError ("服务异常");
            }
            redisUtil.setString ("defaultHrHotelProtocol", path);
        }
        //用人单位加小时工
        if (StringUtils.isEmpty (dto.getHotelId ( ))) {
            throw new ParamsException ("参数hotelId为空");
        }
        company = companyMapper.selectById (dto.getHotelId ( ));
        if (company.getStatus ( ) == 0) throw new ParamsException ("公司状态为未审核，添加失败");
        for (String workerId : hrSet) {
            UserCompany userCompany = userCompanyMapper.selectByWorkerIdHrId (dto.getHotelId ( ), workerId);
            if (userCompany != null && userCompany.getStatus ( ) != 2 && userCompany.getStatus ( ) != 4) {
                throw new BusinessException ("已提交,请勿重复");
            }
        }
        for (String workerId : hrSet) {
            UserCompany us = userCompanyMapper.selectByWorkerIdHrId (dto.getHotelId ( ), workerId);
            if (us == null) {
                us = new UserCompany ( );
                us.setUserType(UserType.worker);
                us.setStatus (0);
                us.setCompanyType (1);
                us.setCompanyId (dto.getHotelId ( ));
                us.setUserId (userMapper.selectByWorkerId (workerId).getPid ( ));
                us.setBindProtocol (path);
                userCompanyMapper.insert (us);
            } else {
                us.setStatus (0);
                userCompanyMapper.update (us);
            }

        }
        messageService.bindUserHrCompany(company.getName(), dto.getHotelId ( ), new ArrayList<>(dto.getSet ()), 2);
        return ResultDO.buildSuccess ("操作成功");
    }

    /**
     * 人力公司同意解绑小时工

     * @param messageId     消息id
     * @param status        1同意
     * @return
     */
    @Override
    public String hrUnbindWorker(String messageId, String status) {
        if (StringUtils.isEmpty(messageId)) {
            throw new ParamsException("参数错误");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new ParamsException("消息参数错误");
        }

        if(message.getStatus() == 1){
            throw new ParamsException("消息已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        Inform inform = new Inform();
        inform.setCreateTime(OffsetDateTime.now());
        inform.setModifyTime(OffsetDateTime.now());
        inform.setAcceptType(1);
        inform.setSendType(2);
        inform.setReceiveId(message.getWorkerId());
        if ("1".equals(status)) {
            User user = userMapper.selectByWorkerId(message.getWorkerId());
            Map<String, Object> columnMap = new HashMap<>();
            columnMap.put("company_id", message.getHrCompanyId());
            columnMap.put("user_id", user.getPid());
            List<UserCompany> list = userCompanyMapper.selectByMap(columnMap);

                if (list == null || list.size() == 0) {
                throw new ParamsException("数据异常");
            }
            UserCompany userCompany = list.get(0);
            userCompany.setStatus(4);
            userCompanyMapper.update(userCompany);
            Company company = companyMapper.selectById(userCompany.getCompanyId());
            //更新小时工接受该人力的任务
            List<TaskWorker> taskWorkerList = taskWorkerMapper.selectByUserHr(user.getPid(), company.getPid());
            if (taskWorkerList != null && taskWorkerList.size() > 0) {
                for (TaskWorker taskWorker : taskWorkerList) {
                    taskWorker.setStatus(3);
                    taskWorker.setRefusedReason("已解除绑定");
                    taskWorkerMapper.updateById(taskWorker);
                }
            }

            String num = dictMapper.findByNameAndCode("WorkerBindHrMaxNum", "7").getText();
            inform.setTitle("解绑成功");

            inform.setContent(company.getName() + "同意了你的申请解绑。你可以添加新的合作人力公司，每人最多只能绑定"+num+"家人力公司");
            if(company.getActiveWorkers () == null){
                company.setActiveWorkers (0);
            }

            if (company.getActiveWorkers () >= 1) {
                company.setActiveWorkers (company.getActiveWorkers () - 1);
            }else{
                throw new ParamsException("数据异常");
            }
            company.setBindWorkers(true);
            companyMapper.updateById(company);
            Worker worker = workerMapper.queryById(user.getWorkerId());
            if (worker.getActiveCompanys() == null) {
                worker.setActiveCompanys(0);
            }

            if(worker.getActiveCompanys () >= 1){
                worker.setActiveCompanys (worker.getActiveCompanys () - 1);
            }else{
                throw new ParamsException("数据异常");
            }

            worker.setBindCompanys (true);
            workerMapper.updateById (worker);
        }
        informMapper.insertInform(inform);
        return "操作成功";
    }

    /**
     * 人力查询待审核的用人单位信息
     *     * @param hrCompanyId
     * @param page
     * @param pageNum
     * @return
     */
    @Override
    public ResultDO selectExamineCompanies(String hrCompanyId, Integer page, Integer pageNum) {
        if (StringUtils.isEmpty(hrCompanyId)) {
            throw new ParamsException("参数错误");
        }
        PageHelper.startPage(page, pageNum, true);
        List<Map<String, Object>> list = companyMapper.selectExamineCompanies(hrCompanyId);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);

        Map<String, Object> param = new HashMap<>();
        param.put("page", page);
        param.put("total", pageInfo.getTotal());
        param.put("result", list);
        return ResultDO.buildSuccess(param);
    }

    /**
     * 查询合作的人力公司信息

     * @param page          页码
     * @param pageNum       页数
     * @return
     */
    @Override
    public ResultDO selectCooperatorHr(QueryCooperateRequest map, Integer page, Integer pageNum) {
        if (StringUtils.isEmpty(map.getHotelId())) {
            throw new ParamsException("参数错误");
        }
        PageHelper.startPage(page, pageNum, true);
        List<Map<String, Object>> list = hotelHrCompanyMapper.selectCooperateHr(map);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
        Map<String, Object> param = new HashMap<>();
        param.put("page", page);
        param.put("total", pageInfo.getTotal());
        param.put("result", list);
        return ResultDO.buildSuccess(param);
    }

    /**
     * 人力处理用人单位绑定申请或用人单位处理人力绑定
     *
     * @param messageId 消息id
     * @param status    0拒绝1同意
     * @return
     */
    @Override
    public ResultDO hrHandlerHotelBind(String messageId, String status) {

        if (StringUtils.isEmpty(messageId)) {
            throw new ParamsException("参数错误");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new BusinessException("已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        Inform inform = new Inform();
        inform.setCreateTime(OffsetDateTime.now());
        inform.setModifyTime(OffsetDateTime.now());
        HotelHrCompany hotelHrCompany = hotelHrCompanyMapper.findOneHotelHr(message.getHotelId(), message.getHrCompanyId());
        if (hotelHrCompany == null) {

            throw new BusinessException("查询不到人力用人单位关系");
        }
        if (hotelHrCompany.getStatus() != 3) {
            return ResultDO.buildError("已处理");
        }
        Company hotel = null;
        Company hr = null;
        if ("1".equals(status)) {
            inform.setTitle("绑定成功");
            if (message.getApplicantType() == 2) {
                inform.setSendType(3);
                inform.setAcceptType(2);
                inform.setReceiveId(message.getHrCompanyId());
                hotel = companyMapper.selectById(message.getHotelId());
                hr = companyMapper.selectById(message.getHrCompanyId());

                if(hotel.getActiveCompanys () == null){
                    hotel.setActiveCompanys (0);
                }

                if(hotel.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("HotelBindHrMaxNum","4").getText ())){
                    inform.setContent(hotel.getName() + "超出了绑定人力公司数目上限");
                    hotelHrCompany.setStatus(4);
                    hotelHrCompanyMapper.update(hotelHrCompany);
                    return ResultDO.buildSuccess("超过用人单位绑定数目上限");                }

                if(hr.getActiveCompanys () == null){
                    hr.setActiveCompanys (0);
                }
                if (hr.getActiveCompanys() == Integer.parseInt(dictMapper.findByNameAndCode("HrBindHotelMaxNum", "5").getText())) {
                    inform.setContent(hr.getName() + "超出了绑定用人单位数目上限");                    hotelHrCompany.setStatus(4);
                    hotelHrCompanyMapper.update(hotelHrCompany);

                    return ResultDO.buildSuccess ("超过人力公司绑定数目上限");
                }

                hotel.setActiveCompanys (hotel.getActiveCompanys ()+1);
                if(hotel.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("HotelBindHrMaxNum","4").getText ())){
                    hotel.setBindCompanys (false);
                }

                companyMapper.updateById (hotel);


                hr.setActiveCompanys (hr.getActiveCompanys ()+1);
                if(hr.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("HrBindHotelMaxNum","5").getText ())){
                    hr.setBindCompanys (false);
                }
                companyMapper.updateById (hr);
                inform.setContent(hotel.getName() + "同意了你的绑定申请，成功添加为合作用人单位,添加合作用人单位代表同意劳务合作协议。你可以接受合作用人单位派发的任务，选择小时工，确保能够及时完美的完成任务，可以获得和支出相应的酬劳。");
                hotelHrCompany.setStatus(0);
                hotelHrCompanyMapper.update(hotelHrCompany);
            } else if (message.getApplicantType() == 3) {
                inform.setSendType(2);
                inform.setAcceptType(3);
                inform.setReceiveId(message.getHotelId());
                hr = companyMapper.selectById(message.getHrCompanyId());

                if(hr.getActiveCompanys () == null){
                    hr.setActiveCompanys (0);
                }
                if (hr.getActiveCompanys() == Integer.parseInt(dictMapper.findByNameAndCode("HrBindHotelMaxNum", "5").getText())) {

                    inform.setContent(hr.getName() + "超出了绑定用人单位数目上限");
                    hotelHrCompany.setStatus(4);
                    hotelHrCompanyMapper.update(hotelHrCompany);

                    return ResultDO.buildSuccess ("超过人力公司绑定数目上限");
                }
                hotel = companyMapper.selectById(message.getHotelId());

                if(hotel.getActiveCompanys () == null){
                    hotel.setActiveCompanys (0);
                }

                if(hotel.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("HotelBindHrMaxNum","4").getText ())){
                    inform.setContent(hotel.getName() + "超出了绑定人力公司数目上限");
                    hotelHrCompany.setStatus(4);
                    hotelHrCompanyMapper.update(hotelHrCompany);
                    return ResultDO.buildSuccess("超过用人单位绑定数目上限");                }

                hr.setActiveCompanys (hr.getActiveCompanys ()+1);
                if(hr.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("HrBindHotelMaxNum","5").getText ())){
                    hr.setBindCompanys (false);
                }

                companyMapper.updateById (hr);
                hotel.setActiveCompanys (hotel.getActiveCompanys ()+1);
                if(hotel.getActiveCompanys () == Integer.parseInt (dictMapper.findByNameAndCode ("HotelBindHrMaxNum","4").getText ())){
                    hotel.setBindCompanys (false);
                }

                companyMapper.updateById (hotel);
                inform.setContent(hr.getName() + "接受了你的绑定申请，成功添加为合作人力公司。添加人力公司代表同意劳务合作协议，你可以向合作的人力公司派发任务，由合作的的人力公司选择小时工，并支出相应的酬劳，确保能及时完美的完成任务。");
                hotelHrCompany.setStatus(0);
                hotelHrCompanyMapper.update(hotelHrCompany);
            } else {
                throw new BusinessException("数据错误");
            }

        } else if ("0".equals(status)) {
            inform.setTitle("绑定拒绝");
            if (message.getApplicantType() == 2) {
                inform.setSendType(3);
                inform.setAcceptType(2);
                inform.setReceiveId(message.getHrCompanyId());
                hotel = companyMapper.selectById(message.getHotelId());
                inform.setContent(hotel.getName() + "拒绝了你的绑定申请，等以后有机会希望可以再合作。");
            } else if (message.getApplicantType() == 3) {
                inform.setSendType(2);
                inform.setAcceptType(3);
                inform.setReceiveId(message.getHotelId());
                hotel = companyMapper.selectById(message.getHrCompanyId());
                inform.setContent(hotel.getName() + "拒绝了你的绑定申请，等以后有机会希望可以再合作。");
            } else {
                throw new BusinessException("数据错误");
            }
            hotelHrCompany.setStatus(4);
            hotelHrCompanyMapper.update(hotelHrCompany);

        } else {
            throw new ParamsException("参数错误");
        }
        informMapper.insertInform(inform);
        return ResultDO.buildSuccess("处理成功");
    }

    /**
     * 人力查询合作的用人单位
     * @param map
     * @param paginator     分页
     * @return
     */
    @Override
    public ResultDO hrQueryCooperatorHotel(QueryCooperateRequest map, Paginator paginator) {
        if (StringUtils.isEmpty(map.getHrCompanyId())) {
            throw new ParamsException("参数错误");
        }
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<Map<String, Object>> list = companyMapper.selectCooperateHotel(map);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
        Map<String, Object> param = new HashMap<>();
        param.put("page", pageInfo.getPageNum());
        param.put("total", pageInfo.getTotal());
        param.put("result", list);
        return ResultDO.buildSuccess(param);
    }

    /**
     * 用人单位查询待审核的人力公司
     * @param request
     * @param paginator
     * @return
     */
    @Override
    public ResultDO hotelExamineHr(QueryCooperateRequest request, Paginator paginator) {
        if (request == null || StringUtils.isEmpty(request.getHotelId())) {
            throw new ParamsException("参数错误");
        }
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<Map<String, Object>> list = companyMapper.hotelExamineCompany(request);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
        Map<String, Object> param = new HashMap<>();
        param.put("page", pageInfo.getPageNum());
        param.put("total", pageInfo.getTotal());
        param.put("result", list);
        return ResultDO.buildSuccess(param);
    }

    /**
     * 用人单位处理小时工请假
     * @return
     */
    @Override
    public ResultDO hotelHandleLeave(String messageId, String status) {

        if (StringUtils.isEmpty(messageId) || StringUtils.isEmpty(status)) {
            throw new ParamsException("参数错误");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new BusinessException("已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);

        //发送通知

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        Map<String,String> map = new HashMap <> ();
        InformTemplate informTemplate = null;
        if ("1".equals(status)) {
            Holiday holiday = new Holiday();
            holiday.setFromDate(message.getSupplementTime());
            holiday.setToDate(message.getSupplementTimeEnd());
            holiday.setTaskWorkerId(message.getWorkerTaskId());
            holidayMapper.insert(holiday);
            informTemplate = informTemplateMapper.selectByCode(InformType.apply_for_leave_success.name());
            map.put("hotel", companyMapper.selectById(message.getHotelId()).getName());
            map.put("date", message.getSupplementTime().format(format));
            map.put("time", message.getSupplementTimeEnd().format(format));
            map.put("reason", message.getContent());

        } else if ("0".equals(status)) {
            informTemplate = informTemplateMapper.selectByCode(InformType.apply_for_leave_fail.name());
            map.put("hotel", companyMapper.selectById(message.getHotelId()).getName());
            map.put("reason", message.getContent());

        } else {
            throw new ParamsException("参数错误");
        }
        String content = StringKit.templateReplace(informTemplate.getContent(), map);
 
        informService.sendInformInfo (3,1,content,message.getWorkerId (), informTemplate.getTitle());
        try {

            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (message.getWorkerId ( )).getMobile ( ), content));
        } catch (APIConnectionException e) {

            e.printStackTrace ( );
        } catch (APIRequestException e) {

        }
        return ResultDO.buildSuccess("处理成功");
    }

    /**
     * 用人单位同意人力公司申请调配并派发任务
     * @param request
     * @return
     */
    @Override
    public ResultDO deploymentHandle(CreateTaskRequest request) {

        if (request == null || StringUtils.isEmpty(request.getMessageId())) {
            throw new ParamsException("参数错误");
        }
        Set<TaskHrCompanyDTO> set = request.getHrCompanySet();
        if (set == null || set.size() == 0) {
            throw new ParamsException("派发的人力公司不能为空");
        }
        Message message = messageMapper.selectById(request.getMessageId());
        if (message == null || message.getStatus() == 1) {
            throw new ParamsException("已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        if(taskMapper.getFirstById(message.getTaskId()).getFromDate().isBefore(OffsetDateTime.now())) {
            informService.sendInformInfo(3, 2, "由于用人单位未及时处理您的调配申请，此申请默认拒绝", message.getHrCompanyId(), "申请调配处理超时");
            try {
                jpushClient.jC.sendPush(JPushManage.buildPushObject_all_alias_message(companyMapper.findCompanyById(message.getHrCompanyId ()).getLeaderMobile(), "由于用人单位未及时处理您的调配申请，此申请默认拒绝"));
            } catch (APIConnectionException e) {
                e.printStackTrace ( );
            } catch (APIRequestException e) {

            }
            return ResultDO.buildSuccess("任务已开始，处理超时");
        }
        request.setTaskId(message.getTaskId());
        request.setTaskHrId(message.getHrTaskId());


        //用人单位再派发

        taskService.hotelAgainSendTask(request);
        //申请调配成功通知
        
        InformTemplate inf = informTemplateMapper.selectByCode (InformType.hr_allocation_success.name ());
        Map<String,String> map = new HashMap <> ();
        map.put("hotel",companyMapper.findCompanyById (message.getHotelId ()).getName ());
        String content = StringKit.templateReplace(inf.getContent (), map);
        informService.sendInformInfo (inf.getSendType (),2,content,message.getHrCompanyId (),inf.getTitle ());
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (message.getHrCompanyId ()).getLeaderMobile ( ), content));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {
        }
       
        TaskHrCompany taskHrCompany = taskHrCompanyMapper.queryByTaskId (message.getHrTaskId ());
        taskHrCompany.setNeedWorkers (taskHrCompany.getNeedWorkers()-Integer.parseInt (message.getMinutes ()));
        if(taskHrCompany.getNeedWorkers () == taskHrCompany.getConfirmedWorkers ()){
            taskHrCompany.setStatus (5);
        }
        taskHrCompanyMapper.updateById (taskHrCompany);
        return ResultDO.buildSuccess("处理成功");
    }

    /**
     * 用人单位处理小时工工作记录
     *     * @param record
     * @return
     */
    @Override
    public ResultDO hotelHandleWorkerRecord(HotelHandleWorkerRecord record) {

        if (StringUtils.isEmpty(record.getDate()) || StringUtils.isEmpty(record.getStatus()) || StringUtils.isEmpty(record.getTaskWorkerId())) {
            throw new ParamsException("参数不能为空");
        }

        if ("3".equals(record.getStatus())) {
            SimpleDateFormat f = new SimpleDateFormat("yyyy/MM/dd");
            try {
                Date d = f.parse(record.getDate());
                OffsetDateTime createTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(d.getTime()), ZoneId.systemDefault());
                TaskWorker taskWorker = taskWorkerMapper.selectById(record.getTaskWorkerId());
                if (taskWorker == null) {
                    return ResultDO.buildSuccess("查询不到小时工任务信息");
                }
                WorkLog workLog = new WorkLog();
                workLog.setCreateTime(createTime);
                workLog.setModifyTime(OffsetDateTime.now());
                workLog.setFromDate(OffsetDateTime.of(createTime.getYear(), createTime.getMonthValue(), createTime.getDayOfMonth(), taskWorker.getDayStartTime().getHour(), taskWorker.getDayStartTime().getMinute(), 0, 0, createTime.getOffset()));
                workLog.setToDate(OffsetDateTime.of(createTime.getYear(), createTime.getMonthValue(), createTime.getDayOfMonth(), taskWorker.getDayEndTime().getHour(), taskWorker.getDayEndTime().getMinute(), 0, 0, createTime.getOffset()));
                workLog.setMinutes(0);
                workLog.setStatus(3);
                workLog.setEmployerConfirmStatus(1);
                workLog.setTaskId(taskWorker.getTaskHrId());
                workLog.setTaskWorkerId(taskWorker.getPid());
                workLogMapper.insert(workLog);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return ResultDO.buildSuccess("操作成功");
        }
        List<WorkLog> workLogList = workLogMapper.selectByDate(record);
        if (workLogList == null || workLogList.size() == 0) {
            return ResultDO.buildError("查询不到工作记录");
        }
        boolean flag = true;
        for (WorkLog workLog1 : workLogList) {
            if (workLog1.getEmployerConfirmStatus() == null || workLog1.getEmployerConfirmStatus() == 0) {
                if ("1".equals(record.getStatus())) {
                    workLog1.setStatus(2);
                } else if ("2".equals(record.getStatus())) {
                    workLog1.setStatus(1);
                }

                workLog1.setEmployerConfirmStatus(1);
                workLogMapper.updateById(workLog1);
                flag = false;
                break;
            }
        }

        if (flag) {
            for (WorkLog workLog1 : workLogList) {
                if ("1".equals(record.getStatus())) {
                    if (workLog1.getStatus() == 1 && workLog1.getEmployerConfirmStatus() == 1) {
                        workLog1.setStatus(6);
                        workLogMapper.updateById(workLog1);
                        break;
                    }
                } else if ("2".equals(record.getStatus())) {
                    if (workLog1.getStatus() == 2 && workLog1.getEmployerConfirmStatus() == 1) {
                        workLog1.setStatus(6);
                        workLogMapper.updateById(workLog1);
                        break;
                    }
                }

            }
        }

        return ResultDO.buildSuccess("操作成功");
    }

    /**
     * 用人单位申请解绑人力公司
     */
    @Override
    public ResultDO hotelRelieveHrCompanySet(HotelHrIdBindDTO dto) {
        if (dto == null) {
            throw new ParamsException("参数不能为空");
        }

        if (dto.getSet().size() == 0) {
            throw new ParamsException("解绑的公司不能为空");
        }

        if (dto.getReason() == null || dto.getReason().equals("")) {
            throw new ParamsException("解绑的理由不能为空");
        }

        //1: 用人单位移除的人力公司
        //2: 人力移除的用人单位
        Integer type = dto.getRelieveType();

        Set<String> hrSet = dto.getSet();
        HotelHrCompany hotelHr = null;
        Company company = null;
        if (type == 1) {

            //用人单位解绑人力
            if (StringUtils.isEmpty(dto.getHotelId())) {
                throw new ParamsException("参数hotelId为空");
            }
            int num = hotelHrCompanyMapper.selectRelieveCountByHotelId(dto);
            if (num != dto.getSet().size()) {
                throw new BusinessException("已申请解绑,请勿重复");
            }
            for (String hrId : hrSet) {
                hotelHr = hotelHrCompanyMapper.selectByHrHotelId(hrId, dto.getHotelId());
                if(taskHrCompanyMapper.queryByHotelIdAndHrId(dto.getHotelId(),hrId)>0){
                    throw new BusinessException("贵公司与该公司存在未完成的任务，暂时不能解绑");
                }
                if (hotelHr == null) {
                    throw new BusinessException("数据异常，获取绑定关系失败");
                } else {
                    hotelHr.setRelieveType(1);
                    hotelHr.setStatus(5);
                    hotelHrCompanyMapper.update(hotelHr);
                }
            }
            company = companyMapper.selectById(dto.getHotelId());

        } else if (type == 2) {

            //人力解绑用人单位
            if (StringUtils.isEmpty(dto.getHrId())) {
                throw new ParamsException("参数hrId为空");
            }
            int num = hotelHrCompanyMapper.selectRelieveCountByHrId(dto);
            if (num != dto.getSet().size()) {
                throw new BusinessException("已申请解绑,请勿重复");
            }

            for (String hotelId : hrSet) {
                hotelHr = hotelHrCompanyMapper.selectByHrHotelId(dto.getHrId(), hotelId);
                if(taskHrCompanyMapper.queryByHotelIdAndHrId(hotelId,dto.getHrId())>0){
                    throw new BusinessException("贵公司与该公司存在未完成的任务，暂时不能解绑");
                }
                if (hotelHr == null) {
                    throw new BusinessException("数据异常，获取绑定关系失败");
                } else {
                    hotelHr.setRelieveType(2);
                    hotelHr.setStatus(5);
                    hotelHrCompanyMapper.update(hotelHr);
                }
            }
            company = companyMapper.selectById(dto.getHrId());
        }

        messageService.hotelBindHrCompany(dto.getSet(), company, "applyUnbindMessage", type, dto.getReason());
        return ResultDO.buildSuccess("操作成功");
    }

    /**
     * 查询人力绑定的用人单位
     * @param request
     * @return
     */
    @Override
    public List<CompanyCooperate> queryHrBindHotel(CompanyQueryDTO request) {

        List<Map<String, Object>> list = companyMapper.queryHotelsByHrId(request);
        List<CompanyCooperate> cooperateList = new ArrayList<>();
        if (list != null) {
            for (Map<String, Object> map : list) {
                CompanyCooperate cooperate = new CompanyCooperate();
                cooperate.setName(map.get("name").toString());
                cooperate.setLogo(map.get("logo") == null ? "" : map.get("logo").toString());
                cooperate.setAddress((String)map.get("area") + (String)map.get("address"));
                cooperate.setLicense((String)map.get("businessLicense"));
                cooperate.setMobile((String)map.get("leaderMobile"));
                cooperate.setLeader((String)map.get("leader"));
                int status = (Integer)map.get("status");
                if (status == 0) {
                    cooperate.setStatus("未审核");
                } else if (status == 1) {
                    cooperate.setStatus("已审核");
                } else if (status == 2) {
                    cooperate.setStatus("已冻结");
                }
                cooperateList.add(cooperate);
            }
        }
        return cooperateList;
    }

    /**
     * 查询用人单位绑定的人力
     * @param request
     * @return
     */
    @Override
    public List<CompanyCooperate> queryHotelBindHr(CompanyQueryDTO request) {

        List<Map<String, Object>> list =  companyMapper.queryCompanysByHotelId(request);
        List<CompanyCooperate> cooperateList = new ArrayList<>();
        if (list != null) {
            for (Map<String, Object> map : list) {
                CompanyCooperate cooperate = new CompanyCooperate();
                cooperate.setLeader((String)map.get("leader"));
                cooperate.setMobile((String)map.get("leaderMobile"));
                cooperate.setLicense((String)map.get("businessLicense"));
                cooperate.setLogo(map.get("logo").toString());
                cooperate.setName(map.get("name").toString());
                cooperate.setAddress((String)map.get("area") + (String)map.get("address"));
                int status = (Integer)map.get("status");
                if (status == 0) {
                    cooperate.setStatus("未审核");
                } else if (status == 1) {
                    cooperate.setStatus("已审核");
                } else if (status == 2) {
                    cooperate.setStatus("已冻结");
                }
                cooperateList.add(cooperate);
            }

        }
        return cooperateList;
    }

    /**
     * 查询所有用人单位信息
     * @return
     */
    @Override
    public List<EmployerInfo> queryHotelInfo() {

        int count = companyMapper.selectCompanyCount(1);
        if (count == 0) {
            return new ArrayList<EmployerInfo>();
        }
        PageHelper.startPage(1, count, true);
        List<EmployerInfo> list = companyMapper.selectHotelInfo(1);
        if (list == null) {
            list = new ArrayList<EmployerInfo>();
        }
        return list;
    }

    /**
     * 查询所有人力信息
     * @return
     */
    @Override
    public List<HrInfo> queryInfo() {
        int count = companyMapper.selectCompanyCount(2);
        if (count == 0) {
            return new ArrayList<HrInfo>();
        }
        PageHelper.startPage(1, count, true);
        List<HrInfo> list = companyMapper.selectHrInfo(2);
        if (list == null) {
            list = new ArrayList<HrInfo>();
        }
        return list;
    }

    @Override
    public ResultDO hotelCancelHandle(HotelCancelParam request) {
        System.out.println ("param:"+request);
        if (StringUtils.isEmpty(request.getMessageId()) || StringUtils.isEmpty(request.getStatus())) {
            throw new ParamsException("参数错误");
        }
        Message message = messageMapper.selectById(request.getMessageId());
        if (message == null || message.getStatus() == 1) {
            throw new ParamsException("已处理");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        Task task = taskMapper.selectById (message.getTaskId ( ));
        if(request.getStatus () == 0){
            if (StringUtils.isEmpty(request.getWorkerId ())) {
                throw new ParamsException("所选小时工不能为空 ");
            }
            //更新用人单位任务的已确认小时工人数和拒绝小时工人数
            if (task == null) {
                throw new ParamsException ("查询不到用人单位任务");
            }
            task.setConfirmedWorkers (task.getConfirmedWorkers ( ) - 1);
            task.setRefusedWorkers (task.getRefusedWorkers ( ) + 1);
            if (task.getStatus ( ) == 4) {
                task.setStatus (3);
            }
            taskMapper.updateAllColumnById (task);
            //更新小时工任务
            TaskWorker taskWorker = taskWorkerMapper.selectById (message.getWorkerTaskId ( ));
            if (taskWorker == null) {
                throw new ParamsException ("小时工任务查询不到");
            }
            taskWorker.setStatus (2);
            taskWorker.setRefusedReason (message.getContent ( ));
            taskWorkerMapper.updateById (taskWorker);

            //给取消任务的小时工发送通知

            String content = companyMapper.findCompanyById (taskWorker.getHotelId ()).getName () + "同意了你的取消任务申请。";
            informService.sendInformInfo (2, 1, content, message.getWorkerId ( ), "申请取消成功");
            try {
                jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (message.getWorkerId ( )).getMobile ( ), content));
            } catch (APIConnectionException e) {
                e.printStackTrace ( );
            } catch (APIRequestException e) {

            }
            if (task.getFromDate ( ).isBefore (OffsetDateTime.now ( ))) {
                return ResultDO.buildSuccess ("任务已执行，派发失败");
            }
            //给新的小时工派发任务
            TaskWorker workerTask = new TaskWorker ( );
            workerTask.setStatus (0);
            workerTask.setDayEndTime (taskWorker.getDayEndTime ( ));
            workerTask.setDayStartTime (taskWorker.getDayStartTime ( ));
            workerTask.setToDate (taskWorker.getToDate ( ));
            workerTask.setFromDate (taskWorker.getFromDate ( ));
            workerTask.setHotelName (task.getHotelName ( ));
            workerTask.setHourlyPay (task.getHourlyPay ( ));
            workerTask.setTaskContent (task.getTaskContent ( ));
            workerTask.setTaskTypeCode (task.getTaskTypeCode ( ));
            workerTask.setTaskTypeText (task.getTaskTypeText ( ));
            //workerTask.setTaskHrId (null);
            workerTask.setWorkerId (request.getWorkerId ());
            User us = userMapper.selectByWorkerId (request.getWorkerId ());
            workerTask.setUserId (us.getPid ( ));
            workerTask.setUserName (us.getNickname ( ));
            //workerTask.setHrCompanyName (null);
            workerTask.setHotelId (task.getHotelId ( ));
            //workerTask.setHrCompanyId (null);
            workerTask.setHotelTaskId (task.getPid ());
            workerTask.setSettlementPeriod (task.getWorkerSettlementPeriod ());
            workerTask.setSettlementNum (task.getWorkerSettlementNum ());
            workerTask.setType (1);
            taskWorkerMapper.insert (workerTask);
            //发送消息
            List list = new ArrayList ();
            list.add (workerTask);
            messageService.hotelDistributeWorkerTask (list, task, false);
        }else{
            String content = companyMapper.findCompanyById (task.getHotelId ()).getName () + "拒绝了你的取消任务申请，希望你能完成该任务。";
            informService.sendInformInfo (3, 1, content, message.getWorkerId ( ), "申请取消被拒绝");
            try {
                jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (message.getWorkerId ( )).getMobile ( ), content));
            } catch (APIConnectionException e) {
                e.printStackTrace ( );
            } catch (APIRequestException e) {

            }
        }
        return ResultDO.buildSuccess ("操作成功");
    }

    @Override
    public ResultDO hotelAgreeWorkerRefuseAndPost(String messageId, String workerId) {
        if (StringUtils.isEmpty (workerId) || StringUtils.isEmpty (messageId)) {
            throw new ParamsException ("参数不能为空");
        }

        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new ParamsException("已处理");
        }
        message.setStatus(1);
        messageMapper.updateAllColumnById(message);

        Task task = taskMapper.selectById (message.getTaskId ( ));
        if (task == null) {
            throw new ParamsException ("查询不到用人单位任务");
        }
        Company hotel = companyMapper.findCompanyById (task.getHotelId ());
        //小时工任务更新
        TaskWorker taskWorker = taskWorkerMapper.selectById (message.getWorkerTaskId ( ));
        if (taskWorker == null) {
            throw new ParamsException ("查询不到小时工任务信息");
        }
        //发送通知给拒绝任务的小时工
        String content = hotel.getName ( ) + "同意了你的拒绝任务申请,拒绝原因：" + message.getContent ( );
        informService.sendInformInfo (2, 1, content, message.getWorkerId ( ), "拒绝任务");
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (message.getWorkerId ( )).getMobile ( ), content));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {

        }
        if (!message.isStop ( )) {
            if (task.getFromDate ( ).isBefore (OffsetDateTime.now ( ))) {
                return ResultDO.buildSuccess ("任务已开始，处理超时");
            }
        }
        //派发小时工任务
        List <Map <String, String>> list = new ArrayList <> ( );
        Map <String, String> m = new HashMap <> ( );
        TaskWorker taskWork = new TaskWorker ( );
        User user = userMapper.queryByWorkerId (workerId);
        taskWork.setUserId (user.getPid ( ));
        taskWork.setWorkerId (user.getWorkerId ( ));
        taskWork.setUserName (user.getUsername ( ));
        taskWork.setStatus (0);
        taskWork.setFromDate (taskWorker.getFromDate ( ));
        taskWork.setToDate (taskWorker.getToDate ( ));
        taskWork.setHourlyPay (taskWorker.getHourlyPay ( ));
        taskWork.setTaskTypeCode (taskWorker.getTaskTypeCode ( ));
        taskWork.setTaskContent (taskWorker.getTaskContent ( ));
        taskWork.setTaskTypeText (taskWorker.getTaskTypeText ( ));
        taskWork.setHotelName (taskWorker.getHotelName ( ));
        taskWork.setHotelId (task.getHotelId ( ));
        taskWork.setDayStartTime (task.getDayStartTime ( ));
        taskWork.setDayEndTime (task.getDayEndTime ( ));
        taskWork.setHotelTaskId (task.getPid ( ));
        taskWork.setSettlementPeriod (taskWorker.getSettlementPeriod ());
        taskWork.setSettlementNum (taskWorker.getSettlementNum ());
        taskWork.setType (1);
        taskWorkerMapper.insert (taskWork);
        List lst = new ArrayList ();
        lst.add (taskWork);
        messageService.hotelDistributeWorkerTask (lst, task, false);
        return ResultDO.buildSuccess ("派发成功");
    }

}
