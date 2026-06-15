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
@Schema(description = "报告返回")
public class ChildReportVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "关联儿童ID")
    private Long childId;

    @Schema(description = "报告编号（唯一）")
    private String reportNo;

    @Schema(description = "报告url")
    private String reportUrl;

    @Schema(description = "上传报告医生ID")
    private Long uploadDoctorId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
