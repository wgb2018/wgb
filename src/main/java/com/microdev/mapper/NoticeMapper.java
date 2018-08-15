package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Notice;
import com.microdev.param.NoticeDetails;
import com.microdev.param.QueryNoticeRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeMapper extends BaseMapper<Notice> {
    List<Notice> queryList(QueryNoticeRequest request);

    List<Notice> queryMyList(QueryNoticeRequest request);

    List<Notice> queryRecommend(Integer type);

    NoticeDetails queryDetailsHr(QueryNoticeRequest request);

    NoticeDetails queryDetailsWorker(QueryNoticeRequest request);

    NoticeDetails selectDetailsApply(QueryNoticeRequest request);
}
