package com.ecnu.service;

import com.ecnu.common.DeleteRequest;
import com.ecnu.common.PageResponse;
import com.ecnu.model.dto.AssessmentForm1SubmitDTO;
import com.ecnu.model.dto.AssessmentForm2SubmitDTO;
import com.ecnu.model.dto.AssessmentTaskPageRequestDTO;
import com.ecnu.model.dto.AssessmentTaskPublishDTO;
import com.ecnu.model.vo.AssessmentForm1VO;
import com.ecnu.model.vo.AssessmentForm2VO;
import com.ecnu.model.vo.AssessmentTaskVO;

public interface AssessmentTaskService {

    /**
     * 发布评估任务（同时创建 task + form1 + form2）
     */
    boolean publish(AssessmentTaskPublishDTO publishDTO);

    /**
     * 删除评估任务（软删除 task + form1 + form2）
     */
    boolean delete(DeleteRequest deleteRequest);

    /**
     * 根据id获取评估任务
     */
    AssessmentTaskVO getById(Long id);

    /**
     * 分页获取评估任务列表
     */
    PageResponse list(AssessmentTaskPageRequestDTO pageRequest);

    /**
     * 根据任务ID获取一阶段表单
     */
    AssessmentForm1VO getForm1ByTaskId(Long taskId);

    /**
     * 根据任务ID获取二阶段表单
     */
    AssessmentForm2VO getForm2ByTaskId(Long taskId);

    /**
     * 开始任务：分配医生 + 更新状态为进行中
     */
    boolean startTask(Long id);

    /**
     * 提交一阶段表单 + 更新 current_step = 2
     */
    boolean submitForm1(AssessmentForm1SubmitDTO dto);

    /**
     * 提交二阶段表单 + 更新 task_status = 2（已完成）
     */
    boolean submitForm2(AssessmentForm2SubmitDTO dto);
}
