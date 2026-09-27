package com.ecnu.service;

import com.ecnu.model.dto.ProjectIntroductionUpdateDTO;
import com.ecnu.model.vo.ProjectIntroductionVO;

public interface ProjectIntroductionService {
    ProjectIntroductionVO get();

    ProjectIntroductionVO save(ProjectIntroductionUpdateDTO dto);
}
