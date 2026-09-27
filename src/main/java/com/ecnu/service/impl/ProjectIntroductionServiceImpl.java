package com.ecnu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.ecnu.exception.ErrorCode;
import com.ecnu.exception.ThrowUtils;
import com.ecnu.mapper.ProjectIntroductionMapper;
import com.ecnu.model.dto.ProjectIntroductionUpdateDTO;
import com.ecnu.model.entity.ProjectIntroduction;
import com.ecnu.model.vo.ProjectIntroductionVO;
import com.ecnu.service.ProjectIntroductionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectIntroductionServiceImpl implements ProjectIntroductionService {
    @Resource
    private ProjectIntroductionMapper projectIntroductionMapper;

    @Override
    public ProjectIntroductionVO get() {
        ProjectIntroduction introduction = projectIntroductionMapper.get();
        return introduction == null ? null : BeanUtil.toBean(introduction, ProjectIntroductionVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectIntroductionVO save(ProjectIntroductionUpdateDTO dto) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(dto.getTitle()), ErrorCode.PARAMS_ERROR, "项目介绍标题不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(dto.getContent()), ErrorCode.PARAMS_ERROR, "项目介绍正文不能为空");
        ThrowUtils.throwIf(dto.getTitle().length() > 120, ErrorCode.PARAMS_ERROR, "标题不能超过120字");
        ThrowUtils.throwIf(dto.getContent().length() > 20000, ErrorCode.PARAMS_ERROR, "正文不能超过20000字");
        ProjectIntroduction introduction = new ProjectIntroduction();
        introduction.setTitle(dto.getTitle().trim());
        introduction.setContent(dto.getContent().trim());
        projectIntroductionMapper.save(introduction);
        return get();
    }
}
