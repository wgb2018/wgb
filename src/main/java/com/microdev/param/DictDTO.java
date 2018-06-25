package com.microdev.param;

import lombok.Data;

/**
 * @author liutf
 */
@Data
public class DictDTO {
    private String pid;
    /**
     * 名    taskType
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
    private int ordinal = 0;
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

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
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
