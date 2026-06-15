package com.ecnu.mapper;

import com.ecnu.annotation.AutoFill;
import com.ecnu.model.dto.ChildInfoPageRequestDTO;
import com.ecnu.model.entity.ChildInfo;
import com.ecnu.model.enums.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChildInfoMapper {

    /**
     * 添加儿童信息
     * @param childInfo
     * @return
     */
    @AutoFill(OperationType.INSERT)
    boolean add(ChildInfo childInfo);

    /**
     * 根据id删除儿童信息（软删除）
     * @param id
     * @return
     */
    boolean deleteById(Long id);

    /**
     * 根据id获取儿童信息
     * @param id
     * @return
     */
    ChildInfo getById(Long id);

    /**
     * 修改儿童信息
     * @param childInfo
     * @return
     */
    @AutoFill(OperationType.UPDATE)
    boolean update(ChildInfo childInfo);

    /**
     * 分页获取儿童信息（支持按姓名/编号模糊搜索，评估方式精确筛选）
     * @param pageRequest
     * @return
     */
    List<ChildInfo> list(ChildInfoPageRequestDTO pageRequest);
}
