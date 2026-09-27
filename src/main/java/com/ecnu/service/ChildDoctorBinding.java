package com.ecnu.service;

import com.ecnu.context.BaseContext;
import com.ecnu.exception.ErrorCode;
import com.ecnu.exception.ThrowUtils;
import com.ecnu.mapper.DoctorMapper;
import com.ecnu.model.entity.Doctor;
import com.ecnu.model.enums.RoleEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/** Shared validation for child assignments and assessment ownership. */
@Service
public class ChildDoctorBinding {
    @Resource
    private DoctorMapper doctorMapper;

    public Doctor requireAssignableDoctor(Long doctorId) {
        ThrowUtils.throwIf(doctorId == null, ErrorCode.PARAMS_ERROR, "请选择负责医生");
        Doctor doctor = doctorMapper.getByIdForUpdate(doctorId);
        ThrowUtils.throwIf(doctor == null, ErrorCode.PARAMS_ERROR, "负责医生不存在或已删除");
        ThrowUtils.throwIf(!RoleEnum.DOCTOR.getCode().equals(doctor.getRole()),
                ErrorCode.PARAMS_ERROR, "负责医生必须为普通医生");
        ThrowUtils.throwIf(!Integer.valueOf(1).equals(doctor.getStatus()),
                ErrorCode.PARAMS_ERROR, "负责医生已被禁用，请选择正常状态的医生");
        return doctor;
    }

    /** Administrators may query all records; doctors are always scoped by server identity. */
    public Long currentDoctorScope() {
        Long id = BaseContext.getCurrentId();
        ThrowUtils.throwIf(id == null, ErrorCode.NOT_LOGIN_ERROR);
        Doctor doctor = doctorMapper.getById(id);
        ThrowUtils.throwIf(doctor == null || !Integer.valueOf(1).equals(doctor.getStatus()),
                ErrorCode.NO_AUTH_ERROR);
        if (RoleEnum.ADMIN.getCode().equals(doctor.getRole())) {
            return null;
        }
        ThrowUtils.throwIf(!RoleEnum.DOCTOR.getCode().equals(doctor.getRole()), ErrorCode.NO_AUTH_ERROR);
        return id;
    }

    public void checkReadAccess(Long doctorId) {
        Long scope = currentDoctorScope();
        ThrowUtils.throwIf(scope != null && !scope.equals(doctorId),
                ErrorCode.NO_AUTH_ERROR, "只能访问分配给自己的儿童及评估任务");
    }

    public void checkAssessmentAccess(Long doctorId) {
        Long scope = currentDoctorScope();
        ThrowUtils.throwIf(scope == null || !scope.equals(doctorId),
                ErrorCode.NO_AUTH_ERROR, "只有负责医生可以开始或提交评估");
    }
}
