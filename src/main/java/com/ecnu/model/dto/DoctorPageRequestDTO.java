package com.ecnu.model.dto;

import com.ecnu.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DoctorPageRequestDTO extends PageRequest {

    /**
     * 医生姓名（模糊搜索）
     */
    private String doctorName;

    /**
     * 登录账号（模糊搜索）
     */
    private String account;

    /**
     * 角色类型（精确匹配）
     */
    private Long role;
}
