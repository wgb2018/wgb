package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.mapper.CompanyMapper;
import com.microdev.mapper.NoticeMapper;
import com.microdev.mapper.NoticeServiceMapper;
import com.microdev.model.Notice;
import com.microdev.param.CreateNoticeRequest;
import com.microdev.param.CreateTaskRequest;
import com.microdev.param.QueryNoticeRequest;
import com.microdev.service.NoticeService;
import com.microdev.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.List;

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
    @Override
    public ResultDO createNotice(CreateNoticeRequest request) {

        if (StringUtils.isEmpty(request.getFromDateL ()) || StringUtils.isEmpty(request.getToDateL ()) || StringUtils.isEmpty(request.getHotelId ()) || StringUtils.isEmpty(request.getNeedWorkers ())) {
            throw new ParamsException ("参数不能为空");
        }
        /*if(request.getService ().size ()==0){
            throw new ParamsException ("服务类型不能为空");
        }*/
        Notice notice = new Notice();
        notice.setCreateTime (OffsetDateTime.now ());
        notice.setContent (request.getContent ());
        notice.setFromDate (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getFromDateL ()),ZoneId.systemDefault ()));
        notice.setToDate (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getToDateL ()),ZoneId.systemDefault ()));
        notice.setHotelId (request.getHotelId ());
        notice.setNeedWorkers (request.getNeedWorkers ());
        notice.setHrNeedWorkers (request.getHrNeedWorkers ());
        if(request.getHrCompanySet ().size () == 0){
            notice.setType (1);
            notice.setStatus (0);
            //发布小时工公告
        }else if(request.getWorkerSet ().size () == 0){
            CreateTaskRequest req = new CreateTaskRequest();
            req.setFromDate (notice.getFromDate ());
            req.setToDate (notice.getToDate ());
            req.setHotelId (notice.getHotelId ());
            req.setTaskContent (request.getTaskContent ());
            req.setTaskTypeCode (request.getTaskTypeCode ());
            req.setTaskTypeText (request.getTaskTypeText ());
            req.setSettlementPeriod (request.getSettlementPeriod ());
            req.setSettlementNum (request.getSettlementNum ());
            //发布酒店任务
            taskService.createTask (req);
            notice.setType (2);
        }else{
            CreateTaskRequest req = new CreateTaskRequest();
            req.setFromDate (notice.getFromDate ());
            req.setToDate (notice.getToDate ());
            req.setHotelId (notice.getHotelId ());
            req.setTaskContent (request.getTaskContent ());
            req.setTaskTypeCode (request.getTaskTypeCode ());
            req.setTaskTypeText (request.getTaskTypeText ());
            req.setSettlementPeriod (request.getSettlementPeriod ());
            req.setSettlementNum (request.getSettlementNum ());
            //发布酒店任务
            taskService.createTask (req);
            notice.setType (3);
        }




        noticeMapper.insert (notice);
        for (String sid:request.getService ()) {
            noticeServiceMapper.insert (notice.getPid (),sid);
        }
        return ResultDO.buildSuccess ("发布成功");
    }

    @Override
    public ResultDO queryNotice(Paginator paginator, QueryNoticeRequest request) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合

        if(request.getDate ()!=null){
            try{
                Integer year = Integer.parseInt (request.getDate ().split ("-")[0]);
                Integer month = Integer.parseInt (request.getDate ().split ("-")[1]);
                request.setFromDate (OffsetDateTime.of (year,month,1,0,0,0,0, ZoneOffset.UTC));
                if(month == 12){
                    request.setToDate (OffsetDateTime.of (year+1,1,1,0,0,0,0, ZoneOffset.UTC));
                }else{
                    request.setToDate (OffsetDateTime.of (year,month+1,1,0,0,0,0, ZoneOffset.UTC));
                }
            }catch(Exception e){
                e.printStackTrace ();
                throw new ParamsException ("时间格式错误");
            }
        }
        List<Notice> list = noticeMapper.queryList(request);
        for (Notice n:list) {
             n.setHotel (companyMapper.findCompanyById (n.getHotelId ()));
             n.setService (noticeServiceMapper.queryService(n.getPid ()));
             n.setCreateTimeL (n.getCreateTime ().getLong (ChronoField.INSTANT_SECONDS));
        }
        PageInfo<Notice> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess (result);
    }
}
