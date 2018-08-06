package com.microdev.common;

import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.DictDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class WorkerUnbind {

    private static final Logger logger = LoggerFactory.getLogger(WorkerUnbind.class);
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private DictMapper dictMapper;
    @Autowired
    private UserCompanyMapper userCompanyMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TaskWorkerMapper taskWorkerMapper;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private WorkerMapper workerMapper;
    @Autowired
    private InformMapper informMapper;
    @Autowired
    private HotelHrCompanyMapper hotelHrCompanyMapper;
    @Autowired
    private TaskHrCompanyMapper taskHrCompanyMapper;

    @Scheduled(cron = "0 0 * * * ?")
    public void scanUnbindMessage() {
        DictDTO dict = dictMapper.findByNameAndCode("MaxUnbindDay","9");
        if (dict != null) {
            Integer maxNum = Integer.parseInt(dict.getText());
            int num = messageMapper.selectWorkerUnbindCount();
            if (num == 0) return;
            List<Message> list = null;
            int end = 0;
            for (int i = 0; i <= num; ) {
                if (i + 500 > num) {
                    end = num;
                } else {
                    end = i + 500;
                }
                list = messageMapper.selectWorkerUnbindMessage(i, end);
                updateInfo(list, maxNum);
                i = i + 500;
            }
        } else {
            throw new BusinessException("查询不到最大解绑天数");
        }

    }

    private void updateInfo(List<Message> list, Integer maxNum) {
        if (list != null || list.size() > 0) {

            for (Message message : list) {
                boolean flag = comparaTime(message.getCreateTime(), maxNum);
                if (flag) {
                    message.setStatus(1);
                    messageMapper.updateById(message);
                    if (message.getApplicantType() == 1) {
                        UserCompany userCompany = userCompanyMapper.selectByWorkerIdHrId(message.getHrCompanyId(), message.getWorkerId());
                        if (userCompany == null) {
                            logger.error("小时工人力关系查询不到,workerId=" + message.getWorkerId() + ";hrId=" + message.getHrCompanyId());
                        } else {
                            //解绑小时工和人力
                            if (userCompany.getStatus() == 3) {
                                userCompany.setStatus(4);
                                userCompany.setRelieveTime(OffsetDateTime.now());
                                userCompanyMapper.updateById(userCompany);
                                relieveBind(userCompany);
                            }

                        }
                    } else if (message.getApplicantType() == 2 || message.getApplicantType() == 3){
                        HotelHrCompany hotelHrCompany = hotelHrCompanyMapper.selectByHrHotelId(message.getHrCompanyId(), message.getHotelId());
                        if (hotelHrCompany == null) {
                            logger.error("人力与用人单位关系查询不到，hrId=" + message.getHrCompanyId() + "; hotelId=" + message.getHotelId());
                        } else {
                            //解绑人力和用人单位并将任务终止
                            if (hotelHrCompany.getStatus() == 5) {
                                Company company = companyMapper.selectById(hotelHrCompany.getHotelId());
                                Company hrCompany = companyMapper.selectById(hotelHrCompany.getHrId());
                                if (company == null) {
                                    logger.error("查询不到用人单位,hotelId=" + hotelHrCompany.getHotelId());
                                    continue;
                                }
                                if (hrCompany == null) {
                                    logger.error("查询不到人力，hrId=" + hotelHrCompany.getHrId());
                                }
                                if (company.getActiveCompanys() != null && company.getActiveCompanys() > 0) {
                                    company.setActiveCompanys(company.getActiveCompanys() - 1);
                                    company.setBindCompanys(true);
                                } else {
                                    logger.error("数据异常,hotelId=" + hotelHrCompany.getHotelId());
                                    continue;
                                }
                                if (hrCompany.getActiveCompanys() != null && hrCompany.getActiveCompanys() > 0) {
                                    hrCompany.setActiveCompanys(hrCompany.getActiveCompanys() - 1);
                                    hrCompany.setBindCompanys(true);
                                } else {
                                    logger.error("数据异常,hrId=" + hotelHrCompany.getHrId());
                                    continue;
                                }
                                //更新人力及用人单位的活跃公司数量
                                companyMapper.updateById(company);
                                companyMapper.updateById(hrCompany);
                                hotelHrCompany.setStatus(1);
                                hotelHrCompany.setRelieveTime(OffsetDateTime.now());
                                if (message.getApplicantType() == 2) {
                                    hotelHrCompany.setRelieveType(2);
                                } else {
                                    hotelHrCompany.setRelieveType(3);
                                }
                                hotelHrCompanyMapper.updateById(hotelHrCompany);
                                hotelHrRelieveBind(hotelHrCompany.getHotelId(), message.getApplyType(), message.getContent(), hotelHrCompany.getHrId());
                            }

                        }
                    }
                }
            }
        }
    }

    private void hotelHrRelieveBind(String hotelId, Integer applyType, String content, String hrId) {
        List<TaskHrCompany> list = taskHrCompanyMapper.selectWorkHrTask(hotelId);
        if (list != null && list.size() > 0) {
            List<String> hrTaskList = new ArrayList<>();
            for (TaskHrCompany hrTask : list) {
                hrTaskList.add(hrTask.getPid());
                hrTask.setStatus(8);
                hrTask.setRefusedReason(content);
                taskHrCompanyMapper.updateById(hrTask);
            }
            List<TaskWorker> taskWorkerList = taskWorkerMapper.selectByHrTaskList(hrTaskList);
            for (TaskWorker taskWorker : taskWorkerList) {
                taskWorker.setStatus(3);
                if (applyType == 2) {
                    taskWorker.setRefusedReason("用人单位终止任务");
                } else if (applyType == 3) {
                    taskWorker.setRefusedReason("人力终止任务");
                }
                taskWorkerMapper.updateById(taskWorker);
            }
            Inform inform = new Inform();
            inform.setContent(list.get(0).getHrCompanyName() + "解除了和您的合作关系");
            inform.setTitle("解绑成功");
            inform.setSendType(2);
            inform.setAcceptType(3);
            inform.setReceiveId(hotelId);
            informMapper.insertInform(inform);

            inform = new Inform();
            inform.setContent(list.get(0).getHotelName() + "解除了和您的合作关系");
            inform.setTitle("解绑成功");
            inform.setSendType(3);
            inform.setAcceptType(2);
            inform.setReceiveId(hrId);
            informMapper.insertInform(inform);
        }
    }

    private void relieveBind(UserCompany userCompany) {

        User user = userMapper.selectById(userCompany.getUserId());
        if (user == null) {
            logger.error("查询不到工作者:" + user.getPid());
            return;
        }

        //更新小时工接受该人力的任务
        List<TaskWorker> taskWorkerList = taskWorkerMapper.selectByUserHr(user.getPid(), userCompany.getCompanyId());
        if (taskWorkerList != null && taskWorkerList.size() > 0) {
            for (TaskWorker taskWorker : taskWorkerList) {
                taskWorker.setStatus(3);
                taskWorker.setRefusedReason("已解除绑定");
                taskWorkerMapper.updateById(taskWorker);
            }
        }
        Company company = companyMapper.selectById(userCompany.getCompanyId());
        if (company == null) {
            return;
        }

        String num = dictMapper.findByNameAndCode("WorkerBindHrMaxNum", "7").getText();

        if (company.getActiveWorkers() == null) {
            company.setActiveWorkers(0);
        }
        if (company.getActiveWorkers() >= 1) {
            company.setActiveWorkers(company.getActiveWorkers() - 1);
        } else {
            throw new ParamsException("数据异常");
        }
        company.setBindWorkers(true);
        companyMapper.updateById(company);
        Worker worker = workerMapper.queryById(user.getWorkerId());
        if (worker.getActiveCompanys() == null) {
            worker.setActiveCompanys(0);
        }
        if (worker.getActiveCompanys() >= 1) {
            worker.setActiveCompanys(worker.getActiveCompanys() - 1);
        } else {
            throw new ParamsException("数据异常");
        }
        worker.setBindCompanys(true);
        workerMapper.updateById(worker);

        Inform inform = new Inform();
        inform.setCreateTime(OffsetDateTime.now());
        inform.setModifyTime(OffsetDateTime.now());
        inform.setAcceptType(1);
        inform.setSendType(2);
        inform.setReceiveId(user.getWorkerId());
        inform.setTitle("解绑成功");
        inform.setContent(company.getName() + "同意了你的申请解绑。你可以添加新的合作人力公司，每人最多只能绑定" + num + "家人力公司");
        informMapper.insertInform(inform);

        inform = new Inform();
        inform.setCreateTime(OffsetDateTime.now());
        inform.setModifyTime(OffsetDateTime.now());
        inform.setAcceptType(2);
        inform.setSendType(1);
        inform.setReceiveId(userCompany.getCompanyId());
        inform.setTitle("解绑成功");
        inform.setContent(user.getNickname() + "解除了绑定关系");
        informMapper.insertInform(inform);
    }


    private boolean comparaTime(OffsetDateTime createTime, int maxNum) {
        OffsetDateTime nowTime = OffsetDateTime.now();
        long leaveTime = nowTime.toEpochSecond() - createTime.toEpochSecond();
        return (leaveTime / 3600) >= (maxNum * 24);
    }
}
