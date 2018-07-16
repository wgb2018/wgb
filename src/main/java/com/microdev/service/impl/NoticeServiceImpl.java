package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.mapper.NoticeMapper;
import com.microdev.mapper.NoticeServiceMapper;
import com.microdev.model.Notice;
import com.microdev.param.CreateNoticeRequest;
import com.microdev.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;

@Transactional
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper,Notice> implements NoticeService {
    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private NoticeServiceMapper noticeServiceMapper;
    @Override
    public ResultDO createNotice(CreateNoticeRequest request) {
        System.out.println (request);
        if (StringUtils.isEmpty(request.getFromDateL ()) || StringUtils.isEmpty(request.getToDateL ()) || StringUtils.isEmpty(request.getHotelId ()) || StringUtils.isEmpty(request.getNeedWorkers ())) {
            throw new ParamsException ("参数不能为空");
        }
        if(request.getService ().size ()==0){
            throw new ParamsException ("服务类型不能为空");
        }
        Notice notice = new Notice();
        notice.setContent (request.getContent ());
        notice.setFromDate (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getFromDateL ()),ZoneId.systemDefault ()));
        notice.setToDate (OffsetDateTime. ofInstant (Instant.ofEpochMilli (request.getToDateL ()),ZoneId.systemDefault ()));
        notice.setHotelId (request.getHotelId ());
        notice.setNeedworkers (request.getNeedWorkers ());
        noticeMapper.insert (notice);
        for (String sid:request.getService ()) {
            noticeServiceMapper.insert (notice.getPid (),sid);
        }
        return ResultDO.buildSuccess ("发布成功");
    }
}
