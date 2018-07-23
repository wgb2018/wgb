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
        List<Evaluate> l1 = new ArrayList <> ();
        List<Evaluate> l2 = new ArrayList <> ();
        List<Evaluate> l3 = new ArrayList <> ();
        List<Evaluate> l4 = new ArrayList <> ();
        List<Evaluate> l5 = new ArrayList <> ();
        List<Evaluate> t1 = new ArrayList <> ();
        List<Evaluate> t2 = new ArrayList <> ();
        List<Evaluate> t3 = new ArrayList <> ();
        List<Evaluate> t4 = new ArrayList <> ();
        List<Evaluate> t5 = new ArrayList <> ();
        Map<String,Object> map1 = new HashMap<> ();
        Map<String,Object> map2 = new HashMap<> ();
        Map<String,Object> map3 = new HashMap<> ();
        Map<String,Object> map4 = new HashMap<> ();
        Map<String,Object> map5 = new HashMap<> ();
        Map<String,Object> map6 = new HashMap<> ();
        Map<String,Object> map7 = new HashMap<> ();
        Map<String,Object> map8 = new HashMap<> ();
        Map<String,Object> map9 = new HashMap<> ();
        Map<String,Object> map0 = new HashMap<> ();
        List<Map<String,Object>> ls = new ArrayList <> ();
        for (Evaluate e:list) {
            if(e.getType () == 0){
                switch (e.getLevel ()){
                    case 1 : l1.add (e); break;
                    case 2 : l2.add (e); break;
                    case 3 : l3.add (e); break;
                    case 4 : l4.add (e); break;
                    case 5 : l5.add (e);
                }

            }else if(e.getType () == 1){
                switch (e.getLevel ()){
                    case 1 : t1.add (e); break;
                    case 2 : t2.add (e); break;
                    case 3 : t3.add (e); break;
                    case 4 : t4.add (e); break;
                    case 5 : t5.add (e);
                }
            }
        }
        map1.put ("list",l1);
        map1.put ("type","worker1");
        map2.put ("list",l2);
        map2.put ("type","worker2");
        map3.put ("list",l3);
        map3.put ("type","worker3");
        map4.put ("list",l4);
        map4.put ("type","worker4");
        map5.put ("list",l5);
        map5.put ("type","worker5");
        map6.put ("list",t1);
        map6.put ("type","hr1");
        map7.put ("list",t2);
        map7.put ("type","hr2");
        map8.put ("list",t3);
        map8.put ("type","hr3");
        map9.put ("list",t4);
        map9.put ("type","hr4");
        map0.put ("list",t5);
        map0.put ("type","hr5");
        ls.add (map1);
        ls.add (map2);
        ls.add (map3);
        ls.add (map4);
        ls.add (map5);
        ls.add (map6);
        ls.add (map7);
        ls.add (map8);
        ls.add (map9);
        ls.add (map0);
        return ResultDO.buildSuccess (ls);
    }

    @Override
    public ResultDO deleteEvaluate(List <String> list) {
        evaluateMapper.deleteBatchIds (list);
        return ResultDO.buildSuccess ("删除成功");
    }
}
