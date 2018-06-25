package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
@Data
@TableName("worker")
public class Worker extends BaseEntity {
    /**
     * 小时工身份证正面照片
     */
    private String idcardFront ="";

    /**
     * 小时工身份证背面照片
     */
    private String idcardBack ="";

    /**
     * 小时工健康证
     */
    private String healthCard ="";

    /**
     * 小时工身份证号码
     */
    private String idcardNumber ="";
    /**
     * 用户二维码
     */
    private String qrCode ="";
    /**
     * 用户二维码
     */
    private Integer activeCompanys;

    private boolean bindCompanys = true;

}
