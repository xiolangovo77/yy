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
@Schema(description = "报告添加DTO")
public class ChildReportAddDTO {

    @Schema(description = "关联儿童ID")
    private Long childId;

    @Schema(description = "报告编号（唯一）")
    private String reportNo;

    @Schema(description = "报告url")
    private String reportUrl;
}
