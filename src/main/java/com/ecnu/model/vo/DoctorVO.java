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
@Schema(description = "医生信息返回")
public class DoctorVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "职称（主任医师/主治医师等）")
    private String title;

    @Schema(description = "所在医院")
    private String hospital;

    @Schema(description = "登录账号（手机号/邮箱）")
    private String account;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "角色类型 0-普通医生 1-管理员")
    private Long role;

    @Schema(description = "账号状态 0-禁用 1-正常")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
