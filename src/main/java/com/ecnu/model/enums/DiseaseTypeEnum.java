package com.ecnu.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 疾病判定枚举
 */
@Getter
@Schema(description = "疾病判定枚举")
public enum DiseaseTypeEnum {

    ASD("ASD", "自闭症谱系障碍"),
    TD("TD", "典型发育"),
    DD("DD", "发育迟缓");

    @Schema(description = "疾病编码")
    private final String code;

    @Schema(description = "疾病名称")
    private final String name;

    DiseaseTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据编码校验是否合法
     */
    public static boolean isValid(String code) {
        if (code == null) return false;
        for (DiseaseTypeEnum e : values()) {
            if (e.getCode().equalsIgnoreCase(code)) return true;
        }
        return false;
    }
}
