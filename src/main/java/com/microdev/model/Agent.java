package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("agent")
public class Agent extends BaseEntity{

    private String userId;
    //代理商名称
    private String agentName;
    //代理商级别1总代理2省级代理3市级代理
    private Integer level;
    //代理商编号
    private String identifer;
    //上级代理商编号
    private String parentIdentifer;
    //企业名称
    private String companyName;
    //企业信用代码
    private String creditCode;
    //主营业务
    private String mainBusiness;
    //企业性质
    private String companyProperty;
    //所属行业
    private String industry;
    //省
    private String province;
    //市
    private String city;
    //区
    private String area;
    //详细地址
    private String address;
    //联系人姓名
    private String linkMan;
    //联系电话
    private String linkPhone;
    //公司邮箱
    private String companyEmail;
    //公司传真
    private String companyFax;
    //开户银行
    private String bank;
    //所属支行
    private String subBranch;
    //开户账号
    private String accountNumber;
    //发票抬头
    private String invoicesTitle;
    //(开始)有效时间
    private OffsetDateTime fromDate;
    //（结束）有效时间
    private OffsetDateTime toDate;
}
