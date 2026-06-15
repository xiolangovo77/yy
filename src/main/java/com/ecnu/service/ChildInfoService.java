package com.ecnu.service;

import com.ecnu.common.DeleteRequest;
import com.ecnu.common.PageResponse;
import com.ecnu.model.dto.ChildInfoAddDTO;
import com.ecnu.model.dto.ChildInfoPageRequestDTO;
import com.ecnu.model.dto.ChildInfoUpdateDTO;
import com.ecnu.model.vo.ChildInfoVO;

public interface ChildInfoService {

    /**
     * 添加儿童信息
     * @param childInfoAddDTO
     * @return
     */
    boolean add(ChildInfoAddDTO childInfoAddDTO);

    /**
     * 删除儿童信息（软删除）
     * @param deleteRequest
     * @return
     */
    boolean delete(DeleteRequest deleteRequest);

    /**
     * 根据id获取儿童信息
     * @param id
     * @return
     */
    ChildInfoVO getById(Long id);

    /**
     * 修改儿童信息
     * @param childInfoUpdateDTO
     * @return
     */
    boolean update(ChildInfoUpdateDTO childInfoUpdateDTO);

    /**
     * 分页获取儿童信息
     * @param pageRequest
     * @return
     */
    PageResponse list(ChildInfoPageRequestDTO pageRequest);
}
