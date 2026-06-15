package com.ecnu.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "医生登录返回")
public class DoctorLoginVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "登录账号")
    private String account;

    @Schema(description = "角色类型 0-普通医生 1-管理员")
    private Long role;

    @Schema(description = "jwt令牌")
    private String token;
}
