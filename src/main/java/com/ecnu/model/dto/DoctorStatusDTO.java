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
@Schema(description = "医生状态修改DTO")
public class DoctorStatusDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "账号状态 0-禁用 1-正常")
    private Integer status;
}
