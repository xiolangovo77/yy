package com.ecnu.controller;

import com.ecnu.annotation.RequireRole;
import com.ecnu.common.BaseResponse;
import com.ecnu.common.ResultUtils;
import com.ecnu.model.dto.ProjectIntroductionUpdateDTO;
import com.ecnu.model.enums.RoleEnum;
import com.ecnu.model.vo.ProjectIntroductionVO;
import com.ecnu.service.ProjectIntroductionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project-introduction")
@Tag(name = "项目介绍接口")
public class ProjectIntroductionController {
    @Resource
    private ProjectIntroductionService projectIntroductionService;

    @GetMapping
    @RequireRole({RoleEnum.DOCTOR, RoleEnum.ADMIN})
    @Operation(summary = "读取面向评估医生的项目介绍")
    public BaseResponse<ProjectIntroductionVO> get() {
        return ResultUtils.success(projectIntroductionService.get());
    }

    @PutMapping
    @RequireRole({RoleEnum.ADMIN})
    @Operation(summary = "管理员修改项目介绍")
    public BaseResponse<ProjectIntroductionVO> save(@RequestBody ProjectIntroductionUpdateDTO dto) {
        return ResultUtils.success(projectIntroductionService.save(dto));
    }
}
