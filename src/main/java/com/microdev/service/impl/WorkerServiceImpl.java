package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.context.ServiceContextHolder;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.exception.TaskWorkerNotFoundException;
import com.microdev.common.exception.WorkLogNotFoundException;
import com.microdev.common.utils.DateUtil;
import com.microdev.common.utils.StringKit;
import com.microdev.converter.WorkLogConverter;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.param.api.response.GetBalanceResponse;
import com.microdev.param.api.response.GetCurrentTaskResponse;
import com.microdev.service.DictService;
import com.microdev.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;

@Transactional
@Service
public class WorkerServiceImpl extends ServiceImpl<WorkerMapper, Worker> implements WorkerService {
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

    @Override
    public GetCurrentTaskResponse getCurrentTask(String workerId) {
        User user = userMapper.queryByWorkerId(workerId);
        String userId = user.getPid();
        if (user == null) {
            throw new ParamsException("参数userId输入有误");
        }

        //com.microdev.common.context.User user = ServiceContextHolder.getServiceContext().getUser();

        //if(user.用户类型为小时工)
        //判断小时工标识ID是否与传入参数一致
        //else if(user.用户类型为人力公司)//人力公司查看其下小时工的工作状况(若有该需求可由该接口提供)
        //判断该小时工当前是否与该人力公司存在绑定关系

        Task task = null;
        WorkLog log = null;
        TaskHrCompany taskHrCompany = null;
        Integer hour = OffsetDateTime.now().getHour();
        Integer minute = OffsetDateTime.now().getMinute();
        Integer second = OffsetDateTime.now().getSecond();
        OffsetDateTime time = OffsetDateTime.of(1970, 1, 1, hour, minute, second, 0, ZoneOffset.UTC);
        //取进行中的任务
        TaskWorker taskWorker = taskWorkerMapper.findWorkerNowTask(
                userId, TaskWorkerStatus.ACCEPTED.ordinal(),
                //结束60分钟内的任务 仍然当作当前任务 以便进行打卡签退操作(如若改动 需与前端保持一致)
                OffsetDateTime.now().plusMinutes(-1), time);
        //如果没有当前进行中的任务 则获取最近的下一个任务
        if (taskWorker == null) {
            taskWorker = taskWorkerMapper.findWorkerNextTask(userId, TaskWorkerStatus.ACCEPTED.ordinal(), OffsetDateTime.now());
        }
        //如果没有下一个任务(也没有进行中的任务) 取前一个任务
        if (taskWorker == null) {
            taskWorker = taskWorkerMapper.findWorkerBeforeTask(userId, TaskWorkerStatus.ACCEPTED.ordinal(), OffsetDateTime.now());
        }

        if (taskWorker != null) {
            taskHrCompany = taskHrCompanyMapper.queryByTaskId(taskWorker.getTaskHrId());
            task = taskMapper.getFirstById(taskHrCompany.getTaskId());
            //取最近的一条工作记录以获取打卡信息
            log = workLogMapper.findFirstByTaskWorkerId(taskWorker.getPid());
        }

        GetCurrentTaskResponse response = new GetCurrentTaskResponse();
        if (task != null) {
            Company hotel = companyMapper.findCompanyById(task.getHotelId());
            response.setHotelId(task.getHotelId());
            response.setHotelName(task.getHotelName());
            response.setHotelLogo(hotel.getLogo());
            response.setHotelAddress(hotel.getAddress());

            response.setHrCompanyId(taskHrCompany.getHrCompanyId());
            response.setHrCompanyName(taskWorker.getHrCompanyName());

            response.setTaskId(task.getPid());
            response.setTaskType(task.getTaskTypeText());
            response.setTaskContent(task.getTaskContent());

            response.setFromDate(taskWorker.getFromDate());
            response.setToDate(taskWorker.getToDate());

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

            List<WorkLog> workLogs = workLogMapper.findByTaskWorkId(taskWorker.getPid());

            response.setWorkLogs(workLogConverter.toResponse(workLogs));

            response.setTaskWorkerId(taskWorker.getPid());
            response.setTaskWorkerHours(
                    new BigDecimal(taskWorker.getMinutes() / 60.00).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            response.setTaskWorkerStatus(taskWorker.getStatus());
        }

        return response;
    }

    @Override
    public boolean punch(String taskWorkerId, PunchType punchType, OffsetDateTime punchTime) {
        com.microdev.common.context.User user = ServiceContextHolder.getServiceContext().getUser();

        TaskWorker taskWorker = taskWorkerMapper.findFirstById(taskWorkerId);
        if (taskWorker == null || !taskWorker.getUserId().equals(user.getId())) {
            throw new TaskWorkerNotFoundException("未找到小时工任务信息");
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
                    Long minutes = seconds_span / 60;

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
                }
            } else {
                throw new WorkLogNotFoundException("无相应工作记录");
            }
        }
        log.setPunchDate(OffsetDateTime.now());
        workLogMapper.updateById(log);
        return true;
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

        Message m = new Message();
        m.setSupplementTime(info.getTime());
        m.setSupplementTimeEnd(info.getEndTime());
        m.setContent(info.getReason());
        Map<String, Object> tp = taskWorkerMapper.selectUserAndWorkerId(info.getTaskWorkerId());

        MessageTemplate mess = messageTemplateMapper.findFirstByCode("applyLeaveMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageTitle(mess.getTitle());
        m.setWorkerId((String) tp.get("workerId"));
        m.setWorkerTaskId(info.getTaskWorkerId());
        Map<String, String> param = new HashMap<>();
        param.put("userName", (String) tp.get("username"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        param.put("startTime", info.getTime().format(formatter));
        param.put("taskContent", info.getReason());
        param.put("endTime", info.getEndTime().format(formatter));
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setApplyType(3);
        m.setStatus(0);

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

        Message m = new Message();
        m.setContent(info.getReason());
        m.setSupplementTime(info.getTime());
        m.setMinutes(info.getMinutes());
        Map<String, Object> tp = taskWorkerMapper.selectUserAndWorkerId(info.getTaskWorkerId());

        MessageTemplate mess = messageTemplateMapper.findFirstByCode("applyOvertimeMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageTitle(mess.getTitle());
        m.setWorkerId((String) tp.get("workerId"));
        m.setWorkerTaskId(info.getTaskWorkerId());
        Map<String, String> param = new HashMap<>();
        param.put("userName", (String) tp.get("username"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        param.put("startTime", info.getTime().format(formatter));
        param.put("taskContent", info.getReason());
        param.put("minutes", info.getMinutes().toString());
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setApplyType(3);
        m.setStatus(0);

        messageMapper.insert(m);
        return true;
    }

    /**
     * 小时工申请取消任务
     */
    @Override
    public boolean applyCancelTask(WorkerSupplementRequest info) {
        if (info == null) {
            throw new ParamsException("参数不能为空");
        }
        if (!StringUtils.hasLength(info.getReason())) {
            throw new ParamsException("理由不能为空");
        }
        if (!StringUtils.hasLength(info.getTaskWorkerId())) {
            throw new ParamsException("参数taskWorkerId不能为空");
        }

        Message m = new Message();
        Map<String, Object> tp = taskWorkerMapper.selectUserAndWorkerId(info.getTaskWorkerId());
        m.setContent(info.getReason());
        MessageTemplate mess = messageTemplateMapper.findFirstByCode("applyCancelTaskMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageTitle(mess.getTitle());
        m.setWorkerId((String) tp.get("workerId"));
        m.setWorkerTaskId(info.getTaskWorkerId());
        m.setHrCompanyId(info.getApplicateId());
        Map<String, String> param = new HashMap<>();
        param.put("userName", (String) tp.get("username"));
        param.put("taskContent", info.getReason());
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setApplyType(2);
        m.setStatus(0);
        messageMapper.insert(m);

        return true;
    }

    /**
     * 查询补签的记录
     */
    @Override
    public PageInfo<SupplementResponse> selectNoPunchPageInfo(PageRequest page) {
        if (page == null) {
            throw new ParamsException("参数不能为空");
        }
        if (StringUtils.isEmpty(page.getId())) {
            throw new ParamsException("参数id不能为空");
        }
        if (StringUtils.isEmpty(page.getPage())) {
            throw new ParamsException("页码不能为空");
        }
        if (StringUtils.isEmpty(page.getPageSize())) {
            throw new ParamsException("页数不能为空");
        }

        PageHelper.startPage(page.getPage(), page.getPageSize(), true);
        List<SupplementResponse> list = workLogMapper.selectNoPunchByWorkerId(page.getId());
        PageInfo<SupplementResponse> info = new PageInfo<>(list);
        if (list == null) {
            info.setList(new ArrayList<>());
        } else {
            info.setList(list);
        }
        return info;
    }

    /**
     * 查询补签记录详情
     */
    @Override
    public SupplementResponse selectNoPunchDetails(String workLogId, String companyId) {
        if (StringUtils.isEmpty(companyId) || StringUtils.isEmpty(workLogId)) {
            throw new ParamsException("参数不能为空");
        }
        Map<String, Object> param = new HashMap<>();
        param.put("workLogId", workLogId);
        param.put("companyId", companyId);
        return workLogMapper.selectNoPunchDetail(param);
    }

    /**
     * 小时工发起补签
     */
    @Override
    public boolean supplementWork(WorkerSupplementRequest info) {
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

        Message m = new Message();
        m.setSupplementTime(info.getTime());
        m.setContent(info.getReason());
        Map<String, Object> tp = taskWorkerMapper.selectUserAndWorkerId(info.getTaskWorkerId());

        MessageTemplate mess = messageTemplateMapper.findFirstByCode("applySupplementMessage");
        m.setMessageCode(mess.getCode());
        m.setMessageTitle(mess.getTitle());
        m.setWorkerId((String) tp.get("workerId"));
        m.setWorkerTaskId(info.getTaskWorkerId());
        Map<String, String> param = new HashMap<>();
        param.put("userName", (String) tp.get("username"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        param.put("time", info.getTime().format(formatter));
        param.put("taskContent", info.getReason());
        String c = StringKit.templateReplace(mess.getContent(), param);
        m.setMessageContent(c);
        m.setApplyType(3);
        m.setStatus(0);

        messageMapper.insert(m);
        return true;
    }

    /**
     * 查询小时工工作记录
     */
    @Override
    public UserTaskResponse selectUserTaskInfo(String taskWorkerId, String userId) {
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(userId)) {
            throw new ParamsException("参数不能为空");
        }
        UserTaskResponse response = userMapper.selectUserInfo(userId);
        if (response == null) {
            throw new ParamsException("查询不到用户信息");
        }
        response.setAge(DateUtil.CaculateAge(response.getBirthday()));
        response.setBirthday(null);
        //查询服务区域
        List<String> areaList = areaRelationMapper.selectAreaByUserId(userId);
        if (areaList == null) {
            response.setAreaList(areaList);
        } else {
            response.setAreaList(new ArrayList<>());
        }
        //查询服务类型
        List<String> serviceList = dictService.selectServiceTypeByUserId(userId);
        if (serviceList == null) {
            response.setServiceList(serviceList);
        } else {
            response.setAreaList(new ArrayList<>());
        }
        Set<WorkLog> set = new HashSet<>();
        // 查询用户工作记录
        TaskWorker taskWorker = taskWorkerMapper.selectById(taskWorkerId);
        if (taskWorker == null) {
            throw new ParamsException("查询不到用户工作任务");
        }
        OffsetDateTime startDay = taskWorker.getFromDate();
        OffsetDateTime endDay = taskWorker.getToDate();
        OffsetTime dayStart = taskWorker.getDayStartTime();
        OffsetTime dayEnd = taskWorker.getDayEndTime();

        long start = dayStart.getLong(ChronoField.SECOND_OF_DAY);
        long end = dayEnd.getLong(ChronoField.SECOND_OF_DAY);

        long workDay = endDay.getLong(ChronoField.EPOCH_DAY) - startDay.getLong(ChronoField.EPOCH_DAY);
        // 查询打卡记录
        DateTimeFormatter d = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<WorkLog> list = workLogMapper.selectUserPunchDetail(taskWorkerId);
        if (list == null || list.size() == 0) {

            List<Map<String, Object>> holidayList = holidayMapper.selectUserHolidayByTaskWorkId(taskWorkerId);
            WorkLog log = null;
            if (holidayList == null) {

                for (int i = 0; i < workDay; i++) {
                    log = new WorkLog();
                    log.setCreateTime(startDay.plusDays(1));
                    startDay = startDay.plusDays(1);
                    log.setStatus(3);
                    set.add(log);
                }
            } else {
                for (; ; ) {
                    int day = startDay.getDayOfYear();
                    boolean flag = true;
                    log = new WorkLog();
                    for (Map<String, Object> holiday : holidayList) {
                        String hStart = (String) holiday.get("fromDate");
                        String[] str = hStart.split(",");
                        TemporalAccessor temp = d.parse(str[str.length - 1]);
                        if (day == temp.getLong(ChronoField.DAY_OF_YEAR)) {
                            flag = false;
                            String hEnd = (String) holiday.get("toDate");
                            String[] str2 = hEnd.split(",");
                            TemporalAccessor temp2 = d.parse(str2[str2.length - 1]);
                            if (day < temp2.getLong(ChronoField.DAY_OF_YEAR)) {
                                if (temp.getLong(ChronoField.MINUTE_OF_DAY) > start) {
                                    log.setStatus(9);
                                } else {
                                    if (isLeave(d, start, end, str, str2)) {
                                        log.setStatus(8);
                                    } else {
                                        log.setStatus(9);
                                    }
                                }

                            } else {
                                if (temp2.getLong(ChronoField.MINUTE_OF_DAY) >= end) {
                                    if (isLeave(d, start, end, str, str2)) {
                                        log.setStatus(8);
                                    } else {
                                        log.setStatus(9);
                                    }
                                } else {
                                    log.setStatus(9);
                                }

                            }
                            log.setCreateTime(startDay);
                            startDay = startDay.plusDays(1);
                            set.add(log);
                            break;
                        }
                    }
                    if (flag) {
                        log.setCreateTime(startDay);
                        startDay = startDay.plusDays(1);
                        log.setStatus(3);
                        set.add(log);
                    }
                    workDay--;
                    if (workDay == 0)
                        break;
                }
            }
        } else {
            int size = list.size();
            if (workDay > size) {
                List<Map<String, Object>> holidayList = holidayMapper.selectUserHolidayByTaskWorkId(taskWorkerId);
                Iterator<WorkLog> it = list.iterator();
                WorkLog w = null;
                while (it.hasNext()) {
                    WorkLog log = it.next();
                    if (log.getCreateTime().getDayOfYear() == startDay.getDayOfYear()) {
                        if (holidayList == null || holidayList.size() == 0) {
                            set.add(log);
                        } else {
                            // 工作打卡时间
                            long s1 = log.getFromDate().getLong(ChronoField.SECOND_OF_DAY);
                            long s2 = log.getToDate().getLong(ChronoField.SECOND_OF_DAY);
                            if (s1 <= start && s2 >= end) {
                                set.add(log);
                            } else {
                                w = new WorkLog();
                                boolean f = true;

                                for (Map<String, Object> holiday : holidayList) {
                                    String hStart = (String) holiday.get("fromDate");
                                    String[] str = hStart.split(",");
                                    TemporalAccessor temp = d.parse(str[0]);

                                    String hEnd = (String) holiday.get("toDate");
                                    String[] str2 = hEnd.split(",");
                                    TemporalAccessor temp2 = d.parse(str2[str2.length - 1]);
                                    if (temp.getLong(ChronoField.DAY_OF_YEAR) <= startDay.getDayOfYear()
                                            && temp2.getLong(ChronoField.DAY_OF_YEAR) > startDay.getDayOfYear()) {
                                        if (temp.getLong(ChronoField.DAY_OF_YEAR) == startDay.getDayOfYear()) {
                                            if (temp.getLong(ChronoField.MINUTE_OF_DAY) > start) {
                                                w.setStatus(9);
                                            } else {
                                                if (isLeave(d, start, end, str, str2)) {
                                                    w.setStatus(8);
                                                } else {
                                                    w.setStatus(9);
                                                }
                                            }
                                        } else {
                                            if (isLeave(d, start, end, str, str2)) {
                                                w.setStatus(8);
                                            } else {
                                                w.setStatus(9);
                                            }

                                        }
                                    } else if (temp.getLong(ChronoField.DAY_OF_YEAR) <= startDay.getDayOfYear()
                                            && temp2.getLong(ChronoField.DAY_OF_YEAR) == startDay.getDayOfYear()) {
                                        if (temp.getLong(ChronoField.DAY_OF_YEAR) < startDay.getDayOfYear()) {
                                            if (temp2.getLong(ChronoField.MINUTE_OF_DAY) >= end) {
                                                if (isLeave(d, start, end, str, str2)) {
                                                    w.setStatus(8);
                                                } else {
                                                    w.setStatus(9);
                                                }
                                            } else {
                                                w.setStatus(9);
                                            }
                                        } else if (temp.getLong(ChronoField.DAY_OF_YEAR) == startDay.getDayOfYear()) {
                                            if (temp.getLong(ChronoField.MINUTE_OF_DAY) > start || temp2.getLong(ChronoField.MINUTE_OF_DAY) < end) {
                                                w.setStatus(9);
                                            } else {
                                                if (isLeave(d, start, end, str, str2)) {
                                                    w.setStatus(8);
                                                } else {
                                                    w.setStatus(9);
                                                }
                                            }
                                        }
                                        w.setCreateTime(startDay);
                                        startDay = startDay.plusDays(1);
                                        f = false;
                                        set.add(w);
                                        break;
                                    }
                                }
                                if (f) {
                                    w.setCreateTime(startDay);
                                    w.setStatus(3);
                                    set.add(w);
                                    startDay = startDay.plusDays(1);
                                    if (log.getCreateTime().getDayOfYear() == endDay.getDayOfYear())
                                        break;
                                }
                            }
                        }
                    } else {
                        if (holidayList == null || holidayList.size() == 0) {
                            while (true) {
                                w = new WorkLog();
                                w.setCreateTime(startDay);
                                w.setStatus(3);
                                set.add(w);
                                startDay = startDay.plusDays(1);
                                if (log.getCreateTime().getDayOfYear() == endDay.getDayOfYear())
                                    break;
                            }
                        } else {
                            w = new WorkLog();
                            boolean f = true;

                            for (Map<String, Object> holiday : holidayList) {
                                String hStart = (String) holiday.get("fromDate");
                                String[] str = hStart.split(",");
                                TemporalAccessor temp = d.parse(str[0]);

                                String hEnd = (String) holiday.get("toDate");
                                String[] str2 = hEnd.split(",");
                                TemporalAccessor temp2 = d.parse(str2[str2.length - 1]);
                                if (temp.getLong(ChronoField.DAY_OF_YEAR) <= startDay.getDayOfYear()
                                        && temp2.getLong(ChronoField.DAY_OF_YEAR) > startDay.getDayOfYear()) {
                                    if (temp.getLong(ChronoField.DAY_OF_YEAR) == startDay.getDayOfYear()) {
                                        if (temp.getLong(ChronoField.MINUTE_OF_DAY) > start) {
                                            w.setStatus(9);
                                        } else {
                                            if (isLeave(d, start, end, str, str2)) {
                                                w.setStatus(8);
                                            } else {
                                                w.setStatus(9);
                                            }
                                        }
                                    } else {
                                        if (isLeave(d, start, end, str, str2)) {
                                            w.setStatus(8);
                                        } else {
                                            w.setStatus(9);
                                        }
                                    }
                                } else if (temp.getLong(ChronoField.DAY_OF_YEAR) <= startDay.getDayOfYear()
                                        && temp2.getLong(ChronoField.DAY_OF_YEAR) == startDay.getDayOfYear()) {
                                    if (temp.getLong(ChronoField.DAY_OF_YEAR) == startDay.getDayOfYear()) {
                                        if (temp2.getLong(ChronoField.MINUTE_OF_DAY) < end) {
                                            w.setStatus(9);
                                        } else {
                                            if (temp.getLong(ChronoField.MINUTE_OF_DAY) <= start) {
                                                if (isLeave(d, start, end, str, str2)) {
                                                    w.setStatus(8);
                                                } else {
                                                    w.setStatus(9);
                                                }
                                            } else {
                                                w.setStatus(9);
                                            }
                                        }
                                    } else {
                                        if (temp2.getLong(ChronoField.MINUTE_OF_DAY) < end) {
                                            w.setStatus(9);
                                        } else {
                                            if (isLeave(d, start, end, str, str2)) {
                                                w.setStatus(8);
                                            } else {
                                                w.setStatus(9);
                                            }
                                        }
                                    }
                                }
                                w.setCreateTime(startDay);
                                startDay = startDay.plusDays(1);
                                f = false;
                                set.add(w);
                                break;
                            }
                            if (f) {
                                w.setCreateTime(startDay);
                                w.setStatus(3);
                                set.add(w);
                                startDay = startDay.plusDays(1);
                                if (log.getCreateTime().getDayOfYear() == endDay.getDayOfYear())
                                    break;
                            }
                        }
                    }
                }
            } else {
                set.addAll(list);
            }
        }
        response.setList(list);
        return response;
    }
    /**
     *  修改小时工服务类型及服务地区
     */
    @Override
    public void mpdifyAreaAndService(AreaAndServiceRequest request) {
        //添加区域
        Map<String,Integer> areaList = request.getAreaCode();
            Iterator<Map.Entry<String, Integer>> entries = areaList.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Integer> entry = entries.next();
                companyMapper.insertAreaRelation(request.getWorkerID(),entry.getKey(),entry.getValue());
                if(entry.getValue()==1){
                    Map<String,String> list = dictMapper.findCity(entry.getKey());
                    for (String key : list.keySet()) {
                        Map<String,String> list2= dictMapper.findArea(key);
                        for (String key1 : list2.keySet()) {
                            companyMapper.insertCompanyArea(request.getWorkerID(),key1,0);
                        }
                    }
                }else if(entry.getValue()==2){
                    Map<String,String> list2= dictMapper.findArea(entry.getKey());
                    for (String key1 : list2.keySet()) {
                        companyMapper.insertCompanyArea(request.getWorkerID(),key1,0);
                    }
                }else{
                    companyMapper.insertCompanyArea(request.getWorkerID(),entry.getKey(),0);
                }
            }
        //添加服务类型
        List<String> serviceType = request.getServiceType();
        for(int i = 0;i<serviceType.size();i++){
            taskTypeRelationMapper.insertTaskTypeRelation(request.getWorkerID(),serviceType.get(i),0);
        }
    }

    @Override
    public Map<String, Object> queryWorker(String id) {
        return workerMapper.queryWorker(id);
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
        leaveTime += d.parse(str2[0]).getLong(ChronoField.MINUTE_OF_DAY) - start;
        int i = 0;
        for (; ; i++) {
            if (i >= str.length - 1) break;
            long s = d.parse(str[i]).getLong(ChronoField.MINUTE_OF_DAY);
            long t = d.parse(str2[i]).getLong(ChronoField.MINUTE_OF_DAY);
            leaveTime += t - s;
        }
        if (i > 0) {
            leaveTime += end - d.parse(str[i]).getLong(ChronoField.MINUTE_OF_DAY);
        }
        return leaveTime > (end - start);
    }
}
