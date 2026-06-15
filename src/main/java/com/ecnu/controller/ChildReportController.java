package com.ecnu.controller;

import com.ecnu.annotation.RequireRole;
import com.ecnu.common.BaseResponse;
import com.ecnu.common.DeleteRequest;
import com.ecnu.common.ResultUtils;
import com.ecnu.exception.ErrorCode;
import com.ecnu.exception.ThrowUtils;
import com.ecnu.model.dto.ChildReportAddDTO;
import com.ecnu.model.dto.ChildReportUpdateDTO;
import com.ecnu.model.enums.RoleEnum;
import com.ecnu.model.vo.ChildReportVO;
import com.ecnu.service.ChildReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@Slf4j
@Tag(name = "儿童筛查报告接口")
public class ChildReportController {

    @Resource
    private ChildReportService childReportService;

    /**
     * 添加报告
     */
    @PostMapping
    @Operation(summary = "添加报告")
    @RequireRole({RoleEnum.ADMIN})
    public BaseResponse<Boolean> add(@RequestBody ChildReportAddDTO childReportAddDTO) {
        log.info("添加报告：{}", childReportAddDTO);
        ThrowUtils.throwIf(childReportAddDTO == null, ErrorCode.PARAMS_ERROR);
        boolean result = childReportService.add(childReportAddDTO);
        return ResultUtils.success(result);
    }

    /**
     * 删除报告
     */
    @DeleteMapping
    @Operation(summary = "删除报告")
    @RequireRole({RoleEnum.ADMIN})
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest deleteRequest) {
        log.info("删除报告：{}", deleteRequest);
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = childReportService.delete(deleteRequest);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取报告
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id获取报告")
    @RequireRole({RoleEnum.DOCTOR, RoleEnum.ADMIN})
    public BaseResponse<ChildReportVO> getById(@PathVariable Long id) {
        log.info("根据id获取报告：{}", id);
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        ChildReportVO result = childReportService.getById(id);
        return ResultUtils.success(result);
    }

    /**
     * 修改报告
     */
    @PutMapping
    @Operation(summary = "修改报告")
    @RequireRole({RoleEnum.ADMIN})
    public BaseResponse<Boolean> update(@RequestBody ChildReportUpdateDTO childReportUpdateDTO) {
        log.info("修改报告：{}", childReportUpdateDTO);
        ThrowUtils.throwIf(childReportUpdateDTO == null, ErrorCode.PARAMS_ERROR);
        boolean result = childReportService.update(childReportUpdateDTO);
        return ResultUtils.success(result);
    }
}
