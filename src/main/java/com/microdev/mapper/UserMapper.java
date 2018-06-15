package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.FeedBack;
import com.microdev.model.User;
import com.microdev.param.FeedbackQueryDTO;
import com.microdev.param.UserDTO;
import com.microdev.param.UserTaskResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper extends BaseMapper<User> {

    public List<User> query(UserDTO user) throws Exception;

    public User findByMobile(String mobile);

    public User queryByWorkerId(String id);

    public User queryByUserId(String userId);

    void update(User user);

    UserTaskResponse selectUserInfo(@Param("workerId") String workerId);

    User selectByWorkerId(String workerId);

    List<String> selectIdByWorkerId(@Param("set") List<String> set);

    List<FeedBack> queryFreeback(FeedbackQueryDTO feedbackQueryDTO);
}
