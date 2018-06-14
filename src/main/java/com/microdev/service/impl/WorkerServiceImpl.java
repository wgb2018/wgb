package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.context.ServiceContextHolder;
import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.DateUtil;
import com.microdev.common.utils.LocationUtils;
import com.microdev.common.utils.StringKit;
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
import com.microdev.type.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import java.math.BigDecimal;
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
        OffsetTime time = OffsetDateTime.now ().toOffsetTime ();
        OffsetTime timeA = time.minusMinutes (30);
                //取进行中的任务
        TaskWorker taskWorker = taskWorkerMapper.findWorkerNowTask(
                userId, TaskWorkerStatus.ACCEPTED.ordinal(),
                //结束60分钟内的任务 仍然当作当前任务 以便进行打卡签退操作(如若改动 需与前端保持一致)
                OffsetDateTime.now(), time,timeA);
        //如果没有当前进行中的任务 则获取最近的下一个任务
        if (taskWorker == null) {
            OffsetDateTime of = OffsetDateTime.now ();
            taskWorker = taskWorkerMapper.findWorkerNextTask(userId, TaskWorkerStatus.ACCEPTED.ordinal(),OffsetDateTime.ofInstant(new Date(of.getYear ()-1900,of.getMonthValue ()-1,of.getDayOfMonth ()).toInstant (),ZoneOffset.systemDefault ()).plusDays (1),time);
        }
        /*//如果没有下一个任务(也没有进行中的任务) 取前一个任务
        if (taskWorker == null) {
            taskWorker = taskWorkerMapper.findWorkerBeforeTask(userId, TaskWorkerStatus.ACCEPTED.ordinal(), OffsetDateTime.now());
        }
*/      GetCurrentTaskResponse response = new GetCurrentTaskResponse();
        if (taskWorker != null) {
            taskHrCompany = taskHrCompanyMapper.queryByTaskId(taskWorker.getTaskHrId());
            task = taskMapper.getFirstById(taskHrCompany.getTaskId());
            //取最近的一条工作记录以获取打卡信息
            log = workLogMapper.findFirstByTaskWorkerId(taskWorker.getPid());
        }else{
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
    public String punch(String taskWorkerId, PunchType punchType, OffsetDateTime punchTime,Measure measure) {
        com.microdev.common.context.User user = ServiceContextHolder.getServiceContext().getUser();

        TaskWorker taskWorker = taskWorkerMapper.findFirstById(taskWorkerId);
        if (taskWorker == null || !taskWorker.getUserId().equals(user.getId())) {
            //未找到小时工任务信息
            return "打卡失败";
        }
        Company hotel = companyMapper.findCompanyById (taskHrCompanyMapper.queryByTaskId (taskWorkerMapper.findFirstById (taskWorkerId).getTaskHrId ()).getHotelId ());
        Double m = LocationUtils.getDistance (hotel.getLatitude (),hotel.getLongitude (),measure.getLatitude (),measure.getLongitude ());
        if(m>500){
            return "打卡地点距离工作地超过500米";
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
            log.setTaskId(taskMapper.selectTaskIdByTaskWorkerId(taskWorkerId));
            workLogMapper.insert(log);
        } else {
            log = workLogMapper.findFirstByTaskWorkerId(taskWorkerId);
            if (log != null) {
                if (punchType == PunchType.REPAST) {//用餐 用餐次数加1
                    log.setRepastTimes(log.getRepastTimes() + 1);
                } else if (punchType == PunchType.PUNCHOUT) {//签退 计算工时数 迭代更新
                    log.setToDate(punchTime);

                    //求时间差
                    Duration duration = Duration.between(log.getFromDate(), log.getToDate());
                    Long seconds_span = duration.getSeconds();
                    Long minutes = (seconds_span / 60);

                    //不再进行就餐打卡
                    //minutes = minutes - taskWorker.getRepastTimes() * 30;//减去用餐时间

                    if (minutes < 0) {
                        minutes = new Long(0);
                    }

                    if (minutes > 0) {
                        taskHrCompany =
                                taskHrCompanyMapper.queryByTaskId(taskWorker.getTaskHrId());
                        task = taskMapper.getFirstById(taskHrCompany.getTaskId());
                        Double shouldPayMoney_hrtoworker = (minutes / 60.00) * taskWorker.getHourlyPay();
                        Double shouldPayMoney_hoteltohr = (minutes / 60.00) * task.getHourlyPay();

                        //小数点后保留两位(第三位四舍五入)
                        shouldPayMoney_hrtoworker = new BigDecimal(shouldPayMoney_hrtoworker).
                                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        shouldPayMoney_hoteltohr = new BigDecimal(shouldPayMoney_hoteltohr).
                                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        taskWorkerMapper.addMinutes(taskWorkerId, minutes, shouldPayMoney_hrtoworker);
                        taskHrCompanyMapper.addMinutes(taskWorker.getTaskHrId(), minutes,
                                shouldPayMoney_hrtoworker, shouldPayMoney_hoteltohr);
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
    public boolean askForLeave(WorkerSupplementRequest info) {
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

        OffsetDateTime time = DateUtil.strToOffSetDateTime(info.getTime(), "yyyy/MM/dd HH:mm");
        OffsetDateTime endTime = DateUtil.strToOffSetDateTime(info.getEndTime(), "yyyy/MM/dd HH:mm");
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
        TaskWorker taskWorker = taskWorkerMapper.findFirstById (info.getTaskWorkerId());
        if (taskWorker == null) {
            throw new ParamsException("参数错误");
        }
        m.setHrCompanyId(taskWorker.getHrCompanyId());
        m.setHrTaskId(taskWorker.getTaskHrId());
        m.setTaskId (taskWorker.getHotelTaskId());
        Map<String, String> param = new HashMap<>();
        param.put("userName", tp.getUsername());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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
        return true;
    }

    /**
     * 小时工申请加班
     */
    @Override
    public boolean askWorkOvertime(WorkerSupplementRequest info) {
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

        OffsetDateTime time = DateUtil.strToOffSetDateTime(info.getTime(), "yyyy/MM/dd");
        Message m = new Message();
        m.setContent(info.getReason());
        m.setSupplementTime(time);
        m.setMinutes(info.getMinutes() + "");
        WorkerCancelTask tp = taskWorkerMapper.selectUserAndWorkerId(info.getTaskWorkerId());

        MessageTemplate mess = messageTemplateMapper.findFirstByCode("applyOvertimeMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageType(2);
        m.setMessageTitle(mess.getTitle());
        m.setWorkerId(tp.getWorkerId());
        m.setWorkerTaskId(info.getTaskWorkerId());
        m.setHotelId(tp.getHotelId());
        m.setHrTaskId(tp.getTaskHrId());
        m.setTaskId (tp.getHotelTaskId());
        m.setHrCompanyId(tp.getHrId());

        Map<String, String> param = new HashMap<>();
        param.put("userName", tp.getUsername());
        param.put("startTime", info.getTime());
        param.put("taskContent", info.getReason());
        param.put("minutes", info.getMinutes().toString());
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setApplyType(3);
        m.setStatus(0);
        m.setIsTask(0);

        messageMapper.insert(m);
        return true;
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
        Map map = new HashMap<String,Object> ();
        map.put("message_type",7);
        map.put("worker_task_id",info.getTaskWorkerId());
        map.put("status",0);
        if(messageMapper.selectByMap (map).size ()>0){
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
        m.setHrTaskId (tp.getTaskHrId());
        m.setTaskId (tp.getTaskId());
        m.setHotelId(tp.getHotelId());
        Map<String, String> param = new HashMap<>();
        param.put("userName",  tp.getUsername());
        param.put("taskContent", info.getReason());
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setApplyType(2);
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
        if (list == null) {
            info.setList(new ArrayList<>());
        } else {
            info.setList(list);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("page", paginator.getPage());
        result.put("total", paginator.getPageSize());
        result.put("list", list);
        return result;
    }

    /**
     * 查询补签记录详情
     */
    @Override
    public SupplementResponse selectNoPunchDetails(String taskWorkerId, String date) {
        if (StringUtils.isEmpty(date) || StringUtils.isEmpty(taskWorkerId)) {
            throw new ParamsException("参数不能为空");
        }

        Map<String, Object> param = new HashMap<>();
        param.put("taskWorkerId", taskWorkerId);
        param.put("date", date);
        return workLogMapper.selectNoPunchDetail(param);
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

        OffsetDateTime time = DateUtil.strToOffSetDateTime(info.getTime(), "yyyy/MM/dd HH:mm");
        Message m = new Message();
        m.setSupplementTime(time);
        m.setContent(info.getReason());
        WorkerCancelTask tp = taskWorkerMapper.selectUserAndWorkerId(info.getTaskWorkerId());
        MessageTemplate mess = messageTemplateMapper.findFirstByCode("applySupplementMessage");
        Map map = new HashMap<String,Object> ();
        map.put("message_type",1);
        map.put("worker_task_id",info.getTaskWorkerId());
        map.put("status",0);
        Map<String, String> param = new HashMap<>();
        param.put("userName",  tp.getUsername ());
        param.put("time", info.getTime());
        param.put("taskContent", info.getReason());
        String c = StringKit.templateReplace(mess.getContent(), param);
        map.put("message_content",c);
        if(messageMapper.selectByMap (map).size ()>0){
            return "你已提交过申请";
        }
        m.setMessageCode(mess.getCode());
        m.setMessageTitle(mess.getTitle());
        m.setWorkerId( tp.getWorkerId());
        m.setWorkerTaskId (info.getTaskWorkerId());
        m.setMessageType(1);
        m.setApplicantType(1);
        m.setHrTaskId(tp.getTaskHrId());
        m.setWorkerTaskId(info.getTaskWorkerId());
        m.setHotelId(tp.getHotelId());
        m.setTaskId (tp.getTaskId());
        m.setMessageContent(c);
        m.setApplyType(3);
        m.setStatus(0);
        m.setIsTask(0);

        messageMapper.insert(m);
        return "申请补签提交成功";
    }

    /**
     * 查询小时工工作记录
     */
    @Override
    public UserTaskResponse selectUserTaskInfo(String taskWorkerId, String workerId) {
        if (StringUtils.isEmpty(taskWorkerId) || StringUtils.isEmpty(workerId)) {
            throw new ParamsException("参数不能为空");
        }
        UserTaskResponse response = new UserTaskResponse();
        // 查询用户工作记录
        TaskDateInfo taskWorker = taskWorkerMapper.selectTaskWorkerDate(taskWorkerId);
        if (taskWorker == null) {
            throw new ParamsException("查询不到用户工作任务");
        }
        Calendar startDay = DateUtil.dateToCalendar (taskWorker.getFromDate());
        Calendar nowDate = Calendar.getInstance ();
        Calendar endDay = DateUtil.dateToCalendar (taskWorker.getToDate());
        if (nowDate.compareTo(startDay) < 0) {
            log.info("任务还没有开始");
            return response;
        }
        if (nowDate.compareTo(endDay) < 0) {
            endDay = nowDate;
        }
        Calendar dayStart = DateUtil.dateToCalendar (taskWorker.getDayStartTime());
        Calendar dayEnd = DateUtil.dateToCalendar (taskWorker.getDayEndTime());

        startDay.set (startDay.get (Calendar.YEAR),startDay.get (Calendar.MONTH), startDay.get (Calendar.DAY_OF_YEAR), dayStart.get (Calendar.HOUR_OF_DAY), dayStart.get (Calendar.MINUTE));
        endDay.set (endDay.get (Calendar.YEAR), endDay.get (Calendar.MONTH), endDay.get (Calendar.DAY_OF_YEAR),
                dayEnd.get (Calendar.HOUR_OF_DAY), dayEnd.get (Calendar.MINUTE));
        long start = dayStart.getTimeInMillis () / 1000;
        long end = dayEnd.getTimeInMillis () / 1000;
        boolean expire;
        //long workDay = endDay.getLong(ChronoField.EPOCH_DAY) - startDay.getLong(ChronoField.EPOCH_DAY);
        // 查询打卡记录
        SimpleDateFormat d = new SimpleDateFormat ("yyyy/MM/dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        List<WorkerDetail> detailList = new ArrayList<>();//存放小时工所有打卡记录
        List<WorkerOneDayInfo> list = workLogMapper.selectUserPunchDetail(taskWorkerId);
        List<HolidayDateInfo> holidayList = holidayMapper.selectHolidayByTaskWorkId(taskWorkerId);
        PunchInfo workLog = null;
        WorkerDetail detail = null;
        List<PunchInfo> workList = null;
        Map<String, Integer> hotelStatus = null;
        Map<String, Integer> sysStatus = null;
        if (list == null || list.size() == 0) {
            if (holidayList == null || holidayList.size() == 0) {
                //没有打卡也没有请假
                while (startDay.compareTo(nowDate) <= 0) {
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
                    expire = (nowDate.getTimeInMillis () - startDay.getTimeInMillis ()) / 3600000  >= 168 ? true : false;
                    if (expire) {
                        detail.setExpire("1");
                    } else  {
                        detail.setExpire("0");
                    }
                    workList.add(workLog);
                    detail.setTime(d.format (startDay.getTime ()));
                    detailList.add(detail);
                    detail.setSysStatus(sysStatus);
                    detail.setHotelStatus(hotelStatus);
                    startDay.add (Calendar.DAY_OF_YEAR, 1);
                }

            } else {
                while (true) {
                    if (startDay.compareTo(nowDate) > 0) break;
                    detail = new WorkerDetail();
                    workList = new ArrayList<>();
                    workLog = new PunchInfo();
                    hotelStatus = new HashMap<>();
                    sysStatus = new HashMap<>();
                    initMapStatus(hotelStatus, 4);
                    initMapStatus(sysStatus, 5);
                    long time = 0L;
                    int num = 0;
                    expire = (nowDate.getTimeInMillis() - startDay.getTimeInMillis()) / 3600000  >= 168 ? true : false;
                    for (HolidayDateInfo holiday : holidayList) {
                        Calendar holidayFrom = DateUtil.dateToCalendar (holiday.getFromDate ());
                        Calendar holidayToDay = DateUtil.dateToCalendar (holiday.getToDate ());
                        if (holidayFrom.get (Calendar.DAY_OF_YEAR) == startDay.get (Calendar.DAY_OF_YEAR)) {
                            //当天请假
                            sysStatus.put("leave", 1);
                            hotelStatus.put("leave", 1);
                            workLog.setEndTime("--");
                            workLog.setStartTime("--");
                            workList.add(workLog);

                            if ((holiday.getFromDate().getTime ()/60000) <= (startDay.getTimeInMillis ()/60000)) {

                                if (holidayToDay.get (Calendar.DAY_OF_YEAR) > startDay.get (Calendar.DAY_OF_YEAR) || (holidayToDay.get (Calendar.DAY_OF_YEAR) == startDay.get (Calendar.DAY_OF_YEAR) && holidayToDay.getTimeInMillis ()/1000 >= end)) {

                                    break;
                                } else {
                                    //当天部分时间请假
                                    num++;
                                    time += holidayToDay.getTimeInMillis ()/1000 - start;
                                }
                            } else {
                                if (holidayToDay.get (Calendar.DAY_OF_YEAR) == startDay.get (Calendar.DAY_OF_YEAR)) {
                                    if (holidayToDay.getTimeInMillis () / 1000 < end) {
                                        num++;
                                        time += holidayToDay.getTimeInMillis () / 60000 - holidayFrom.getTimeInMillis ()/6000;
                                    }
                                } else if (holidayToDay.get (Calendar.DAY_OF_YEAR) > startDay.get (Calendar.DAY_OF_YEAR)){
                                    num++;
                                    time += end - holidayFrom.getTimeInMillis ()/1000;
                                } else {
                                    throw new ParamsException("请假时间错误");
                                }
                            }

                        } else if (holidayFrom.get (Calendar.DAY_OF_YEAR) < startDay.get (Calendar.DAY_OF_YEAR)){
                            //请假是从当天之前开始计算
                            sysStatus.put("leave", 1);
                            hotelStatus.put("leave", 1);
                            workLog.setEndTime("--");
                            workLog.setStartTime("--");
                            workList.add(workLog);
                            if (holidayToDay.get (Calendar.DAY_OF_YEAR) > endDay.get (Calendar.DAY_OF_YEAR)) {
                                //全体请假
                                break;
                            } else if (holidayToDay.get (Calendar.DAY_OF_YEAR) == endDay.get (Calendar.DAY_OF_YEAR)){
                                if (holidayToDay.getTimeInMillis ()/1000 >= end) {
                                    break;
                                } else {
                                    num++;
                                    time += holidayToDay.getTimeInMillis ()/1000 - start;
                                }
                            } else {
                                throw new ParamsException("请假时间错误");
                            }
                        }
                    }
                    if (num > 0  && time < (end - start)) {
                        workLog = new PunchInfo();
                        sysStatus.put("comeLate", 1);
                        workLog.setStartTime("--");
                        workLog.setEndTime("--");

                        workList.add(workLog);
                    }
                    if (expire) {
                        detail.setExpire("1");
                    } else  {
                        detail.setExpire("0");
                    }
                    detail.setTime(d.format (startDay.getTime ()));
                    startDay.add (Calendar.DAY_OF_YEAR, 1);
                    detail.setSysStatus(sysStatus);
                    detail.setHotelStatus(hotelStatus);
                    detailList.add(detail);
                }
            }
        } else {
            //有打卡记录
            for (WorkerOneDayInfo param : list) {
                detail = new WorkerDetail();
                workList = new ArrayList<>();
                Calendar time = DateUtil.dateToCalendar (param.getCreateTime());
                String[] currentStartTime = param.getFromDate().split(",");

                String[] currentEndTime = null;
                if (!StringUtils.isEmpty(param.getToDate())) {
                    currentEndTime = param.getToDate ().split (",");
                }
                String[] confirmStatus = param.getEmployerConfirmStatus().split(",");
                

                //如果当天没有打卡记录
                while (startDay.get (Calendar.YEAR) != time.get (Calendar.YEAR) && startDay.getTimeInMillis ()/1000 <= nowDate.getTimeInMillis ()/1000) {
                    expire = (nowDate.getTimeInMillis ()/1000 - startDay.getTimeInMillis ()/1000) / 3600  >= 168 ? true : false;
                    int minutes = containsTime(startDay, endDay,  holidayList);
                    if (hotelStatus == null || sysStatus == null) {
                        hotelStatus = new HashMap<>();
                        sysStatus = new HashMap<>();
                        initMapStatus(hotelStatus, 4);
                        initMapStatus(sysStatus, 5);
                    }
                    //没有请假
                    if (minutes == 0) {
                        workLog = new PunchInfo();
                        sysStatus.put("stay", 1);
                        workLog = new PunchInfo();
                        workLog.setEndTime("--");
                        workLog.setStartTime("--");
                        workList.add(workLog);
                    } else {
                        //请假时间小于上班时间
                        if (minutes < (end - start)) {
                            workLog = new PunchInfo();
                            workLog.setEndTime("--");
                            workLog.setStartTime("--");
                            sysStatus.put("leave", 1);
                            sysStatus.put("comeLate", 1);
                            hotelStatus.put("leave", 1);
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
                    detail.setTime(d.format (startDay.getTime ()));
                    startDay.add (Calendar.DAY_OF_YEAR, 1);
                    detail.setHotelStatus(hotelStatus);
                    detail.setSysStatus(sysStatus);
                    if (expire) {
                        detail.setExpire("1");
                    } else  {
                        detail.setExpire("0");
                    }
                    detailList.add(detail);
                    hotelStatus = null;
                    sysStatus = null;
                }
				hotelStatus = new HashMap<>();
                sysStatus = new HashMap<>();
                initMapStatus(hotelStatus, 4);
                initMapStatus(sysStatus, 5);                //有打卡记录
                expire = (nowDate.getTimeInMillis ()/1000 - startDay.getTimeInMillis ()/1000) / 3600  >= 168 ? true : false;
                if (expire) {
                    detail.setExpire("1");
                } else  {
                    detail.setExpire("0");
                }

                Date t = null;
                try {
                   t = timeFormat.parse (currentStartTime[0]);
                } catch (ParseException e) {
                    e.printStackTrace ( );
                }
                workLog = new PunchInfo();
                Calendar tc = DateUtil.dateToCalendar (t);
                if (dayStart.getTime ().compareTo(t) < 0) {
                    int minutes = containsTime(tc, startDay,  holidayList);
                    if (minutes == 0) {
                        sysStatus.put("comeLate", 1);
                        if ("1".equals(confirmStatus[0])) {
                            sysStatus.put("comeLate", 1);
                        }
                    } else {
                        sysStatus.put("leave", 1);
                        hotelStatus.put("leave", 1);
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
                            Date start2 = null;
                            Date end1 = null;
                            try {
                                start2 = timeFormat.parse (currentStartTime[i]);
                                end1 = timeFormat.parse (currentEndTime[i - 1]);
                            } catch (ParseException e) {
                                e.printStackTrace ( );
                            }
                            Calendar cStart2 = DateUtil.dateToCalendar (start2);
                            Calendar cEnd1 = DateUtil.dateToCalendar (end1);
                            if (start2.compareTo(end1) <= 0) {

                            } else {
                                int minutes = containsTime(cEnd1, cStart2,  holidayList);
                                if (minutes == 0) {
                                    sysStatus.put("comeLate", 1);
                                    if ("1".equals(confirmStatus[i])) {
                                        hotelStatus.put("comeLate", 1);
                                    }
                                } else {
                                    sysStatus.put("leave", 1);
                                    hotelStatus.put("leave", 1);
                                }
                            }
                            workList.add(workLog);
                        }
                        workLog = new PunchInfo();

                        workLog.setStartTime(currentStartTime[i]);
                        if (currentEndTime.length == i + 1) {

                            workLog.setEndTime(currentEndTime[i]);
                            Date start2 = null;
                            Date end1 = null;
                            try {
                                start2 = timeFormat.parse (currentStartTime[i]);
                                end1 = timeFormat.parse (currentEndTime[i - 1]);
                            } catch (ParseException e) {
                                e.printStackTrace ( );
                            }

                            if (start2.compareTo(end1) <= 0) {
                                if (start2.getTime ()/1000 >= dayStart.getTimeInMillis ()/1000) {

                                } else {
                                   sysStatus.put("earlier", 1);
                                   if ("1".equals(confirmStatus[i])) {
                                       hotelStatus.put("earlier", 1);
                                   }
                                }
                            } else {
                                sysStatus.put("comeLate", 1);
                                if ("1".equals(confirmStatus[i])) {
                                    hotelStatus.put("comeLate", 1);
                                }
                            }
                            workList.add(workLog);
                        } else {
                            workLog.setEndTime("--");
                            sysStatus.put("forget", 1);
                            if ("1".equals(confirmStatus[i])) {
                                hotelStatus.put("forget", 1);
                            }
                            workList.add(workLog);
                        }
                    }
                } else {
                    //忘打卡
                    workLog.setEndTime("--");
                    sysStatus.put("forget", 1);
                    if ("1".equals(confirmStatus[0])) {
                        hotelStatus.put("forget", 1);
                    }
                    workList.add(workLog);
                    int minutes = containsTime(startDay, endDay,  holidayList);
                    //查询是否有请假
                    if (minutes > 0) {
                        workLog = new PunchInfo();
                        sysStatus.put("leave", 1);
                        hotelStatus.put("leave", 1);
                        workList.add(workLog);
                    }
                }
                detail.setWorkList(workList);
                detail.setSysStatus(sysStatus);
                detail.setHotelStatus(hotelStatus);
                detail.setTime(d.format (startDay.getTime ()));
                detailList.add(detail);
                startDay.add (Calendar.DAY_OF_YEAR, 1);
            }
        }
        response.setList(detailList);
        return response;
    }

    /**
     * 初始化小时工工作记录打卡状态
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
     *  修改服务类型及服务地区
     */    public void mpdifyAreaAndService(AreaAndServiceRequest request) {
         System.out.println ("param:"+request);
         if(request.getAreaCodeList ()==null){
             //删除旧数据
             if(request.getAreaCode()!=null){
                 companyMapper.deleteAreaRelation(request.getId());
                 companyMapper.deleteCompanyArea(request.getId());
                 //添加区域
                 List<UserArea> areaList = request.getAreaCode();
                 for (UserArea ua:areaList) {
                     if(ua.getAreaLevel ()==1){
                         List<Map<String,String>> list = dictMapper.findCity(ua.getAreaId ());
                         companyMapper.insertAreaRelation(request.getId(),ua.getAreaId (),ua.getAreaLevel (),dictMapper.findProvinceNameById (ua.getAreaId ()));
                         if(list == null){
                             companyMapper.insertCompanyArea(request.getId(),ua.getAreaId (),request.getIdType ());
                         }
                         for (Map<String,String> key : list) {
                             List<Map<String,String>> list2 = dictMapper.findArea(key.get("areaId"));
                             System.out.println ("2:"+list2);
                             if(list2 == null ){
                                 companyMapper.insertCompanyArea(request.getId(),key.get("areaId"),request.getIdType ());
                             }
                             for (Map<String,String> key2 : list2) {
                                 companyMapper.insertCompanyArea(request.getId(),key2.get("areaId"),request.getIdType ());
                             }
                         }
                     }else if(ua.getAreaLevel ()==2){

                         companyMapper.insertAreaRelation(request.getId(),ua.getAreaId (),ua.getAreaLevel (),dictMapper.findCityNameById (ua.getAreaId ()));
                         List<Map<String,String>> list2= dictMapper.findArea(ua.getAreaId ());
                         System.out.println ("3:"+list2);
                         if(list2 == null ){
                             companyMapper.insertCompanyArea(request.getId(),ua.getAreaId (),request.getIdType ());
                         }
                         for (Map<String,String> key2 : list2) {
                             companyMapper.insertCompanyArea(request.getId(),key2.get("areaId"),request.getIdType ());
                         }
                     }else{
                         companyMapper.insertAreaRelation(request.getId(),ua.getAreaId (),ua.getAreaLevel (),dictMapper.findAreaNameById (ua.getAreaId ()));
                         companyMapper.insertCompanyArea(request.getId(),ua.getAreaId (),request.getIdType ());
                     }
                 }
             }
         }else{
             //删除旧数据
             companyMapper.deleteAreaRelation(request.getId());
             companyMapper.deleteCompanyArea(request.getId());
             List<String> lis = request.getAreaCodeList ();
             //添加区域
             for(int i=0;i<lis.size ();i++){
                  if(dictMapper.isProvince (lis.get (i))!=null){//第一级
                      List<Map<String,String>> list = dictMapper.findCity(lis.get (i));
                      companyMapper.insertAreaRelation(request.getId(),lis.get (i),1,dictMapper.findProvinceNameById (lis.get (i)));
                      if(list == null){
                          companyMapper.insertCompanyArea(request.getId(),lis.get (i),request.getIdType ());
                      }
                      for (Map<String,String> key : list) {
                          List<Map<String,String>> list2 = dictMapper.findArea(key.get("areaId"));
                          System.out.println ("2:"+list2);
                          if(list2 == null ){
                              companyMapper.insertCompanyArea(request.getId(),key.get("areaId"),request.getIdType ());
                          }
                          for (Map<String,String> key2 : list2) {
                              companyMapper.insertCompanyArea(request.getId(),key2.get("areaId"),request.getIdType ());
                          }
                      }
                  }else if (dictMapper.isCity (lis.get (i))!=null){//第二级
                      companyMapper.insertAreaRelation(request.getId(),lis.get (i),2,dictMapper.findCityNameById (lis.get (i)));
                      List<Map<String,String>> list2= dictMapper.findArea(lis.get (i));
                      System.out.println ("3:"+list2);
                      if(list2 == null ){
                          companyMapper.insertCompanyArea(request.getId(),lis.get (i),request.getIdType ());
                      }
                      for (Map<String,String> key2 : list2) {
                          companyMapper.insertCompanyArea(request.getId(),key2.get("areaId"),request.getIdType ());
                      }
                  }else{//第三级
                      companyMapper.insertAreaRelation(request.getId(),lis.get (i),3,dictMapper.findAreaNameById (lis.get (i)));
                      companyMapper.insertCompanyArea(request.getId(),lis.get (i),request.getIdType ());
                  }

             }
         }

        if(request.getServiceType ()!=null){
            taskTypeRelationMapper.deleteTaskTypeRelation(request.getId());
            //添加服务类型
            List<String> serviceType = request.getServiceType();
            for(int i = 0;i<serviceType.size();i++){
                taskTypeRelationMapper.insertTaskTypeRelation(request.getId(),serviceType.get(i),request.getIdType ());
            }
        }
    }

    @Override
    public Map<String, Object> queryWorker(String id) {
        Map<String, Object> map = workerMapper.queryWorker(id);
        String str = "";
        if (map.get("birthday") != null) {
            str = map.get("birthday").toString ().substring (0,10);
        }

        map.put("birthday",str);
        List l1 = dictService.findServiceArea (id);
        List l2 = dictMapper.queryTypeByUserId (id);
        map.put("areaCode",l1==null?new ArrayList<>():l1);
        map.put("serviceType",l2==null?new ArrayList<>():l2);
        return map;
    }

    @Override
    public ResultDO pagingWorkers(Paginator paginator, WorkerQueryDTO workerQueryDTO) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        System.out.println (workerQueryDTO);
        //查询数据集合
        List<Map<String,Object>> list = workerMapper.queryWorkers(workerQueryDTO);
        PageInfo<Map<String,Object>> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess(result);
    }

    /**
     * 小时工申请绑定人力公司
     * @param workerId      小时工workerId
     * @param set           人力公司id
     * @return
     */
    @Override
    public String workerApplybind(String workerId, List<String> set) {
        if (StringUtils.isEmpty(workerId) || set == null || set.size() == 0) {
            throw new ParamsException("参数错误");
        }
        User user = userMapper.selectByWorkerId(workerId);
        if (user == null) throw new ParamsException("查询不到用户");
        DictDTO dict = dictMapper.findByNameAndCode("WorkerBindHrMaxNum","1");
        Integer maxNum = Integer.parseInt(dict.getText());
        int nowNum = userCompanyMapper.selectWorkerBindCount(user.getPid());
        if (nowNum >= maxNum || (nowNum + set.size()) >= maxNum) {
            throw new BusinessException("绑定人数达到上限");
        }
        int bindNum = userCompanyMapper.selectIsBindUserId(user.getPid(), set);
        if (bindNum > 0) {
            throw new BusinessException("已提交绑定申请");
        }
        List<UserCompany> userCompanyList = new ArrayList<>();
        UserCompany userCompany = null;
        for (String str : set) {
            userCompany = new UserCompany();
            userCompany.setCompanyType(2);
            userCompany.setUserType(UserType.worker);
            userCompany.setUserId(user.getPid());
            userCompany.setCompanyId(str);
            userCompany.setStatus(0);
            userCompanyList.add(userCompany);
        }
        userCompanyMapper.saveBatch(userCompanyList);
        //发送消息
        messageService.bindUserHrCompany(user.getNickname(), workerId, set, 1);
        return "申请成功";
    }

    /**
     * 统计请假时间
     * @param startTime     上班时间
     * @param endTime       上班打卡时间
     * @param holidayList   请假数据
     * @return
     */
    private int containsTime(Calendar startTime, Calendar endTime, List<HolidayDateInfo> holidayList) {
        int num = 0;
        Calendar time2 = Calendar.getInstance ();
        time2.set (startTime.get (Calendar.YEAR), startTime.get (Calendar.MONTH), startTime.get (Calendar.DAY_OF_YEAR),
                endTime.get (Calendar.HOUR_OF_DAY), endTime.get (Calendar.MINUTE), endTime.get (Calendar.SECOND));
        for (HolidayDateInfo holiday : holidayList) {
            Calendar from = DateUtil.dateToCalendar (holiday.getFromDate ());
            Calendar to = DateUtil.dateToCalendar (holiday.getToDate ());
            long hStart = from.getTimeInMillis ()/60000;
            long hEnd = to.getTimeInMillis ()/60000;
            long startMinute = startTime.getTimeInMillis ()/1000;
            long endMinute = time2.getTimeInMillis ()/1000;
            if (holiday.getFromDate().compareTo(startTime.getTime ()) > 0) continue;
            if (holiday.getFromDate().compareTo(startTime.getTime ()) <= 0) {
                //请假开始日期小于当前日期
                if (holiday.getToDate().compareTo(startTime.getTime ()) >= 0) {
                    if (holiday.getToDate().compareTo(time2.getTime ()) < 0) {
                        num += hEnd - startMinute;
                    } else  {
                        num += endMinute - startMinute;
                    }
                }
            } else if (from.get (Calendar.DAY_OF_YEAR) == startTime.get (Calendar.DAY_OF_YEAR)) {
                //请假日期和当前日期一致
                if (to.get (Calendar.DAY_OF_YEAR) == startTime.get (Calendar.DAY_OF_YEAR)) {
                    if (hStart <= startMinute) {
                        if (holiday.getToDate().compareTo(startTime.getTime ()) > 0) {
                            if (to.getTimeInMillis ()/60000 >= time2.getTimeInMillis ()/60000) {
                                num += time2.getTimeInMillis ()/60000 - startTime.getTimeInMillis ()/60000;
                            } else {
                                num += to.getTimeInMillis ()/60000 - startTime.getTimeInMillis ()/60000;
                            }
                        }
                    } else {
                        if (to.getTimeInMillis ()/60000 >= time2.getTimeInMillis ()/60000) {
                            num += time2.getTimeInMillis ()/60000 - from.getTimeInMillis ()/60000;
                        } else {
                            num += to.getTimeInMillis ()/60000 - from.getTimeInMillis ()/60000;
                        }
                    }
                } else if (to.get (Calendar.DAY_OF_YEAR) > startTime.get (Calendar.DAY_OF_YEAR)) {
                    if (from.getTimeInMillis ()/60000 <= startTime.getTimeInMillis ()/60000) {
                        num += time2.getTimeInMillis ()/60000 - startTime.getTimeInMillis ()/60000;
                    } else if (from.getTimeInMillis ()/60000 <= time2.getTimeInMillis ()/60000){
                        num += time2.getTimeInMillis ()/60000 - from.getTimeInMillis ()/60000;
                    }
                }
            }
        }
        return num;
    }
    /**
     * 判断是否请假
     *
     * @param d     格式化對象
     * @param start 每天工作开始时间
     * @param end   每天工作结束时间
     * @param str   请假开始时间
     * @param str2  请假结束时间
     * @return true 请假 false 请假旷工
     */
    private boolean isLeave(DateTimeFormatter d, long start, long end, String[] str, String[] str2) {
        long leaveTime = 0L;
        OffsetDateTime time = DateUtil.strToOffSetDateTime(str2[0], "yyyy-MM-dd HH:mm:ss");
        leaveTime += time.getLong(ChronoField.MINUTE_OF_DAY) - start;
        int i = 0;
        for (; ; i++) {
            if (i >= str.length - 1) break;
            time = DateUtil.strToOffSetDateTime(str[i], "yyyy-MM-dd HH:mm:ss");
            long s = time.getLong(ChronoField.MINUTE_OF_DAY);
            time = DateUtil.strToOffSetDateTime(str2[i], "yyyy-MM-dd HH:mm:ss");
            long t = time.getLong(ChronoField.MINUTE_OF_DAY);
            leaveTime += t - s;
        }
        if (i > 0) {
            time = DateUtil.strToOffSetDateTime(str[i], "yyyy-MM-dd HH:mm:ss");
            leaveTime += end - time.getLong(ChronoField.MINUTE_OF_DAY);
        }
        return leaveTime > (end - start);
    }

    /**
     * 小时工处理人力支付
     * @param param
     * @return
     */
    @Override
    public ResultDO workerHandleHrPay(Map<String, String> param) {
        if (StringUtils.isEmpty(param.get("messageId")) || StringUtils.isEmpty(param.get("status"))) {
            throw new ParamsException("参数不能为空");
        }
        String status = param.get("status");
        String messageId = param.get("messageId");
        String reason = param.get("reason");
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new ParamsException("查询不到消息");
        }
        message.setStatus(1);
        messageMapper.updateAllColumnById(message);

        User user = userMapper.selectByWorkerId(message.getWorkerId());
        if (user == null) {
            throw new BusinessException("查询不到小时工信息");
        }
        if ("0".equals(status)) {
            //拒绝
            String content = "小时工" + user.getNickname() + "拒绝了你发起的一笔支付信息，金额为" + Double.valueOf(message.getMinutes()) + ",拒绝理由为" + reason;
            informService.sendInformInfo(1, 2, content, message.getHrCompanyId(), "账目被拒绝");
        } else if ("1".equals(status)) {
            //同意
            TaskWorker taskWorker = taskWorkerMapper.selectById(message.getWorkerTaskId());
            if (taskWorker == null) {
                throw new ParamsException("查询不到小时工任务信息");
            }
            taskWorker.setHavePayMoney(taskWorker.getHavePayMoney() + Double.valueOf(message.getMinutes()));
            taskWorkerMapper.updateAllColumnById(taskWorker);
            TaskHrCompany taskHrCompany = taskHrCompanyMapper.selectById(message.getHrTaskId());
            if (taskHrCompany == null) {
                throw new ParamsException("查询不到人力任务");
            }
            taskHrCompany.setWorkersHavePay(taskHrCompany.getWorkersHavePay() + Double.valueOf(message.getMinutes()));
            taskHrCompanyMapper.updateAllColumnById(taskHrCompany);

            //发送通知
            String content = "小时工" + user.getNickname() + "同意了你发起的一笔支付信息，金额为" + message.getMinutes();
            informService.sendInformInfo(1, 2, content, message.getHrCompanyId(), "账目已同意");
        }
        return ResultDO.buildSuccess("成功");
    }

}
