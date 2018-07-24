package com.microdev.param;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommentResponse {

    private String name;
    private String logo;
    private String createTime;
    private String taskTypeText;
    private String level;
    private String comment;
    private String commentId;
    private List<String> labelList = new ArrayList<>();
    private String status;
    private String targetName;//被评价对象
}
