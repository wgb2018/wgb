package com.microdev.service.impl;


import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.StringKit;
import com.microdev.converter.TaskConverter;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.service.CompanyService;
import com.microdev.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    UserCompanyMapper userCompanyMapper;
    @Autowired
    UserMapper userMapper;

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
        if(queryDTO.getObservertype () == 1){
            String total = dictMapper.findByNameAndCode ("WorkerBindHrMaxNum","1").getText ();
            Map<String,Object> map = new HashMap <> ();
            map.put("user_id",userMapper.queryByWorkerId (queryDTO.getObserverId ()).getPid ());
            Integer num = userCompanyMapper.selectByMap (map).size();
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
        List<Map<String, Object>> list = companyMapper.queryHotelsByHrId(request);
        logger.info("-------------" + list.toString());

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
        Company company = companyMapper.findCompanyById(id);
        if (company == null) {
            company = new Company();
        }
        return ResultDO.buildSuccess(company);
    }
    /**
     * 人力资源公司和酒店关系绑定 根据ID绑定
     */
    @Override
    public ResultDO hotelAddHrCompanyById(String hotelId, String hrCompanyId, String messageId, Integer type) {
        if (StringUtils.isEmpty(messageId) || (type != 1 && type != 2)) {
            throw new ParamsException("参数不能为空");
        }
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getStatus() == 1) {
            throw new ParamsException("消息已经被处理");
        }
        message.setStatus(1);
        messageMapper.updateAllColumnById(message);
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("hotel_id", message.getHotelId());
        columnMap.put("hr_id", message.getHrCompanyId());
        List<HotelHrCompany> list = hotelHrCompanyMapper.selectByMap(columnMap);
        if (list == null || list.size() == 0) {
            HotelHrCompany h = new HotelHrCompany();
            h.setDeleted(false);
            h.setStatus(0);
            h.setBindType(type);
            h.setBindTime(OffsetDateTime.now());
            h.setHrId(message.getHrCompanyId());
            h.setHotelId(message.getHotelId());
            hotelHrCompanyMapper.insert(h);
        } else {
            HotelHrCompany h = list.get(0);
            if (h.getStatus() == 1) {
                h.setDeleted(false);
                h.setStatus(0);
                h.setBindType(type);
                h.setBindTime(OffsetDateTime.now());
                hotelHrCompanyMapper.updateAllColumnById(h);
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
        logger.info(list.toString());
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
    public boolean supplementResponse(String id, String status) {
        if (!"0".equals(status) && !"1".equals(status)) {
            throw new ParamsException("参数错误");
        }
        Message mg = new Message();
        Message oldMsg = messageMapper.selectById(id);
        mg.setStatus(1);
        messageMapper.updateById(oldMsg);


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
        }
        return true;
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
        m.setStatus(0);
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
    public boolean workExpand(String id, String status) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(status) ) {
            throw new ParamsException("参数不能为空");
        }
        Message m = messageMapper.selectById(id);
        m.setStatus(1);
        messageMapper.updateById(m);

        if ("1".equals(status)) {
            Integer minutes = m.getMinutes() == null ? 0 : m.getMinutes();

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
        }
        return true;
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
        if (type == 1) {
            if (StringUtils.isEmpty(dto.getHotelId())) {
                throw new ParamsException("参数hotelId为空");
            }
            Company company = companyMapper.selectById(dto.getHotelId());
            messageService.hotelBindHrCompany(dto.getSet(), company, "applyBindMessage", type);

            for (String hrId : hrSet) {
                hotelHr = new HotelHrCompany();
                hotelHr.setStatus(3);
                hotelHr.setHotelId(company.getPid());
                hotelHr.setHrId(hrId);
                list.add(hotelHr);
            }
        } else if (type == 2) {
            if (StringUtils.isEmpty(dto.getHrId())) {
                throw new ParamsException("参数hrId为空");
            }
            Company company = companyMapper.selectById(dto.getHrId());
            messageService.hotelBindHrCompany(dto.getSet(), company, "applyBindMessage", type);
            for (String hotelId : hrSet) {
                hotelHr = new HotelHrCompany();
                hotelHr.setStatus(3);
                hotelHr.setHotelId(hotelId);
                hotelHr.setHrId(company.getPid());
                list.add(hotelHr);
            }
        }
        if (list.size() > 0) {
            hotelHrCompanyMapper.saveBatch(list);
        }
        return ResultDO.buildSuccess("申请成功");
    }
}
