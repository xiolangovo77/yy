package com.ecnu.mapper;

import com.ecnu.annotation.AutoFill;
import com.ecnu.model.entity.AssessmentForm1;
import com.ecnu.model.enums.OperationType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AssessmentForm1Mapper {

    @AutoFill(OperationType.INSERT)
    boolean add(AssessmentForm1 assessmentForm1);

    /**
     * 根据任务ID获取一阶段表单
     */
    AssessmentForm1 getByTaskId(Long taskId);

    /**
     * 更新一阶段表单
     */
    @AutoFill(OperationType.UPDATE)
    boolean update(AssessmentForm1 assessmentForm1);

    /**
     * 根据任务ID删除一阶段表单（软删除）
     */
    boolean deleteByTaskId(Long taskId);
}
