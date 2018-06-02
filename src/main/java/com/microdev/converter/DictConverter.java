package com.microdev.converter;

import com.microdev.model.Dict;
import com.microdev.param.DictDTO;
import org.springframework.context.annotation.Configuration;

/**
 * Created by LTF on 2017/5/4 0004.
 */
@Configuration
public class DictConverter {


    public Dict toDO(DictDTO dictDTO) {
        Dict dict = new Dict();
        dict.setName(dictDTO.getName());
        dict.setCode(dictDTO.getCode());
        dict.setText(dictDTO.getText());
        dict.setOrdinal(dictDTO.getOrdinal());
        dict.setExtend(dictDTO.getExtend());
        if(dictDTO.getRemark()!=null){
            dict.setRemark(dictDTO.getRemark());
        }
        return dict;
    }

    public DictDTO toDTO(Dict dict) {
        DictDTO dictDTO = new DictDTO();
        dictDTO.setPid(dict.getPid());
        dictDTO.setName(dict.getName());
        dictDTO.setCode(dict.getCode());
        dictDTO.setText(dict.getText());
        if(dict.getOrdinal()!=null){
            dictDTO.setOrdinal(dict.getOrdinal());
        }
        if(dict.getExtend()!=null){
            dictDTO.setExtend(dict.getExtend());
        }
        if(dict.getRemark()!=null){
            dictDTO.setRemark(dict.getRemark());
        }
        return dictDTO;
    }
}
