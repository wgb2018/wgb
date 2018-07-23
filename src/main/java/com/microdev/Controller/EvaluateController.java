package com.microdev.Controller;

import com.microdev.common.ResultDO;
import com.microdev.param.EvaluateParam;
import com.microdev.service.EvaluateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EvaluateController {
    @Autowired
    EvaluateService evaluateService;

    /**
     * 添加评价标签
     */
    @PostMapping("/add/evaluate")
    public ResultDO createEvaluate(@RequestBody List<EvaluateParam> request) throws Exception{
        return evaluateService.createEvaluate(request);
    }
    /**
     * 标签列表
     */
    @PostMapping("/list/evaluate")
    public ResultDO listEvaluate(@RequestBody EvaluateParam request) throws Exception{
        return evaluateService.listEvaluate(request);
    }
    /**
     * 标签列表
     */
    @PostMapping("/delete/evaluate")
    public ResultDO deleteEvaluate(@RequestBody List<String> list) throws Exception{
        return evaluateService.deleteEvaluate(list);
    }

}
