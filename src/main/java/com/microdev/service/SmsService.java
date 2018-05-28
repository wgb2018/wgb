package com.microdev.service;

import com.microdev.param.SmsSendDTO;

public interface SmsService {
    void sendSms(SmsSendDTO smsSendDTO);
}
