package com.ecnu.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "一阶段表单提交DTO")
public class AssessmentForm1SubmitDTO {

    @Schema(description = "评估任务ID")
    private Long taskId;

    @Schema(description = "疾病判定 ASD/TD/DD")
    private String diseaseType;

    @Schema(description = "诊断信心 0-100")
    private Integer confidence;

    @Schema(description = "诊断依据")
    private String diagnosticBasis;

    @Schema(description = "医生补充备注")
    private String doctorRemark;
}
