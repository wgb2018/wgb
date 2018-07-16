package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Notice;
import com.microdev.param.QueryNoticeRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeMapper extends BaseMapper<Notice> {
    List<Notice> queryList(QueryNoticeRequest request);
}
