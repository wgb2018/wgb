package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.BusinessException;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.StringKit;
import com.microdev.mapper.InformMapper;
import com.microdev.mapper.InformTemplateMapper;
import com.microdev.model.Inform;
import com.microdev.model.InformTemplate;
import com.microdev.param.InformRequestDTO;
import com.microdev.param.InformType;
import com.microdev.param.NoticeResponse;
import com.microdev.service.InformService;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InformServiceImpl extends ServiceImpl<InformMapper,Inform>  implements InformService {
    @Autowired
    private InformTemplateMapper informTemplateMapper;
    @Autowired
    private InformMapper informMapper;


    /**
     *查询消息及未读消息
     * @return
     */
    @Override
    public Map<String, Object> selectMessageInfo(InformRequestDTO dto, Paginator paginator) {

        if (StringUtils.isEmpty(dto.getRole()) || StringUtils.isEmpty(dto.getId())) {
            throw new ParamsException("参数不能为空");
        }
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> param = new HashMap<>();
        param.put("status", 0);
        if ("hr".equals(dto.getRole())) {
            param.put("sendType", 3);
            param.put("acceptType", 2);
            param.put("receiveId", dto.getId());
            result.put("companyNum", informMapper.selectUnReadCount(param));
            param.put("sendType", 1);
            result.put("workerNum", informMapper.selectUnReadCount(param));
            param.put("sendType", 4);
            result.put("systemNum", informMapper.selectUnReadCount(param));
            param.remove("status");
            if (dto.getType() == 1) {
                param.put("sendType", 1);
            } else if (dto.getType() == 3) {
                param.put("sendType", 3);
            } else if (dto.getType() == 4) {
                param.put("sendType", 4);
            } else {
                throw new ParamsException("参数错误");
            }

        } else if ("hotel".equals(dto.getRole())) {
            param.put("sendType", 2);
            param.put("acceptType", 3);
            param.put("receiveId", dto.getId());
            result.put("hrNum", informMapper.selectUnReadCount(param));
            param.put("sendType", 4);
            result.put("systemNum", informMapper.selectUnReadCount(param));
            //页查询type类型的消息
            param.put("sendType", 1);
            result.put("workerNum", informMapper.selectUnReadCount(param));
            param.remove("status");

            if (dto.getType() == 2) {
                param.put("sendType", 2);
            } else if (dto.getType() == 4) {
                param.put("sendType", 4);
            } else if (dto.getType() == 1) {
                param.put("sendType", 1);
            } else {
                throw new ParamsException("参数错误");
            }
        } else if ("worker".equals(dto.getRole())) {
            param.put("sendType", 3);
            param.put("acceptType", 1);
            param.put("receiveId", dto.getId());
            result.put("companyNum", informMapper.selectUnReadCount(param));
            param.put("sendType", 2);
            result.put("hrNum", informMapper.selectUnReadCount(param));
            param.put("sendType", 4);
            result.put("systemNum", informMapper.selectUnReadCount(param));
            param.remove("status");
            if (dto.getType() == 2) {
                param.put("sendType", 2);
            } else if (dto.getType() == 3) {
                param.put("sendType", 3);
            } else if (dto.getType() == 4) {
                param.put("sendType", 4);
            } else {
                throw new ParamsException("参数错误");
            }
        } else {
            throw new ParamsException("参数错误");
        }

        //分页查询type类型的消息

        PageHelper.startPage(paginator.getPage(), paginator.getPageSize(), true);
        List<NoticeResponse> list = informMapper.selectInfromByParam(param);
        PageInfo<NoticeResponse> pageInfo = new PageInfo<>(list);
        for (NoticeResponse response : list) {
            response.setTime(response.getCreateTime().getLong(ChronoField.INSTANT_SECONDS) * 1000);
        }
        result.put("page", pageInfo.getPageNum());
        result.put("total", pageInfo.getTotal());
        result.put("list", list);
        return result;
    }

    /**
     * 发送通知
     * @param sendType      发送类型1小时工2人力3用人单位4系统
     * @param acceptType    接收类型1小时工2人力3用人单位
     * @param content       通知内容
     * @param receiveId     接收方id
     * @param title         消息标题
     */
    @Override
    public void sendInformInfo(int sendType, int acceptType, String content, String receiveId, String title) {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(receiveId) || StringUtils.isEmpty(title)) {
            throw new ParamsException("参数不能为空");
        }
        Inform inform = new Inform();
        inform.setCreateTime(OffsetDateTime.now());
        inform.setModifyTime(OffsetDateTime.now());
        inform.setTitle(title);
        inform.setContent(content);
        inform.setAcceptType(acceptType);
        inform.setSendType(sendType);
        inform.setStatus(0);
        inform.setReceiveId(receiveId);
        informMapper.insert(inform);
    }

    /**
     * 根据接受方id查询未读通知数量
     * @param param
     * @return
     */
    @Override
    public int selectNoticeCountByReceiveId(Map<String, Object> param) {
        return 0;
    }

    /**
     * 根据条件查询数量
     * @param param
     * @return
     */
    @Override
    public int selectCountByParam(Map<String, Object> param) {
        return informMapper.selectUnReadCount(param);
    }

    /**
     * 更新通知状态为已读
     * @param noticeId      通知id
     * @return
     */
    @Override
    public ResultDO updateInformStatus(String noticeId) {
        if (StringUtils.isEmpty(noticeId)) {
            throw new ParamsException("参数不能为空");
        }
        Inform inform = informMapper.selectById(noticeId);
        if (inform == null) {
            return ResultDO.buildError("查询不到通知信息");
        }
        inform.setStatus(1);
        informMapper.updateById(inform);
        return ResultDO.buildSuccess("更新成功");
    }

    /**
     * 删除通知
     * @param list
     * @return
     */
    @Override
    public ResultDO deleteInformInfo(List<String> list) {
        if (list == null || list.size() == 0) {
            return ResultDO.buildError("参数不能为空");
        }
        informMapper.updateBatch(list);
        return ResultDO.buildSuccess("删除成功");
    }


}
