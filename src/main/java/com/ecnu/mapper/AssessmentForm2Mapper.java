package com.ecnu.mapper;

import com.ecnu.annotation.AutoFill;
import com.ecnu.model.entity.AssessmentForm2;
import com.ecnu.model.enums.OperationType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AssessmentForm2Mapper {

    @AutoFill(OperationType.INSERT)
    boolean add(AssessmentForm2 assessmentForm2);

    /**
     * 根据任务ID获取二阶段表单
     */
    AssessmentForm2 getByTaskId(Long taskId);

    /**
     * 更新二阶段表单
     */
    @AutoFill(OperationType.UPDATE)
    boolean update(AssessmentForm2 assessmentForm2);

    /**
     * 根据任务ID删除二阶段表单（软删除）
     */
    boolean deleteByTaskId(Long taskId);
}
