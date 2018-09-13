package com.microdev.service.impl;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.FilePush;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.JPushManage;
import com.microdev.common.utils.Maths;
import com.microdev.common.utils.RedisUtil;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.*;
import com.microdev.service.*;
import com.microdev.type.ConstantData;
import com.microdev.type.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.*;

@Transactional
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper,Notice> implements NoticeService {
    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private NoticeServiceMapper noticeServiceMapper;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TaskHrCompanyMapper taskHrCompanyMapper;
    @Autowired
    private DictMapper dictMapper;
    @Autowired
    private EnrollService enrollService;
    @Autowired
    private EnrollMapper enrollMapper;
    @Autowired
    private UserCompanyMapper userCompanyMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private FilePush filePush;
    @Autowired
    private TaskHrCompanyService taskHrCompanyService;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private HotelHrCompanyMapper hotelHrCompanyMapper;
    @Autowired
    private JpushClient jpushClient;
    @Autowired
    private TaskWorkerMapper taskWorkerMapper;
    @Autowired
    private InformMapper informMapper;
    @Autowired
    private WorkerMapper workerMapper;
    @Autowired
    private DictService dictService;
    @Override
    public ResultDO createNotice(CreateNoticeRequest request) {

        if (StringUtils.isEmpty (request.getFromDateL ( )) || StringUtils.isEmpty (request.getToDateL ( ))) {
            throw new ParamsException ("参数不能为空");
        }
        Notice notice;
        if (request.getType ( ) == 1) {
            if (StringUtils.isEmpty (request.getHotelId ( )) || StringUtils.isEmpty (request.getType ( )) || StringUtils.isEmpty (request.getNeedWorkers ( ))) {
                throw new ParamsException ("参数错误：hotelId = " + request.getHotelId ( ) + "type=" + request.getType ( ));
            }
            TaskViewDTO task = null;
            //发布用人单位任务

            CreateTaskRequest req = new CreateTaskRequest ( );
            req.setFromDateL (request.getFromDateL ( ));
            req.setToDateL (request.getToDateL ( ));
            req.setDayStartTimeL (request.getDayStartTimeL ( ));
            req.setDayEndTimeL (request.getDayEndTimeL ( ));
            req.setHotelId (request.getHotelId ( ));
            req.setTaskContent (request.getTaskContent ( ));
            req.setTaskTypeCode (request.getTaskTypeCode ( ));
            req.setTaskTypeText (request.getTaskTypeText ( ));
            req.setSettlementPeriod (request.getSettlementPeriod ( ));
            req.setSettlementNum (request.getSettlementNum ( ));
            req.setWorkerSettlementPeriod (request.getWorkerSettlementPeriod ());
            req.setWorkerSettlementNum (request.getWorkerSettlementNum ());
            req.setNeedhrCompanys (request.getHrNeedWorkers ( ) + request.getNeedWorkers ( ));
            req.setHourlyPay (request.getHourlyPay ( ));
            req.setHrCompanySet (request.getHrCompanySet ( ));
            //发布用人单位任务
            ResultDO rs = taskService.createTask (req);
            task = (TaskViewDTO) rs.getData ( );
            if (request.getHrNeedWorkers ( ) > 0) {
                int workerNum = 0;
                for (TaskHrCompanyDTO t : request.getHrCompanySet ( )) {
                    workerNum += t.getNeedWorkers ( );
                }
                if (workerNum < request.getHrNeedWorkers ( )) {
                    //发布用人单位派发人力任务公告
                    notice = new Notice ( );
                    notice.setCreateTime (OffsetDateTime.now ( ));
                    notice.setFromDate (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getFromDateL ()+OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getDayStartTimeL ()),ZoneOffset.systemDefault ()).toOffsetTime ().getLong (ChronoField.SECOND_OF_DAY )*1000),ZoneOffset.systemDefault ()));
                    notice.setToDate (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getToDateL ()+OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getDayEndTimeL ()),ZoneOffset.systemDefault ()).toOffsetTime ().getLong (ChronoField.SECOND_OF_DAY )*1000),ZoneOffset.systemDefault ()));
                    notice.setHotelId (request.getHotelId ( ));
                    notice.setNeedWorkers (request.getHrNeedWorkers ( ));
                    for (TaskHrCompanyDTO t : request.getHrCompanySet ( )) {
                        notice.setNeedWorkers (notice.getNeedWorkers ( ) - t.getNeedWorkers ( ));
                    }
                    notice.setConfirmedWorkers (0);
                    notice.setType (1);
                    notice.setStatus (0);
                    notice.setSettlementPeriod (request.getSettlementPeriod ( ).toString ( ));
                    notice.setSettlementNum (request.getSettlementNum ( ).toString ( ));
                    notice.setContent (request.getTaskContent ());
                    notice.setHourPay (request.getHourlyPay ( ).toString ( ));
                    notice.setTaskId (task.getPid ( ));
                    notice.setTaskTypeText (request.getTaskTypeText ( ));
                    notice.setTaskTypeIcon (dictMapper.selectById (request.getTaskTypeCode ( )).getExtend ( ));
                    noticeMapper.insert (notice);
                    noticeServiceMapper.insert (notice.getPid ( ), request.getTaskTypeCode ( ));
                }

            }
            if (request.getNeedWorkers ( ) > 0) {
                //发布用人单位招聘小时工公告
                notice = new Notice ( );
                notice.setCreateTime (OffsetDateTime.now ( ));
                notice.setFromDate (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getFromDateL ()+OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getDayStartTimeL ()),ZoneOffset.systemDefault ()).toOffsetTime ().getLong (ChronoField.SECOND_OF_DAY )*1000),ZoneOffset.systemDefault ()));
                notice.setToDate (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getToDateL ()+OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getDayEndTimeL ()),ZoneOffset.systemDefault ()).toOffsetTime ().getLong (ChronoField.SECOND_OF_DAY )*1000),ZoneOffset.systemDefault ()));
                notice.setHotelId (request.getHotelId ( ));
                notice.setNeedWorkers (request.getNeedWorkers ( )  - request.getWorkerSet ().size ());
                notice.setType (2);
                notice.setStatus (0);
                notice.setHourPay (request.getHourlyPay ( ).toString ( ));
                notice.setContent (request.getTaskContent ());
                notice.setSettlementPeriod (request.getWorkerSettlementPeriod ( ).toString ( ));
                notice.setSettlementNum (request.getWorkerSettlementNum ( ).toString ( ));
                notice.setTaskId (task == null?"":task.getPid ());
                notice.setTaskTypeText (request.getTaskTypeText ( ));
                notice.setTaskTypeIcon (dictMapper.selectById (request.getTaskTypeCode ( )).getExtend ( ));
                noticeMapper.insert (notice);
                noticeServiceMapper.insert (notice.getPid ( ), request.getTaskTypeCode ( ));
                //
                List <TaskWorker> list = new ArrayList <> ( );
                for (String id : request.getWorkerSet ()) {
                    System.out.println (id);
                    TaskWorker taskWorker = new TaskWorker ( );
                    userMapper.queryByWorkerId (id);
                    User user = userMapper.queryByWorkerId (id);
                    taskWorker.setUserId (user.getPid ( ));
                    taskWorker.setWorkerId (user.getWorkerId ( ));
                    taskWorker.setUserName (user.getUsername ( ));
                    taskWorker.setStatus (0);
                    taskWorker.setFromDate (task.getFromDate ( ));
                    taskWorker.setToDate (task.getToDate ( ));
                    taskWorker.setHourlyPay (task.getHourlyPay ( ));
                    taskWorker.setTaskTypeCode (request.getTaskTypeCode ( ));
                    taskWorker.setTaskContent (task.getTaskContent ( ));
                    taskWorker.setTaskTypeText (task.getTaskTypeText ( ));
                    taskWorker.setHotelName (task.getHotelName ( ));
                    taskWorker.setHotelId (request.getHotelId ( ));
                    taskWorker.setDayStartTime (task.getDayStartTime ( ));
                    taskWorker.setDayEndTime (task.getDayEndTime ( ));
                    taskWorker.setHotelTaskId (task.getPid ( ));
                    taskWorker.setSettlementPeriod (request.getWorkerSettlementPeriod ());
                    taskWorker.setSettlementNum (request.getWorkerSettlementNum ());
                    taskWorker.setType (1);
                    taskWorkerMapper.insert (taskWorker);
                    list.add (taskWorker);
                }
                Task ts = new Task();
                ts.setHotelName (task.getHotelName ());
                ts.setTaskTypeText (task.getTaskTypeText ());
                ts.setHotelId (request.getHotelId ( ));
                ts.setPid (task.getPid ());
                messageService.hotelDistributeWorkerTask (list, ts, false);
            }
        } else if (request.getType ( ) == 2) {
            if (StringUtils.isEmpty (request.getHrCompanyId ( )) || StringUtils.isEmpty (request.getType ( )) || StringUtils.isEmpty (request.getHrNeedWorkers ( ))) {
                throw new ParamsException ("参数错误：hrId = " + request.getHrCompanyId ( ) + "type=" + request.getType ( ));
            }
            if (request.getService ( ).size ( ) == 0) {
                throw new ParamsException ("服务类型不能为空");
            }
            //人力发布招人公告
            notice = new Notice ( );
            notice.setCreateTime (OffsetDateTime.now ( ));
            notice.setFromDate (OffsetDateTime.ofInstant (Instant.ofEpochMilli (request.getFromDateL ( )), ZoneId.systemDefault ( )));
            notice.setToDate (OffsetDateTime.ofInstant (Instant.ofEpochMilli (request.getToDateL ( )), ZoneId.systemDefault ( )));
            notice.setHrCompanyId (request.getHrCompanyId ( ));
            notice.setNeedWorkers (request.getHrNeedWorkers ( ));
            notice.setType (4);
            notice.setStatus (0);
            notice.setContent (request.getContent ( ));
            if(request.getStatureDown ( ) == 0 && request.getStatureUp ( ) == 300){
                notice.setStature ("不限");
            }else{
                notice.setStature (request.getStatureDown ( ) + " - " + request.getStatureUp ( ));
            }
            notice.setStatureDown (request.getStatureDown ( ));
            notice.setStatureUp (request.getStatureUp ( ));
            if(request.getWeightDown ( ) == 0 && request.getWeightUp ( ) == 500){
                notice.setWeight ("不限");
            }else{
                notice.setWeight (request.getWeightDown ( ) + " - " + request.getWeightUp ( ));
            }
            notice.setWeightDown (request.getWeightDown ( ));
            notice.setWeightUp (request.getWeightUp ( ));
            notice.setSex (request.getSex ( ));
            notice.setHourPayRange (request.getHourPayRangeDown ( ) + " - " + request.getHourPayRangeUp ( ));
            notice.setHourlyPayDown (request.getHourPayRangeDown ( ));
            notice.setHourlyPayUp (request.getHourPayRangeUp ( ));
            notice.setHealthcard (request.getHealthcard ( ) == null || request.getHealthcard ( ) == 1 ? "不需要" : "需要");
            notice.setEducation (request.getEducation ( ) + "");
            noticeMapper.insert (notice);
            for (String sid : request.getService ( )) {
                noticeServiceMapper.insert (notice.getPid ( ), sid);
            }
        }
        return ResultDO.buildSuccess ("发布成功");
    }

    @Override
    public ResultDO queryNotice(Paginator paginator, QueryNoticeRequest request) {
        PageHelper.startPage (paginator.getPage ( ), paginator.getPageSize ( ));
        //查询数据集合
        if (request.getDate ( ) != null) {
            try {
                Integer year = Integer.parseInt (request.getDate ( ).split ("-")[0]);
                Integer month = Integer.parseInt (request.getDate ( ).split ("-")[1]);
                request.setFromDate (OffsetDateTime.of (year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC));
                if (month == 12) {
                    request.setToDate (OffsetDateTime.of (year + 1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
                } else {
                    request.setToDate (OffsetDateTime.of (year, month + 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
                }
            } catch (Exception e) {
                e.printStackTrace ( );
                throw new ParamsException ("时间格式错误");
            }
        }
        Task task;
        TaskHrCompany taskHrCompany;
        List <Notice> list = noticeMapper.queryList (request);
        Company company = new Company ( );
        for (Notice n : list) {
            if (n.getType ( ) < 3) {
                company = companyMapper.findCompanyById (n.getHotelId ( ));
                task = taskMapper.selectById (n.getTaskId ( ));
                if(n.getType ( ) != 2){
                    n.setHourPay (task.getHourlyPay ( ).toString ( ));
                    n.setTaskTypeText (task.getTaskTypeText ( ));
                    n.setTaskTypeIcon (dictMapper.selectById (task.getTaskTypeCode ( )).getExtend ( ));
                }
            } else if (n.getType ( ) == 3) {
                company = companyMapper.findCompanyById (n.getHrCompanyId ( ));
                taskHrCompany = taskHrCompanyMapper.selectById (n.getTaskId ( ));
                n.setHourPay (taskHrCompany.getHourlyPay ( ) + "");
                n.setTaskTypeText (taskHrCompany.getTaskTypeText ( ));
                n.setTaskTypeIcon (dictMapper.selectById (taskHrCompany.getTaskTypeCode ( )).getExtend ( ));
                n.setSettlementNum (taskHrCompany.getWorkerSettlementNum ( ).toString ( ));
                n.setSettlementPeriod (taskHrCompany.getWorkerSettlementPeriod ( ).toString ( ));
            } else if (n.getType ( ) == 4) {
                company = companyMapper.findCompanyById (n.getHrCompanyId ( ));
                n.setHourPay (n.getHourPayRange ( ));
            }
            n.setService (noticeServiceMapper.queryService (n.getPid ( )));
            n.setName (company.getName ( ));
            n.setAddress (company.getAddress ( ));
            n.setLogo (company.getLogo ( ));
            n.setService (noticeServiceMapper.queryService (n.getPid ( )));
            n.setCreateTimeL (n.getCreateTime ( ).getLong (ChronoField.INSTANT_SECONDS));
        }
        PageInfo <Notice> pageInfo = new PageInfo <> (list);
        HashMap <String, Object> result = new HashMap <> ( );
        //设置获取到的总记录数total：
        result.put ("total", pageInfo.getTotal ( ));
        //设置数据集合rows：
        result.put ("result", pageInfo.getList ( ));
        result.put ("page", paginator.getPage ( ));
        return ResultDO.buildSuccess (result);
    }

    @Override
    public ResultDO myselfNotice(Paginator paginator, QueryNoticeRequest request) {
        PageHelper.startPage (paginator.getPage ( ), paginator.getPageSize ( ));
        //查询数据集合
        if (request.getDate ( ) != null) {
            try {
                Integer year = Integer.parseInt (request.getDate ( ).split ("-")[0]);
                Integer month = Integer.parseInt (request.getDate ( ).split ("-")[1]);
                request.setFromDate (OffsetDateTime.of (year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC));
                if (month == 12) {
                    request.setToDate (OffsetDateTime.of (year + 1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
                } else {
                    request.setToDate (OffsetDateTime.of (year, month + 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
                }
            } catch (Exception e) {
                e.printStackTrace ( );
                throw new ParamsException ("时间格式错误");
            }
        }
        Task task;
        TaskHrCompany taskHrCompany;
        List <Notice> list = noticeMapper.queryMyList (request);
        Company company = new Company ( );
        for (Notice n : list) {
            if (n.getType ( ) < 3) {
                company = companyMapper.findCompanyById (n.getHotelId ( ));
                task = taskMapper.selectById (n.getTaskId ( ));
                if(n.getType ( ) != 2) {
                    n.setHourPay (task.getHourlyPay ( ).toString ( ));
                    n.setTaskTypeText (task.getTaskTypeText ( ));
                    n.setTaskTypeIcon (dictMapper.selectById (task.getTaskTypeCode ( )).getExtend ( ));
                }
            } else if (n.getType ( ) == 3) {
                company = companyMapper.findCompanyById (n.getHrCompanyId ( ));
                taskHrCompany = taskHrCompanyMapper.selectById (n.getTaskId ( ));
                n.setHourPay (taskHrCompany.getHourlyPay ( ) + "");
                n.setTaskTypeText (taskHrCompany.getTaskTypeText ( ));
                n.setTaskTypeIcon (dictMapper.selectById (taskHrCompany.getTaskTypeCode ( )).getExtend ( ));
                n.setSettlementNum (taskHrCompany.getWorkerSettlementNum ( ).toString ( ));
                n.setSettlementPeriod (taskHrCompany.getWorkerSettlementPeriod ( ).toString ( ));
            } else if (n.getType ( ) == 4) {
                company = companyMapper.findCompanyById (n.getHrCompanyId ( ));
                n.setHourPay (n.getHourPayRange ( ));
            }
            n.setService (noticeServiceMapper.queryService (n.getPid ( )));
            n.setName (company.getName ( ));
            n.setAddress (company.getAddress ( ));
            n.setLogo (company.getLogo ( ));
            n.setCreateTimeL (n.getCreateTime ( ).getLong (ChronoField.INSTANT_SECONDS));
            n.setEnrollWorkers (enrollMapper.selectEnrollNum (n.getPid ( )).toString ( ));
            n.setHaveHandle (enrollMapper.selectUnHandleEnrollNum (n.getPid ( ))>0);
        }
        PageInfo <Notice> pageInfo = new PageInfo <> (list);
        HashMap <String, Object> result = new HashMap <> ( );
        //设置获取到的总记录数total：
        result.put ("total", pageInfo.getTotal ( ));
        //设置数据集合rows：
        result.put ("result", pageInfo.getList ( ));
        result.put ("page", paginator.getPage ( ));
        return ResultDO.buildSuccess (result);
    }

    @Override
    public ResultDO acceptNotice(AcceptNoticeRequest request) {
        Notice notice = noticeMapper.selectById (request.getNoticeId ( ));
        if (notice == null) {
            throw new ParamsException ("参数错误");
        }
        if (notice.getType ( ) == 1) {
            if(notice.getFromDate ().isBefore (OffsetDateTime.now ())){
                return ResultDO.buildError ("任务已开始，无法报名");
            }
            Map<String,Object> map = new HashMap();
            map.put ("task_id",notice.getTaskId ());
            map.put ("hr_company_id",request.getHrCompanyId ());
            List<TaskHrCompany> ts = taskHrCompanyMapper.selectByMap (map);
            if(ts.size ()>0){
                return ResultDO.buildError ("已存在相应任务，无法报名");
            }
            return enrollService.hrApplyRegistration (request);
        } else if (notice.getType ( ) == 2) {
            if(notice.getFromDate ().isBefore (OffsetDateTime.now ())){
                return ResultDO.buildError ("任务已开始，无法报名");
            }
            if(notice.getToDate ().isBefore (OffsetDateTime.now ())){
                return ResultDO.buildError ("招聘已结束，无法报名");
            }
            Map<String,Object> map = new HashMap();
            map.put ("hotel_task_id",notice.getTaskId ());
            map.put ("worker_id",request.getWorkerId ());
            List<TaskWorker> ts = taskWorkerMapper.selectByMap (map);
            if(ts.size ()>0){
                return ResultDO.buildError ("已存在相应任务，无法报名");
            }
            return enrollService.workerApplyHotel (request);
        } else if (notice.getType ( ) == 3) {
            if(notice.getFromDate ().isBefore (OffsetDateTime.now ())){
                return ResultDO.buildError ("任务已开始，无法报名");
            }
            Map<String,Object> map = new HashMap();
            map.put ("task_hr_id",notice.getTaskId ());
            map.put ("worker_id",request.getWorkerId ());
            List<TaskWorker> ts = taskWorkerMapper.selectByMap (map);
            if(ts.size ()>0){
                return ResultDO.buildError ("已存在相应任务，无法报名");
            }
            return enrollService.workerApplyHr (request);
        } else {
            request.setHrCompanyId (notice.getHrCompanyId ());
            if(notice.getToDate ().isBefore (OffsetDateTime.now ())){
                return ResultDO.buildError ("招聘已结束，无法报名");
            }
            return enrollService.workerApplyRegistration (request);
        }
    }

    @Override
    public ResultDO recommendtNotice(QueryNoticeRequest request) {
        List <List <Notice>> ls = new ArrayList <> ( );
        int[] ary = {2, 3, 4};
        List <Notice> notice;
        Task task;
        TaskHrCompany taskHrCompany;
        Company company = new Company ( );
        for (int i = 0; i < 3; i++) {
            if(noticeMapper.queryRecommend (ary[i])!=null){
                notice = noticeMapper.queryRecommend (ary[i]);
            }else{
                notice = new ArrayList <> ();
            }
            for (Notice n : notice) {
                if (n.getType ( ) < 3) {
                    company = companyMapper.findCompanyById (n.getHotelId ( ));
                    task = taskMapper.selectById (n.getTaskId ( ));
                    if(n.getType ( ) != 2) {
                        n.setHourPay (task.getHourlyPay ( ).toString ( ));
                        n.setTaskTypeText (task.getTaskTypeText ( ));
                        n.setTaskTypeIcon (dictMapper.selectById (task.getTaskTypeCode ( )).getExtend ( ));
                    }
                } else if (n.getType ( ) == 3) {
                    company = companyMapper.findCompanyById (n.getHrCompanyId ( ));
                    taskHrCompany = taskHrCompanyMapper.selectById (n.getTaskId ( ));
                    n.setHourPay (taskHrCompany.getHourlyPay ( ) + "");
                    n.setTaskTypeText (taskHrCompany.getTaskTypeText ( ) + "");
                    n.setTaskTypeIcon (dictMapper.selectById (taskHrCompany.getTaskTypeCode ( )).getExtend ( ));
                    n.setSettlementNum (taskHrCompany.getWorkerSettlementNum ( ).toString ( ));
                    n.setSettlementPeriod (taskHrCompany.getWorkerSettlementPeriod ( ).toString ( ));
                } else if (n.getType ( ) == 4) {
                    company = companyMapper.findCompanyById (n.getHrCompanyId ( ));
                    n.setHourPay (n.getHourPayRange ( ));
                }
                n.setService (noticeServiceMapper.queryService (n.getPid ( )));
                n.setName (company.getName ( ) + "");
                n.setAddress (company.getAddress ( ) + "");
                n.setLogo (company.getLogo ( ) + "");
                n.setCreateTimeL (n.getCreateTime ( ).getLong (ChronoField.INSTANT_SECONDS));

            }
            ls.add (notice);
        }
        return ResultDO.buildSuccess (ls);
    }

    @Override
    public ResultDO detailsNotice(QueryNoticeRequest request) {
        System.out.println (request);
        if (StringUtils.isEmpty (request.getNoticeId ( ))) {
            throw new ParamsException ("公告ID为空");
        }
        Notice notice = noticeMapper.selectById (request.getNoticeId ( ));
        if (notice == null) {
            throw new ParamsException ("公告ID不存在");
        }
        if (notice.getType ( ) == 1 || notice.getType ( ) == 2) {//1:用人单位发布任务给人力  2:用人单位招聘小时工 3:人力发布任务给小时工 4:人力招聘小时工
            NoticeDetails noticeDetails = noticeMapper.queryDetailsHr (request);
            if(notice.getType ( ) == 2){
                noticeDetails.setTaskTypeText (notice.getTaskTypeText ());
                noticeDetails.setHourlyPay (notice.getHourPay ());
                noticeDetails.setCreateTime (notice.getCreateTimeL ());
                noticeDetails.setFromDate (notice.getFromDate ());
                noticeDetails.setToDate (notice.getToDate ());
                noticeDetails.setTaskContent (notice.getContent ());
                noticeDetails.setDayStartTime (notice.getFromDate ().toOffsetTime ());
                noticeDetails.setDayEndTime (notice.getToDate ().toOffsetTime ());
            }
            noticeDetails.setUserId (userMapper.findByMobile (companyMapper.findCompanyById (noticeDetails.getHotelId ()).getLeaderMobile ()).getPid ());
            return ResultDO.buildSuccess (noticeDetails);
        } else if (notice.getType ( ) == 3) {
            NoticeDetails noticeDetails = noticeMapper.queryDetailsWorker (request);
            noticeDetails.setUserId (userMapper.findByMobile (companyMapper.findCompanyById (noticeDetails.getHrcompanyId ()).getLeaderMobile ()).getPid ());
            return ResultDO.buildSuccess (noticeDetails);
        } else if (notice.getType ( ) == 4) {
            NoticeDetails noticeDetails = noticeMapper.selectDetailsApply (request);
            noticeDetails.setTaskServices (noticeServiceMapper.queryService (notice.getPid ( )));
            noticeDetails.setUserId (userMapper.findByMobile (companyMapper.findCompanyById (noticeDetails.getHrcompanyId ()).getLeaderMobile ()).getPid ());
            return ResultDO.buildSuccess (noticeDetails);
        }
        return null;
    }

    @Override
    public ResultDO detailsAccept(Paginator paginator, QueryNoticeRequest request) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        PageInfo<EnrollerResponse> pageInfo;
        HashMap<String,Object> result = new HashMap<>();
        System.out.println (request);
        if (StringUtils.isEmpty (request.getNoticeId ( ))) {
            throw new ParamsException ("noticeId为空");
        }
        if (StringUtils.isEmpty (request.getType ( ))) {
            throw new ParamsException ("type为空");
        }
        if (StringUtils.isEmpty (request.getStatus ( ))) {
            throw new ParamsException ("status为空");
        }
        Notice notice = noticeMapper.selectById (request.getNoticeId ());
        if(notice == null){
            throw new ParamsException ("noticeId错误");
        }

        if(request.getType ().equals ("1")){
            List<EnrollerResponse> list = enrollMapper.selectEnrollWorker(request.getNoticeId (),request.getStatus ());
            pageInfo = new PageInfo<>(list);
            //设置获取到的总记录数total：
            result.put("total",pageInfo.getTotal());
            //设置数据集合rows：
            result.put("result",pageInfo.getList());
            result.put("page",paginator.getPage());
            return ResultDO.buildSuccess (notice.getNeedWorkers () - notice.getConfirmedWorkers (),result,enrollMapper.selectEnrollCount (request.getNoticeId ()));
        }else if(request.getType ().equals ("2")){
            List<EnrollerResponse> list = enrollMapper.selectEnrollHr (request.getNoticeId (),request.getStatus ());
            pageInfo = new PageInfo<>(list);
            //设置获取到的总记录数total：
            result.put("total",pageInfo.getTotal());
            //设置数据集合rows：
            result.put("result",pageInfo.getList());
            result.put("page",paginator.getPage());
            return ResultDO.buildSuccess (notice.getNeedWorkers () - notice.getConfirmedWorkers (),result,enrollMapper.selectEnrollCount (request.getNoticeId ()));
        }
            return null;
    }

    @Override
    public ResultDO noticeHandle(NoticeHandleParam request) {
        if (request.getParam ( ).size ( ) == 0) {
            throw new ParamsException ("enrollId为空");
        }
        Enroll enroll;
        Notice notice = null;
        HrTaskDistributeRequest req = new HrTaskDistributeRequest ( );
        Set <String> set = new HashSet <> ( );
        String companyId = null;
        Set <TaskHrCompanyDTO> sct = new HashSet <> ( );
        int i = 0;
        TaskHrCompanyDTO tD = new TaskHrCompanyDTO();
        Inform inform = new Inform();
        String content;
        Task task = null;

        for (NoticeHandle param : request.getParam ( )) {
            enroll = enrollMapper.selectById (param.getEnrollId ( ));
            if (enroll == null) {
                throw new ParamsException ("noticeId有误");
            }
            if (notice == null) {
                notice = noticeMapper.selectById (enroll.getRequestId ( ));
            }
            if (notice == null) {
                return ResultDO.buildError ("");
            }
            if(notice.getTaskId ()!=null && task == null){
                task  = taskMapper.selectById (notice.getTaskId ());
            }
            if (notice.getStatus ( ) == 1) {
                return ResultDO.buildError ("");
            }
            if(i == 0){
                if(notice.getType () == 1 || notice.getType () == 3 || notice.getType () == 2){
                    if(notice.getFromDate ().isBefore (OffsetDateTime.now ())){
                        i++;
                    }
                }else if(notice.getType () == 4){
                    if(notice.getToDate ().isBefore (OffsetDateTime.now ())){
                        i++;
                    }
                }
            }
            if(i > 0){
                enroll.setStatus (2);
                enroll.setAssign (0);
                enrollMapper.updateById (enroll);
                if(notice.getType ( ) == 1 ){
                    //发送拒绝通知
                    try {
                        String mobile = companyMapper.findCompanyById (enroll.getHrCompanyId ()).getLeaderMobile ( );
                        jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (mobile, "报名已截止"));
                    } catch (APIConnectionException e) {
                        e.printStackTrace ( );
                    } catch (APIRequestException e) {
                        e.printStackTrace ( );
                    }
                    inform.setContent("报名已截止");
                    inform.setTitle("报名失败");
                    inform.setSendType(3);
                    inform.setAcceptType(2);
                    inform.setReceiveId(enroll.getHrCompanyId ());
                }else if(notice.getType ( ) == 2){
                    try {
                        jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (enroll.getWorkerId ()).getMobile (), "报名已截止"));
                    } catch (APIConnectionException e) {
                        e.printStackTrace ( );
                    } catch (APIRequestException e) {
                        e.printStackTrace ( );
                    }
                    inform.setContent("报名已截止");
                    inform.setTitle("报名失败");
                    inform.setSendType(3);
                    inform.setAcceptType(1);
                    inform.setReceiveId(enroll.getWorkerId ());
                }else{
                    try {
                        jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (enroll.getWorkerId ()).getMobile (), "报名已截止"));
                    } catch (APIConnectionException e) {
                        e.printStackTrace ( );
                    } catch (APIRequestException e) {
                        e.printStackTrace ( );
                    }
                    inform.setContent("报名已截止");
                    inform.setTitle("报名失败");
                    inform.setSendType(2);
                    inform.setAcceptType(1);
                    inform.setReceiveId(enroll.getWorkerId ());
                }
                informMapper.insertInform(inform);
                continue;
            }
            if(request.getStatus () == 1){
                enroll.setStatus (2);
                enroll.setAssign (0);
                enrollMapper.updateById (enroll);
                if(notice.getType ( ) == 1 ){
                    //发送拒绝通知
                    content = companyMapper.findCompanyById (notice.getHotelId ()).getName ()+"拒绝了你的报名申请：报名人数为"+param.getAllotWorkers ()+"人";
                    try {
                        jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (enroll.getHrCompanyId ()).getLeaderMobile ( ), content));
                    } catch (APIConnectionException e) {
                        e.printStackTrace ( );
                    } catch (APIRequestException e) {
                        e.printStackTrace ( );
                    }
                    inform.setContent(content);
                    inform.setTitle("报名被拒绝");
                    inform.setSendType(3);
                    inform.setAcceptType(2);
                    inform.setReceiveId(notice.getHrCompanyId ());
                }else if(notice.getType ( ) == 2){
                    content = companyMapper.findCompanyById (notice.getHotelId ()).getName ()+"拒绝了你的报名申请";
                    try {
                        jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (enroll.getWorkerId ()).getMobile (), content));
                    } catch (APIConnectionException e) {
                        e.printStackTrace ( );
                    } catch (APIRequestException e) {
                        e.printStackTrace ( );
                    }
                    inform.setContent(content);
                    inform.setTitle("报名被拒绝");
                    inform.setSendType(3);
                    inform.setAcceptType(1);
                    inform.setReceiveId(enroll.getWorkerId ());
                }else{
                    content = companyMapper.findCompanyById (notice.getHrCompanyId ()).getName ()+"拒绝了你的报名申请";
                    try {
                        jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (enroll.getWorkerId ()).getMobile (), content));
                    } catch (APIConnectionException e) {
                        e.printStackTrace ( );
                    } catch (APIRequestException e) {
                        e.printStackTrace ( );
                    }
                    inform.setContent(content);
                    inform.setTitle("报名被拒绝");
                    inform.setSendType(2);
                    inform.setAcceptType(1);
                    inform.setReceiveId(enroll.getWorkerId ());
                }
                informMapper.insertInform(inform);
                continue;

            }
            enroll.setStatus (1);
            enroll.setAssign (param.getAllotWorkers ( ));
            enrollMapper.updateById (enroll);
            //发送同意通知
            if(notice.getType ( ) == 1 ){
                //发送拒绝通知
                content = companyMapper.findCompanyById (notice.getHotelId ()).getName ()+"同意了你的报名申请：报名人数为"+param.getAllotWorkers ()+"人";
                try {
                    jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (enroll.getHrCompanyId ()).getLeaderMobile ( ), content));
                } catch (APIConnectionException e) {
                    e.printStackTrace ( );
                } catch (APIRequestException e) {
                    e.printStackTrace ( );
                }
                inform.setContent(content);
                inform.setTitle("报名成功");
                inform.setSendType(3);
                inform.setAcceptType(2);
                inform.setReceiveId(enroll.getHrCompanyId ());
            }else if(notice.getType ( ) == 2){
                content = companyMapper.findCompanyById (notice.getHotelId ()).getName ()+"同意了你的报名申请";
                try {
                    jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (enroll.getWorkerId ()).getMobile (), content));
                } catch (APIConnectionException e) {
                    e.printStackTrace ( );
                } catch (APIRequestException e) {
                    e.printStackTrace ( );
                }
                inform.setContent(content);
                inform.setTitle("报名成功");
                inform.setSendType(3);
                inform.setAcceptType(1);
                inform.setReceiveId(enroll.getWorkerId ());
            }else{
                content = companyMapper.findCompanyById (notice.getHrCompanyId ()).getName ()+"同意了你的报名申请";
                try {
                    jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (userMapper.queryByWorkerId (enroll.getWorkerId ()).getMobile (), content));
                } catch (APIConnectionException e) {
                    e.printStackTrace ( );
                } catch (APIRequestException e) {
                    e.printStackTrace ( );
                }
                inform.setContent(content);
                inform.setTitle("报名成功");
                inform.setSendType(2);
                inform.setAcceptType(1);
                inform.setReceiveId(enroll.getWorkerId ());
            }
            //添加合作伙伴
            if (companyId == null) {
                if (notice.getType ( ) == 3 || notice.getType ( ) == 4) {
                    companyId = notice.getHrCompanyId ( );
                } else if (notice.getType ( ) == 1 || notice.getType ( ) == 2) {
                    companyId = notice.getHotelId ( );
                }
            }
            if(notice.getType () != 1){
                UserCompany userCompany = userCompanyMapper.selectByWorkerIdHrId (companyId, enroll.getWorkerId ( ));
                if (userCompany == null) {
                    userCompany = new UserCompany ();
                    //从redis中查询是否有协议
                    String path = redisUtil.getString ("defaultWorkerHrProtocol");
                    if (StringUtils.isEmpty (path)) {
                        try {
                            path = filePush.pushFileToServer (ConstantData.CATALOG.getName ( ), ConstantData.WORKHRPROTOCOL.getName ( ));
                        } catch (Exception e) {
                            e.printStackTrace ( );
                            return ResultDO.buildError ("服务异常");
                        }
                        redisUtil.setString ("defaultWorkerHrProtocol", path);
                    }
                    userCompany.setCompanyId (companyId);
                    userCompany.setUserType (UserType.worker);
                    userCompany.setCompanyType (2);
                    userCompany.setUserId (userMapper.selectByWorkerId (enroll.getWorkerId ( )).getPid ( ));
                    userCompany.setStatus (1);
                    userCompany.setBindProtocol (path);
                    userCompanyMapper.insert (userCompany);
                } else if (userCompany.getStatus ( ) == 0) {
                    userCompany.setStatus (1);
                    //清楚消息中的申请绑定待处理
                    messageMapper.updateAllBind(enroll.getWorkerId ( ),companyId);
                } else if (userCompany.getStatus ( ) == 2) {
                    userCompany.setStatus (1);
                } else if (userCompany.getStatus ( ) == 4) {
                    userCompany.setStatus (1);
                }
                notice.setConfirmedWorkers (notice.getConfirmedWorkers ( ) + 1);
                if (notice.getConfirmedWorkers ( ) > notice.getNeedWorkers ( )) {
                    return ResultDO.buildError ("");
                }
                userCompanyMapper.updateById (userCompany);
            }else {
                HotelHrCompany hotelHrCompany = hotelHrCompanyMapper.selectByHrHotelId (enroll.getHrCompanyId (),notice.getHotelId ());
                tD.setHrCompanyId (enroll.getHrCompanyId ());
                tD.setNeedWorkers (param.getAllotWorkers ());
                sct.add (tD);
                if(hotelHrCompany == null){
                    hotelHrCompany = new HotelHrCompany ();
                    hotelHrCompany.setStatus (0);
                    hotelHrCompany.setBindType (1);
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
                    hotelHrCompany.setBindProtocol (path);
                    hotelHrCompany.setHotelId (notice.getHotelId ());
                    hotelHrCompany.setHrId (enroll.getHrCompanyId ());
                    hotelHrCompany.setBindTime (OffsetDateTime.now ());
                    hotelHrCompanyMapper.insert (hotelHrCompany);
                }else if(hotelHrCompany.getStatus () == 1){//合作过 关系变成合作
                    hotelHrCompany.setStatus (0);
                }else if(hotelHrCompany.getStatus () == 3){//待审核 关系变成合作
                    hotelHrCompany.setStatus (0);
                    Map<String,Object> map = new HashMap <> ();
                    map.put("messageType",13);
                    map.put("hrCompanyId",enroll.getHrCompanyId ());
                    map.put("hotelId",notice.getHotelId ());
                    List<Message> mes1 = messageService.selectByMap (map);
                    if(mes1.size ()!=0){
                        for (Message ms:mes1) {
                            ms.setStatus (1);
                        }
                    }
                    messageService.updateBatchById (mes1);
                }else if(hotelHrCompany.getStatus () == 4){
                    hotelHrCompany.setStatus (0);
                }
                notice.setConfirmedWorkers (notice.getConfirmedWorkers ()+param.getAllotWorkers ());
                if(notice.getConfirmedWorkers () > notice.getNeedWorkers ()){
                    return ResultDO.buildError ("报名人数超过剩余所需人数");
                }
                hotelHrCompanyMapper.updateById (hotelHrCompany);
            }
            if (notice.getNeedWorkers ( ) == notice.getConfirmedWorkers ( )) {
                notice.setStatus (1);
                enrollMapper.refuseByNoticeId (notice.getPid ( ));
            }
            noticeMapper.updateById (notice);
            if (notice.getType ( ) == 3 || notice.getType ( ) == 2) {
                //派发任务
                req.setMessageId (null);
                req.setHrTaskId (notice.getTaskId ( ));
                req.setNoticeTask (true);
                set.add (enroll.getWorkerId ( ));
            }
        }
        req.setWorkerIds (set);
        if (set.size ( ) > 0 && notice.getType ( ) == 3) {
            taskHrCompanyService.TaskHrDistribute (req);
        }else if(set.size ( ) > 0 && notice.getType ( ) == 2){
            List <TaskWorker> list = new ArrayList <> ( );
            for (String id : set) {
                System.out.println (id);
                TaskWorker taskWorker = new TaskWorker ( );
                userMapper.queryByWorkerId (id);
                User user = userMapper.queryByWorkerId (id);
                taskWorker.setUserId (user.getPid ( ));
                taskWorker.setWorkerId (user.getWorkerId ( ));
                taskWorker.setUserName (user.getUsername ( ));
                taskWorker.setStatus (1);
                taskWorker.setFromDate (task.getFromDate ( ));
                taskWorker.setToDate (task.getToDate ( ));
                taskWorker.setHourlyPay (task.getHourlyPay ( ));
                taskWorker.setTaskTypeCode (task.getTaskTypeCode ( ));
                taskWorker.setTaskContent (task.getTaskContent ( ));
                taskWorker.setTaskTypeText (task.getTaskTypeText ( ));
                taskWorker.setHotelName (task.getHotelName ( ));
                taskWorker.setHotelId (task.getHotelId ( ));
                taskWorker.setDayStartTime (task.getDayStartTime ( ));
                taskWorker.setDayEndTime (task.getDayEndTime ( ));
                taskWorker.setHotelTaskId (task.getPid ( ));
                taskWorker.setSettlementPeriod (task.getWorkerSettlementPeriod ());
                taskWorker.setSettlementNum (task.getWorkerSettlementNum ());
                taskWorker.setType (1);
                taskWorkerMapper.insert (taskWorker);
                list.add (taskWorker);
            }
            //messageService.hotelDistributeWorkerTask (list, task, false).getPid ( );
        }
        CreateTaskRequest createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setNoticeTask (true);
        createTaskRequest.setHrCompanySet (sct);
        if(sct.size ()>0){
            if(task!=null){
                AddHrTask(task,createTaskRequest);
            }else{
                throw new ParamsException ("公告数据异常");
            }

        }
        if(i>0){
            return ResultDO.buildSuccess ("报名已截止");
        }
        return ResultDO.buildSuccess ("处理成功");
    }

    @Override
    public ResultDO enrollHandle(Paginator paginator, QueryNoticeRequest request) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        PageInfo<EnrollDetails> pageInfo;
        String userId;
        if(request.getType ().equals ("1")){
            List<EnrollDetails> list = enrollMapper.selectEnrollDetails(request);
            pageInfo = new PageInfo<>(list);

            for (EnrollDetails e:list) {
                if(e.getType () == 2){
                    userId = userMapper.findByMobile (companyMapper.findCompanyById (e.getHotelId ()).getLeaderMobile ()).getPid ();
                }else if(e.getType () == 3){
                    userId = userMapper.findByMobile (companyMapper.findCompanyById (e.getHrId ()).getLeaderMobile ()).getPid ();
                }else{
                    userId = userMapper.findByMobile (companyMapper.findCompanyById (e.getHrId ()).getLeaderMobile ()).getPid ();
                    e.setService (noticeServiceMapper.queryService (e.getNoticeId ()));
                }
                e.setUserId (userId);
            }
        }else{
            List<EnrollDetails> list = enrollMapper.selecthrEnrollDetails(request);
            for (EnrollDetails e:list) {
                userId = userMapper.findByMobile (companyMapper.findCompanyById (e.getHotelId ()).getLeaderMobile ()).getPid ();
                e.setUserId (userId);
            }
            pageInfo = new PageInfo<>(list);
        }
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess(result);
    }

    @Override
    public ResultDO recommendWorker(String id) {
        WorkerQueryDTO workerQueryDTO = new WorkerQueryDTO();
        workerQueryDTO.setHotelId (id);
        List<Map<String, Object>> map = workerMapper.queryRecommendWorkers (workerQueryDTO);
        for (Map<String, Object> mp:map) {
            String str = "";
            if (mp.get("birthday") != null) {
                str = mp.get("birthday").toString().substring(0, 10);
            }
            mp.put("birthday", str);
            List l1 = dictService.findServiceArea(mp.get("workerId").toString ());
            List l2 = dictMapper.queryTypeByUserId(mp.get("workerId").toString ());
            mp.put("areaCode", l1 == null ? new ArrayList<>() : l1);
            mp.put("serviceType", l2 == null ? new ArrayList<>() : l2);
        }

        return ResultDO.buildSuccess (map);
    }

    //循环添加人力资源任务
    private  Set<TaskHrCompany> AddHrTask(Task task,CreateTaskRequest createTaskRequest ){
        Set setHrTask= new HashSet<TaskHrCompany>();
        for(TaskHrCompanyDTO hrCompanyDTO:createTaskRequest.getHrCompanySet()){
            if (!StringUtils.hasLength(String.valueOf(hrCompanyDTO.getNeedWorkers())) ||hrCompanyDTO.getNeedWorkers()==0) {
                throw new ParamsException("任务需要的人数不能为空");
            }
            Company hotel=companyMapper.findCompanyById(task.getHotelId());
            Company hrCompany=companyMapper.findCompanyById(hrCompanyDTO.getHrCompanyId());
            if(hrCompany==null){
                throw new ParamsException("编号："+hrCompanyDTO.getHrCompanyId()+"人力资源公司不存在");
            }
            if(hrCompany.getStatus()==null ||hrCompany.getStatus()!=1){
                throw new ParamsException(hrCompany.getName()+":人力资源公司状态不是已审核");
            }

            TaskHrCompany taskHrCompany=new TaskHrCompany();
            taskHrCompany.setHrCompanyId(hrCompany.getPid());
            taskHrCompany.setHrCompanyName(hrCompany.getName());
            taskHrCompany.setHotelId(hotel.getPid());
            taskHrCompany.setHotelName(hotel.getName());
            taskHrCompany.setTaskId(task.getPid());
            if(createTaskRequest.isNoticeTask ()){
                taskHrCompany.setStatus(2);
            }else{
                taskHrCompany.setStatus(1);
            }
            taskHrCompany.setRefusedWorkers(0);
            taskHrCompany.setConfirmedWorkers(0);
            taskHrCompany.setNeedWorkers(hrCompanyDTO.getNeedWorkers());
            taskHrCompany.setDeleted(false);
            taskHrCompany.setTaskTypeText(task.getTaskTypeText());
            taskHrCompany.setTaskTypeCode(task.getTaskTypeCode());
            taskHrCompany.setTaskContent(task.getTaskContent());
            taskHrCompany.setHourlyPayHotel(task.getHourlyPay());
            taskHrCompany.setFromDate (task.getFromDate ());
            taskHrCompany.setToDate (task.getToDate ());
            taskHrCompany.setDayStartTime (task.getDayStartTime ());
            taskHrCompany.setDayEndTime (task.getDayEndTime ());
            taskHrCompany.setDistributeWorkers (0);
            taskHrCompany.setSettlementNum (task.getSettlementNum ());
            taskHrCompany.setSettlementPeriod (task.getSettlementPeriod ());
            taskHrCompany.setWorkerSettlementPeriod (0);
            taskHrCompany.setWorkerSettlementNum (0);
            taskHrCompanyMapper.insert(taskHrCompany);
            //生成一个待派发的消息
            if(!createTaskRequest.isNoticeTask ()){
                Map <String, Object> param = new HashMap <> ( );
                param.put ("hrCompanyId", taskHrCompany.getHrCompanyId ( ));
                param.put ("hotelId", taskHrCompany.getHotelId ( ));
                param.put ("applicantType", 2);
                param.put ("applyType", 2);
                param.put ("hrTaskId", taskHrCompany.getPid ( ));
                param.put ("taskId", taskHrCompany.getTaskId ( ));
                param.put ("messageType", 11);
                param.put ("messageCode", "awaitSendMessage");
                messageService.sendMessageInfo (param);
            }
            setHrTask.add(taskHrCompany);
        }
        return setHrTask;
    }

}
