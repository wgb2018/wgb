package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;


@Data
@TableName("hotel_pay_hr_details")
public class HotelPayHrDetails extends BaseEntity {

    private String taskHrId;

    //本次付款金额
    private  Double thisPayMoney=0.0;

}
