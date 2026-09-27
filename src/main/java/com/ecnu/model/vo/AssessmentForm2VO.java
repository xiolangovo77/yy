package com.ecnu.model.vo;

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
@Schema(description = "第二阶段评估表单返回")
public class AssessmentForm2VO {

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

    @Schema(description = "报告有用程度（1-5分，程度递增）")
    private Integer reportUsefulness;

    @Schema(description = "报告证据有效性（1-5分，程度递增）")
    private Integer evidenceValidity;

    @Schema(description = "事实是否准确（1-5分，程度递增）")
    private Integer factAccuracy;


    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
