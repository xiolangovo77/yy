package com.ecnu.controller;

import com.ecnu.annotation.RequireRole;
import com.ecnu.common.BaseResponse;
import com.ecnu.common.DeleteRequest;
import com.ecnu.common.PageResponse;
import com.ecnu.common.ResultUtils;
import com.ecnu.context.BaseContext;
import com.ecnu.exception.ErrorCode;
import com.ecnu.exception.ThrowUtils;
import com.ecnu.model.dto.DoctorAddDTO;
import com.ecnu.model.dto.DoctorLoginDTO;
import com.ecnu.model.dto.DoctorPageRequestDTO;
import com.ecnu.model.dto.DoctorProfileUpdateDTO;
import com.ecnu.model.dto.DoctorRegisterDTO;
import com.ecnu.model.dto.DoctorStatusDTO;
import com.ecnu.model.dto.DoctorUpdateDTO;
import com.ecnu.model.enums.RoleEnum;
import com.ecnu.model.vo.DoctorLoginVO;
import com.ecnu.model.vo.DoctorVO;
import com.ecnu.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doctor")
@Slf4j
@Tag(name = "医生信息接口")
public class DoctorController {

    @Resource
    private DoctorService doctorService;

    /**
     * 添加医生
     * @param doctorAddDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "添加医生")
    @RequireRole(RoleEnum.ADMIN)
    public BaseResponse<Boolean> add(@RequestBody DoctorAddDTO doctorAddDTO) {
        log.info("添加医生：{}", doctorAddDTO);
        ThrowUtils.throwIf(doctorAddDTO == null, ErrorCode.PARAMS_ERROR);
        boolean result = doctorService.add(doctorAddDTO);
        return ResultUtils.success(result);
    }

    /**
     * 删除医生
     * @param deleteRequest
     * @return
     */
    @DeleteMapping
    @Operation(summary = "删除医生")
    @RequireRole(RoleEnum.ADMIN)
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest deleteRequest) {
        log.info("删除医生：{}", deleteRequest);
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = doctorService.delete(deleteRequest);
        return ResultUtils.success(result);
    }

    /**
     * 修改医生状态
     * @param doctorStatusDTO
     * @return
     */
    @PostMapping("/status")
    @Operation(summary = "修改医生状态")
    @RequireRole(RoleEnum.ADMIN)
    public BaseResponse<Boolean> updateStatus(@RequestBody DoctorStatusDTO doctorStatusDTO) {
        log.info("修改医生状态：{}", doctorStatusDTO);
        ThrowUtils.throwIf(doctorStatusDTO == null, ErrorCode.PARAMS_ERROR);
        boolean result = doctorService.updateStatus(doctorStatusDTO);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取医生信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id获取医生信息")
    public BaseResponse<DoctorVO> getById(@PathVariable Long id) {
        log.info("根据id获取医生信息：{}", id);
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        DoctorVO result = doctorService.getById(id);
        return ResultUtils.success(result);
    }

    /**
     * 修改医生信息
     * @param doctorUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "修改医生信息")
    @RequireRole(RoleEnum.ADMIN)
    public BaseResponse<Boolean> update(@RequestBody DoctorUpdateDTO doctorUpdateDTO) {
        log.info("修改医生信息：{}", doctorUpdateDTO);
        ThrowUtils.throwIf(doctorUpdateDTO == null, ErrorCode.PARAMS_ERROR);
        boolean result = doctorService.update(doctorUpdateDTO);
        return ResultUtils.success(result);
    }

    /**
     * 分页获取医生信息
     * @param pageRequest
     * @return
     */
    @PostMapping("/list")
    @Operation(summary = "分页获取医生信息")
    @RequireRole(RoleEnum.ADMIN)
    public BaseResponse<PageResponse> list(@RequestBody DoctorPageRequestDTO pageRequest) {
        log.info("分页获取医生信息：{}", pageRequest);
        ThrowUtils.throwIf(pageRequest == null, ErrorCode.PARAMS_ERROR);
        PageResponse result = doctorService.list(pageRequest);
        return ResultUtils.success(result);
    }

    /**
     * 医生修改个人资料（登录医生修改自己的信息）
     * @param profileUpdateDTO
     * @return
     */
    @PutMapping("/profile")
    @Operation(summary = "医生修改个人资料")
    public BaseResponse<Boolean> updateProfile(@RequestBody DoctorProfileUpdateDTO profileUpdateDTO) {
        log.info("医生修改个人资料：{}", profileUpdateDTO);
        ThrowUtils.throwIf(profileUpdateDTO == null, ErrorCode.PARAMS_ERROR);
        boolean result = doctorService.updateProfile(profileUpdateDTO);
        return ResultUtils.success(result);
    }

    /**
     * 医生注册（公开接口，自动分配普通医生角色）
     * @param doctorRegisterDTO
     * @return
     */
    @PostMapping("/register")
    @Operation(summary = "医生注册")
    public BaseResponse<Boolean> register(@RequestBody DoctorRegisterDTO doctorRegisterDTO) {
        log.info("医生注册：{}", doctorRegisterDTO);
        ThrowUtils.throwIf(doctorRegisterDTO == null, ErrorCode.PARAMS_ERROR);
        boolean result = doctorService.register(doctorRegisterDTO);
        return ResultUtils.success(result);
    }

    /**
     * 医生登录
     * @param doctorLoginDTO
     * @return
     */
    @PostMapping("/login")
    @Operation(summary = "医生登录")
    public BaseResponse<DoctorLoginVO> login(@RequestBody DoctorLoginDTO doctorLoginDTO) {
        log.info("医生登录：{}", doctorLoginDTO);
        ThrowUtils.throwIf(doctorLoginDTO == null, ErrorCode.PARAMS_ERROR);
        DoctorLoginVO result = doctorService.login(doctorLoginDTO);
        return ResultUtils.success(result);
    }

    /**
     * 医生退出
     * @return
     */
    @PostMapping("/logout")
    @Operation(summary = "医生退出")
    public BaseResponse<Boolean> logout() {
        Long id = BaseContext.getCurrentId();
        log.info("医生退出：{}", id);
        boolean result = doctorService.logout();
        return ResultUtils.success(result);
    }
}
