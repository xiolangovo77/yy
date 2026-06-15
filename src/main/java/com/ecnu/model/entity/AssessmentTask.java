package com.ecnu.model.entity;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评估任务表")
public class AssessmentTask {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "负责医生ID")
    private Long doctorId;

    @Schema(description = "关联儿童ID")
    private Long childId;

    @Schema(description = "评估任务编号（唯一）")
    private String taskNo;

    @Schema(description = "完成状态 0-未开始 1-进行中 2-已完成")
    private Integer taskStatus;

    @Schema(description = "当前步骤 1-第一阶段表单 2-第二阶段表单")
    private Integer currentStep;

    @Hidden
    @Schema(description = "软删除标记 0-未删除 1-已删除")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
