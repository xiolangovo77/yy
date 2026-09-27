package com.ecnu.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "修改面向评估医生的项目介绍")
public class ProjectIntroductionUpdateDTO {
    @Schema(description = "标题，最多120字")
    private String title;

    @Schema(description = "纯文本正文，最多20000字，保留换行")
    private String content;
}
