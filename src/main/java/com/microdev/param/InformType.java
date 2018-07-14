package com.microdev.param;

public enum InformType {
    apply_for_leave_success("申请请假"), worker_bind_hr_success("小时工绑定人力公司"), worker_bind_hr_fail("小时工绑定人力公司"), worker_relieve("小时工解绑"), worker_apply_cancel_success("小时工申请取消任务"),
    exchange_worker("人力公司调换小时工"), register_worker("注册小时工"),hotel_bind_hr_success("用人单位绑定人力公司"), replacement_success("用人单位换人"), replacement_fail("用人单位换人"), hotel_account_agree("用人单位支付"),
    apply_for_leave_fail("申请请假"),hotel_account_refuse("用人单位支付"),accept_hotel_task("用人单位任务"),refuse_hotel_task("用人单位任务"),register_hotel("注册用人单位"),hr_bind_hotel_success("人力公司绑定用人单位"),
    register_hr("注册人力公司"),hr_bind_worker_success("人力公司绑定小时工"),accept_hr_task("人力公司任务"),hr_account_agree("人力公司支付"),hr_account_refuse("人力公司支付"),apply_for_overtime_success("申请加时"),
    apply_for_overtime_fail("申请加时"),apply_for_supplement_success("申请补签"),apply_for_supplement_fail("申请补签"),come_late("迟到通知"),absenteeism("矿工通知"),early_retreat("早退通知"),hr_bind_worker_fail("人力绑定小时工"),
    hotel_bind_hr_fail("用人单位绑定人力公司"),hr_bind_hotel_fail("人力公司绑定用人单位"),hr_allocation_success("人力申请调配"),hr_allocation_fail("人力申请调配"),worker_apply_cancel_fail("小时工申请取消任务"),hotel_agree_apply("用人单位同意任务拒绝");

    String text;

    InformType(String text) {
        this.text = text;
    }
}
