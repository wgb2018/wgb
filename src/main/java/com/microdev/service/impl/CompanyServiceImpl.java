package com.microdev.service.impl;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.PagedList;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.StringKit;
import com.microdev.converter.TaskConverter;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.service.CompanyService;
import com.microdev.service.DictService;
import com.microdev.service.MessageService;
import com.microdev.type.UserType;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;

@Transactional
@Service
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper,Company> implements CompanyService{

    private static final Logger logger = LoggerFactory.getLogger(CompanyServiceImpl.class);

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

    @Override
    public ResultDO pagingCompanys(Paginator paginator, CompanyQueryDTO queryDTO) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<Company> list = companyMapper.queryCompanys(queryDTO);
        PageInfo<Company> pageInfo = new PageInfo<>(list);
        System.out.println ("last:"+pageInfo.isHasNextPage ());
        HashMap<String,Object> result = new HashMap<>();
         //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
         //设置数据集合rows：
        result.put("result",list);
        result.put("page",paginator.getPage());
        if(queryDTO.getObservertype () == 0){
            String total = dictMapper.findByNameAndCode ("WorkerBindHrMaxNum","1").getText ();
            Map<String,Object> map = new HashMap <> ();
            /*map.put("user_id",userMapper.queryByWorkerId (queryDTO.getObserverId ()).getPid ());
            System.out.println ("userId:"+userMapper.queryByWorkerId (queryDTO.getObserverId ()).getPid ());
            map.put("status","1 or status = 3");*/
            Wrapper<UserCompany> et = new EntityWrapper<UserCompany> ().where("user_id={0}",userMapper.queryByWorkerId (queryDTO.getObserverId ()).getPid ()).in("status","1,3");
            //userCompanyMapper.selectList (et);
            Integer num = userCompanyMapper.selectList(et).size();
            map.clear ();
            map.put("bindTotalNum",Integer.parseInt (total));
            map.put("bindNum",num);
            return ResultDO.buildSuccess("您已经绑定"+num+"家人力公司，还可以绑定"+total+"家人力公司",result,map,null);
        }
        return ResultDO.buildSuccess(result);
    }

    @Override
    public ResultDO hrCompanyHotels(Paginator paginator, CompanyQueryDTO request) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        logger.info(request.toString());
        List<Map<String, Object>> list = companyMapper.queryHotelsByHrId(request);

        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",list);
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess(result);
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
        }

        return ResultDO.buildSuccess(company);
    }
    /**
     * 人力资源公司和酒店关系绑定 根据ID绑定
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
        Inform inform = null;
        if (type == 1) {//酒店处理
            inform = new Inform();
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
                    throw new BusinessException("酒店人力关系数据查询不到");
                } else {
                    HotelHrCompany h = list.get(0);
                    h.setDeleted(false);
                    h.setStatus(0);
                    h.setBindType(type);
                    h.setBindTime(OffsetDateTime.now());
                    hotelHrCompanyMapper.updateAllColumnById(h);
                }
                inform.setTitle("绑定成功");
                inform.setContent(company.getName() + "同意了你的绑定申请，成功添加为合作酒店,添加合作酒店代表同意劳务合作协议。你可以接受合作酒店派发的任务，选择小时工，确保能够及时完美的完成任务，可以获得和支出相应的酬劳。");
            } else {
                throw new BusinessException("参数错误");
            }
            informMapper.insertInform(inform);
        } else {//人力处理
            inform = new Inform();
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
                    throw new BusinessException("酒店人力关系数据查询不到");
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
     * 人力资源公司和酒店关系移除
     */
    @Override
    public ResultDO hotelRemoveHrCompany(HotelHrIdBindDTO hotelHrDTO) {
        HotelHrCompany hotelHr=hotelHrCompanyMapper.findOneHotelHr(hotelHrDTO.getHotelId(),hotelHrDTO.getHrId());
        if(hotelHr==null){
            throw new ParamsException("没有发现酒店和人力公司的绑定记录");
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
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",list);
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess(result);
    }

    @Override
    public ResultDO hotelNotHrCompanies(String id) {
        List<Company> list=  companyMapper.queryNotCompanysByHotelId(id);
        return ResultDO.buildSuccess(list);
    }

    @Override
    public ResultDO hrCompanyNotHotels(String id) {
        List<Company> list = companyMapper.queryNotHotelsByHrId(id);
        return ResultDO.buildSuccess(list);
    }

    /**
     * 酒店反馈小时工补签申请
     * @param id        消息id
     * @param status    0拒绝1同意
     * @return
     */
    @Override
    public String supplementResponse(String id, String status) {
        if (!"0".equals(status) && !"1".equals(status)) {
            throw new ParamsException("参数错误");
        }
        Inform inform = new Inform();
        Message oldMsg = messageMapper.selectById(id);
        oldMsg.setStatus(1);
        messageMapper.updateById(oldMsg);

        Company c = companyMapper.selectById(oldMsg.getHotelId());
        inform.setAcceptType(1);
        inform.setSendType(3);
        inform.setStatus(0);
        inform.setReceiveId(oldMsg.getWorkerId());
        if ("1".equals(status)) {
            PunchMessageDTO punch = messageMapper.selectPunchMessage(id);
            if (punch == null) {
                throw new ParamsException("数据异常");
            }
            Map<String, Object> param = new HashMap<>();
            param.put("id", punch.getId());
            param.put("punchDate", OffsetDateTime.now());
            param.put("modifyTime", OffsetDateTime.now(Clock.system(ZoneId.of("Asia/Shanghai"))));
            Long minutes = 0L;
            if (punch.getToDate() == null) {
                if (punch.getFromDate().compareTo(punch.getSupplement()) < 0) {
                    param.put("toDate", punch.getSupplement());
                    long seconds = punch.getSupplement().getLong(ChronoField.SECOND_OF_DAY) - punch.getFromDate().getLong(ChronoField.SECOND_OF_DAY);
                    minutes = seconds / 60;
                } else {
                    param.put("toDate", punch.getFromDate());
                    param.put("fromDate", punch.getSupplement());
                    long seconds = punch.getFromDate().getLong(ChronoField.SECOND_OF_DAY) - punch.getSupplement().getLong(ChronoField.SECOND_OF_DAY);
                    minutes = seconds / 60;
                }
                param.put("punchDate", OffsetDateTime.now());
                param.put("minutes", minutes);
                workLogMapper.updateByMapId(param);
            }

            if (minutes > 0) {
                Map<String, Double> mapPay = taskMapper.selectHrAndTaskHourPay(punch.getTaskId());
                Double shouldPayMoney_hrtoworker = (minutes / 60.00) * mapPay.get("hrPay");
                Double shouldPayMoney_hoteltohr = (minutes / 60.00) * mapPay.get("taskPay");

                //小数点后保留两位(第三位四舍五入)
                shouldPayMoney_hrtoworker = new BigDecimal(shouldPayMoney_hrtoworker).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                shouldPayMoney_hoteltohr = new BigDecimal(shouldPayMoney_hoteltohr).
                        setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                Map<String, Object> p = new HashMap<>();
                taskWorkerMapper.addMinutes(punch.getTaskWorkerId(),minutes,shouldPayMoney_hrtoworker);
                taskHrCompanyMapper.addMinutes(punch.getTaskHrId(),minutes,shouldPayMoney_hrtoworker,shouldPayMoney_hoteltohr);
                taskMapper.addMinutes(punch.getTaskId(),minutes,shouldPayMoney_hoteltohr);
            }

            inform.setTitle("申请补签成功");
            inform.setContent(c.getName() + "拒绝了你的补签申请。" + oldMsg.getMessageContent());
        } else if ("0".equals(status)) {

            inform.setTitle("申请补签被拒绝");
            inform.setContent(c.getName() + "拒绝了你的补签申请。" + oldMsg.getMessageContent());
        } else {
            throw new ParamsException("参数错误");
        }
        informMapper.insertInform(inform);
        return "成功";
    }
    /**
     * 酒店申请替换小时工
     */
    @Override
    public boolean changeWorker(Map<String, Object> map) {
        if (map == null) {
            throw new ParamsException("参数不能为空");
        }
        if (StringUtils.isEmpty(map.get("taskWorkerId").toString())) {
            throw new ParamsException("参数taskWorkerId不能为空");
        }
        if (StringUtils.isEmpty(map.get("hotelId").toString())) {
            throw new ParamsException("参数hotelId不能为空");
        }
        if ( StringUtils.isEmpty(map.get("reason").toString())) {
            throw new ParamsException("参数reason不能为空");
        }
        if ( StringUtils.isEmpty(map.get("hrCompanyId").toString())) {
            throw new ParamsException("参数hrCompanyId不能为空");
        }

        Message m = new Message();
        Map<String, Object> tp = taskWorkerMapper.selectHrId((String)map.get("taskWorkerId"));

        MessageTemplate mess = messageTemplateMapper.findFirstByCode("applyChangeMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageTitle(mess.getTitle());
        m.setContent((String)map.get("reason"));
        m.setWorkerId(null);
        m.setHrCompanyId((String)map.get("hrCompanyId"));
        m.setHotelId((String)map.get("hotelId"));
        m.setWorkerTaskId((String)map.get("taskWorkerId"));
        Map<String, String> param = new HashMap<>();
        param.put("userName", (String)tp.get("hotelName"));
        param.put("taskContent", (String)map.get("reason"));
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setApplyType(2);
        m.setApplicantType(3);
        m.setStatus(0);
        m.setIsTask(0);
        m.setMessageType(9);
        messageMapper.insert(m);
        return true;
    }
    /**
     * 酒店账目明细
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
     * 分页查询酒店事务
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
     * 展示酒店待处理的信息详情
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
        return ResultDO.buildSuccess("成功");
    }
    /**
     * 酒店再发布
     */
    @Override
    public boolean hotelPublish(HotelDeployInfoRequest request) {
        if (request == null) {
            throw new ParamsException("参数不能为空");
        }
        if (!StringUtils.hasLength(request.getHotelId())) {
            throw new ParamsException("参数hotelId不能为空");
        }
        if (StringUtils.isEmpty(request.getName())) {
            throw new ParamsException("酒店名称不能为空");
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
            message.setApplyType(2);
            message.setContent(request.getTaskContent());
            message.setSupplementTime(request.getFromDate());
            message.setSupplementTimeEnd(request.getToDate());
            message.setHotelId(request.getHotelId());
            message.setHrCompanyId((String)param.get("id"));
            message.setMessageCode(mess.getCode());
            message.setMessageTitle(mess.getTitle());
            message.setStatus(0);
            message.setIsTask(0);
            message.setMessageType(6);
            message.setStatus((Integer)param.get("number"));

            map.put("number", String.valueOf(param.get("number")));
            String c = StringKit.templateReplace(mess.getContent(), map);
            message.setMessageContent(c);
            list.add(message);
        }
        messageMapper.saveBatch(list);
        return true;
    }

    /**
     * 酒店处理小时工加时
     * @param id            消息id
     * @param status        0拒绝1同意
     * @return
     */
    @Override
    public String workExpand(String id, String status) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(status) ) {
            throw new ParamsException("参数不能为空");
        }
        Message m = messageMapper.selectById(id);
        m.setStatus(1);
        messageMapper.updateById(m);

        Inform inform = new Inform();
        inform.setReceiveId(m.getWorkerId());
        inform.setSendType(3);
        inform.setAcceptType(1);
        Company c = companyMapper.selectById(m.getHotelId());

        if ("1".equals(status)) {
            Integer minutes = m.getMinutes() == null ? 0 : Integer.valueOf(m.getMinutes());

            Map<String, Object> map = new HashMap<>();
            map.put("taskWorkerId", m.getWorkerTaskId());
            map.put("time", m.getSupplementTime());
            WorkLog log = workLogMapper.selectWorkLogByTime(map).get(0);
            Integer logMinutes = log.getMinutes() == null ? 0 : log.getMinutes();
            log.setMinutes(logMinutes + minutes);
            workLogMapper.updateById(log);

            Map<String, Double> mapPay = taskMapper.selectHrAndTaskHourPay(log.getTaskId());
            Double shouldPayMoney_hrtoworker = (minutes / 60.00) * mapPay.get("hrPay");
            Double shouldPayMoney_hoteltohr = (minutes / 60.00) * mapPay.get("taskPay");
            shouldPayMoney_hrtoworker = new BigDecimal(shouldPayMoney_hrtoworker).
                    setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            shouldPayMoney_hoteltohr = new BigDecimal(shouldPayMoney_hoteltohr).
                    setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            taskWorkerMapper.addMinutes(m.getWorkerTaskId(),minutes.longValue(),shouldPayMoney_hrtoworker);
            TaskWorker taskWorker = taskWorkerMapper.selectById(m.getWorkerTaskId());
            taskHrCompanyMapper.addMinutes(taskWorker.getTaskHrId(),minutes.longValue(),shouldPayMoney_hrtoworker,shouldPayMoney_hoteltohr);
            taskMapper.addMinutes(log.getTaskId(),minutes.longValue(),shouldPayMoney_hoteltohr);


            inform.setTitle("申请加时成功");
            inform.setContent(c.getName() + "同意了你的加时请求。" + m.getMessageContent());
        } else if ("0".equals(status)) {

            inform.setTitle("申请加时被拒绝");
            inform.setContent(c.getName() + "拒绝了你的加时请求。" + m.getMessageContent());
        } else {
            throw new ParamsException("参数错误");
        }

        informMapper.insertInform(inform);
        return "申请成功";
    }
    /**
     * 酒店申请绑定人力资源公司或人力公司申请绑定酒店
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
        if (type == 1) {
            //酒店加人力
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
                    hotelHr = hotelHrCompanyMapper.selectByHrHotelId(dto.getHotelId(), hrId);
                    if (hotelHr == null) {
                        hotelHr = new HotelHrCompany();
                        hotelHr.setStatus(3);
                        hotelHr.setBindType(type);
                        hotelHr.setHotelId(dto.getHotelId());
                        hotelHr.setHrId(hrId);
                        list.add(hotelHr);
                    } else {
                        hotelHr.setStatus(3);
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
                    list.add(hotelHr);
                }
            }
            company = companyMapper.selectById(dto.getHotelId());

        } else if (type == 2) {
            //人力加酒店
            if (StringUtils.isEmpty(dto.getHrId())) {
                throw new ParamsException("参数hrId为空");
            }
            int num = hotelHrCompanyMapper.selectBindCountByHrId(dto);
            if (num > 0) {
                throw new BusinessException("已绑定数据,请勿重复");
            }
            num = hotelHrCompanyMapper.selectIsBIndByCompanyId(dto);
            if (num > 0) {
                for (String hotelId : hrSet) {
                    hotelHr = hotelHrCompanyMapper.selectByHrHotelId(hotelId, dto.getHrId());
                    if (hotelHr == null) {
                        hotelHr = new HotelHrCompany();
                        hotelHr.setBindType(2);
                        hotelHr.setStatus(3);
                        hotelHr.setHotelId(hotelId);
                        hotelHr.setHrId(dto.getHrId());
                        list.add(hotelHr);
                    } else {
                        hotelHr.setStatus(3);
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
                    list.add(hotelHr);
                }
            }
            company = companyMapper.selectById(dto.getHrId());
        }
        if (list.size() > 0) {
            hotelHrCompanyMapper.saveBatch(list);
        }

        messageService.hotelBindHrCompany(dto.getSet(), company, "applyBindMessage", type);
        return ResultDO.buildSuccess("申请成功");
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
        message.setStatus(1);
        messageMapper.updateById(message);
        Inform inform = new Inform();
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
            inform.setTitle("解绑成功");
            inform.setContent(company.getName() + "同意了你的申请解绑。你可以添加新的合作人力公司，没人最多只能绑定5家人力公司");
        }
        informMapper.insertInform(inform);
        return "成功";
    }

    /**
     * 人力查询待审核的酒店信息
     * @param hrCompanyId
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
        logger.info("size=" + list.size());
        for (Map<String, Object> map : list) {
            for(Map.Entry<String, Object> m : map.entrySet()) {
                System.out.print("key:" + m.getKey() + ";value=" + m.getValue());
            }
            System.out.println();
        }
        Map<String, Object> param = new HashMap<>();
        param.put("page", page);
        param.put("total", pageInfo.getTotal());
        param.put("result", list);
        return ResultDO.buildSuccess(param);
    }

    /**
     * 查询合作的人力公司信息
     * @param map
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
     * 人力处理酒店绑定申请或酒店处理人力绑定
     * @param messageId     消息id
     * @param status        0拒绝1同意
     * @return
     */
    @Override
    public ResultDO hrHandlerHotelBind(String messageId, String status) {

        if (StringUtils.isEmpty(messageId)) {
            throw new ParamsException("参数错误");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("查询不到这个消息");
        }
        message.setStatus(1);
        messageMapper.updateById(message);
        Inform inform = new Inform();
        inform.setSendType(2);
        inform.setAcceptType(3);
        inform.setReceiveId(message.getHotelId());
        HotelHrCompany hotelHrCompany = hotelHrCompanyMapper.findOneHotelHr(message.getHotelId(), message.getHrCompanyId());
        if (hotelHrCompany == null) {
            throw new BusinessException("查询不到人力酒店关系");
        }
        Company company = null;
        if ("1".equals(status)) {

            hotelHrCompany.setStatus(0);
            hotelHrCompanyMapper.update(hotelHrCompany);
            inform.setTitle("绑定成功");
            if (message.getApplicantType() == 2) {
                company = companyMapper.selectById(message.getHotelId());
                inform.setContent(company.getName() + "同意了你的绑定申请，成功添加为合作酒店,添加合作酒店代表同意劳务合作协议。你可以接受合作酒店派发的任务，选择小时工，确保能够及时完美的完成任务，可以获得和支出相应的酬劳。");
            } else if (message.getApplicantType() == 3) {
                company = companyMapper.selectById(message.getHrCompanyId());
                inform.setContent(company.getName() + "接受了你的绑定申请，成功添加为合作人力公司。添加人力公司代表同意劳务合作协议，你可以向合作的人力公司派发任务，由合作的的人力公司选择小时工，并支出相应的酬劳，确保能及时完美的完成任务。");
            } else {
                throw new BusinessException("数据错误");
            }

        } else if ("0".equals(status)) {
            inform.setTitle("绑定拒绝");

            if (message.getApplicantType() == 2) {
                company = companyMapper.selectById(message.getHotelId());
            } else if (message.getApplicantType() == 3) {
                company = companyMapper.selectById(message.getHrCompanyId());
            } else {
                throw new BusinessException("数据错误");
            }
            hotelHrCompany.setStatus(4);
            hotelHrCompanyMapper.update(hotelHrCompany);
            inform.setContent(company.getName() + "拒绝了你的绑定申请，等以后有机会希望可以再合作。");
        } else {
            throw new ParamsException("参数错误");
        }
        informMapper.insertInform(inform);
        return ResultDO.buildSuccess("处理成功");
    }

    /**
     * 人力查询合作的酒店
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
     * 酒店查询待审核的人力公司
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
     *酒店处理小时工请假
     * @param messageId    消息id
     * @param status       0拒绝1同意
     * @return
     */
    @Override
    public ResultDO hotelHandleLeave(String messageId, String status) {

        if (StringUtils.isEmpty(messageId) || StringUtils.isEmpty(status)) {
            throw new ParamsException("参数错误");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("查询不到消息记录");
        }
        message.setStatus(1);
        messageMapper.updateById(message);

        if ("1".equals(status)) {
            Holiday holiday = new Holiday();
            holiday.setFromDate(message.getSupplementTime());
            holiday.setToDate(message.getSupplementTimeEnd());
            holiday.setTaskWorkerId(message.getWorkerTaskId());
            holidayMapper.insertAllColumn(holiday);
        } else if (!"0".equals(status)){
            throw new ParamsException("参数错误");
        }
        return ResultDO.buildSuccess("处理成功");
    }

}
