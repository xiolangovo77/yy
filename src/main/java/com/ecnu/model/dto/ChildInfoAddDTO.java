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
@Schema(description = "儿童信息添加DTO")
public class ChildInfoAddDTO {

    @Schema(description = "儿童编号（唯一）")
    private String childNo;

    @Schema(description = "儿童姓名")
    private String childName;

    @Schema(description = "负责医生ID")
    private Long doctorId;

    @Schema(description = "年龄（岁）")
    private Integer age;

    @Schema(description = "性别（男/女/未提供）")
    private String gender;

    @Schema(description = "评估方式")
    private String assessType;

    @Schema(description = "补充备注")
    private String remark;
}
