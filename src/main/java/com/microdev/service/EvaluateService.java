package com.microdev.service;

import com.baomidou.mybatisplus.service.IService;
import com.microdev.common.ResultDO;
import com.microdev.model.Evaluate;
import com.microdev.param.EvaluateParam;

import java.util.List;

public interface EvaluateService extends IService<Evaluate> {
    ResultDO createEvaluate(List<EvaluateParam> request) throws Exception;

    ResultDO modifyEvaluate(EvaluateParam request) throws Exception;

    ResultDO listEvaluate(EvaluateParam request);

    ResultDO deleteEvaluate(List<String> list);
}
