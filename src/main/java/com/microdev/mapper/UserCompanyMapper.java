package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.TaskWorker;
import com.microdev.model.User;
import com.microdev.model.UserCompany;
import com.microdev.param.HrQueryWorkerDTO;
import com.microdev.param.WorkerBindCompany;
import com.microdev.param.WorkerCooperate;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface UserCompanyMapper extends BaseMapper<UserCompany> {

    void addNoPromiseTasks(@Param("userId") String userId, @Param("companyId") String companyId);

    //获取绑定记录是否存在
    UserCompany findOneUserCompany(@Param("companyId") String companyId, @Param("userId")String userId);

    void save(UserCompany userCompany);

    void update(UserCompany userCompany);

	List<User> getSelectableWorker(HrQueryWorkerDTO queryDTO);

    List<User> getSelectableWorkerH(HrQueryWorkerDTO queryDTO);

    List<UserCompany> selectAllWorker(HrQueryWorkerDTO queryDTO);

    List<TaskWorker> getUnSelectableWorker(HrQueryWorkerDTO queryDTO);

    List<WorkerBindCompany> selectHrCompanyByUserId(@Param("workerId") String userId,@Param("type") Integer type,@Param("name") String name);

    int saveBatch(List<UserCompany> list);

    int selectWorkerBindCount(String userId);

    int selectIsbind(@Param("companyId") String companyId,@Param("set") List<String> list);

    int selectIsBindUserId(@Param("userId") String userId,@Param("set") List<String> set);

    List<Map<String, Object>> selectUserByHrId(@Param("hrCompanyId") String hrCompanyId);

    int selectHrBindCount(@Param("companyId") String companyId);

    UserCompany selectByWorkerIdHrId(@Param("companyId") String companyId,@Param("workerId") String workerId);

    int selectBindCountByWorkerId(@Param("workerId") String workerId);

    List<WorkerCooperate> selectHrBindWorker(HrQueryWorkerDTO dto);
}
