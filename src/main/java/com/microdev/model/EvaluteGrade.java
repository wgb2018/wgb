package com.microdev.model;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

@Data
@TableName("evalute_grade")
public class EvaluteGrade extends BaseEntity{

    /**
     * 角色id
     */
    private String roleId;
    /**
     * 评价分数
     */
    private double grade;
    /**
     * 评价次数
     */
    private int amount = 0;
}
