package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.User;
import com.microdev.param.UserDTO;
import com.microdev.param.UserTaskResponse;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper extends BaseMapper<User> {

    public List<User> query(UserDTO user) throws Exception;

    public User findByMobile(String mobile);

    public User queryByWorkerId(String id);

    public User queryByUserId(String userId);

    void update(User user);

    @Select("SELECT u.username AS username, u.sex AS sex,u.birthday AS birthday,u.mobile AS mobile,w.health_card AS healthCard"+
            " FROM (SELECT * FROM USER WHERE id = #{id}) u INNER JOIN worker w ON u.worker_id = w.id")
    UserTaskResponse selectUserInfo(String id);

    User selectByWorkerId(String workerId);

    List<String> selectIdByWorkerId(@Param("set") List<String> set);
}
