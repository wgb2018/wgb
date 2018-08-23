package com.microdev.param;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class TaskWorkerViewDTO {
    /**
     * 接受人列表
     */
    private List<Map<String, Object>> confirmedList=new ArrayList<> ();
    /**
     * 决绝人列表
     */
    private List<Map<String, Object>> refusedList=new ArrayList<>();
    private List<Map<String, Object>> distributedList=new ArrayList<>();

}
