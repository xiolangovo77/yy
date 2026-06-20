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
import com.ecnu.mapper.ChildReportMapper;
import com.ecnu.model.dto.ChildReportAddDTO;
import com.ecnu.model.dto.ChildReportPageRequestDTO;
import com.ecnu.model.dto.ChildReportUpdateDTO;
import com.ecnu.model.entity.ChildReport;
import com.ecnu.model.vo.ChildReportVO;
import com.ecnu.service.ChildReportService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChildReportServiceImpl implements ChildReportService {

    @Resource
    private ChildReportMapper childReportMapper;

    /**
     * 添加报告
     */
    @Override
    public boolean add(ChildReportAddDTO childReportAddDTO) {
        // 参数校验
        // childId（必填）
        Long childId = childReportAddDTO.getChildId();
        ThrowUtils.throwIf(childId == null, ErrorCode.PARAMS_ERROR, "关联儿童ID不能为空");
        // reportNo（必填）
        String reportNo = childReportAddDTO.getReportNo();
        ThrowUtils.throwIf(StrUtil.hasBlank(reportNo), ErrorCode.PARAMS_ERROR, "报告编号不能为空");
        if (reportNo.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "报告编号不能超过32位");
        }
        // reportUrl（必填）
        String reportUrl = childReportAddDTO.getReportUrl();
        ThrowUtils.throwIf(StrUtil.hasBlank(reportUrl), ErrorCode.PARAMS_ERROR, "报告url不能为空");
        if (reportUrl.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "报告url不能超过512位");
        }

        ChildReport childReport = new ChildReport();
        BeanUtil.copyProperties(childReportAddDTO, childReport);

        // 手动生成雪花算法ID
        childReport.setId(IdUtil.getSnowflakeNextId());

        // 自动获取当前登录医生ID
        childReport.setUploadDoctorId(BaseContext.getCurrentId());

        // 数据库操作
        boolean result = childReportMapper.add(childReport);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 删除报告（软删除）
     */
    @Override
    public boolean delete(DeleteRequest deleteRequest) {
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        boolean result = childReportMapper.deleteById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 分页获取报告列表
     */
    @Override
    public PageResponse list(ChildReportPageRequestDTO pageRequest) {
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

        PageHelper.startPage(current, pageSize);
        List<ChildReport> list = childReportMapper.list(pageRequest);
        ThrowUtils.throwIf(list == null, ErrorCode.OPERATION_ERROR);

        List<ChildReportVO> voList = list.stream()
                .map(r -> BeanUtil.toBean(r, ChildReportVO.class))
                .collect(Collectors.toList());
        PageInfo<ChildReportVO> page = new PageInfo<>(voList);
        return new PageResponse(page.getTotal(), page.getList());
    }

    /**
     * 根据id获取报告
     */
    @Override
    public ChildReportVO getById(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        ChildReport childReport = childReportMapper.getById(id);
        ThrowUtils.throwIf(childReport == null, ErrorCode.NOT_FOUND_ERROR, "报告不存在");
        ChildReportVO childReportVO = new ChildReportVO();
        BeanUtil.copyProperties(childReport, childReportVO);
        return childReportVO;
    }

    /**
     * 修改报告
     */
    @Override
    public boolean update(ChildReportUpdateDTO childReportUpdateDTO) {
        // 校验参数
        Long id = childReportUpdateDTO.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        // childId（必填）
        Long childId = childReportUpdateDTO.getChildId();
        ThrowUtils.throwIf(childId == null, ErrorCode.PARAMS_ERROR, "关联儿童ID不能为空");
        // reportNo（必填）
        String reportNo = childReportUpdateDTO.getReportNo();
        ThrowUtils.throwIf(StrUtil.hasBlank(reportNo), ErrorCode.PARAMS_ERROR, "报告编号不能为空");
        if (reportNo.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "报告编号不能超过32位");
        }
        // reportUrl（必填）
        String reportUrl = childReportUpdateDTO.getReportUrl();
        ThrowUtils.throwIf(StrUtil.hasBlank(reportUrl), ErrorCode.PARAMS_ERROR, "报告url不能为空");
        if (reportUrl.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "报告url不能超过512位");
        }

        ChildReport childReport = new ChildReport();
        BeanUtil.copyProperties(childReportUpdateDTO, childReport);

        // 数据库操作（不修改 upload_doctor_id）
        boolean result = childReportMapper.update(childReport);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }
}
