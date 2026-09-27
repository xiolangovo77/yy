package com.ecnu.model.dto;

import com.ecnu.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VideoResourcePageRequestDTO extends PageRequest {

    /**
     * 关联儿童ID（可选）
     */
    private Long childId;

    /** 仅返回名称以 main 开头的视频（不区分大小写） */
    private Boolean mainOnly;

    /** 服务端设置的医生访问范围 */
    private Long doctorId;
}
