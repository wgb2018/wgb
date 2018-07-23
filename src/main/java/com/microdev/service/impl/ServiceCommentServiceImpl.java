package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.mapper.*;
import com.microdev.model.*;
import com.microdev.param.CommentRequest;
import com.microdev.service.ServiceCommentService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

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
            User user = userMapper.selectById(bill.getWorkerId());
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
            return ResultDO.buildSuccess("评论成功");
        }
        informMapper.insertInform(inform);
        return ResultDO.buildSuccess("评论成功");
    }
}
