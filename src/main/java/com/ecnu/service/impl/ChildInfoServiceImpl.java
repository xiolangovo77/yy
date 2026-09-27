package com.ecnu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ecnu.common.DefaultValue;
import com.ecnu.common.DeleteRequest;
import com.ecnu.common.PageResponse;
import com.ecnu.exception.BusinessException;
import com.ecnu.exception.ErrorCode;
import com.ecnu.exception.ThrowUtils;
import com.ecnu.mapper.ChildInfoMapper;
import com.ecnu.mapper.DoctorMapper;
import com.ecnu.mapper.AssessmentTaskMapper;
import com.ecnu.model.entity.Doctor;
import com.ecnu.model.entity.AssessmentTask;
import com.ecnu.service.ChildDoctorBinding;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import com.ecnu.model.dto.ChildInfoAddDTO;
import com.ecnu.model.dto.ChildDoctorTransferDTO;
import com.ecnu.model.dto.ChildInfoPageRequestDTO;
import com.ecnu.model.dto.ChildInfoUpdateDTO;
import com.ecnu.model.entity.ChildInfo;
import com.ecnu.model.vo.ChildInfoVO;
import com.ecnu.service.ChildInfoService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChildInfoServiceImpl implements ChildInfoService {

    @Resource
    private ChildInfoMapper childInfoMapper;

    @Resource
    private ChildDoctorBinding childDoctorBinding;
    @Resource
    private DoctorMapper doctorMapper;
    @Resource
    private AssessmentTaskMapper assessmentTaskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean transferDoctor(ChildDoctorTransferDTO request) {
        ThrowUtils.throwIf(request.getId() == null, ErrorCode.PARAMS_ERROR, "儿童ID不能为空");
        ChildInfo existing = childInfoMapper.getByIdForUpdate(request.getId());
        ThrowUtils.throwIf(existing == null, ErrorCode.NOT_FOUND_ERROR, "儿童信息不存在");
        childDoctorBinding.requireAssignableDoctor(request.getDoctorId());
        ThrowUtils.throwIf(Objects.equals(existing.getDoctorId(), request.getDoctorId()),
                ErrorCode.PARAMS_ERROR, "请选择其他医生进行转接");
        requireDoctorChangeAllowed(existing.getId(), true);
        ChildInfo change = new ChildInfo();
        change.setId(existing.getId());
        change.setDoctorId(request.getDoctorId());
        ThrowUtils.throwIf(!childInfoMapper.update(change), ErrorCode.OPERATION_ERROR, "转接医生失败");
        assessmentTaskMapper.reassignPendingByChildId(existing.getId(), request.getDoctorId());
        return true;
    }

    /**
     * 添加儿童信息
     * @param childInfoAddDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(ChildInfoAddDTO childInfoAddDTO) {
        // 参数校验
        // childNo（必填）
        String childNo = childInfoAddDTO.getChildNo();
        ThrowUtils.throwIf(StrUtil.hasBlank(childNo), ErrorCode.PARAMS_ERROR, "儿童编号不能为空");
        if (childNo.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "儿童编号不能超过32位");
        }
        // childName（必填）
        String childName = childInfoAddDTO.getChildName();
        ThrowUtils.throwIf(StrUtil.hasBlank(childName), ErrorCode.PARAMS_ERROR, "儿童姓名不能为空");
        if (childName.length() > 64) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "儿童姓名不能超过64位");
        }
        // age（必填）
        Integer age = childInfoAddDTO.getAge();
        ThrowUtils.throwIf(age == null, ErrorCode.PARAMS_ERROR, "年龄不能为空");
        if (age < 0 || age > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "年龄范围不合法");
        }
        // gender（默认值：未提供）
        String gender = childInfoAddDTO.getGender();
        if (StrUtil.hasBlank(gender)) {
            gender = DefaultValue.DEFAULT_GENDER;
            childInfoAddDTO.setGender(gender);
        }
        // assessType（默认值：短视频结构化观察与分析）
        String assessType = childInfoAddDTO.getAssessType();
        if (StrUtil.hasBlank(assessType)) {
            assessType = DefaultValue.DEFAULT_ASSESS_TYPE;
            childInfoAddDTO.setAssessType(assessType);
        }

        childDoctorBinding.requireAssignableDoctor(childInfoAddDTO.getDoctorId());
        ChildInfo childInfo = new ChildInfo();
        BeanUtil.copyProperties(childInfoAddDTO, childInfo);

        // 手动生成雪花算法ID
        childInfo.setId(IdUtil.getSnowflakeNextId());

        // 数据库操作
        boolean result = childInfoMapper.add(childInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 删除儿童信息（软删除）
     * @param deleteRequest
     * @return
     */
    @Override
    public boolean delete(DeleteRequest deleteRequest) {
        // 参数校验
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        // 数据库操作
        boolean result = childInfoMapper.deleteById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 根据id获取儿童信息
     * @param id
     * @return
     */
    @Override
    public ChildInfoVO getById(Long id) {
        // 参数校验
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        // 数据库查询
        ChildInfo childInfo = childInfoMapper.getById(id);
        ThrowUtils.throwIf(childInfo == null, ErrorCode.NOT_FOUND_ERROR, "儿童信息不存在");
        childDoctorBinding.checkReadAccess(childInfo.getDoctorId());
        return toVO(childInfo);
    }

    /**
     * 修改儿童信息
     * @param childInfoUpdateDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(ChildInfoUpdateDTO childInfoUpdateDTO) {
        // 校验参数
        Long id = childInfoUpdateDTO.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        // childNo（必填）
        String childNo = childInfoUpdateDTO.getChildNo();
        ThrowUtils.throwIf(StrUtil.hasBlank(childNo), ErrorCode.PARAMS_ERROR, "儿童编号不能为空");
        if (childNo.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "儿童编号不能超过32位");
        }
        // childName（必填）
        String childName = childInfoUpdateDTO.getChildName();
        ThrowUtils.throwIf(StrUtil.hasBlank(childName), ErrorCode.PARAMS_ERROR, "儿童姓名不能为空");
        if (childName.length() > 64) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "儿童姓名不能超过64位");
        }
        // age（必填）
        Integer age = childInfoUpdateDTO.getAge();
        ThrowUtils.throwIf(age == null, ErrorCode.PARAMS_ERROR, "年龄不能为空");
        if (age < 0 || age > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "年龄范围不合法");
        }
        // gender（默认值：未提供）
        String gender = childInfoUpdateDTO.getGender();
        if (StrUtil.hasBlank(gender)) {
            gender = DefaultValue.DEFAULT_GENDER;
            childInfoUpdateDTO.setGender(gender);
        }
        // assessType（默认值：短视频结构化观察与分析）
        String assessType = childInfoUpdateDTO.getAssessType();
        if (StrUtil.hasBlank(assessType)) {
            assessType = DefaultValue.DEFAULT_ASSESS_TYPE;
            childInfoUpdateDTO.setAssessType(assessType);
        }

        ChildInfo existing = childInfoMapper.getByIdForUpdate(id);
        ThrowUtils.throwIf(existing == null, ErrorCode.NOT_FOUND_ERROR, "儿童信息不存在");
        childDoctorBinding.requireAssignableDoctor(childInfoUpdateDTO.getDoctorId());
        boolean doctorChanged = !Objects.equals(existing.getDoctorId(), childInfoUpdateDTO.getDoctorId());
        if (doctorChanged) {
            requireDoctorChangeAllowed(id, false);
        }
        ChildInfo childInfo = new ChildInfo();
        BeanUtil.copyProperties(childInfoUpdateDTO, childInfo);

        // 数据库操作
        boolean result = childInfoMapper.update(childInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        if (doctorChanged) {
            assessmentTaskMapper.reassignPendingByChildId(id, childInfo.getDoctorId());
        }
        return result;
    }

    /**
     * 分页获取儿童信息
     * @param pageRequest
     * @return
     */
    @Override
    public PageResponse list(ChildInfoPageRequestDTO pageRequest) {
        // 校验参数
        // current
        int current = pageRequest.getCurrent();
        ThrowUtils.throwIf(current < 1, ErrorCode.PARAMS_ERROR, "页码必须大于0");
        // pageSize
        int pageSize = pageRequest.getPageSize();
        ThrowUtils.throwIf(pageSize < 1, ErrorCode.PARAMS_ERROR, "页面大小必须大于0");
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "页面大小必须小于20");
        // sortField
        String sortField = pageRequest.getSortField();
        if (StrUtil.hasBlank(sortField)) {
            sortField = DefaultValue.DEFAULT_SORT_Field;
            pageRequest.setSortField(sortField);
        }
        // sortOrder
        String sortOrder = pageRequest.getSortOrder();
        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = DefaultValue.DEFAULT_SORT_ORDER;
            pageRequest.setSortOrder(sortOrder);
        }
        ThrowUtils.throwIf(!("ascend".equalsIgnoreCase(sortOrder) || "descend".equalsIgnoreCase(sortOrder)),
                ErrorCode.PARAMS_ERROR, "排序仅支持ascend/descend");
        pageRequest.setSortOrder(sortOrder.equalsIgnoreCase("ascend") ? "ASC" : "DESC");

        Long doctorScope = childDoctorBinding.currentDoctorScope();
        if (doctorScope != null) {
            pageRequest.setDoctorId(doctorScope);
        }
        ThrowUtils.throwIf(!List.of("id", "child_no", "child_name", "age", "create_time", "update_time").contains(sortField),
                ErrorCode.PARAMS_ERROR, "不支持的排序字段");
        // 数据库操作
        PageHelper.startPage(current, pageSize);
        List<ChildInfo> list = childInfoMapper.list(pageRequest);
        ThrowUtils.throwIf(list == null, ErrorCode.OPERATION_ERROR);

        // 封装结果：先用原始查询结果获取分页信息，再转换为 VO
        PageInfo<ChildInfo> pageInfo = new PageInfo<>(list);
        List<ChildInfoVO> voList = list.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return new PageResponse(pageInfo.getTotal(), voList);
    }
    private ChildInfoVO toVO(ChildInfo childInfo) {
        ChildInfoVO vo = BeanUtil.toBean(childInfo, ChildInfoVO.class);
        List<AssessmentTask> tasks = assessmentTaskMapper.listByChildId(childInfo.getId());
        boolean canTransfer = hasSinglePendingTask(tasks);
        vo.setTaskPublished(!tasks.isEmpty());
        vo.setCanTransferDoctor(canTransfer);
        vo.setCanChangeDoctor(tasks.isEmpty() || canTransfer);
        if (childInfo.getDoctorId() != null) {
            Doctor doctor = doctorMapper.getById(childInfo.getDoctorId());
            if (doctor != null) vo.setDoctorName(doctor.getDoctorName());
        }
        return vo;
    }

    private void requireDoctorChangeAllowed(Long childId, boolean requireTask) {
        List<AssessmentTask> tasks = assessmentTaskMapper.listByChildId(childId);
        if (!requireTask && tasks.isEmpty()) return;
        ThrowUtils.throwIf(!hasSinglePendingTask(tasks), ErrorCode.OPERATION_ERROR,
                "仅评估任务未开始且未删除时允许转接医生，进行中或已完成的任务不能转接");
    }

    private boolean hasSinglePendingTask(List<AssessmentTask> tasks) {
        return tasks.size() == 1
                && Integer.valueOf(0).equals(tasks.getFirst().getTaskStatus())
                && Integer.valueOf(0).equals(tasks.getFirst().getIsDeleted());
    }

}
