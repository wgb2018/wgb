package com.microdev.service.impl;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.microdev.param.ContactProperties;
import com.microdev.param.SmsSendDTO;
import com.microdev.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author liutf
 */
@Service("aliyunSmsService")
public class SmsServiceImpl implements SmsService{
    private Logger log = LoggerFactory.getLogger(getClass());

    //产品名称:云通信短信API产品,开发者无需替换
    private static final String product = "Dysmsapi";
    //产品域名,开发者无需替换
    private static final String domain = "dysmsapi.aliyuncs.com";
    //暂不支持region化
    private static final String region = "cn-hangzhou";

    @Autowired
    private ContactProperties contactProperties;

    private IAcsClient acsClient;

    @PostConstruct
    public void init() throws ClientException {
        //超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(contactProperties.getSms().getDefaultConnectTimeout()));
        System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(contactProperties.getSms().getDefaultReadTimeout()));
        //初始化acsClient
        IClientProfile profile = DefaultProfile.getProfile(region, contactProperties.getSms().getAccessKeyId(), contactProperties.getSms().getAccessKeySecret());
        DefaultProfile.addEndpoint(region, region, product, domain);
        acsClient = new DefaultAcsClient(profile);
    }


    @Override
    public void sendSms(SmsSendDTO smsSendDTO) {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(smsSendDTO.getMobile());
        request.setSignName(smsSendDTO.getSignName());
        request.setTemplateCode(smsSendDTO.getTemplateCode());
        request.setTemplateParam(JSON.toJSONString(smsSendDTO.getTemplateParam()));

        //上行短信扩展码(无特殊需求用户请忽略此字段)
        //request.setSmsUpExtendCode("90997");
        //outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        //request.setOutId("yourOutId");

        SendSmsResponse response;
        try {
            response = acsClient.getAcsResponse(request);
        } catch (ClientException e) {
            String errMsg = String.format("调用阿里云短信服务异常. params=%s", JSON.toJSONString(request));
            log.error(errMsg, e);
            throw new RuntimeException("短信服务异常", e);
        }
        if (response == null) {
            String errMsg = String.format("调用阿里云短信服务无响应. params=%s", JSON.toJSONString(request));
            log.error(errMsg);
            throw new RuntimeException("短信服务无响应");
        }
        if (!response.getCode().equals("OK")) {
            String errMsg = String.format("调用阿里云短信服务失败. params=%s,result=%s", JSON.toJSONString(request), JSON.toJSONString(response));
            log.error(errMsg);
            throw new RuntimeException(response.getMessage());
        }
    }
}
