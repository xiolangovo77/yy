package com.ecnu.controller;

import com.ecnu.annotation.RequireRole;
import com.ecnu.common.BaseResponse;
import com.ecnu.common.DeleteRequest;
import com.ecnu.common.ResultUtils;
import com.ecnu.exception.ErrorCode;
import com.ecnu.exception.ThrowUtils;
import com.ecnu.model.dto.VideoResourceAddDTO;
import com.ecnu.model.dto.VideoResourceUpdateDTO;
import com.ecnu.model.enums.RoleEnum;
import com.ecnu.model.vo.VideoResourceVO;
import com.ecnu.service.VideoResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/video")
@Slf4j
@Tag(name = "视频资源接口")
public class VideoResourceController {

    @Resource
    private VideoResourceService videoResourceService;

    /**
     * 添加视频资源
     * @param videoResourceAddDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "添加视频资源")
    @RequireRole({RoleEnum.ADMIN})
    public BaseResponse<Boolean> add(@RequestBody VideoResourceAddDTO videoResourceAddDTO) {
        log.info("添加视频资源：{}", videoResourceAddDTO);
        ThrowUtils.throwIf(videoResourceAddDTO == null, ErrorCode.PARAMS_ERROR);
        boolean result = videoResourceService.add(videoResourceAddDTO);
        return ResultUtils.success(result);
    }

    /**
     * 删除视频资源
     * @param deleteRequest
     * @return
     */
    @DeleteMapping
    @Operation(summary = "删除视频资源")
    @RequireRole({RoleEnum.ADMIN})
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest deleteRequest) {
        log.info("删除视频资源：{}", deleteRequest);
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = videoResourceService.delete(deleteRequest);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取视频资源
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id获取视频资源")
    @RequireRole({RoleEnum.DOCTOR, RoleEnum.ADMIN})
    public BaseResponse<VideoResourceVO> getById(@PathVariable Long id) {
        log.info("根据id获取视频资源：{}", id);
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        VideoResourceVO result = videoResourceService.getById(id);
        return ResultUtils.success(result);
    }

    /**
     * 修改视频资源
     * @param videoResourceUpdateDTO
     * @return
     */
    @PutMapping
    @Operation(summary = "修改视频资源")
    @RequireRole({RoleEnum.ADMIN})
    public BaseResponse<Boolean> update(@RequestBody VideoResourceUpdateDTO videoResourceUpdateDTO) {
        log.info("修改视频资源：{}", videoResourceUpdateDTO);
        ThrowUtils.throwIf(videoResourceUpdateDTO == null, ErrorCode.PARAMS_ERROR);
        boolean result = videoResourceService.update(videoResourceUpdateDTO);
        return ResultUtils.success(result);
    }


}
