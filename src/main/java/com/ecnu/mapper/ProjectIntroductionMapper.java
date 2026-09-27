package com.ecnu.mapper;

import com.ecnu.model.entity.ProjectIntroduction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectIntroductionMapper {
    ProjectIntroduction get();

    void save(ProjectIntroduction introduction);
}
