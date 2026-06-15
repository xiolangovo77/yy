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
import com.ecnu.model.dto.ChildInfoAddDTO;
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

    /**
     * 添加儿童信息
     * @param childInfoAddDTO
     * @return
     */
    @Override
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
        ChildInfoVO childInfoVO = new ChildInfoVO();
        BeanUtil.copyProperties(childInfo, childInfoVO);
        return childInfoVO;
    }

    /**
     * 修改儿童信息
     * @param childInfoUpdateDTO
     * @return
     */
    @Override
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

        ChildInfo childInfo = new ChildInfo();
        BeanUtil.copyProperties(childInfoUpdateDTO, childInfo);

        // 数据库操作
        boolean result = childInfoMapper.update(childInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
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

        // 数据库操作
        PageHelper.startPage(current, pageSize);
        List<ChildInfo> list = childInfoMapper.list(pageRequest);
        ThrowUtils.throwIf(list == null, ErrorCode.OPERATION_ERROR);

        // 封装结果
        List<ChildInfoVO> voList = list.stream()
                .map(childInfo -> BeanUtil.toBean(childInfo, ChildInfoVO.class))
                .collect(Collectors.toList());
        PageInfo<ChildInfoVO> page = new PageInfo<>(voList);
        return new PageResponse(page.getTotal(), page.getList());
    }
}
