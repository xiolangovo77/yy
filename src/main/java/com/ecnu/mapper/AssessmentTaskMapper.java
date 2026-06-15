package com.ecnu.mapper;

import com.ecnu.annotation.AutoFill;
import com.ecnu.model.entity.AssessmentTask;
import com.ecnu.model.enums.OperationType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AssessmentTaskMapper {

    @AutoFill(OperationType.INSERT)
    boolean add(AssessmentTask assessmentTask);

    /**
     * 根据id获取评估任务
     */
    AssessmentTask getById(Long id);

    /**
     * 开始任务：分配医生 + 更新状态为进行中
     */
    boolean updateStart(Long id, Long doctorId);

    /**
     * 更新当前步骤
     */
    boolean updateCurrentStep(Long id, Integer currentStep);

    /**
     * 更新任务状态
     */
    boolean updateStatus(Long id, Integer taskStatus);

    /**
     * 根据id删除评估任务（软删除）
     */
    boolean deleteById(Long id);
}
