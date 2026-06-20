package com.ecnu.model.dto;

import com.ecnu.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentTaskPageRequestDTO extends PageRequest {

    /**
     * 任务状态：0-未开始 1-进行中 2-已完成（精确匹配，可选）
     */
    private Integer taskStatus;

    /**
     * 关联儿童ID（可选）
     */
    private Long childId;

    /**
     * 医生ID（可选，医生端查看自己的任务）
     */
    private Long doctorId;
}
