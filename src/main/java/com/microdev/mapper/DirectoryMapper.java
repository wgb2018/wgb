package com.microdev.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.microdev.model.Directory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectoryMapper extends BaseMapper<Directory> {
    List<Directory> selectByDirectory(Directory directory);
}
