package com.ecnu.utils;

import com.ecnu.exception.BusinessException;
import com.ecnu.exception.ErrorCode;

/** 原 text 字段的新提交格式；历史自由文本读取时保持原样。 */
public final class RepetitiveBehaviorValue {
    private RepetitiveBehaviorValue() {}

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择有无重复刻板或感觉异常行为");
        }
        String normalized = value.trim();
        if ("无".equals(normalized) || normalized.matches("有，严重程度：[1-5]分")) {
            return normalized;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR,
                "重复刻板或感觉异常行为请选择有或无；选择有时，严重程度必须为1-5分");
    }
}
