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
@Schema(description = "视频资源添加DTO")
public class VideoResourceAddDTO {

    @Schema(description = "关联儿童ID")
    private Long childId;

    @Schema(description = "视频存储地址")
    private String videoUrl;

    @Schema(description = "视频名称")
    private String videoName;
}
