package com.ecnu.controller;

import com.ecnu.annotation.RequireRole;
import com.ecnu.common.BaseResponse;
import com.ecnu.common.DeleteRequest;
import com.ecnu.common.PageResponse;
import com.ecnu.common.ResultUtils;
import com.ecnu.exception.ErrorCode;
import com.ecnu.exception.ThrowUtils;
import com.ecnu.model.dto.ChildInfoAddDTO;
import com.ecnu.model.dto.ChildDoctorTransferDTO;
import com.ecnu.model.dto.ChildInfoPageRequestDTO;
import com.ecnu.model.dto.ChildInfoUpdateDTO;
import com.ecnu.model.enums.RoleEnum;
import com.ecnu.model.vo.ChildInfoVO;
import com.ecnu.service.ChildInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/child")
@Slf4j
@Tag(name = "儿童信息接口")
public class ChildInfoController {

    @Resource
    private ChildInfoService childInfoService;

    @PutMapping("/transfer")
    @Operation(summary = "转接儿童负责医生")
    @RequireRole({RoleEnum.ADMIN})
    public BaseResponse<Boolean> transferDoctor(@RequestBody ChildDoctorTransferDTO request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(childInfoService.transferDoctor(request));
    }

    /**
     * 添加儿童信息
     * @param childInfoAddDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "添加儿童信息")
    @RequireRole({RoleEnum.ADMIN})
    public BaseResponse<Boolean> add(@RequestBody ChildInfoAddDTO childInfoAddDTO) {
        log.info("添加儿童信息：{}", childInfoAddDTO);
        ThrowUtils.throwIf(childInfoAddDTO == null, ErrorCode.PARAMS_ERROR);
        boolean result = childInfoService.add(childInfoAddDTO);
        return ResultUtils.success(result);
    }

    /**
     * 删除儿童信息
     * @param deleteRequest
     * @return
     */
    @DeleteMapping
    @Operation(summary = "删除儿童信息")
    @RequireRole({RoleEnum.ADMIN})
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest deleteRequest) {
        log.info("删除儿童信息：{}", deleteRequest);
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = childInfoService.delete(deleteRequest);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取儿童信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id获取儿童信息")
    @RequireRole({RoleEnum.DOCTOR, RoleEnum.ADMIN})
    public BaseResponse<ChildInfoVO> getById(@PathVariable Long id) {
        log.info("根据id获取儿童信息：{}", id);
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        ChildInfoVO result = childInfoService.getById(id);
        return ResultUtils.success(result);
    }

    /**
     * 修改儿童信息
     * @param childInfoUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "修改儿童信息")
    @RequireRole({RoleEnum.ADMIN})
    public BaseResponse<Boolean> update(@RequestBody ChildInfoUpdateDTO childInfoUpdateDTO) {
        log.info("修改儿童信息：{}", childInfoUpdateDTO);
        ThrowUtils.throwIf(childInfoUpdateDTO == null, ErrorCode.PARAMS_ERROR);
        boolean result = childInfoService.update(childInfoUpdateDTO);
        return ResultUtils.success(result);
    }

    /**
     * 分页获取儿童信息
     * @param pageRequest
     * @return
     */
    @PostMapping("/list")
    @Operation(summary = "分页获取儿童信息")
    @RequireRole({RoleEnum.DOCTOR, RoleEnum.ADMIN})
    public BaseResponse<PageResponse> list(@RequestBody ChildInfoPageRequestDTO pageRequest) {
        log.info("分页获取儿童信息：{}", pageRequest);
        ThrowUtils.throwIf(pageRequest == null, ErrorCode.PARAMS_ERROR);
        PageResponse result = childInfoService.list(pageRequest);
        return ResultUtils.success(result);
    }
}
