package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.ServiceComment;
import com.microdev.param.CommentResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceCommentMapper extends BaseMapper<ServiceComment> {

    int saveInfo(ServiceComment serviceComment);

    List<CommentResponse> selectHotelCommentInfo(@Param("hotelId") String hotelId);

    List<CommentResponse> selectHrCommentInfo(@Param("hrId") String hrId);

    List<CommentResponse> selectWorkerCommentInfo(@Param("workerId") String workerId);
}
