package com.ecnu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ecnu.common.DefaultValue;
import com.ecnu.common.DeleteRequest;
import com.ecnu.common.PageResponse;
import com.ecnu.context.BaseContext;
import com.ecnu.exception.BusinessException;
import com.ecnu.exception.ErrorCode;
import com.ecnu.exception.ThrowUtils;
import com.ecnu.mapper.AssessmentForm1Mapper;
import com.ecnu.mapper.AssessmentForm2Mapper;
import com.ecnu.mapper.AssessmentTaskMapper;
import com.ecnu.model.dto.AssessmentForm1SubmitDTO;
import com.ecnu.model.dto.AssessmentForm2SubmitDTO;
import com.ecnu.model.dto.AssessmentTaskPageRequestDTO;
import com.ecnu.model.dto.AssessmentTaskPublishDTO;
import com.ecnu.model.enums.DiseaseTypeEnum;
import com.ecnu.model.entity.AssessmentForm1;
import com.ecnu.model.entity.AssessmentForm2;
import com.ecnu.model.entity.AssessmentTask;
import com.ecnu.model.vo.AssessmentForm1VO;
import com.ecnu.model.vo.AssessmentForm2VO;
import com.ecnu.model.vo.AssessmentTaskVO;
import com.ecnu.service.AssessmentTaskService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssessmentTaskServiceImpl implements AssessmentTaskService {

    @Resource
    private AssessmentTaskMapper assessmentTaskMapper;

    @Resource
    private AssessmentForm1Mapper assessmentForm1Mapper;

    @Resource
    private AssessmentForm2Mapper assessmentForm2Mapper;

    /**
     * 发布评估任务（事务：task + form1 + form2 同步创建）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publish(AssessmentTaskPublishDTO publishDTO) {
        // 参数校验
        Long childId = publishDTO.getChildId();
        ThrowUtils.throwIf(childId == null, ErrorCode.PARAMS_ERROR, "关联儿童ID不能为空");
        String taskNo = publishDTO.getTaskNo();
        ThrowUtils.throwIf(StrUtil.hasBlank(taskNo), ErrorCode.PARAMS_ERROR, "任务编号不能为空");
        if (taskNo.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务编号不能超过32位");
        }

        // 生成任务ID（供三表共用）
        Long taskId = IdUtil.getSnowflakeNextId();

        // 1. 创建评估任务
        AssessmentTask task = AssessmentTask.builder()
                .id(taskId)
                .doctorId(DefaultValue.DEFAULT_DOCTOR_ID)
                .childId(childId)
                .taskNo(taskNo)
                .taskStatus(DefaultValue.DEFAULT_TASK_STATUS)
                .currentStep(DefaultValue.DEFAULT_CURRENT_STEP)
                .build();
        boolean taskResult = assessmentTaskMapper.add(task);
        ThrowUtils.throwIf(!taskResult, ErrorCode.OPERATION_ERROR, "创建评估任务失败");

        // 2. 创建第一阶段评估表单
        AssessmentForm1 form1 = AssessmentForm1.builder()
                .id(IdUtil.getSnowflakeNextId())
                .taskId(taskId)
                .childId(childId)
                .diseaseType("")
                .build();
        boolean form1Result = assessmentForm1Mapper.add(form1);
        ThrowUtils.throwIf(!form1Result, ErrorCode.OPERATION_ERROR, "创建一阶段表单失败");

        // 3. 创建第二阶段评估表单
        AssessmentForm2 form2 = AssessmentForm2.builder()
                .id(IdUtil.getSnowflakeNextId())
                .taskId(taskId)
                .childId(childId)
                .diseaseType("")
                .build();
        boolean form2Result = assessmentForm2Mapper.add(form2);
        ThrowUtils.throwIf(!form2Result, ErrorCode.OPERATION_ERROR, "创建二阶段表单失败");

        return true;
    }

    /**
     * 根据id获取评估任务
     */
    @Override
    public AssessmentTaskVO getById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        AssessmentTask task = assessmentTaskMapper.getById(id);
        ThrowUtils.throwIf(task == null, ErrorCode.NOT_FOUND_ERROR, "评估任务不存在");
        AssessmentTaskVO taskVO = new AssessmentTaskVO();
        BeanUtil.copyProperties(task, taskVO);
        return taskVO;
    }

    /**
     * 分页获取评估任务列表
     */
    @Override
    public PageResponse list(AssessmentTaskPageRequestDTO pageRequest) {
        // 校验参数
        int current = pageRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, ErrorCode.PARAMS_ERROR, "页码必须大于0");
        int pageSize = pageRequest.getPageSize();
        ThrowUtils.throwIf(pageSize < 1, ErrorCode.PARAMS_ERROR, "页面大小必须大于0");
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "页面大小必须小于20");
        String sortField = pageRequest.getSortField();
        if (StrUtil.hasBlank(sortField)) {
            sortField = DefaultValue.DEFAULT_SORT_Field;
            pageRequest.setSortField(sortField);
        }
        String sortOrder = pageRequest.getSortOrder();
        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = DefaultValue.DEFAULT_SORT_ORDER;
            pageRequest.setSortOrder(sortOrder);
        }
        ThrowUtils.throwIf(!("ascend".equalsIgnoreCase(sortOrder) || "descend".equalsIgnoreCase(sortOrder)),
                ErrorCode.PARAMS_ERROR, "排序仅支持ascend/descend");
        pageRequest.setSortOrder(sortOrder.equalsIgnoreCase("ascend") ? "ASC" : "DESC");

        // 数据库查询
        PageHelper.startPage(current, pageSize);
        List<AssessmentTask> list = assessmentTaskMapper.list(pageRequest);
        ThrowUtils.throwIf(list == null, ErrorCode.OPERATION_ERROR);

        // 封装结果
        List<AssessmentTaskVO> voList = list.stream()
                .map(task -> BeanUtil.toBean(task, AssessmentTaskVO.class))
                .collect(Collectors.toList());
        PageInfo<AssessmentTaskVO> page = new PageInfo<>(voList);
        return new PageResponse(page.getTotal(), page.getList());
    }

    /**
     * 根据任务ID获取一阶段表单
     */
    @Override
    public AssessmentForm1VO getForm1ByTaskId(Long taskId) {
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        AssessmentForm1 form1 = assessmentForm1Mapper.getByTaskId(taskId);
        ThrowUtils.throwIf(form1 == null, ErrorCode.NOT_FOUND_ERROR, "一阶段表单不存在");
        AssessmentForm1VO vo = new AssessmentForm1VO();
        BeanUtil.copyProperties(form1, vo);
        return vo;
    }

    /**
     * 根据任务ID获取二阶段表单
     */
    @Override
    public AssessmentForm2VO getForm2ByTaskId(Long taskId) {
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        AssessmentForm2 form2 = assessmentForm2Mapper.getByTaskId(taskId);
        ThrowUtils.throwIf(form2 == null, ErrorCode.NOT_FOUND_ERROR, "二阶段表单不存在");
        AssessmentForm2VO vo = new AssessmentForm2VO();
        BeanUtil.copyProperties(form2, vo);
        return vo;
    }

    /**
     * 删除评估任务（软删除 task + form1 + form2）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(DeleteRequest deleteRequest) {
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");

        // 删除评估任务
        boolean taskResult = assessmentTaskMapper.deleteById(id);
        ThrowUtils.throwIf(!taskResult, ErrorCode.OPERATION_ERROR, "删除评估任务失败");

        // 删除一阶段表单
        assessmentForm1Mapper.deleteByTaskId(id);

        // 删除二阶段表单
        assessmentForm2Mapper.deleteByTaskId(id);

        return true;
    }

    /**
     * 开始任务：分配医生 + 更新状态为进行中
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startTask(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        // 校验任务存在
        AssessmentTask task = assessmentTaskMapper.getById(id);
        ThrowUtils.throwIf(task == null, ErrorCode.NOT_FOUND_ERROR, "评估任务不存在");
        // 更新医生 + 状态
        Long doctorId = BaseContext.getCurrentId();
        boolean result = assessmentTaskMapper.updateStart(id, doctorId);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 提交一阶段表单 + 更新 current_step = 2
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitForm1(AssessmentForm1SubmitDTO dto) {
        // 参数校验
        Long taskId = dto.getTaskId();
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        String diseaseType = dto.getDiseaseType();
        ThrowUtils.throwIf(StrUtil.hasBlank(diseaseType), ErrorCode.PARAMS_ERROR, "疾病判定不能为空");
        if (!DiseaseTypeEnum.isValid(diseaseType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "疾病判定仅支持 ASD / TD / DD");
        }
        Integer confidence = dto.getConfidence();
        ThrowUtils.throwIf(confidence == null, ErrorCode.PARAMS_ERROR, "诊断信心不能为空");
        if (confidence < 0 || confidence > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "诊断信心范围为 0-100");
        }
        String diagnosticBasis = dto.getDiagnosticBasis();
        ThrowUtils.throwIf(StrUtil.hasBlank(diagnosticBasis), ErrorCode.PARAMS_ERROR, "诊断依据不能为空");

        // 更新一阶段表单
        AssessmentForm1 form1 = new AssessmentForm1();
        BeanUtil.copyProperties(dto, form1);
        boolean result = assessmentForm1Mapper.update(form1);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "提交一阶段表单失败");

        // 更新任务步骤为第二阶段
        boolean stepResult = assessmentTaskMapper.updateCurrentStep(taskId, 2);
        ThrowUtils.throwIf(!stepResult, ErrorCode.OPERATION_ERROR);
        return true;
    }

    /**
     * 提交二阶段表单 + 更新 task_status = 2（已完成）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitForm2(AssessmentForm2SubmitDTO dto) {
        // 参数校验
        Long taskId = dto.getTaskId();
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        String diseaseType = dto.getDiseaseType();
        ThrowUtils.throwIf(StrUtil.hasBlank(diseaseType), ErrorCode.PARAMS_ERROR, "疾病判定不能为空");
        if (!DiseaseTypeEnum.isValid(diseaseType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "疾病判定仅支持 ASD / TD / DD");
        }
        Integer confidence = dto.getConfidence();
        ThrowUtils.throwIf(confidence == null, ErrorCode.PARAMS_ERROR, "诊断信心不能为空");
        if (confidence < 0 || confidence > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "诊断信心范围为 0-100");
        }
        String diagnosticBasis = dto.getDiagnosticBasis();
        ThrowUtils.throwIf(StrUtil.hasBlank(diagnosticBasis), ErrorCode.PARAMS_ERROR, "诊断依据不能为空");
        String finalConclusion = dto.getFinalConclusion();
        ThrowUtils.throwIf(StrUtil.hasBlank(finalConclusion), ErrorCode.PARAMS_ERROR, "最终诊断结论不能为空");

        // 更新二阶段表单
        AssessmentForm2 form2 = new AssessmentForm2();
        BeanUtil.copyProperties(dto, form2);
        boolean result = assessmentForm2Mapper.update(form2);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "提交二阶段表单失败");

        // 更新任务状态为已完成
        boolean statusResult = assessmentTaskMapper.updateStatus(taskId, 2);
        ThrowUtils.throwIf(!statusResult, ErrorCode.OPERATION_ERROR);
        return true;
    }
}
