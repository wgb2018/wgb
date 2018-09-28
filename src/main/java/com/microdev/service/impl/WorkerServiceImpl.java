package com.microdev.service.impl;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.FilePush;
import com.microdev.common.ResultDO;
import com.microdev.common.context.ServiceContextHolder;
import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.oss.ObjectStoreService;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.*;
import com.microdev.converter.WorkLogConverter;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.param.AreaAndServiceRequest;
import com.microdev.param.api.response.GetBalanceResponse;
import com.microdev.param.api.response.GetCurrentTaskResponse;
import com.microdev.service.DictService;
import com.microdev.service.InformService;
import com.microdev.service.MessageService;
import com.microdev.service.WorkerService;
import com.microdev.type.ConstantData;
import com.microdev.type.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;

@Transactional
@Service
public class WorkerServiceImpl extends ServiceImpl<WorkerMapper, Worker> implements WorkerService {

    private static final Logger log = LoggerFactory.getLogger(WorkerServiceImpl.class);
    @Autowired
    UserMapper userMapper;
    @Autowired
    TaskWorkerMapper taskWorkerMapper;
    @Autowired
    TaskMapper taskMapper;
    @Autowired
    TaskHrCompanyMapper taskHrCompanyMapper;
    @Autowired
    WorkerLogMapper workLogMapper;
    @Autowired
    CompanyMapper companyMapper;
    @Autowired
    private WorkLogConverter workLogConverter;
    @Autowired
    MessageTemplateMapper messageTemplateMapper;
    @Autowired
    MessageMapper messageMapper;
    @Autowired
    HolidayMapper holidayMapper;
    @Autowired
    private AreaRelationMapper areaRelationMapper;
    @Autowired
    private DictService dictService;
    @Autowired
    DictMapper dictMapper;
    @Autowired
    WorkerMapper workerMapper;
    @Autowired
    TaskTypeRelationMapper taskTypeRelationMapper;
    @Autowired
    private UserCompanyMapper userCompanyMapper;
    @Autowired
    private MessageService messageService;
    @Autowired
    private InformService informService;
    @Autowired
    private BillMapper billMapper;
    @Autowired
    JpushClient jpushClient;
    @Autowired
    private FilePush filePush;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public GetCurrentTaskResponse getCurrentTask(String workerId) {
        User user = userMapper.queryByWorkerId(workerId);

        if (user == null) {
            throw new ParamsException("参数userId输入有误");
        }
        String userId = user.getPid();
        //com.microdev.common.context.User user = ServiceContextHolder.getServiceContext().getUser();

        //if(user.用户类型为小时工)
        //判断小时工标识ID是否与传入参数一致
        //else if(user.用户类型为人力公司)//人力公司查看其下小时工的工作状况(若有该需求可由该接口提供)
        //判断该小时工当前是否与该人力公司存在绑定关系

        Task task = null;
        WorkLog log = null;
        TaskHrCompany taskHrCompany = null;
        OffsetTime time = OffsetDateTime.now().toOffsetTime();
        OffsetTime timeA = time.minusMinutes(30);
        OffsetDateTime ti = OffsetDateTime.now().minusMinutes(30);
        //取进行中的任务
        TaskWorker taskWorker = taskWorkerMapper.findWorkerNowTask(
                userId, TaskWorkerStatus.ACCEPTED.ordinal(),
                //结束60分钟内的任务 仍然当作当前任务 以便进行打卡签退操作(如若改动 需与前端保持一致)
                ti, time, timeA);
        //如果没有当前进行中的任务 则获取最近的下一个任务
        if (taskWorker == null) {
            OffsetDateTime of = OffsetDateTime.now();
            taskWorker = taskWorkerMapper.findWorkerNextTask(userId, TaskWorkerStatus.ACCEPTED.ordinal(), OffsetDateTime.ofInstant(new Date(of.getYear() - 1900, of.getMonthValue() - 1, of.getDayOfMonth()).toInstant(), ZoneOffset.systemDefault()).plusDays(1), time);
        }
        /*//如果没有下一个任务(也没有进行中的任务) 取前一个任务
        if (taskWorker == null) {
            taskWorker = taskWorkerMapper.findWorkerBeforeTask(userId, TaskWorkerStatus.ACCEPTED.ordinal(), OffsetDateTime.now());
        }
*/
        GetCurrentTaskResponse response = new GetCurrentTaskResponse();
        if (taskWorker != null) {
            taskHrCompany = taskHrCompanyMapper.queryByTaskId(taskWorker.getTaskHrId());
            if(taskHrCompany!=null){
                task = taskMapper.getFirstById(taskHrCompany.getTaskId());
            }else{
                task = taskMapper.getFirstById(taskWorker.getHotelTaskId ());
            }

            //取最近的一条工作记录以获取打卡信息
            OffsetDateTime of = OffsetDateTime.now();
            OffsetDateTime end = OffsetDateTime.ofInstant(new Date(of.getYear() - 1900, of.getMonthValue() - 1, of.getDayOfMonth()).toInstant(), ZoneOffset.systemDefault()).plusDays(1);
            OffsetDateTime begin = OffsetDateTime.ofInstant(new Date(of.getYear() - 1900, of.getMonthValue() - 1, of.getDayOfMonth()).toInstant(), ZoneOffset.systemDefault());
            log = workLogMapper.findFirstByTaskWorkerId(taskWorker.getPid(), begin, end);
        } else {
            return null;
        }
        if (task != null) {
            Company hotel = companyMapper.findCompanyById(task.getHotelId());
            response.setHotelId(task.getHotelId());
            /*response.setHotelName(task.getHotelName());
            response.setHotelLogo(hotel.getLogo());
            response.setHotelAddress(hotel.getAddress());
            response.setHrCompanyId(taskHrCompany.getHrCompanyId());
            response.setHrCompanyName(taskWorker.getHrCompanyName());
            response.setTaskId(task.getPid());
            response.setTaskType(task.getTaskTypeText());
            response.setTaskContent(task.getTaskContent());
            response.setFromDate(taskWorker.getFromDate());
            response.setToDate(taskWorker.getToDate());*/


            if (log == null) {//从来没有过打卡记录 则应签到
                response.setNeedPunchType(PunchType.PUNCHIN);
            } else {
                if (log.getFromDate() != null && log.getToDate() == null) {//有打卡记录
                    // 而签退时间为空 则应签退
                    response.setNeedPunchType(PunchType.PUNCHOUT);
                } else {
                    response.setNeedPunchType(PunchType.PUNCHIN);
                }
            }
            response.setTaskWorkerId(taskWorker.getPid());
           /* List<WorkLog> workLogs = workLogMapper.findByTaskWorkId(taskWorker.getPid());
            response.setWorkLogs(workLogConverter.toResponse(workLogs));
            response.setTaskWorkerHours(
                    new BigDecimal(taskWorker.getMinutes() / 60.00).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            response.setTaskWorkerStatus(taskWorker.getStatus());*/
        }

        return response;
    }

    @Override
    public String punch(String taskWorkerId, PunchType punchType, OffsetDateTime punchTime, Measure measure) {
        com.microdev.common.context.User user = ServiceContextHolder.getServiceContext().getUser();

        TaskWorker taskWorker = taskWorkerMapper.findFirstById(taskWorkerId);
        if (taskWorker == null || !taskWorker.getUserId().equals(user.getId())) {
            //未找到小时工任务信息
            return "打卡失败";
        }
        Company hotel = companyMapper.findCompanyById(taskWorker.getHotelId ());
        Double m = LocationUtils.getDistance(hotel.getLatitude(), hotel.getLongitude(), measure.getLatitude(), measure.getLongitude());
        /*if (m > 500) {
            return "打卡地点距离工作地" + m + "米,超过500米";
        }*/
        if (!taskWorker.getToDate().isAfter(OffsetDateTime.now().minusMinutes(30))) {
            return "超过打卡正常时间,打卡失败";
        }
        WorkLog log = null;
        Task task = null;
        TaskHrCompany taskHrCompany = null;
        if (punchType == PunchType.PUNCHIN) {//签到 创建新的工作记录
            log = new WorkLog();
            log.setTaskWorkerId(taskWorkerId);
            log.setFromDate(punchTime);
            log.setToDate(null);
            log.setMinutes(0);
            log.setRepastTimes(0);
            log.setPunchDate(OffsetDateTime.now());
            log.setTaskId(taskWorker.getHotelTaskId());
            log.setStatus(0);
            log.setEmployerConfirmStatus(0);
            workLogMapper.insert(log);
        } else {
            OffsetDateTime of = OffsetDateTime.now();
            OffsetDateTime end = OffsetDateTime.ofInstant(new Date(of.getYear() - 1900, of.getMonthValue() - 1, of.getDayOfMonth()).toInstant(), ZoneOffset.systemDefault()).plusDays(1);
            OffsetDateTime begin = OffsetDateTime.ofInstant(new Date(of.getYear() - 1900, of.getMonthValue() - 1, of.getDayOfMonth()).toInstant(), ZoneOffset.systemDefault());
            log = workLogMapper.findFirstByTaskWorkerId(taskWorkerId, begin, end);
            if (log != null) {
                if (punchType == PunchType.REPAST) {//用餐 用餐次数加1
                    log.setRepastTimes(log.getRepastTimes() + 1);
                } else if (punchType == PunchType.PUNCHOUT) {//签退 计算工时数 迭代更新
                    log.setToDate(punchTime);

                    //求时间差
                    Duration duration = Duration.between(taskWorker.getDayStartTime ().plusMinutes (5).isBefore (log.getFromDate().toOffsetTime ())?log.getFromDate().toOffsetTime ():taskWorker.getDayStartTime (), taskWorker.getDayEndTime ().minusMinutes (5).isBefore (log.getToDate().toOffsetTime ())?taskWorker.getDayEndTime ():log.getToDate().toOffsetTime ());
                    Long seconds_span = duration.getSeconds();
                    Long minutes = (seconds_span / 60);

                    //不再进行就餐打卡
                    //minutes = minutes - taskWorker.getRepastTimes() * 30;//减去用餐时间

                    if (minutes < 0) {
                        minutes = new Long(0);
                    }

                    if (minutes > 0) {
                        taskHrCompany = taskHrCompanyMapper.queryByTaskId(taskWorker.getTaskHrId());
                        if(taskHrCompany!=null){
                            task = taskMapper.getFirstById(taskHrCompany.getTaskId());
                        }else{
                            task = taskMapper.getFirstById(taskWorker.getHotelTaskId ());
                        }
                        Double shouldPayMoney_hrtoworker = (minutes / 60.00) * taskWorker.getHourlyPay();
                        Double shouldPayMoney_hoteltohr = (minutes / 60.00) * task.getHourlyPay();

                        //小数点后保留两位(第三位四舍五入)
                        shouldPayMoney_hrtoworker = new BigDecimal(shouldPayMoney_hrtoworker).
                                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        shouldPayMoney_hoteltohr = new BigDecimal(shouldPayMoney_hoteltohr).
                                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        taskWorkerMapper.addMinutes(taskWorkerId, minutes, shouldPayMoney_hrtoworker);
                        if(taskWorker.getTaskHrId()!=null){
                            taskHrCompanyMapper.addMinutes(taskWorker.getTaskHrId(), minutes,
                                    shouldPayMoney_hrtoworker, shouldPayMoney_hoteltohr);
                        }
                        taskMapper.addMinutes(task.getPid(), minutes, shouldPayMoney_hoteltohr);
                    }
                    log.setMinutes(minutes.intValue());
                }
            } else {
                //无相应工作记录
                return "无相应工作记录";
            }
            log.setPunchDate(OffsetDateTime.now());
            workLogMapper.updateById(log);
        }

        return "打卡成功";
    }

    /**
     * 获取小时工合作过的人力公司
     *
     * @param userId
     * @return 人力公司集合
     */
    @Override
    public List<Company> getHrCompanyPartners(String userId) {
        List<Company> result = companyMapper.queryByworkerId(userId);
        if (result == null) {
            return new ArrayList<>();
        }
        return result;
    }

    /**
     * 获取小时工款项信息
     *
     * @param userId 小时工标识ID
     * @return
     */
    @Override
    public GetBalanceResponse getBalance(String userId) {
        GetBalanceResponse response = new GetBalanceResponse();
        List<TaskWorker> taskWorkerList = taskWorkerMapper.findByUserId(userId);
        Double UnsettledAmount = 0d;
        Double settledAmount = 0d;
        for (TaskWorker taskWorker : taskWorkerList) {
            if ("0".equals(taskWorker.getSettled().toString())) {
                UnsettledAmount += taskWorker.getShouldPayMoney();
            } else {
                settledAmount += taskWorker.getShouldPayMoney();
            }
        }
        response.setUnsettledAmount(new BigDecimal(UnsettledAmount).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        response.setSettledAmount(new BigDecimal(settledAmount).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        return response;
    }

    /**
     * 小时工申请请假
     */
    @Override
    public String askForLeave(WorkerSupplementRequest info) {
        if (info == null) {
            throw new ParamsException("参数不能为空");
        }
        if (!StringUtils.hasLength(info.getReason())) {
            throw new ParamsException("理由不能为空");
        }
        if (!StringUtils.hasLength(info.getTaskWorkerId())) {
            throw new ParamsException("参数taskWorkerId不能为空");
        }
        if (info.getTime() == null) {
            throw new ParamsException("开始时间不能为空");
        }
        if (info.getEndTime() == null) {
            throw new ParamsException("结束时间不能为空");
        }

        OffsetDateTime time = null;
        OffsetDateTime endTime = null;
        try {
            time = DateUtil.strToOffSetDateTime(info.getTime(), "yyyy/MM/dd HH:mm");
            endTime = DateUtil.strToOffSetDateTime(info.getEndTime(), "yyyy/MM/dd HH:mm");
        } catch (ParseException e) {
            e.printStackTrace();
            return "日期格式错误，请用yyyy/MM/dd HH:mm格式";
        }

        Message m = new Message();
        m.setSupplementTime(time);
        m.setSupplementTimeEnd(endTime);
        m.setContent(info.getReason());
        WorkerCancelTask tp = taskWorkerMapper.selectUserAndWorkerId(info.getTaskWorkerId());

        MessageTemplate mess = messageTemplateMapper.findFirstByCode("applyLeaveMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageType(3);
        m.setMessageTitle(mess.getTitle());
        m.setWorkerId(tp.getWorkerId());
        m.setWorkerTaskId(info.getTaskWorkerId());
        m.setHotelId(tp.getHotelId());
        TaskWorker taskWorker = taskWorkerMapper.findFirstById(info.getTaskWorkerId());
        if (taskWorker == null) {
            throw new ParamsException("参数错误");
        }
        m.setHrCompanyId(taskWorker.getHrCompanyId());
        m.setHrTaskId(taskWorker.getTaskHrId());
        m.setTaskId(taskWorker.getHotelTaskId());
        Map<String, String> param = new HashMap<>();
        param.put("userName", tp.getUsername());

        param.put("startTime", info.getTime());
        param.put("taskContent", info.getReason());
        param.put("endTime", info.getEndTime());
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setApplyType(3);
        m.setApplicantType(1);
        m.setStatus(0);
        m.setIsTask(0);

        messageMapper.insert(m);

        return "操作成功";
    }

    /**
     * 小时工申请加班
     */
    @Override

    public String askWorkOvertime(WorkerSupplementRequest info) {
        if (info == null) {
            throw new ParamsException("参数不能为空");
        }
        if (!StringUtils.hasLength(info.getReason())) {
            throw new ParamsException("理由不能为空");
        }
        if (!StringUtils.hasLength(info.getTaskWorkerId())) {
            throw new ParamsException("参数taskWorkerId不能为空");
        }
        if (info.getTime() == null) {
            throw new ParamsException("开始时间不能为空");
        }
        if (info.getMinutes() == null || info.getMinutes() <= 0) {
            throw new ParamsException("加时时间不能小于0");
        }

        OffsetDateTime time = null;
        try {
            time = DateUtil.strToOffSetDateTime(info.getTime(), "yyyy/MM/dd");
        } catch (ParseException e) {
            e.printStackTrace();
            return "日期格式错误，请用yyyy/MM/dd HH:mm格式";
        }
        Message m = new Message();
        m.setContent(info.getReason());
        m.setSupplementTime(time);
        m.setMinutes(info.getMinutes() + "");
        TaskWorker taskWorker = taskWorkerMapper.findFirstById (info.getTaskWorkerId());
        if(taskWorker == null){
            throw new ParamsException ("小时工任务不存在");
        }
        WorkerCancelTask tp = null;
        if(taskWorker.getType () == 0){
            tp = taskWorkerMapper.selectUserAndWorkerId(info.getTaskWorkerId());
        }else{
            tp = taskWorkerMapper.selectHotelAndWorkerId (info.getTaskWorkerId());
        }
        MessageTemplate mess = messageTemplateMapper.findFirstByCode("applyOvertimeMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageType(2);
        m.setMessageTitle(mess.getTitle());
        m.setWorkerId(tp.getWorkerId());
        m.setWorkerTaskId(info.getTaskWorkerId());
        m.setHotelId(tp.getHotelId());
        m.setHrTaskId(tp.getTaskHrId());
        m.setTaskId(tp.getTaskId());
        m.setHrCompanyId(tp.getHrId());

        Map<String, String> param = new HashMap<>();
        param.put("userName", tp.getUsername());
        param.put("startTime", info.getTime());
        param.put("taskContent", info.getReason());
        param.put("minutes", info.getMinutes().toString());
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setApplyType(3);
        m.setApplicantType (1);
        m.setStatus(0);
        m.setIsTask(0);

        messageMapper.insert(m);

        return "操作成功";
    }

    /**
     * 小时工申请取消任务
     */
    @Override
    public String applyCancelTask(WorkerSupplementRequest info) {
        if (info == null) {
            throw new ParamsException("参数不能为空");
        }
        if (!StringUtils.hasLength(info.getReason())) {
            throw new ParamsException("理由不能为空");
        }
        if (!StringUtils.hasLength(info.getTaskWorkerId())) {
            throw new ParamsException("参数taskWorkerId不能为空");
        }
        Map map = new HashMap<String, Object>();
        map.put("message_type", 7);
        map.put("worker_task_id", info.getTaskWorkerId());
        map.put("status", 0);
        if (messageMapper.selectByMap(map).size() > 0) {
            return "你已提交过申请";
        }
        Message m = new Message();
        WorkerCancelTask tp = taskWorkerMapper.selectUserAndWorkerId(info.getTaskWorkerId());
        m.setContent(info.getReason());
        MessageTemplate mess = messageTemplateMapper.findFirstByCode("applyCancelTaskMessage");

        m.setSupplementTime(tp.getDayStartTime());
        m.setSupplementTimeEnd(tp.getDayEndTime());
        m.setMessageCode(mess.getCode());
        m.setMessageType(7);
        m.setMessageTitle(mess.getTitle());
        m.setWorkerId(tp.getWorkerId());
        m.setWorkerTaskId(info.getTaskWorkerId());
        m.setHrCompanyId(tp.getHrId());
        m.setHrTaskId(tp.getTaskHrId());
        m.setTaskId(tp.getTaskId());
        m.setHotelId(tp.getHotelId());
        Map<String, String> param = new HashMap<>();
        param.put("userName", tp.getUsername());
        param.put("taskContent", info.getReason());
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        if(tp.getTaskHrId() == null){
            m.setApplicantType (1);
            m.setApplyType(3);
        }else{
            m.setApplicantType (1);
            m.setApplyType(2);
        }
        m.setStatus(0);
        m.setIsTask(0);
        messageMapper.insert(m);

        return "申请取消发送成功";
    }

    /**
     * 查询补签的记录
     */
    @Override
    public Map<String, Object> selectNoPunchPageInfo(ApplyParamDTO applyParamDTO, Paginator paginator) {
        if (StringUtils.isEmpty(applyParamDTO.getId())) {
            throw new ParamsException("参数不能为空");
        }

        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<SupplementResponse> list = workLogMapper.selectNoPunchByWorkerId(applyParamDTO.getId());
        PageInfo<SupplementResponse> info = new PageInfo<>(list);
        Map<String, String> param = null;
        List<Map<String, String>> timeList = null;
        //将签到时间转换成key-value形式
        for (SupplementResponse response : list) {

            timeList = new ArrayList<>();
            String[] start = response.getStartTime().split(",");
            String[] end = response.getEndTime().split(",");
            int len = start.length;
            for (int i = 0; i < len; i++) {
                param = new HashMap<>();
                param.put("fromDate", start[i]);
                param.put("toDate", end[i]);
                timeList.add(param);
            }
            response.setSignDate(timeList);
        }
        if (list == null) {
            info.setList(new ArrayList<>());
        } else {
            info.setList(list);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("page", paginator.getPage());
        result.put("total", info.getTotal());
        result.put("list", list);
        return result;
    }

    /**
     * 小时工发起补签
     */
    @Override
    public String supplementWork(WorkerSupplementRequest info) {
        if (info == null) {
            throw new ParamsException("参数不能为空");
        }
        if (!StringUtils.hasLength(info.getReason())) {
            throw new ParamsException("理由不能为空");
        }
        if (!StringUtils.hasLength(info.getTaskWorkerId())) {
            throw new ParamsException("参数taskWorkerId不能为空");
        }
        if (info.getTime() == null) {
            throw new ParamsException("参数time不能为空");
        }
        WorkerCancelTask tp;
        TaskWorker taskWorker = taskWorkerMapper.selectById (info.getTaskWorkerId());
        if(taskWorker.getType () == 0){
            tp = taskWorkerMapper.selectUserAndWorkerId(info.getTaskWorkerId());
        }else{
            tp = taskWorkerMapper.selectHotelAndWorkerId(info.getTaskWorkerId());
        }
        int repeat = messageMapper.selectIsRepeat(info.getTaskWorkerId(), tp.getWorkerId());
        if (repeat > 0) {
            return "你已经提交过补签申请";
        }
        OffsetDateTime time = null;
        try {
            time = DateUtil.strToOffSetDateTime(info.getTime(), "yyyy/MM/dd HH:mm");
        } catch (ParseException e) {
            e.printStackTrace();
            return "日期格式错误，请用yyyy/MM/dd HH:mm格式";
        }
        Message m = new Message();
        m.setSupplementTime(time);
        m.setContent(info.getReason());

        MessageTemplate mess = messageTemplateMapper.findFirstByCode("applySupplementMessage");
        Map map = new HashMap<String, Object>();
        map.put("message_type", 1);
        map.put("worker_task_id", info.getTaskWorkerId());
        map.put("status", 0);
        Map<String, String> param = new HashMap<>();
        param.put("userName", tp.getUsername());
        param.put("time", info.getTime());
        param.put("taskContent", info.getReason());
        String c = StringKit.templateReplace(mess.getContent(), param);
        map.put("message_content", c);
        if (messageMapper.selectByMap(map).size() > 0) {
            return "你已提交过申请";
        }
        m.setMessageCode(mess.getCode());
        m.setMessageTitle(mess.getTitle());
        m.setWorkerId(tp.getWorkerId());
        m.setWorkerTaskId(info.getTaskWorkerId());
        m.setMessageType(1);
        m.setApplicantType(1);
        m.setHrTaskId(tp.getTaskHrId());
        m.setHotelId(tp.getHotelId());
        m.setTaskId(tp.getTaskId());
        m.setHrCompanyId(tp.getHrId());
        m.setMessageContent(c);
        m.setApplyType(3);
        m.setStatus(0);
        m.setIsTask(0);

        messageMapper.insert(m);
        return "申请补签提交成功";
    }

    /**
     * 初始化小时工工作记录打卡状态
     *
     * @param map
     */
    private void initMapStatus(Map<String, Integer> map, int num) {
        if (map == null) {
            throw new ParamsException("参数错误");
        }
        map.put("comeLate", 0);//迟到0否1是
        map.put("earlier", 0);//早退0否1是
        map.put("stay", 0);//旷工0否1是
        map.put("leave", 0);//请假0否1是
        if (num == 5)
            map.put("forget", 0);//忘打卡0否1是
    }

    /**
     * 修改服务类型及服务地区
     */
    public void mpdifyAreaAndService(AreaAndServiceRequest request) {
        List<AreaParam> li = new ArrayList <AreaParam> ();
        List<AreaParam> ll = new ArrayList <AreaParam> ();
        AreaParam ar = new AreaParam ();
        if (request.getAreaCodeList() == null) {
            //删除旧数据
            if (request.getAreaCode() != null) {
                companyMapper.deleteAreaRelation(request.getId());
                companyMapper.deleteCompanyArea(request.getId());
                //添加区域
                List<UserArea> areaList = request.getAreaCode();
                for (UserArea ua : areaList) {
                    if (ua.getAreaLevel() == 1) {
                        List<Map<String, String>> list = dictMapper.findCity(ua.getAreaId());
                        ar = new AreaParam ();
                        ar.setId (request.getId());
                        ar.setCode (ua.getAreaId());
                        ar.setLevel (ua.getAreaLevel());
                        ar.setName (dictMapper.findProvinceNameById(ua.getAreaId()));
                        ll.add (ar);
                        //companyMapper.insertAreaRelation(request.getId(), ua.getAreaId(), ua.getAreaLevel(), dictMapper.findProvinceNameById(ua.getAreaId()));
                        if (list == null) {
                            ar = new AreaParam ();
                            ar.setId (request.getId());
                            ar.setAreaId (ua.getAreaId());
                            ar.setIdType (request.getIdType());
                            li.add (ar);
                            //companyMapper.insertCompanyArea(request.getId(), ua.getAreaId(), request.getIdType());
                        }
                        for (Map<String, String> key : list) {
                            List<Map<String, String>> list2 = dictMapper.findArea(key.get("areaId"));

                            if (list2 == null) {
                                ar = new AreaParam ();
                                ar.setId (request.getId());
                                ar.setAreaId (key.get("areaId"));
                                ar.setIdType (request.getIdType());
                                li.add (ar);
                                //companyMapper.insertCompanyArea(request.getId(), key.get("areaId"), request.getIdType());
                            }
                            for (Map<String, String> key2 : list2) {
                                ar = new AreaParam ();
                                ar.setId (request.getId());
                                ar.setAreaId (key2.get("areaId"));
                                ar.setIdType (request.getIdType());
                                li.add (ar);
                                //companyMapper.insertCompanyArea(request.getId(), key2.get("areaId"), request.getIdType());
                            }
                        }
                    } else if (ua.getAreaLevel() == 2) {
                        ar = new AreaParam ();
                        ar.setId (request.getId());
                        ar.setCode (ua.getAreaId());
                        ar.setLevel (ua.getAreaLevel());
                        ar.setName (dictMapper.findCityNameById (ua.getAreaId()));
                        ll.add (ar);
                        //companyMapper.insertAreaRelation(request.getId(), ua.getAreaId(), ua.getAreaLevel(), dictMapper.findCityNameById(ua.getAreaId()));
                        List<Map<String, String>> list2 = dictMapper.findArea(ua.getAreaId());

                        if (list2 == null) {
                            ar = new AreaParam ();
                            ar.setId (request.getId());
                            ar.setAreaId (ua.getAreaId());
                            ar.setIdType (request.getIdType());
                            li.add (ar);
                            //companyMapper.insertCompanyArea(request.getId(), ua.getAreaId(), request.getIdType());
                        }
                        for (Map<String, String> key2 : list2) {
                            ar = new AreaParam ();
                            ar.setId (request.getId());
                            ar.setAreaId (key2.get("areaId"));
                            ar.setIdType (request.getIdType());
                            li.add (ar);
                            //companyMapper.insertCompanyArea(request.getId(), key2.get("areaId"), request.getIdType());
                        }
                    } else {
                        ar = new AreaParam ();
                        ar.setId (request.getId());
                        ar.setCode (ua.getAreaId());
                        ar.setLevel (ua.getAreaLevel());
                        ar.setName (dictMapper.findAreaNameById(ua.getAreaId()));
                        ll.add (ar);
                        //companyMapper.insertAreaRelation(request.getId(), ua.getAreaId(), ua.getAreaLevel(), dictMapper.findAreaNameById(ua.getAreaId()));
                        ar = new AreaParam ();
                        ar.setId (request.getId());
                        ar.setAreaId (ua.getAreaId());
                        ar.setIdType (request.getIdType());
                        li.add (ar);
                        //companyMapper.insertCompanyArea(request.getId(), ua.getAreaId(), request.getIdType());
                    }
                }
            }
        } else {
            //删除旧数据
            companyMapper.deleteAreaRelation(request.getId());
            companyMapper.deleteCompanyArea(request.getId());
            List<String> lis = request.getAreaCodeList();
            //添加区域
            for (int i = 0; i < lis.size(); i++) {
                if (dictMapper.isProvince(lis.get(i)) != null) {//第一级
                    List<Map<String, String>> list = dictMapper.findCity(lis.get(i));
                    ar = new AreaParam ();
                    ar.setId (request.getId());
                    ar.setCode (lis.get(i));
                    ar.setLevel (1);
                    ar.setName (dictMapper.findProvinceNameById(lis.get(i)));
                    ll.add (ar);
                    //companyMapper.insertAreaRelation(request.getId(), lis.get(i), 1, dictMapper.findProvinceNameById(lis.get(i)));
                    if (list == null) {
                        ar = new AreaParam ();
                        ar.setId (request.getId());
                        ar.setAreaId (lis.get(i));
                        ar.setIdType (request.getIdType());
                        li.add (ar);
                        //companyMapper.insertCompanyArea(request.getId(), lis.get(i), request.getIdType());
                    }
                    for (Map<String, String> key : list) {
                        List<Map<String, String>> list2 = dictMapper.findArea(key.get("areaId"));
                        if (list2 == null) {
                            ar = new AreaParam ();
                            ar.setId (request.getId());
                            ar.setAreaId (key.get("areaId"));
                            ar.setIdType (request.getIdType());
                            li.add (ar);
                            //companyMapper.insertCompanyArea(request.getId(), key.get("areaId"), request.getIdType());
                        }
                        for (Map<String, String> key2 : list2) {
                            ar = new AreaParam ();
                            ar.setId (request.getId());
                            ar.setAreaId (key.get("areaId"));
                            ar.setIdType (request.getIdType());
                            li.add (ar);
                            //companyMapper.insertCompanyArea(request.getId(), key2.get("areaId"), request.getIdType());
                        }
                    }
                } else if (dictMapper.isCity(lis.get(i)) != null) {//第二级
                    ar = new AreaParam ();
                    ar.setId (request.getId());
                    ar.setCode (lis.get(i));
                    ar.setLevel (2);
                    ar.setName (dictMapper.findCityNameById (lis.get(i)));
                    ll.add (ar);
                    //companyMapper.insertAreaRelation(request.getId(), lis.get(i), 2, dictMapper.findCityNameById(lis.get(i)));
                    List<Map<String, String>> list2 = dictMapper.findArea(lis.get(i));
                    if (list2 == null) {
                        ar = new AreaParam ();
                        ar.setId (request.getId());
                        ar.setAreaId (lis.get(i));
                        ar.setIdType (request.getIdType());
                        li.add (ar);
                        //companyMapper.insertCompanyArea(request.getId(), lis.get(i), request.getIdType());
                    }
                    for (Map<String, String> key2 : list2) {
                        ar = new AreaParam ();
                        ar.setId (request.getId());
                        ar.setAreaId (key2.get("areaId"));
                        ar.setIdType (request.getIdType());
                        li.add (ar);
                        //companyMapper.insertCompanyArea(request.getId(), key2.get("areaId"), request.getIdType());
                    }
                } else {//第三级
                    ar = new AreaParam ();
                    ar.setId (request.getId());
                    ar.setCode (lis.get(i));
                    ar.setLevel (3);
                    ar.setName (dictMapper.findAreaNameById (lis.get(i)));
                    ll.add (ar);
                    //companyMapper.insertAreaRelation(request.getId(), lis.get(i), 3, dictMapper.findAreaNameById(lis.get(i)));
                    ar = new AreaParam ();
                    ar.setId (request.getId());
                    ar.setAreaId (lis.get(i));
                    ar.setIdType (request.getIdType());
                    li.add (ar);
                    //companyMapper.insertCompanyArea(request.getId(), lis.get(i), request.getIdType());
                }

            }
        }
        if(li.size ()>0){
            companyMapper.insertCompanyAreaBatch (li);
        }
        if(ll.size ()>0){
            System.out.println (ll);
            companyMapper.insertAreaRelationBatch (ll);
        }
        li.clear ();
        if (request.getServiceType() != null) {
            taskTypeRelationMapper.deleteTaskTypeRelation(request.getId());
            //添加服务类型
            List<String> serviceType = request.getServiceType();
            for (int i = 0; i < serviceType.size(); i++) {
                ar = new AreaParam ();
                ar.setId (request.getId());
                ar.setTaskTypeId (serviceType.get(i));
                ar.setIdType (request.getIdType());
                li.add (ar);
                //taskTypeRelationMapper.insertTaskTypeRelation(request.getId(), serviceType.get(i), request.getIdType());
            }
        }
        if(li.size ()>0){
            taskTypeRelationMapper.insertTaskTypeRelationBatch (li);
        }

    }

    @Override
    public Map<String, Object> queryWorker(String id) {
        Map<String, Object> map = workerMapper.queryWorker(id);
        String str = "";
        if (map.get("birthday") != null) {
            str = map.get("birthday").toString().substring(0, 10);
        }
        map.put("birthday", str);
        List l1 = dictService.findServiceArea(id);
        List l2 = dictMapper.queryTypeByUserId(id);
        map.put("areaCode", l1 == null ? new ArrayList<>() : l1);
        map.put("serviceType", l2 == null ? new ArrayList<>() : l2);
        List<Map<String, Object>> list = workLogMapper.selectWorkerCommentCount(id);
        if (list != null && list.size() > 0) {
            String grade = "";
            int a = 0;//迟到
            int b = 0;//早退
            int c = 0;//旷工
            for (Map<String, Object> m : list) {
                if ((Integer) m.get("status") == 1) {
                    a += Integer.parseInt(m.get("amount").toString());
                } else if ((Integer) m.get("status") == 2) {
                    b += Integer.parseInt(m.get("amount").toString());
                } else if ((Integer) m.get("status") == 3) {
                    c += Integer.parseInt(m.get("amount").toString());
                } else if ((Integer) m.get("status") == 6) {
                    a += Integer.parseInt(m.get("amount").toString());
                    b += Integer.parseInt(m.get("amount").toString());
                } else if ((Integer) m.get("status") == 7) {
                    a += Integer.parseInt(m.get("amount").toString());
                }
            }
            if (!StringUtils.isEmpty(grade))
                grade += "    ";
            grade += "迟到：" + a;

            if (!StringUtils.isEmpty(grade))
                grade += "    ";
            grade += "早退：" + b;
            if (!StringUtils.isEmpty(grade))
                grade += "    ";
            grade += "旷工：" + c;
            map.put("grade", grade);
        } else {
            map.put("grade", "迟到：0    早退：0    旷工：0");
        }
        return map;
    }

    @Override
    public ResultDO pagingWorkers(Paginator paginator, WorkerQueryDTO workerQueryDTO) {
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize());
        if(workerQueryDTO.getHrId () == null){
            workerQueryDTO.setHrId (workerQueryDTO.getHotelId ());
        }
        //查询数据集合
        List<Map<String, Object>> list = null;
        if (workerQueryDTO.getHrId() == null) {
            list = workerMapper.queryAllWorker(workerQueryDTO);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
            HashMap<String, Object> result = new HashMap<>();
            //设置获取到的总记录数total：
            result.put("total", pageInfo.getTotal());
            //设置数据集合rows：
            result.put("result", pageInfo.getList());
            result.put("page", paginator.getPage());
            return ResultDO.buildSuccess(result);
        } else {
            list = workerMapper.queryWorkers(workerQueryDTO);
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
            HashMap<String, Object> result = new HashMap<>();
            result.put("total", pageInfo.getTotal());
            //设置数据集合rows：
            result.put("result", pageInfo.getList());
            result.put("page", paginator.getPage());
            Map<String, Object> map = new HashMap<>();
            Company company = companyMapper.findCompanyById(workerQueryDTO.getHrId());
            Integer num = company.getActiveWorkers();
            Integer total = Integer.parseInt(dictMapper.findByNameAndCode("HrBindWorkerMaxNum", "10").getText());
            map.put("bindNum", num);
            map.put("bindTotalNum", total);
            return ResultDO.buildSuccess("您已经绑定" + num + "个小时工，还可以绑定" + (total - num) + "个小时工"
                    , result, map, null);
        }
    }

    /**
     * 小时工申请绑定人力公司
     *
     * @param workerId 小时工workerId
     * @param set      人力公司id
     * @return
     */
    @Override
    public String workerApplybind(String workerId, List<String> set) {
        if (StringUtils.isEmpty(workerId) || set == null || set.size() == 0) {
            throw new ParamsException("参数错误");
        }
        User user = userMapper.selectByWorkerId(workerId);
        if (user == null) throw new ParamsException("查询不到用户");
        DictDTO dict = dictMapper.findByNameAndCode("WorkerBindHrMaxNum", "7");
        Integer maxNum = Integer.parseInt(dict.getText());
        int nowNum = userCompanyMapper.selectWorkerBindCount(user.getPid());
        if (nowNum >= maxNum || (nowNum + set.size()) > maxNum) {
            return "绑定人数达到上限";
        }
        int bindNum = userCompanyMapper.selectIsBindUserId(user.getPid(), set);
        if (bindNum > 0) {
            return "已提交绑定申请";
        }
        List<UserCompany> userCompanyList = new ArrayList<>();
        UserCompany userCompany = null;
        //将协议存放到redis服务器中
        String path = redisUtil.getString("defaultWorkerHrProtocol");
        if (StringUtils.isEmpty(path)) {
            try {
                path = filePush.pushFileToServer(ConstantData.CATALOG.getName(), ConstantData.WORKHRPROTOCOL.getName());
                //path = filePush.pushFileToServer(ConstantData.CATALOG.getName(), ConstantData.WORKERTEST.getName());
            } catch (Exception e) {
                e.printStackTrace();
                return "服务异常";
            }
            redisUtil.setString("defaultWorkerHrProtocol", path);
        }

        for (String str : set) {
            userCompany = new UserCompany();
            userCompany.setCompanyType(1);
            userCompany.setUserType(UserType.worker);
            userCompany.setUserId(user.getPid());
            userCompany.setCompanyId(str);
            userCompany.setStatus(0);

            UserCompany temp = userCompanyMapper.findOneUserCompany(str, user.getPid());
            if (temp == null) {
                userCompany.setBindProtocol(path);
                userCompanyList.add(userCompany);
            } else {
                temp.setStatus(0);
                temp.setBindProtocol(path);
                userCompanyMapper.updateById(temp);
                continue;
            }

        }
        if (userCompanyList.size() > 0) {
            userCompanyMapper.saveBatch(userCompanyList);
        }
        //发送消息
        messageService.bindUserHrCompany(user.getNickname(), workerId, set, 1);
        return "申请成功";
    }


    /**
     * 小时工处理支付
     *
     * @param messageId
     * @param status    0拒绝1同意
     * @return
     */
    @Override
    public ResultDO workerHandleHrPay(String messageId, String status) {
        if (StringUtils.isEmpty(messageId) || StringUtils.isEmpty(status)) {
            throw new ParamsException("参数不能为空");
        }

        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new ParamsException("查询不到消息");
        }
        message.setStatus(1);
        messageMapper.updateAllColumnById(message);
        Bill bill = billMapper.selectById(message.getRequestId());
        if (bill == null) {
            throw new ParamsException("未查到相关支付记录");
        }
        User user = userMapper.selectByWorkerId(message.getWorkerId());
        if (user == null) {
            throw new BusinessException("查询不到小时工信息");
        }
        if ("0".equals(status)) {
            //拒绝
            String content = "小时工" + user.getNickname() + "拒绝了你发起的一笔支付信息，金额为" + Double.valueOf(bill.getPayMoney());
            if(message.getHrCompanyId()!=null){
                informService.sendInformInfo(1, 2, content, message.getHrCompanyId(), "账目被拒绝");
                try {
                    jpushClient.jC.sendPush(JPushManage.buildPushObject_all_alias_message(companyMapper.findCompanyById(message.getHrCompanyId ()).getLeaderMobile(), content));
                } catch (APIConnectionException e) {
                    e.printStackTrace();
                } catch (APIRequestException e) {

                }
            }else{
                informService.sendInformInfo(1, 3, content, message.getHotelId (), "账目被拒绝");
                try {
                    jpushClient.jC.sendPush(JPushManage.buildPushObject_all_alias_message(companyMapper.findCompanyById(message.getHotelId()).getLeaderMobile(), content));
                } catch (APIConnectionException e) {
                    e.printStackTrace();
                } catch (APIRequestException e) {

                }
            }
            bill.setStatus(2);
            billMapper.updateById(bill);
            TaskWorker taskWorker = taskWorkerMapper.selectById(message.getWorkerTaskId());
            if (taskWorker == null) {
                throw new ParamsException("查询不到小时工任务信息");
            }
            taskWorker.setUnConfirmedPay(taskWorker.getUnConfirmedPay() - bill.getPayMoney());
            taskWorkerMapper.updateAllColumnById(taskWorker);
            TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById(message.getHrTaskId());
            if (taskHrCompany != null) {
                taskHrCompany.setWorkerUnConfirmed(taskHrCompany.getWorkerUnConfirmed() - bill.getPayMoney());
                taskHrCompanyMapper.updateAllColumnById(taskHrCompany);
            }else{
                Task task = taskMapper.selectById (bill.getHotelId ());
                if(task == null){
                    throw new ParamsException ("查询不到用人单位任务");
                }
                task.setUnConfirmedPay (task.getUnConfirmedPay () - bill.getPayMoney());
                taskMapper.updateById (task);
            }
        } else if ("1".equals(status)) {
            //同意
            //发送通知
            TaskWorker taskWorker = taskWorkerMapper.selectById(message.getWorkerTaskId());
            if (taskWorker == null) {
                throw new ParamsException("查询不到小时工任务信息");
            }
            taskWorker.setHavePayMoney(taskWorker.getHavePayMoney() + bill.getPayMoney());
            taskWorker.setUnConfirmedPay(taskWorker.getUnConfirmedPay() - bill.getPayMoney());
            taskWorkerMapper.updateAllColumnById(taskWorker);
            TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById(message.getHrTaskId());
            if (taskHrCompany != null) {
                taskHrCompany.setWorkerUnConfirmed(taskHrCompany.getWorkerUnConfirmed() - bill.getPayMoney());
                taskHrCompany.setWorkersHavePay(taskHrCompany.getWorkersHavePay() + bill.getPayMoney());
                taskHrCompanyMapper.updateAllColumnById(taskHrCompany);
                String content = "小时工" + user.getNickname() + "同意了你发起的一笔支付信息，金额为" + bill.getPayMoney();
                informService.sendInformInfo(1, 2, content, message.getHrCompanyId(), "账目已同意");
                try {
                    jpushClient.jC.sendPush(JPushManage.buildPushObject_all_alias_message(companyMapper.findCompanyById(message.getHrCompanyId ()).getLeaderMobile(), content));
                } catch (APIConnectionException e) {
                    e.printStackTrace();
                } catch (APIRequestException e) {

                }
            }else{
                Task task = taskMapper.selectById (bill.getTaskId());
                if(task == null){
                    throw new ParamsException ("查询不到用人单位任务");
                }
                task.setUnConfirmedPay (task.getUnConfirmedPay () - bill.getPayMoney());
                task.setHavePayMoney (task.getHourlyPay () + bill.getPayMoney());
                taskMapper.updateById (task);
                String content = "小时工" + user.getNickname() + "同意了你发起的一笔支付信息，金额为" + bill.getPayMoney();
                informService.sendInformInfo(1, 3, content, bill.getHotelId (), "账目已同意");
                try {
                    jpushClient.jC.sendPush(JPushManage.buildPushObject_all_alias_message(companyMapper.findCompanyById(bill.getHotelId ()).getLeaderMobile(), content));
                } catch (APIConnectionException e) {
                    e.printStackTrace();
                } catch (APIRequestException e) {

                }
            }
            bill.setStatus(1);
            billMapper.updateById(bill);
        }
        return ResultDO.buildSuccess("操作成功");
    }

    /**
     * 查询所有工作者信息
     * @return
     */
    @Override
    public List<WorkerInfo> queryWorkerInfo() {
        int count = workerMapper.selectAllCount();
        PageHelper.startPage(1, count , true);
        List<WorkerInfo> list = workerMapper.selectWorkerInfo();
        if (list == null) {
            list = new ArrayList<WorkerInfo>();
        }
        return list;
    }

    /**
     * 查询小时工工作记录
     *
     * @param taskWorkerId
     * @param workerId
     * @return
     */
    @Override
    public UserTaskResponse selectUserTaskInfo(String taskWorkerId, String workerId) {
        if (StringUtils.isEmpty(taskWorkerId) || StringUtils.isEmpty(workerId)) {
            throw new ParamsException("参数不能为空");
        }
        UserTaskResponse response = new UserTaskResponse();
        // 查询用户工作记录
        TaskWorker taskWorker = taskWorkerMapper.selectById(taskWorkerId);
        if (taskWorker == null) {
            throw new ParamsException("查询不到用户工作任务");
        }
        OffsetDateTime startDay = taskWorker.getFromDate();
        OffsetDateTime nowDate = OffsetDateTime.now();
        OffsetDateTime endDay = taskWorker.getToDate();
        DateTimeFormatter t1 = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        if (nowDate.compareTo(startDay) < 0) {
            log.info("任务还没有开始");
            WorkerDetail detail = new WorkerDetail();
            detail.setTime(OffsetDateTime.now().format(t1));
            List<WorkerDetail> list = new ArrayList<>();
            list.add(detail);
            return response;
        }
        if (nowDate.compareTo(endDay) < 0) {
            endDay = nowDate;
        }
        OffsetTime dayStart = taskWorker.getDayStartTime();
        OffsetTime dayEnd = taskWorker.getDayEndTime();

        long start = dayStart.getLong(ChronoField.SECOND_OF_DAY);
        long end = dayEnd.getLong(ChronoField.SECOND_OF_DAY);
        startDay = startDay.plusHours(-startDay.getHour()).plusMinutes(-startDay.getMinute()).plusSeconds(-startDay.getSecond()).plusSeconds(start);
        endDay = endDay.plusHours(-endDay.getHour()).plusMinutes(-endDay.getMinute()).plusSeconds(-endDay.getSecond()).plusSeconds(end);
        boolean expire;

        // 查询打卡记录

        List<WorkerDetail> detailList = new ArrayList<>();//存放小时工所有打卡记录
        List<WorkerOneDayInfo> list = workLogMapper.selectUserPunchDetail(taskWorkerId);
        List<Holiday> holidayList = holidayMapper.selectByTaskWorkId(taskWorkerId);
        PunchInfo workLog = null;
        WorkerDetail detail = null;
        List<PunchInfo> workList = null;
        Map<String, Integer> hotelStatus = null;
        Map<String, Integer> sysStatus = null;
        if (list == null || list.size() == 0) {
            if (holidayList == null || holidayList.size() == 0) {
                //没有打卡也没有请假
                while (startDay.compareTo(nowDate) <= 0 && startDay.compareTo(endDay) < 0) {
                    if (startDay.getDayOfYear() == nowDate.getDayOfYear()) {
                        detail = new WorkerDetail();
                        workList = new ArrayList<>();
                        workLog = new PunchInfo();
                        workLog.setStartTime("--");
                        workLog.setEndTime("--");
                        hotelStatus = new HashMap<>();
                        sysStatus = new HashMap<>();
                        initMapStatus(hotelStatus, 4);
                        initMapStatus(sysStatus, 5);
                        if (nowDate.getLong(ChronoField.MINUTE_OF_DAY) - dayEnd.getLong(ChronoField.MINUTE_OF_DAY) < 30) {
                            //如果是当天且不超过下班时间30分钟

                        } else {
                            sysStatus.put("stay", 1);
                        }

                        detail.setHotelStatus(hotelStatus);
                        detail.setSysStatus(sysStatus);
                        detail.setExpire("0");
                        detail.setTime(startDay.format(t1));
                        workList.add(workLog);
                        detail.setWorkList(workList);
                        detailList.add(detail);
                        startDay = startDay.plusDays(1);
                        continue;
                    }
                    detail = new WorkerDetail();
                    workList = new ArrayList<>();
                    workLog = new PunchInfo();
                    workLog.setStartTime("--");
                    workLog.setEndTime("--");
                    hotelStatus = new HashMap<>();
                    sysStatus = new HashMap<>();
                    initMapStatus(hotelStatus, 4);
                    initMapStatus(sysStatus, 5);
                    sysStatus.put("stay", 1);

                    detail.setHotelStatus(hotelStatus);
                    detail.setSysStatus(sysStatus);
                    expire = (nowDate.toEpochSecond() - startDay.toEpochSecond()) / 3600 >= 168 ? true : false;
                    if (expire) {
                        detail.setExpire("1");
                    } else {
                        detail.setExpire("0");
                    }
                    workList.add(workLog);
                    detail.setTime(startDay.format(t1));
                    detail.setWorkList(workList);
                    detail.setSysStatus(sysStatus);
                    detail.setHotelStatus(hotelStatus);
                    detailList.add(detail);
                    startDay = startDay.plusDays(1);
                }

            } else {
                //没有打卡记录，有请假
                while (true) {
                    if (startDay.compareTo(nowDate) > 0) break;
                    if (startDay.compareTo(endDay) > 0) break;
                    detail = new WorkerDetail();
                    workList = new ArrayList<>();
                    workLog = new PunchInfo();
                    hotelStatus = new HashMap<>();
                    sysStatus = new HashMap<>();
                    initMapStatus(hotelStatus, 4);
                    initMapStatus(sysStatus, 5);
                    workLog.setStartTime("--");
                    workLog.setEndTime("--");

                    expire = (nowDate.toEpochSecond() - startDay.toEpochSecond()) / 3600 >= 168 ? true : false;
                    int[] minutes = judgeTime(startDay.getYear(), startDay.getDayOfYear(), dayStart, dayEnd, holidayList);
                    if (startDay.getDayOfYear() == nowDate.getDayOfYear()) {

                        if (nowDate.getLong(ChronoField.MINUTE_OF_DAY) - dayEnd.getLong(ChronoField.MINUTE_OF_DAY) < 30) {
                            //如果是当天且不超过下班时间30分钟
                            if (minutes[2] > 0) {
                                //有请假
                                sysStatus.put("leave", 1);
                                hotelStatus.put("leave", 1);
                                if (minutes[0] == 1) {
                                    sysStatus.put("comeLate", 1);
                                }
                            } else {
                                sysStatus.put("comeLate", 1);
                            }
                        } else {
                            if (minutes[2] == 0) {
                                sysStatus.put("stay", 1);
                            } else {
                                sysStatus.put("leave", 1);
                                hotelStatus.put("leave", 1);
                                if (minutes[0] == 1) {
                                    sysStatus.put("comeLate", 1);
                                }
                                if (minutes[1] == 1) {
                                    sysStatus.put("earlier", 1);
                                }
                            }

                        }

                        detail.setHotelStatus(hotelStatus);
                        detail.setSysStatus(sysStatus);
                        detail.setExpire("0");
                        detail.setTime(startDay.format(t1));
                        workList.add(workLog);
                        detail.setWorkList(workList);
                        detailList.add(detail);
                        startDay = startDay.plusDays(1);
                        continue;
                    }

                    workLog = new PunchInfo();
                    workLog.setStartTime("--");
                    workLog.setEndTime("--");
                    if (minutes[2] == 0) {
                        sysStatus.put("stay", 1);
                    } else {
                        sysStatus.put("leave", 1);
                        hotelStatus.put("leave", 1);
                        if (minutes[0] == 1) {
                            sysStatus.put("comeLate", 1);
                        }
                        if (minutes[1] == 1) {
                            sysStatus.put("earlier", 1);
                        }
                    }
                    workList.add(workLog);
                    if (expire) {
                        detail.setExpire("1");
                    } else {
                        detail.setExpire("0");
                    }
                    detail.setTime(startDay.format(t1));
                    startDay = startDay.plusDays(1);
                    detail.setSysStatus(sysStatus);
                    detail.setHotelStatus(hotelStatus);
                    detailList.add(detail);
                }
            }
        } else {
            //有打卡记录

            for (WorkerOneDayInfo param : list) {
                if (startDay.getDayOfYear() > endDay.getDayOfYear()) break;
                detail = new WorkerDetail();
                workList = new ArrayList<>();
                OffsetDateTime time = param.getCreateTime();
                String[] currentStartTime = param.getFromDate().split(",");

                String[] currentEndTime = null;
                if (!StringUtils.isEmpty(param.getToDate())) {
                    currentEndTime = param.getToDate().split(",");
                }
                String[] confirmStatuAgrs = param.getEmployerConfirmStatus().split(",");
                String[] statuAgrs = param.getStatus().split(",");
                String confirmStatus = param.getEmployerConfirmStatus();
                String status = param.getStatus();
                boolean flag = false;

                //如果当天没有打卡记录
                while (startDay.getDayOfYear() != time.getDayOfYear() && startDay.compareTo(nowDate) < 0) {
                    detail = new WorkerDetail();
                    workList = new ArrayList<>();
                    expire = (nowDate.toEpochSecond() - startDay.toEpochSecond()) / 3600 >= 168 ? true : false;

                    int[] minutes = judgeTime(startDay.getYear(), startDay.getDayOfYear(), dayStart, dayEnd, holidayList);
                    if (hotelStatus == null || sysStatus == null) {
                        hotelStatus = new HashMap<>();
                        sysStatus = new HashMap<>();
                        initMapStatus(hotelStatus, 4);
                        initMapStatus(sysStatus, 5);
                    }
                    //没有请假
                    if (minutes[2] == 0) {
                        //当前时间且没有到下班时间
                        if (startDay.getDayOfYear() == nowDate.getDayOfYear() && (nowDate.toOffsetTime().compareTo(dayEnd)) < 0) {

                        } else {
                            sysStatus.put("stay", 1);
                        }
                        workLog = new PunchInfo();
                        workLog.setEndTime("--");
                        workLog.setStartTime("--");
                        workList.add(workLog);
                    } else {
                        //请假时间小于上班时间
                        if (minutes[2] + 300 < (end - start)) {
                            workLog = new PunchInfo();
                            workLog.setEndTime("--");
                            workLog.setStartTime("--");
                            sysStatus.put("leave", 1);
                            hotelStatus.put("leave", 1);
                            if (minutes[0] == 1)
                                sysStatus.put("comeLate", 1);
                            if (startDay.getDayOfYear() == nowDate.getDayOfYear() && (nowDate.getLong(ChronoField.MINUTE_OF_DAY) - dayEnd.getLong(ChronoField.MINUTE_OF_DAY)) <= 30) {

                            } else {
                                if (minutes[1] == 1)
                                    sysStatus.put("earlier", 1);
                            }
                            workList.add(workLog);

                        } else {
                            //全天请假
                            workLog = new PunchInfo();
                            workLog.setEndTime("--");
                            workLog.setStartTime("--");
                            sysStatus.put("leave", 1);
                            hotelStatus.put("leave", 1);
                            workList.add(workLog);
                        }
                    }
                    detail.setTime(startDay.format(t1));
                    startDay = startDay.plusDays(1);
                    detail.setHotelStatus(hotelStatus);
                    detail.setSysStatus(sysStatus);
                    if (expire) {
                        detail.setExpire("1");
                    } else {
                        detail.setExpire("0");
                    }
                    detail.setWorkList(workList);
                    detailList.add(detail);

                    hotelStatus = null;
                    sysStatus = null;
                    flag = true;
                }
                //如果进入了没有打卡的流程，要重新生成记录
                if (flag) {
                    detail = new WorkerDetail();
                    workList = new ArrayList<>();
                }
                //每天工作时间(分钟)及应付薪酬

                detail.setWorkHour(param.getTotalTime());
                double d = new BigDecimal(param.getTotalTime()).multiply(new BigDecimal(taskWorker.getHourlyPay())).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP).doubleValue();
                detail.setPayment(d);
                hotelStatus = new HashMap<>();
                sysStatus = new HashMap<>();
                initMapStatus(hotelStatus, 4);
                initMapStatus(sysStatus, 5);                //有打卡记录
                expire = (nowDate.toEpochSecond() - startDay.toEpochSecond()) / 3600 >= 168 ? true : false;
                if (expire) {
                    detail.setExpire("1");
                } else {
                    detail.setExpire("0");
                }
                //判断是否是旷工
                if ("3".equals(statuAgrs[0]) && "1".equals(confirmStatuAgrs[0])) {

                    hotelStatus.put("stay", 1);
                    workLog = new PunchInfo();
                    workLog.setEndTime("--");
                    workLog.setStartTime("--");
                    workList.add(workLog);
                    detail.setWorkList(workList);
                    detail.setTime(startDay.format(t1));
                    detail.setSysStatus(sysStatus);
                    detail.setHotelStatus(hotelStatus);
                    detailList.add(detail);
                    startDay = startDay.plusDays(1);
                    hotelStatus = null;
                    sysStatus = null;
                    continue;
                }

                Date t = null;
                try {
                    t = timeFormat.parse(currentStartTime[0]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                workLog = new PunchInfo();
                OffsetTime tc = OffsetTime.ofInstant(Instant.ofEpochMilli(t.getTime()), ZoneId.systemDefault());
                if (dayStart.getLong(ChronoField.MINUTE_OF_DAY) - tc.getLong(ChronoField.MINUTE_OF_DAY) < -5) {

                    int[] minutes = judgeTime(startDay.getYear(), startDay.getDayOfYear(), dayStart, tc, holidayList);
                    if (minutes[2] == 0) {

                        if ((status.contains("1") || status.contains("6") || status.contains("7")) && confirmStatus.contains("1")) {
                            hotelStatus.put("comeLate", 1);
                        } else {
                            if (minutes[0] == 1) {
                                sysStatus.put("comeLate", 1);
                            }
                        }
                    } else {
                        sysStatus.put("leave", 1);
                        hotelStatus.put("leave", 1);
                        //如果请假时间小于缺勤时间，记为早退
                        if ((status.contains("1") || status.contains("6")) && confirmStatus.contains("1")) {
                            hotelStatus.put("comeLate", 1);
                        } else {
                            if (minutes[0] == 1) {
                                sysStatus.put("comeLate", 1);
                            }
                        }
                    }
                }
                workLog.setStartTime(currentStartTime[0]);
                if (currentEndTime != null && currentEndTime.length > 0) {
                    workLog.setEndTime(currentEndTime[0]);

                    workList.add(workLog);
                    if (currentStartTime.length > 1) {
                        int i;
                        for (i = 1; i < currentStartTime.length - 1; i++) {
                            workLog = new PunchInfo();
                            workLog.setStartTime(currentStartTime[i]);
                            workLog.setEndTime(currentEndTime[i]);
                            workList.add(workLog);
                        }
                        workLog = new PunchInfo();
                        workLog.setStartTime(currentStartTime[i]);
                        if (currentEndTime.length == i + 1) {

                            workLog.setEndTime(currentEndTime[i]);
                            Date end1 = null;
                            try {
                                end1 = timeFormat.parse(currentEndTime[i]);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            OffsetTime te1 = OffsetTime.ofInstant(Instant.ofEpochMilli(end1.getTime()), ZoneId.systemDefault());
                            if (nowDate.getLong(ChronoField.MINUTE_OF_DAY) - dayEnd.getLong(ChronoField.MINUTE_OF_DAY) >= 30) {
                                if ((status.contains("2") || status.contains("6")) && confirmStatus.contains("1")) {
                                    hotelStatus.put("earlier", 1);
                                } else {
                                    if (te1.getLong(ChronoField.MINUTE_OF_DAY) - dayEnd.getLong(ChronoField.MINUTE_OF_DAY) < -5) {
                                        int[] minutes = judgeTime(startDay.getYear(), startDay.getDayOfYear(), te1, dayEnd, holidayList);
                                        if (minutes[2] == 0) {
                                            sysStatus.put("earlier", 1);
                                        } else {
                                            if (minutes[1] == 1) {
                                                sysStatus.put("earlier", 1);
                                                sysStatus.put("leave", 1);
                                                hotelStatus.put("leave", 1);
                                            }
                                        }
                                    }

                                }
                            }
                            workList.add(workLog);
                        } else {
                            workLog.setEndTime("--");
                            if (startDay.getDayOfYear() == nowDate.getDayOfYear() && (nowDate.getLong(ChronoField.MINUTE_OF_DAY) - dayEnd.getLong(ChronoField.MINUTE_OF_DAY) <= 30)) {
                                //如果当天还没有工作结束，就不判断
                            } else {

                                if ((status.contains("4") || status.contains("7")) && confirmStatus.contains("1")) {
                                    hotelStatus.put("forget", 1);
                                } else {
                                    sysStatus.put("forget", 1);
                                }
                            }
                            workList.add(workLog);
                        }
                    } else {
                        //校验结束时间
                        try {
                            Date end1 = timeFormat.parse(currentEndTime[0]);
                            OffsetTime te = OffsetTime.ofInstant(Instant.ofEpochMilli(end1.getTime()), ZoneId.systemDefault());
                            if (nowDate.getLong(ChronoField.MINUTE_OF_DAY) - dayEnd.getLong(ChronoField.MINUTE_OF_DAY) <= 30) {
                                //下班前30分钟内不检查
                            } else {
                                if (dayEnd.getLong(ChronoField.MINUTE_OF_DAY) - te.getLong(ChronoField.MINUTE_OF_DAY) > 5) {

                                    int[] n = judgeTime(startDay.getYear(), startDay.getDayOfYear(), te, dayEnd, holidayList);
                                    if (n[2] > 0) {
                                        sysStatus.put("leave", 1);
                                        hotelStatus.put("leave", 1);
                                        if (confirmStatus.contains("1") && (status.contains("2") || status.contains("6"))) {
                                            hotelStatus.put("earlier", 1);
                                        } else {
                                            if (n[1] == 1) {
                                                sysStatus.put("earlier", 1);
                                            }
                                        }
                                    } else {
                                        if (confirmStatus.contains("1") && (status.contains("2") || status.contains("6"))) {
                                            hotelStatus.put("earlier", 1);
                                        } else {
                                            sysStatus.put("earlier", 1);
                                        }

                                    }
                                }
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    //忘打卡
                    workLog.setEndTime("--");
                    //如果当天还没有工作结束30分钟，就不判断
                    if (startDay.getDayOfYear() == nowDate.getDayOfYear() && (nowDate.getLong(ChronoField.MINUTE_OF_DAY) - dayEnd.getLong(ChronoField.MINUTE_OF_DAY) <= 30)) {

                    } else {

                        if ((status.contains("4") || status.contains("7")) && confirmStatus.contains("1")) {
                            hotelStatus.put("forget", 1);
                        } else {
                            sysStatus.put("forget", 1);
                        }
                    }
                    workList.add(workLog);
                }

                //判断是否有请假
                int[] n = judgeTime(startDay.getYear(), startDay.getDayOfYear(), dayStart, dayEnd, holidayList);
                if (n[2] > 0) {
                    sysStatus.put("leave", 1);
                    hotelStatus.put("leave", 1);
                }
                detail.setWorkList(workList);
                detail.setSysStatus(sysStatus);
                detail.setHotelStatus(hotelStatus);
                detail.setTime(startDay.format(t1));
                detailList.add(detail);
                startDay = startDay.plusDays(1);
                hotelStatus = null;
                sysStatus = null;
            }
            //如果任务没有结束
            while (startDay.compareTo(nowDate) <= 0 && startDay.compareTo(endDay) < 0) {
                expire = (nowDate.toEpochSecond() - startDay.toEpochSecond()) / 3600 >= 168 ? true : false;
                detail = new WorkerDetail();
                workList = new ArrayList<>();
                hotelStatus = new HashMap<>();
                sysStatus = new HashMap<>();
                initMapStatus(hotelStatus, 4);
                initMapStatus(sysStatus, 5);
                workLog = new PunchInfo();
                workLog.setStartTime("--");
                workLog.setEndTime("--");
                workList.add(workLog);
                int[] num = judgeTime(startDay.getYear(), startDay.getDayOfYear(), dayStart, dayEnd, holidayList);
                if (num[2] > 0) {
                    sysStatus.put("leave", 1);
                    hotelStatus.put("leave", 1);
                    if (num[0] == 1) {
                        sysStatus.put("comeLate", 1);
                    }
                    if (num[1] == 1) {
                        if (nowDate.getLong(ChronoField.MINUTE_OF_DAY) - dayEnd.getLong(ChronoField.MINUTE_OF_DAY) > 30) {
                            sysStatus.put("earlier", 1);
                        }
                    }
                } else {
                    sysStatus.put("stay", 1);
                }
                detail.setHotelStatus(hotelStatus);
                detail.setSysStatus(sysStatus);
                detail.setTime(startDay.format(t1));
                detail.setWorkList(workList);
                if (expire) {
                    detail.setExpire("1");
                } else {
                    detail.setExpire("0");
                }
                detailList.add(detail);
                startDay = startDay.plusDays(1);
            }
        }
        if (detailList == null || detailList.size() == 0) {
            WorkerDetail detail1 = new WorkerDetail();
            detailList.add(detail1);
        }
        response.setList(detailList);
        return response;
    }

    /**
     * 判断时间是否再请假时间中
     *
     * @param year        打卡日期的年份
     * @param day         打卡的在一年中的天数
     * @param startTime   打卡的开始时间
     * @param endTime     打卡的结束时间
     * @param holidayList 请假集合
     * @return 索引0存储是否迟到索引1存储是否早退, 索引2存储请假时间
     */
    private int[] judgeTime(int year, int day, OffsetTime startTime, OffsetTime endTime, List<Holiday> holidayList) {
        int num = 0;
        int[] result = new int[3];
        result[0] = 1;
        result[1] = 1;
        for (Holiday holiday : holidayList) {
            OffsetDateTime from = holiday.getFromDate();
            OffsetDateTime to = holiday.getToDate();
            if (from.getYear() > year) {
                continue;
            } else if (from.getYear() < year) {

                if (to.getYear() < year) {
                    continue;
                } else if (to.getYear() > year) {
                    result[2] = (int) (endTime.getLong(ChronoField.MINUTE_OF_DAY) - startTime.getLong(ChronoField.MINUTE_OF_DAY));
                    result[0] = 0;
                    result[1] = 0;
                    return result;
                } else {
                    if (to.getDayOfYear() < day) continue;
                    //请假结束日期大于打卡日期
                    result[0] = 0;
                    if (to.getDayOfYear() > day) {
                        result[1] = 0;
                        num += (int) (endTime.getLong(ChronoField.SECOND_OF_DAY) - startTime.getLong(ChronoField.SECOND_OF_DAY));
                        break;
                    } else {

                        if (to.toOffsetTime().compareTo(endTime) >= 0) {
                            result[1] = 0;
                            num += (int) (endTime.getLong(ChronoField.SECOND_OF_DAY) - startTime.getLong(ChronoField.SECOND_OF_DAY));
                        } else {
                            num += (int) (to.getLong(ChronoField.SECOND_OF_DAY) - startTime.getLong(ChronoField.SECOND_OF_DAY));
                        }
                    }

                }
            } else {
                //同一年
                if (from.getDayOfYear() > day) {
                    //请假日期大于打卡日期
                    continue;
                } else if (from.getDayOfYear() < day) {
                    //请假日期小于打卡日期

                    if (to.getYear() > year) {
                        result[1] = 0;
                        result[0] = 0;
                        num += endTime.getLong(ChronoField.SECOND_OF_DAY) - startTime.getLong(ChronoField.SECOND_OF_DAY);
                    } else if (to.getDayOfYear() < day) {

                    } else {
                        if (to.getDayOfYear() > day) {
                            result[1] = 0;
                            num += endTime.getLong(ChronoField.SECOND_OF_DAY) - startTime.getLong(ChronoField.SECOND_OF_DAY);
                        }
                        if (to.getDayOfYear() == day) {

                            if (startTime.compareTo(to.toOffsetTime()) > 0) {
                                continue;
                            } else {
                                result[0] = 0;
                                if (endTime.compareTo(to.toOffsetTime()) <= 0) {
                                    result[1] = 0;
                                    num += endTime.getLong(ChronoField.SECOND_OF_DAY) - startTime.getLong(ChronoField.SECOND_OF_DAY);
                                } else {
                                    num += to.getLong(ChronoField.SECOND_OF_DAY) - startTime.getLong(ChronoField.SECOND_OF_DAY);
                                }
                            }

                        }
                    }
                } else {
                    //请假日期等于打卡日期
                    if (to.getYear() > year || to.getDayOfYear() > day) {
                        //请假结束日期大于当天
                        if (startTime.compareTo(from.toOffsetTime()) > 0) {
                            num += endTime.getLong(ChronoField.SECOND_OF_DAY) - startTime.getLong(ChronoField.SECOND_OF_DAY);
                            result[0] = 0;
                            result[1] = 0;
                        } else {
                            num += endTime.getLong(ChronoField.SECOND_OF_DAY) - from.getLong(ChronoField.SECOND_OF_DAY);
                        }
                    } else {
                        //当天
                        if (startTime.compareTo(from.toOffsetTime()) >= 0) {
                            result[0] = 0;
                            if (endTime.compareTo(to.toOffsetTime()) <= 0) {
                                result[1] = 0;
                                num += endTime.getLong(ChronoField.SECOND_OF_DAY) - startTime.getLong(ChronoField.SECOND_OF_DAY);
                            } else {
                                num += to.getLong(ChronoField.SECOND_OF_DAY) - startTime.getLong(ChronoField.SECOND_OF_DAY);
                            }

                        } else {
                            //请假时间晚于startTime
                            if (endTime.compareTo(from.toOffsetTime()) <= 0) {
                                continue;
                            } else if (endTime.compareTo(to.toOffsetTime()) > 0) {
                                num += to.getLong(ChronoField.SECOND_OF_DAY) - from.getLong(ChronoField.SECOND_OF_DAY);
                            } else {
                                result[1] = 0;
                                num += endTime.getLong(ChronoField.SECOND_OF_DAY) - from.getLong(ChronoField.SECOND_OF_DAY);
                            }
                        }
                    }
                }
            }
        }
        result[2] = num;
        return result;
    }

}
