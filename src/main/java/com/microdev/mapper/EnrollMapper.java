package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Enroll;
import com.microdev.param.EnrollerResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollMapper extends BaseMapper<Enroll> {
    Integer selectEnrollNum(String id);

    List<EnrollerResponse> selectEnrollWorker(@Param ("noticeId") String noticeId,@Param ("type") Integer type);

    List<EnrollerResponse> selectEnrollHr(@Param ("noticeId") String noticeId,@Param ("type") Integer type);

    void refuseByNoticeId(String id);
}
