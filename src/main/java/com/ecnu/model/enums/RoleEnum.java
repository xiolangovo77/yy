package com.ecnu.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 医生角色枚举
 * 对应 sys_doctor 表 role 字段
 */
@Getter
@Schema(description = "医生角色枚举")
public enum RoleEnum {

    DOCTOR(0L, "普通医生"),
    ADMIN(1L, "管理员");

    @Schema(description = "角色编码")
    private final Long code;

    @Schema(description = "角色名称")
    private final String name;

    RoleEnum(Long code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据角色编码获取枚举
     * @param code 角色编码
     * @return 对应的角色枚举，找不到返回null
     */
    public static RoleEnum getByCode(Long code) {
        if (code == null) {
            return null;
        }
        for (RoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }

    /**
     * 根据角色编码获取角色名称
     * @param code 角色编码
     * @return 角色名称，找不到返回空字符串
     */
    public static String getNameByCode(Long code) {
        RoleEnum role = getByCode(code);
        return role != null ? role.getName() : "";
    }
}