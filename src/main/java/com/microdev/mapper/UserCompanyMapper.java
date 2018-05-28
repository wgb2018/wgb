package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.TaskWorker;
import com.microdev.model.User;
import com.microdev.model.UserCompany;
import com.microdev.param.HrQueryWorkerDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCompanyMapper extends BaseMapper<UserCompany> {

    void addNoPromiseTasks(@Param("userId") String userId, @Param("companyId") String companyId);

    //获取绑定记录是否存在
    UserCompany findOneUserCompany(@Param("companyId") String companyId, @Param("userId")String userId);

    void save(UserCompany userCompany);

    void update(UserCompany userCompany);

    List<User> getSelectableWorker(HrQueryWorkerDTO queryDTO);

    List<TaskWorker> getUnSelectableWorker(HrQueryWorkerDTO queryDTO);



}
