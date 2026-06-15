package com.ecnu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ecnu.common.DeleteRequest;
import com.ecnu.context.BaseContext;
import com.ecnu.exception.BusinessException;
import com.ecnu.exception.ErrorCode;
import com.ecnu.exception.ThrowUtils;
import com.ecnu.mapper.ChildReportMapper;
import com.ecnu.model.dto.ChildReportAddDTO;
import com.ecnu.model.dto.ChildReportUpdateDTO;
import com.ecnu.model.entity.ChildReport;
import com.ecnu.model.vo.ChildReportVO;
import com.ecnu.service.ChildReportService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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
