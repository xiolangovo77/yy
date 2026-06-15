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
@Schema(description = "医生个人资料修改DTO")
public class DoctorProfileUpdateDTO {

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "职称（主任医师/主治医师等）")
    private String title;

    @Schema(description = "所在医院")
    private String hospital;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "新密码（选填，不填则保留原密码）")
    private String password;
}
