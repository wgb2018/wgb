package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.ApplyParamDTO;
import com.microdev.param.CommentRequest;
import com.microdev.param.CommentResponse;
import com.microdev.service.ServiceCommentService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Transactional
public class ServiceCommentServiceImpl extends ServiceImpl<ServiceCommentMapper, ServiceComment> implements ServiceCommentService {

    @Autowired
    private ServiceCommentMapper serviceCommentMapper;
    @Autowired
    private BillMapper billMapper;
    @Autowired
    private EvaluateCommentRelationMapper evaluateCommentRelationMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private InformMapper informMapper;
    @Autowired
    private EvaluateMapper evaluateMapper;
    @Autowired
    private EvaluteGradeMapper evaluteGradeMapper;

    /**
     * 提交评论
     * @param commentRequest
     * @return
     */
    @Override
    public ResultDO serviceCommentSubmit(CommentRequest commentRequest) {
        if (commentRequest == null || StringUtils.isEmpty(commentRequest.getBillId())
                || commentRequest.getLevel() < 1 || StringUtils.isEmpty(commentRequest.getRoleType())) {
            throw new ParamsException("参数不能为空");
        }
        Bill bill = billMapper.selectById(commentRequest.getBillId());
        if (bill == null) {
            throw new ParamsException("查询不到付款记录");
        }
        if (bill.getCommentStatus() != 0) {
            throw new ParamsException("不能评论");
        }
        int level = commentRequest.getLevel();
        Set<String> evaluateId = commentRequest.getEvaluateId();
        ServiceComment comment = new ServiceComment();
        comment.setBillId(bill.getPid());
        comment.setLevel(level);
        if (!StringUtils.isEmpty(commentRequest.getComment())) {
            comment.setComment(commentRequest.getComment());
        }
        serviceCommentMapper.saveInfo(comment);
        if (evaluateId != null && evaluateId.size() > 0) {
            EvaluateCommentRelation relation = null;
            for (String id : evaluateId) {
                relation = new EvaluateCommentRelation();
                relation.setEvaluateId(id);
                relation.setServiceCommentId(comment.getPid());
                evaluateCommentRelationMapper.insert(relation);
            }
        }
        bill.setCommentStatus(2);
        billMapper.updateById(bill);

        String roleType = commentRequest.getRoleType();
        Inform inform = new Inform();
        inform.setTitle("支付评价");
        if ("worker".equals(roleType)) {
            inform.setSendType(1);
            inform.setAcceptType(2);
            User user = userMapper.selectByWorkerId (bill.getWorkerId());
            if (user == null) {
                throw new ParamsException("查询不到工作者信息");
            }
            inform.setContent(user.getNickname() + "已经对您的支付做了评价");
            inform.setReceiveId(bill.getHrCompanyId());
        } else if ("hr".equals(roleType)) {
            inform.setSendType(2);
            inform.setAcceptType(3);
            Company company = companyMapper.selectById(bill.getHrCompanyId());
            if (company == null) {
                throw new ParamsException("查询不到人力信息");
            }
            inform.setContent(company.getName() + "已经对您的支付做了评价");
            inform.setReceiveId(bill.getHotelId());
        } else {
            inform.setSendType(1);
            inform.setAcceptType(3);
            User user = userMapper.selectByWorkerId (bill.getWorkerId());
            if (user == null) {
                throw new ParamsException("查询不到工作者信息");
            }
            inform.setContent(user.getNickname() + "已经对您的支付做了评价");
            inform.setReceiveId(bill.getHrCompanyId());
        }
        informMapper.insertInform(inform);

        //更新评分
        String roleId = "";
        if ("worker".equals(roleType)) {
            roleId = bill.getHrCompanyId();
        } else if ("hr".equals(roleType)) {
            roleId = bill.getHotelId();
        }else{
            roleId = bill.getHotelId();
        }
        EvaluteGrade evaluteGrade = evaluteGradeMapper.selectByRoleId(roleId);
        if (evaluteGrade == null) {
            evaluteGrade = new EvaluteGrade();
            evaluteGrade.setGrade(commentRequest.getLevel());
            evaluteGrade.setRoleId(roleId);
            evaluteGrade.setAmount(1);
            evaluteGradeMapper.saveInfo(evaluteGrade);
        } else {
            int count = evaluteGrade.getAmount();
            double value = new BigDecimal(evaluteGrade.getGrade()).multiply(new BigDecimal(count)).add(new BigDecimal(commentRequest.getLevel())).divide(new BigDecimal(count + 1), 1, RoundingMode.HALF_UP).doubleValue();
            evaluteGrade.setGrade(value);
            evaluteGrade.setAmount(count + 1);
            evaluteGradeMapper.updateById(evaluteGrade);
        }

        return ResultDO.buildSuccess("评论成功");
    }

    /**
     * 查看角色信用记录
     * @return
     */
    @Override
    public ResultDO selectCommentInfo(Paginator paginator, ApplyParamDTO param) {

        if (param == null || StringUtils.isEmpty(param.getRoleType()) || StringUtils.isEmpty(param.getId())) {
            throw new ParamsException("参数不能为空");
        }
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        Map<String, Object> result = new HashMap<>();
        List<CommentResponse> responseList = null;
        if ("hr".equals(param.getRoleType())) {
            responseList = serviceCommentMapper.selectHrCommentInfo(param);

        } else if ("worker".equals(param.getRoleType())) {
            responseList = serviceCommentMapper.selectWorkerCommentInfo(param);

        } else if ("hotel".equals(param.getRoleType())) {
            responseList = serviceCommentMapper.selectHotelCommentInfo(param);

        } else {
            throw new ParamsException("角色类型错误");
        }
        PageInfo<CommentResponse> pageInfo = new PageInfo<>(responseList);
        if ("worker".equals(param.getRoleType())) {
            if (responseList != null && responseList.size() > 0) {
                for (CommentResponse response : responseList) {
                    if (!StringUtils.isEmpty(response.getStatus())) {
                        String[] arr = response.getStatus().split(",");
                        List<String> list = response.getLabelList();
                        for (String s : arr) {
                            if ("1".equals(s)) {
                                list.add("迟到");
                            } else if ("2".equals(s)) {
                                list.add("早退");
                            } else if ("3".equals(s)) {
                                list.add("旷工");
                            } else if ("4".equals(s)) {
                                list.add("忘打卡");
                            } else if ("5".equals(s)) {
                                list.add("请假");
                            } else if ("6".equals(s)) {
                                list.add("迟到");
                                list.add("早退");
                            } else if ("7".equals(s)) {
                                list.add("迟到");
                                list.add("忘打卡");
                            }
                        }

                    }
                    response.setStatus ("");
                }
            }
        } else {
            if (responseList != null && responseList.size() > 0) {
                for (CommentResponse response : responseList) {
                    if (!StringUtils.isEmpty(response.getStatus())) {
                        String[] arr = response.getStatus().split(",");
                        List<String> list = response.getLabelList();
                        for (String s : arr) {
                            list.add(s);
                        }
                    }
                    response.setStatus ("");
                }
            }
        }
        if (responseList == null) {
            responseList = new ArrayList<>();
        }
        result.put("page", paginator.getPage());
        result.put("total", pageInfo.getTotal());
        result.put("list", responseList);
        return ResultDO.buildSuccess(result);
    }

    /**
     * 查看人力/用人单位信用记录
     * @param paginator
     * @param param
     * @return
     */
    @Override
    public ResultDO selectPcCommentInfo(Paginator paginator, ApplyParamDTO param) {

        if (!StringUtils.isEmpty(param.getRoleType()) && !"hr".equals(param.getRoleType())
                && !"hotel".equals(param.getRoleType())) {
            throw new ParamsException("参数值错误");
        }
        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<CommentResponse> list = serviceCommentMapper.selectCommentInfoPc(param);
        Map<String, Object> result = new HashMap<>();
        PageInfo<CommentResponse> pageInfo = new PageInfo<>(list);
        if (list == null) {
            list = new ArrayList<>();
        } else {
            for (CommentResponse response : list) {
                if (!StringUtils.isEmpty(response.getStatus())) {
                    String[] arr = response.getStatus().split(",");
                    List<String> strList = response.getLabelList();
                    for (String str : arr) {
                        strList.add(str);
                    }
                }
                response.setStatus("");
            }
        }
        result.put("page", paginator.getPage());
        result.put("total", pageInfo.getTotal());
        result.put("list", list);
        return ResultDO.buildSuccess(result);
    }
}
