package com.ecnu.model.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Data
@Schema(description = "医生完成评估后的报告评价")
public class AssessmentFeedbackVO {
    @Schema(description = "主键ID")
    private Long id;

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

    @Schema(description = "儿童ID")
    private Long childId;

    @Schema(description = "评价医生ID")
    private Long doctorId;

    @Schema(description = "提交时间")
    private LocalDateTime createTime;

}
