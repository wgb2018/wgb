package com.microdev.service.impl;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.common.ResultDO;
import com.microdev.common.utils.JPushManage;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.AcceptNoticeRequest;
import com.microdev.service.EnrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EnrollServiceImpl extends ServiceImpl<EnrollMapper,Enroll> implements EnrollService {
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private JpushClient jpushClient;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private TaskHrCompanyMapper taskHrCompanyMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EnrollMapper enrollMapper;
    @Override
    public ResultDO hrApplyRegistration(AcceptNoticeRequest request) {
        Enroll m = new Enroll();
        m.setValue (request.getEnrollWorkers ());
        Task task = taskMapper.selectById (noticeMapper.selectById (request.getNoticeId ()).getTaskId ());
        m.setTaskId (task.getPid ());
        m.setEnrollType  (1);
        m.setApplyType (3);
        m.setApplicantType (2);
        m.setHrCompanyId (request.getHrCompanyId ());
        m.setIsTask (1);
        m.setStatus (0);
        m.setRequestId (request.getNoticeId ());
        m.setHotelId (task.getHotelId ());
        m.setEnrollCode ("hrApplyRegistrationMessage");
        m.setEnrollTitle ("报名申请");
        enrollMapper.insert (m);
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (task.getHotelId ()).getLeaderMobile ( ), "您收到"+companyMapper.findCompanyById (request.getHrCompanyId ()).getName ()+"发送的报名申请：报名人数为"+request.getEnrollWorkers ()+"人"));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {
            e.printStackTrace ( );
        }
        return ResultDO.buildSuccess ("发送成功");
    }

    @Override
    public ResultDO workerApplyHotel(AcceptNoticeRequest request) {
        Enroll m = new Enroll();
        Notice notice = noticeMapper.selectById (request.getNoticeId ());
        m.setEnrollType (2);
        m.setApplyType (3);
        m.setApplicantType (1);
        m.setValue (1);
        m.setWorkerId (request.getWorkerId ());
        m.setIsTask (1);
        m.setStatus (0);
        m.setRequestId (request.getNoticeId ());
        m.setHotelId (notice.getHotelId ());
        m.setEnrollCode ("workerApplyHotel");
        m.setEnrollTitle ("报名申请");
        enrollMapper.insert (m);
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (notice.getHotelId ()).getLeaderMobile ( ), "您收到"+userMapper.queryByWorkerId (request.getWorkerId ()).getNickname ()+"发送的报名申请"));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {
            e.printStackTrace ( );
        }
        return ResultDO.buildSuccess ("发送成功");
    }

    @Override
    public ResultDO workerApplyHr(AcceptNoticeRequest request) {
        Notice notice = noticeMapper.selectById (request.getNoticeId ());
        Enroll m = new Enroll();
        m.setValue (1);
        TaskHrCompany taskHrCompany = taskHrCompanyMapper.queryByTaskId (notice.getTaskId ());
        m.setTaskId (taskHrCompany.getTaskId ());
        m.setEnrollType (3);
        m.setHrTaskId (taskHrCompany.getPid ());
        m.setApplyType (2);
        m.setApplicantType (1);
        m.setWorkerId (request.getWorkerId ());
        m.setIsTask (1);
        m.setStatus (0);
        m.setRequestId (request.getNoticeId ());
        m.setHotelId (notice.getHotelId ());
        m.setHrCompanyId (taskHrCompany.getHrCompanyId ());
        m.setEnrollCode ("workerApplyHr");
        m.setEnrollTitle ("报名申请");
        enrollMapper.insert (m);
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (taskHrCompany.getHrCompanyId ()).getLeaderMobile ( ), "您收到"+userMapper.queryByWorkerId (request.getWorkerId ()).getNickname ()+"发送的报名申请：报名人数为1人"));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {
            e.printStackTrace ( );
        }
        return ResultDO.buildSuccess ("发送成功");
    }

    @Override
    public ResultDO workerApplyRegistration(AcceptNoticeRequest request) {
        Enroll m = new Enroll();
        m.setEnrollType (4);
        m.setValue (1);
        m.setApplyType (2);
        m.setApplicantType (1);
        m.setWorkerId (request.getWorkerId ());
        m.setIsTask (1);
        m.setStatus (0);
        m.setRequestId (request.getNoticeId ());
        m.setHrCompanyId (request.getHrCompanyId ());
        m.setEnrollCode ("workerApplyRegistration");
        m.setEnrollTitle ("申请绑定");
        enrollMapper.insert (m);
        try {
            jpushClient.jC.sendPush (JPushManage.buildPushObject_all_alias_message (companyMapper.findCompanyById (request.getHrCompanyId ()).getLeaderMobile ( ), "您收到"+userMapper.queryByWorkerId (request.getWorkerId ()).getNickname ()+"发送的报名申请"));
        } catch (APIConnectionException e) {
            e.printStackTrace ( );
        } catch (APIRequestException e) {
            e.printStackTrace ( );
        }
        return ResultDO.buildSuccess ("发送成功");
    }
}
