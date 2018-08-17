package com.microdev.common;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liutf
 */
@Data
public class ResultDO {
    /**
     * 是否成功
     */
    private boolean success;
    /**
     * 错误码
     */
    private Integer code;
    /**
     * 错误码
     */
    private Integer value;
    /**
     * 返回信息
     */
    private String message;
    /**
     * 返回数据
     */
    private Object data;
    /**
     * 扩展返回信息
     */
    private Map<String, Object> extra;

    public ResultDO() {
    }

    public ResultDO(boolean success) {
        this.success = success;
    }


    /**
     * 构建失败的返回
     */
    public static ResultDO buildError(String message) {
        return buildError(message, null, null,null);
    }

    public static ResultDO buildError(String message,Integer code) {
        return buildError(message, null, null,code);
    }

    public static ResultDO buildError(Object data) {
        return buildError(null, data, null,null);
    }

    public static ResultDO buildError(String message, Object data) {
        return buildError(message, data, null,null);
    }

    public static ResultDO buildError(String message, Object data, Map<String, Object> extra,Integer code) {
        ResultDO resultDO = new ResultDO (false);
        resultDO.message = message;
        resultDO.data = data;
        resultDO.extra = extra;
        resultDO.code = code;
        return resultDO;
    }

    /**
     * 构建成功的返回
     */
    public static ResultDO buildSuccess(String message) {
        return buildSuccess(message, null, null,null,null);
    }

    public static ResultDO buildSuccess(Object data) {
        return buildSuccess(null, data, null,null,null);
    }

    public static ResultDO buildSuccess(Integer value, Object data) {
        return buildSuccess(null, data, null,null,value);
    }
    public static ResultDO buildSuccess(Integer value, Object data, Map<String, Object> extra) {
        return buildSuccess(null, data, extra,null,value);
    }

    public static ResultDO buildSuccess(String message, Object data) {
        return buildSuccess(message, data, null,null,null);
    }

    public static ResultDO buildSuccess(String message, Object data, Map<String, Object> extra,Integer code,Integer value) {
        ResultDO resultDO = new ResultDO (true);
        resultDO.message = message;
        resultDO.data = data;
        resultDO.extra = extra;
        resultDO.code = code;
        resultDO.value = value;
        return resultDO;
    }
    public static ResultDO buildSuccess(String message, Object data, Map<String, Object> extra,Integer code) {
        ResultDO resultDO = new ResultDO (true);
        resultDO.message = message;
        resultDO.data = data;
        resultDO.extra = extra;
        resultDO.code = code;
        resultDO.value = null;
        return resultDO;
    }


    /**
     * 添加扩展信息
     */
    public ResultDO addExtra(String key, Object value) {
        if (extra == null) {
            extra = new HashMap<String, Object>();
        }
        extra.put(key, value);
        return this;
    }
}
