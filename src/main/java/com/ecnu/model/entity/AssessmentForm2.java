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
@Schema(description = "第二阶段评估表单表")
public class AssessmentForm2 {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "关联评估任务ID")
    private Long taskId;

    @Schema(description = "关联儿童ID")
    private Long childId;

    @Schema(description = "最终疾病判定 ASD/TD/DD")
    private String diseaseType;

    @Schema(description = "最终诊断信心 0-100")
    private Integer confidence;

    @Schema(description = "最终诊断依据")
    private String diagnosticBasis;

    @Schema(description = "最终诊断结论")
    private String finalConclusion;

    @Schema(description = "医生补充备注")
    private String doctorRemark;

    @Hidden
    @Schema(description = "软删除标记 0-未删除 1-已删除")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
