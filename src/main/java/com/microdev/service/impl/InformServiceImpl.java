package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.common.utils.StringKit;
import com.microdev.mapper.InformMapper;
import com.microdev.mapper.InformTemplateMapper;
import com.microdev.model.Inform;
import com.microdev.model.InformTemplate;
import com.microdev.param.InformRequestDTO;
import com.microdev.param.InformType;
import com.microdev.service.InformService;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InformServiceImpl extends ServiceImpl<InformMapper,Inform>  implements InformService {
    @Autowired
    private InformTemplateMapper informTemplateMapper;
    @Autowired
    private InformMapper informMapper;

    @Override
    public void sendInform(String acceptype, String receiveTd, InformType informType,Map<String, String> templateParams) {
        InformTemplate param = new InformTemplate ();
        param.setCode (informType);
        InformTemplate informTemplate = informTemplateMapper.selectOne (param);
        if(informTemplate == null){
            //throw  new Exception ("没有找到通知模板");
        }
        String content = StringKit.templateReplace(informTemplate.getContent(), templateParams);
        Inform inform = new Inform();
        //nform.

    }

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
            param.remove("status");

            if (dto.getType() == 2) {
                param.put("sendType", 2);
            } else if (dto.getType() == 4) {
                param.put("sendType", 4);
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
        List<Map<String, Object>> list = informMapper.selectInfromByParam(param);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);
        result.put("page", pageInfo.getPageNum());
        result.put("pageSize", pageInfo.getPageSize());
        result.put("list", list);
        return result;
    }


}
