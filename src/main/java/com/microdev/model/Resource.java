package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.microdev.type.ActionType;
import com.microdev.type.ResourceType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author liutf
 */
@Data
@TableName("resource")
public class Resource extends BaseEntity {
    /**
     * 资源名
     */
    private String name;
    /**
     * 顺序
     */
    private Integer sort = 0;
    /**
     * 请求方式
     */
    private ActionType action;
    /**
     * 资源路径
     */
    private String uri;
    /**
     * 图标
     */
    private String icon;
    /**
     * 资源类型
     */
    private ResourceType type;
    /**
     * 父节点
     */
    private Resource parent;
    /**
     * 子节点
     */
    @TableField(exist = false)
    private List<Resource> children = new ArrayList<>();

}
