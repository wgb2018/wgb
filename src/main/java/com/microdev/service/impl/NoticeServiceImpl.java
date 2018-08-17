package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.FilePush;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
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
            if(request.getHrNeedWorkers ( ) > 0){
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
                req.setNeedhrCompanys (request.getHrNeedWorkers ( ) + request.getNeedWorkers ( ));
                req.setHourlyPay (request.getHourlyPay ( ));
                req.setHrCompanySet (request.getHrCompanySet ( ));
                //发布用人单位任务
                ResultDO rs = taskService.createTask (req);
                task = (TaskViewDTO) rs.getData ( );
            }

            if (request.getHrNeedWorkers ( ) > 0) {
                if (request.getHrCompanySet ( ).size ( ) < request.getHrNeedWorkers ( )) {
                    //发布用人单位派发人力任务公告
                    notice = new Notice ( );
                    notice.setCreateTime (OffsetDateTime.now ( ));
                    notice.setFromDate (OffsetDateTime.ofInstant (Instant.ofEpochMilli (request.getFromDateL ( )), ZoneId.systemDefault ( )));
                    notice.setToDate (OffsetDateTime.ofInstant (Instant.ofEpochMilli (request.getToDateL ( )), ZoneId.systemDefault ( )));
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
                    notice.setContent (request.getContent ());
                    notice.setHourPay (request.getHourlyPay ( ).toString ( ));
                    notice.setTaskId (task.getPid ( ));
                    notice.setTaskTypeText (request.getTaskTypeText ( ));
                    notice.setTaskTypeIcon (dictMapper.selectById (request.getTaskTypeCode ( )).getExtend ( ));
                    if (notice.getNeedWorkers ( ) > 0) {
                        noticeMapper.insert (notice);
                    }
                }

            }
            if (request.getNeedWorkers ( ) > 0) {
                //发布用人单位招聘小时工公告
                notice = new Notice ( );
                notice.setCreateTime (OffsetDateTime.now ( ));
                notice.setFromDate (OffsetDateTime.ofInstant (Instant.ofEpochMilli (request.getFromDateL ( )), ZoneId.systemDefault ( )));
                notice.setToDate (OffsetDateTime.ofInstant (Instant.ofEpochMilli (request.getToDateL ( )), ZoneId.systemDefault ( )));
                notice.setHotelId (request.getHotelId ( ));
                notice.setNeedWorkers (request.getNeedWorkers ( ));
                notice.setType (2);
                notice.setStatus (0);
                notice.setHourPay (request.getHourlyPay ( ).toString ( ));
                notice.setContent (request.getContent ());
                notice.setSettlementPeriod (request.getWorkerSettlementPeriod ( ).toString ( ));
                notice.setSettlementNum (request.getWorkerSettlementNum ( ).toString ( ));
                notice.setTaskId (task == null?"":task.getPid ());
                notice.setTaskTypeText (request.getTaskTypeText ( ));
                notice.setTaskTypeIcon (dictMapper.selectById (request.getTaskTypeCode ( )).getExtend ( ));
                noticeMapper.insert (notice);
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
            notice.setSex (request.getSex ( ).toDBString());
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
            return enrollService.hrApplyRegistration (request);
        } else if (notice.getType ( ) == 2) {
            return enrollService.workerApplyHotel (request);
        } else if (notice.getType ( ) == 3) {
            return enrollService.workerApplyHr (request);
        } else {
            request.setHrCompanyId (notice.getHrCompanyId ());
            return enrollService.workerApplyRegistration (request);
        }
    }

    @Override
    public ResultDO recommendtNotice(QueryNoticeRequest request) {
        List <List <Notice>> ls = new ArrayList <> ( );
        int[] ary = {2, 3, 4, 1};
        List <Notice> notice;
        Task task;
        TaskHrCompany taskHrCompany;
        Company company = new Company ( );
        for (int i = 0; i < 4; i++) {
            notice = noticeMapper.queryRecommend (ary[i]);
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
            return ResultDO.buildSuccess (noticeDetails);
        } else if (notice.getType ( ) == 3) {
            return ResultDO.buildSuccess (noticeMapper.queryDetailsWorker (request));
        } else if (notice.getType ( ) == 4) {
            NoticeDetails noticeDetails = noticeMapper.selectDetailsApply (request);
            noticeDetails.setTaskServices (noticeServiceMapper.queryService (notice.getPid ( )));
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
        TaskHrCompanyDTO tD = new TaskHrCompanyDTO();
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
            if (notice.getStatus ( ) == 1) {
                return ResultDO.buildError ("");
            }
            enroll.setStatus (1);
            enroll.setAssign (param.getAllotWorkers ( ));
            enrollMapper.updateById (enroll);
            //添加合作伙伴
            if (companyId != null) {
                if (notice.getType ( ) == 3 || notice.getType ( ) == 4) {
                    companyId = notice.getHrCompanyId ( );
                } else if (notice.getType ( ) == 1 || notice.getType ( ) == 2) {
                    companyId = notice.getHotelId ( );
                }
            }
            if(notice.getType () != 1){
                UserCompany userCompany = userCompanyMapper.selectByWorkerIdHrId (companyId, enroll.getWorkerId ( ));
                if (userCompany == null) {
                    List <UserCompany> userCompanyList = new ArrayList <> ( );
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
                    userCompany.setStatus (0);
                    userCompany.setBindProtocol (path);
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
            if (notice.getType ( ) == 3) {
                //派发任务
                req.setMessageId (null);
                req.setHrTaskId (notice.getTaskId ( ));
                set.add (enroll.getWorkerId ( ));
            }
        }
        req.setWorkerIds (set);
        if (set.size ( ) > 0) {
            taskHrCompanyService.TaskHrDistribute (req);
        }
        CreateTaskRequest createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setNoticeTask (true);
        createTaskRequest.setHrCompanySet (sct);
        if(sct.size ()>0){
            AddHrTask(taskMapper.selectById (notice.getTaskId ()),createTaskRequest);
        }
        return ResultDO.buildSuccess ("处理成功");
    }

    @Override
    public ResultDO enrollHandle(Paginator paginator, QueryNoticeRequest request) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        PageInfo<EnrollDetails> pageInfo;
        if(request.getType ().equals ("1")){
            List<EnrollDetails> list = enrollMapper.selectEnrollDetails(request);
            pageInfo = new PageInfo<>(list);
            for (EnrollDetails e:list) {
                if(e.getType () == 4){
                    e.setService (noticeServiceMapper.queryService (e.getNoticeId ()));
                }
            }
        }else{
            List<EnrollDetails> list = enrollMapper.selecthrEnrollDetails(request);
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


            setHrTask.add(taskHrCompany);
        }
        return setHrTask;
    }

}
