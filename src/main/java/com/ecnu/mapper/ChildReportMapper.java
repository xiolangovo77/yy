package com.ecnu.mapper;

import com.ecnu.annotation.AutoFill;
import com.ecnu.model.entity.ChildReport;
import com.ecnu.model.enums.OperationType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChildReportMapper {

    /**
     * 添加报告
     */
    @AutoFill(OperationType.INSERT)
    boolean add(ChildReport childReport);

    /**
     * 根据id删除报告（软删除）
     */
    boolean deleteById(Long id);

    /**
     * 根据id获取报告
     */
    ChildReport getById(Long id);

    /**
     * 修改报告
     */
    @AutoFill(OperationType.UPDATE)
    boolean update(ChildReport childReport);
}
