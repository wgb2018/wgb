package com.microdev.param;

/**
 * Created by Louis Liu on 2016/12/22 0022.
 */
public enum SmsType {
    login("登录"), register("注册"), bind("绑定"), reset_password("重置密码"), identity_check("身份验证"),
    hr_dispatch_worker("人力公司派单给小时工"), worker_feedback_hr("小时工反馈任务状态给人力公司"),hr_company_apply_hotel("人力公司申请添加用人单位"),
    hotel_apply_hr_company("用人单位申请添加人力公司"), worker_apply_hr_company("小时工申请添加用人单位"), apply_register("申请注册");

    String text;

    SmsType(String text) {
        this.text = text;
    }
}
