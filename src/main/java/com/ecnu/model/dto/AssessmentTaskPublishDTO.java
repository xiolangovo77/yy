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
@Schema(description = "发布评估任务DTO")
public class AssessmentTaskPublishDTO {

    @Schema(description = "关联儿童ID")
    private Long childId;

    @Schema(description = "评估任务编号（唯一）")
    private String taskNo;
}
