package com.microdev.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.microdev.common.ResultDO;
import com.microdev.common.exception.ParamsException;
import com.microdev.common.paging.Paginator;
import com.microdev.converter.DictConverter;
import com.microdev.mapper.DictMapper;
import com.microdev.model.Dict;
import com.microdev.model.UserArea;
import com.microdev.param.DictDTO;
import com.microdev.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DictServiceImpl extends ServiceImpl<DictMapper,Dict> implements DictService{
    @Autowired
    DictMapper dictMapper;
    @Autowired
    private DictConverter dictConverter;
    @Override
    public DictDTO create(DictDTO params) {
        DictDTO oldDic= dictMapper.findByNameAndCode(params.getName(),params.getCode());
        if(oldDic!=null){
            throw new ParamsException("字典编码不能重复");
        }
        List<String> m = dictMapper.selectMaxCode();
        int b = 0;
        for (String a:m) {
            if(Integer.parseInt (a)>b){
                b = Integer.parseInt (a);
            }
        }
        Dict newDict = dictConverter.toDO(params);
        newDict.setCode (b + 1 + "");
        newDict.setDeleted(false);
        dictMapper.insert(newDict);
        DictDTO dictDTO = dictConverter.toDTO(newDict);
        dicts.put(dictDTO.getName() + "$" + dictDTO.getCode(), dictDTO);
        return dictDTO;
    }

    @Override
    public void delete(String id) {
        Dict dict = dictMapper.findOne(id);
        dicts.remove(dict.getName() + "$" + dict.getCode());
        dictMapper.delete(id);
    }

    @Override
    public void update(DictDTO dictDTO) {
        if(!StringUtils.hasLength(dictDTO.getPid ())){
            throw new ParamsException("请求参数有误：缺少id");
        }
        Dict newDict = dictMapper.findOne(dictDTO.getPid());
        newDict.setRemark(dictDTO.getRemark());
        newDict.setText(dictDTO.getText());
        newDict.setCode(dictDTO.getCode());
        newDict.setExtend (dictDTO.getExtend ());
        dictMapper.updateById(newDict);
        dicts.put(dictDTO.getName() + "$" + dictDTO.getCode(), dictDTO);
//        Collection<DictDTO> list = dictMap.values();
//        List<String> ids = list.stream().map(DictDTO::getId).collect(Collectors.toList());
//        Map<String, Dict> origMap = dictionaryRepository.findAll(ids).stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
//
//        List<Dict> newDicts = list.stream().map(it -> {
//            Dict orig = origMap.get(it.getId());
//            return dictConverter.toDO(orig, it);
//        }).collect(Collectors.toList());
//
//        dictionaryRepository.save(newDicts);
//        fill(dictionaryRepository.findAll(ids));
    }


    @Override
    public ResultDO findByName(String name) {
        List<Dict> list=dictMapper.findByName(name);
        Set<DictDTO> set=new HashSet<>();
        for (Dict item :list){
            set.add(dictConverter.toDTO(item));
        }
        return ResultDO.buildSuccess(set);
    }

    @Transactional(readOnly = true)
    @Override
    public DictDTO getById(String id) {
        return dictConverter.toDTO(dictMapper.findOne(id));
    }

    @Transactional(readOnly = true)
    @Override
    public ResultDO paging(Paginator paginator, DictDTO dictDTO) {
        PageHelper.startPage(paginator.getPage(),paginator.getPageSize());
        //查询数据集合
        List<Dict> list = dictMapper.queryDicts(dictDTO);
        PageInfo<Dict> pageInfo = new PageInfo<>(list);
        HashMap<String,Object> result = new HashMap<>();
        //设置获取到的总记录数total：
        result.put("total",pageInfo.getTotal());
        //设置数据集合rows：
        result.put("result",pageInfo.getList());
        result.put("page",paginator.getPage());
        return ResultDO.buildSuccess(result);
    }

    @Override
    public List<DictDTO> list() {
        return Optional.ofNullable(dictMapper.findAll()).orElse(new ArrayList<>())
                .stream()
                .map(o -> dictConverter.toDTO(o))
                .collect(Collectors.toList());
    }


    //TODO 先暂时缓存到内存中，未来要迁移到 Redis 中
    public static final Map<String, DictDTO> dicts = new HashMap<>();

    @PostConstruct
    public void init() {
        //fill(dictMapper.findAll());
        fill(null);
    }

    private void fill(List<Dict> list) {
        if(list!=null){
            Map<String, DictDTO> map = list.stream().collect(Collectors.toMap(dict -> (dict.getName() + "$" + dict.getCode()), dict -> dictConverter.toDTO(dict)));
            dicts.putAll(map);
        }
    }

    public static DictDTO findByNameAndCode(String name, String code) {
        return dicts.get(name + "$" + code);
    }

    public static DictDTO findFirst(Enum enumType) {
        return dicts.get(enumType.getClass().getSimpleName() + "$" + enumType.name());
    }

    public static List<DictDTO> findList(final String name) {
        return dicts.entrySet().stream()
                .filter(it -> it.getKey().startsWith(name + "$"))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public static List<String> findTextList(Enum[] es) {
        if (es == null) return null;
        return Arrays.stream(es)
                .map(it -> findByNameAndCode(it.getClass().getSimpleName(), it.name()).getText())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> selectServiceTypeByUserId(String id) {
        return dictMapper.selectTypeByUserId(id);
    }

    @Override
    public List <UserArea> findServiceArea(String id) {
        List<UserArea> list = dictMapper.findServiceArea (id);
        List<UserArea> result = new ArrayList<>();
        for (UserArea ua : list) {
           if(ua.getAreaLevel ()==1){
               result.add (ua);
           }else if(ua.getAreaLevel ()==2){
               ua.setAreaName (dictMapper.findSeriveAreaSecond (ua.getAreaId ()).getAreaName ()+"-"+ua.getAreaName ());
               result.add (ua);
           }else if(ua.getAreaLevel ()==3){
               ua.setAreaName (dictMapper.findSeriveAreaSecond (dictMapper.findSeriveAreaThird (ua.getAreaId ()).getAreaId ()).getAreaName ()+"-"+dictMapper.findSeriveAreaThird (ua.getAreaId ()).getAreaName ()+"-"+ua.getAreaName ());
               result.add (ua);
           }
        }
        return result;
    }
}
