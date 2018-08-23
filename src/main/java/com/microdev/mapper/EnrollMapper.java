package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Enroll;
import com.microdev.param.EnrollDetails;
import com.microdev.param.EnrollerResponse;
import com.microdev.param.QueryNoticeRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface EnrollMapper extends BaseMapper<Enroll> {
    Integer selectEnrollNum(String id);

    Integer selectUnHandleEnrollNum(String id);

    List<EnrollerResponse> selectEnrollWorker(@Param ("noticeId") String noticeId,@Param ("type") Integer type);

    Map selectEnrollCount(String noticeId);

    List<EnrollerResponse> selectEnrollHr(@Param ("noticeId") String noticeId,@Param ("type") Integer type);

    void refuseByNoticeId(String id);

    Integer selectCountNum(@Param ("id") String id,@Param ("type") Integer type);

    List<EnrollDetails> selectEnrollDetails(QueryNoticeRequest request);

    List<EnrollDetails> selecthrEnrollDetails(QueryNoticeRequest request);
}
