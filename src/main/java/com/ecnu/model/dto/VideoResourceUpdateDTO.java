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
@Schema(description = "视频资源修改DTO")
public class VideoResourceUpdateDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "关联儿童ID")
    private Long childId;

    @Schema(description = "视频存储地址")
    private String videoUrl;

    @Schema(description = "视频名称")
    private String videoName;
}
