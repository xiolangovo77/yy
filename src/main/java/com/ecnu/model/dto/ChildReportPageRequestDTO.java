package com.ecnu.model.dto;

import com.ecnu.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChildReportPageRequestDTO extends PageRequest {

    /**
     * 关联儿童ID（可选）
     */
    private Long childId;
}
