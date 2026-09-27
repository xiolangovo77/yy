package com.ecnu.model.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "医生完成评估后的报告评价")
public class AssessmentFeedbackSubmitDTO {
    @Schema(description = "评估任务ID")
    private Long taskId;

    @Schema(description = "报告是否能让您快速找到患者核心问题")
    private String coreProblemClarity;

    @Schema(description = "使用系统后是否能提高判断质量/效率")
    private String judgmentImprovement;

    @Schema(description = "报告中的观察事实是否准确")
    private String observationAccuracy;

    @Schema(description = "报告改进建议（选填，最多2000字）")
    private String suggestions;

}
