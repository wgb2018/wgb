package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;


/**
 * 数据字典
 * Created by Louis Liu on 2017/5/12 0012.
 */
@Data
@TableName("dict")
public class Dict extends BaseEntity {
    /**
     * 名    TaskType
     */
    private String name;
    /**
     * 编码   guest_room
     */
    private String code;
    /**
     * 文本   客房服务
     */
    private String text;
    /**
     * 顺序      0
     */
    private Integer ordinal;
    /**
     * 备注
     */
    private String remark;
    /**
     * 扩展属性
     */
    private String extend;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }
}
