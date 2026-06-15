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
@Schema(description = "视频资源返回")
public class VideoResourceVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "关联儿童ID")
    private Long childId;

    @Schema(description = "视频存储地址")
    private String videoUrl;

    @Schema(description = "视频名称")
    private String videoName;

    @Schema(description = "上传医生ID")
    private Long uploadDoctorId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
