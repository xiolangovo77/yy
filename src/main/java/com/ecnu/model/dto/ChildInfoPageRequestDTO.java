package com.ecnu.model.dto;

import com.ecnu.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChildInfoPageRequestDTO extends PageRequest {

    /**
     * 儿童姓名（模糊搜索）
     */
    private String childName;

    /**
     * 儿童编号（模糊搜索）
     */
    private String childNo;

    /**
     * 评估方式（精确匹配）
     */
    private String assessType;
}
