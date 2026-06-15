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
@Schema(description = "医生登录DTO")
public class DoctorLoginDTO {

    @Schema(description = "登录账号（手机号/邮箱）")
    private String account;

    @Schema(description = "密码（加密存储）")
    private String password;
}
