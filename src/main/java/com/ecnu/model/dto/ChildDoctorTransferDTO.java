package com.ecnu.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "儿童转接医生请求")
public class ChildDoctorTransferDTO {
    @Schema(description = "儿童ID")
    private Long id;

    @Schema(description = "新负责医生ID")
    private Long doctorId;
}
