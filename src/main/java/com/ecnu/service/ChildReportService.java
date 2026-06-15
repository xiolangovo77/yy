package com.ecnu.service;

import com.ecnu.common.DeleteRequest;
import com.ecnu.model.dto.ChildReportAddDTO;
import com.ecnu.model.dto.ChildReportUpdateDTO;
import com.ecnu.model.vo.ChildReportVO;

public interface ChildReportService {

    /**
     * 添加报告
     */
    boolean add(ChildReportAddDTO childReportAddDTO);

    /**
     * 删除报告（软删除）
     */
    boolean delete(DeleteRequest deleteRequest);

    /**
     * 根据id获取报告
     */
    ChildReportVO getById(Long id);

    /**
     * 修改报告
     */
    boolean update(ChildReportUpdateDTO childReportUpdateDTO);
}
