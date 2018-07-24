package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.ServiceComment;
import com.microdev.param.ApplyParamDTO;
import com.microdev.param.CommentResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ServiceCommentMapper extends BaseMapper<ServiceComment> {

    int saveInfo(ServiceComment serviceComment);

    List<CommentResponse> selectHotelCommentInfo(ApplyParamDTO dto);

    List<CommentResponse> selectHrCommentInfo(ApplyParamDTO dto);

    List<CommentResponse> selectWorkerCommentInfo(ApplyParamDTO dto);

    List<CommentResponse> selectCommentInfoPc(ApplyParamDTO dto);
}
