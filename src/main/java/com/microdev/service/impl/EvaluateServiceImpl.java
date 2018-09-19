package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.mapper.EvaluateMapper;
import com.microdev.model.Evaluate;
import com.microdev.param.EvaluateParam;
import com.microdev.service.EvaluateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class EvaluateServiceImpl extends ServiceImpl<EvaluateMapper,Evaluate> implements EvaluateService {
    @Autowired
    EvaluateMapper evaluateMapper;
    @Override
    public ResultDO createEvaluate(List<EvaluateParam> req) throws Exception{
        System.out.println (req);
        if(req.size ()>0){
            evaluateMapper.deleteAll(req.get(0).getType (),req.get(0).getLevel ());
        }
        for (EvaluateParam request:req) {
            if(request.getLevel () == null || request.getText () == null || request.getType () == null){
                throw new ParamsException ("参数错误");
            }
            Evaluate evaluate = new Evaluate ();
            evaluate.setLevel (request.getLevel ());
            evaluate.setText (request.getText ());
            evaluate.setType (request.getType ());
            try{
                evaluateMapper.insert (evaluate);
            }catch(Exception e){
                e.printStackTrace ();
                throw new Exception ("已存在该标签，请勿重复添加");
            }
        }
        return ResultDO.buildSuccess ("添加成功");

    }

    @Override
    public ResultDO modifyEvaluate(EvaluateParam request) throws Exception {
        System.out.println (request);
        if(request.getId () == null || request.getText () == null){
            throw new ParamsException ("参数错误");
        }
        Evaluate evaluate = evaluateMapper.selectById (request.getId ());
        if(evaluate == null){
            throw new ParamsException ("查询不到该标签信息");
        }
        evaluate.setText (request.getText ());
        try{
            evaluateMapper.updateById (evaluate);
        }catch(Exception e){
            e.printStackTrace ();
            throw new Exception ("已存在该标签，修改失败");
        }
        return ResultDO.buildSuccess ("修改成功");
    }

    @Override
    public ResultDO listEvaluate(EvaluateParam request) {
        List<Evaluate> list = evaluateMapper.queryList (request);
        return ResultDO.buildSuccess (list);
    }

    @Override
    public ResultDO deleteEvaluate(List <String> list) {
        evaluateMapper.deleteBatchIds (list);
        return ResultDO.buildSuccess ("删除成功");
    }
}
