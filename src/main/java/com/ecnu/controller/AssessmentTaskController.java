package com.ecnu.controller;

import com.ecnu.annotation.RequireRole;
import com.ecnu.common.BaseResponse;
import com.ecnu.common.DeleteRequest;
import com.ecnu.common.ResultUtils;
import com.ecnu.exception.ErrorCode;
import com.ecnu.exception.ThrowUtils;
import com.ecnu.model.dto.AssessmentForm1SubmitDTO;
import com.ecnu.model.dto.AssessmentForm2SubmitDTO;
import com.ecnu.model.dto.AssessmentTaskPublishDTO;
import com.ecnu.model.enums.RoleEnum;
import com.ecnu.model.vo.AssessmentForm1VO;
import com.ecnu.model.vo.AssessmentForm2VO;
import com.ecnu.model.vo.AssessmentTaskVO;
import com.ecnu.service.AssessmentTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task")
@Slf4j
@Tag(name = "评估任务接口")
public class AssessmentTaskController {

    @Resource
    private AssessmentTaskService assessmentTaskService;

    /**
     * 发布评估任务
     */
    @PostMapping("/publish")
    @Operation(summary = "发布评估任务")
    @RequireRole({RoleEnum.ADMIN})
    public BaseResponse<Boolean> publish(@RequestBody AssessmentTaskPublishDTO publishDTO) {
        log.info("发布评估任务：{}", publishDTO);
        ThrowUtils.throwIf(publishDTO == null, ErrorCode.PARAMS_ERROR);
        boolean result = assessmentTaskService.publish(publishDTO);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取评估任务
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id获取评估任务")
    @RequireRole({RoleEnum.DOCTOR, RoleEnum.ADMIN})
    public BaseResponse<AssessmentTaskVO> getById(@PathVariable Long id) {
        log.info("获取评估任务：{}", id);
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        AssessmentTaskVO result = assessmentTaskService.getById(id);
        return ResultUtils.success(result);
    }

    /**
     * 根据任务ID获取一阶段表单
     */
    @GetMapping("/{taskId}/form1")
    @Operation(summary = "根据任务ID获取一阶段表单")
    @RequireRole({RoleEnum.DOCTOR, RoleEnum.ADMIN})
    public BaseResponse<AssessmentForm1VO> getForm1ByTaskId(@PathVariable Long taskId) {
        log.info("获取一阶段表单：taskId={}", taskId);
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR);
        AssessmentForm1VO result = assessmentTaskService.getForm1ByTaskId(taskId);
        return ResultUtils.success(result);
    }

    /**
     * 根据任务ID获取二阶段表单
     */
    @GetMapping("/{taskId}/form2")
    @Operation(summary = "根据任务ID获取二阶段表单")
    @RequireRole({RoleEnum.DOCTOR, RoleEnum.ADMIN})
    public BaseResponse<AssessmentForm2VO> getForm2ByTaskId(@PathVariable Long taskId) {
        log.info("获取二阶段表单：taskId={}", taskId);
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR);
        AssessmentForm2VO result = assessmentTaskService.getForm2ByTaskId(taskId);
        return ResultUtils.success(result);
    }

    /**
     * 开始任务：分配医生 + 更新状态为进行中
     */
    @PutMapping("/{id}/start")
    @Operation(summary = "开始任务")
    @RequireRole({RoleEnum.DOCTOR, RoleEnum.ADMIN})
    public BaseResponse<Boolean> startTask(@PathVariable Long id) {
        log.info("开始任务：{}", id);
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        boolean result = assessmentTaskService.startTask(id);
        return ResultUtils.success(result);
    }

    /**
     * 提交一阶段表单 + 更新 current_step = 2
     */
    @PostMapping("/sub_form1")
    @Operation(summary = "提交一阶段表单")
    @RequireRole({RoleEnum.DOCTOR, RoleEnum.ADMIN})
    public BaseResponse<Boolean> submitForm1(@RequestBody AssessmentForm1SubmitDTO dto) {
        log.info("提交一阶段表单：{}", dto);
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        boolean result = assessmentTaskService.submitForm1(dto);
        return ResultUtils.success(result);
    }

    /**
     * 提交二阶段表单 + 更新 task_status = 2
     */
    @PostMapping("/sub_form2")
    @Operation(summary = "提交二阶段表单")
    @RequireRole({RoleEnum.DOCTOR, RoleEnum.ADMIN})
    public BaseResponse<Boolean> submitForm2(@RequestBody AssessmentForm2SubmitDTO dto) {
        log.info("提交二阶段表单：{}", dto);
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        boolean result = assessmentTaskService.submitForm2(dto);
        return ResultUtils.success(result);
    }

    /**
     * 删除评估任务
     */
    @DeleteMapping
    @Operation(summary = "删除评估任务")
    @RequireRole({RoleEnum.ADMIN})
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest deleteRequest) {
        log.info("删除评估任务：{}", deleteRequest);
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = assessmentTaskService.delete(deleteRequest);
        return ResultUtils.success(result);
    }
}
