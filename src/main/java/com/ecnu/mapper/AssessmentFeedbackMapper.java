package com.ecnu.mapper;

import com.ecnu.annotation.AutoFill;
import com.ecnu.model.entity.AssessmentFeedback;
import com.ecnu.model.enums.OperationType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AssessmentFeedbackMapper {
    @AutoFill(OperationType.INSERT)
    boolean add(AssessmentFeedback feedback);

    AssessmentFeedback getByTaskId(Long taskId);

    boolean existsByTaskId(Long taskId);

    boolean deleteByTaskId(Long taskId);
}
