package com.ecnu.service;

import com.ecnu.common.DeleteRequest;
import com.ecnu.common.PageResponse;
import com.ecnu.model.dto.DoctorAddDTO;
import com.ecnu.model.dto.DoctorLoginDTO;
import com.ecnu.model.dto.DoctorPageRequestDTO;
import com.ecnu.model.dto.DoctorProfileUpdateDTO;
import com.ecnu.model.dto.DoctorRegisterDTO;
import com.ecnu.model.dto.DoctorStatusDTO;
import com.ecnu.model.dto.DoctorUpdateDTO;
import com.ecnu.model.vo.DoctorLoginVO;
import com.ecnu.model.vo.DoctorVO;

public interface DoctorService {

    /**
     * 添加医生
     * @param doctorAddDTO
     * @return
     */
    boolean add(DoctorAddDTO doctorAddDTO);

    /**
     * 删除医生（软删除）
     * @param deleteRequest
     * @return
     */
    boolean delete(DeleteRequest deleteRequest);

    /**
     * 修改医生状态
     * @param doctorStatusDTO
     * @return
     */
    boolean updateStatus(DoctorStatusDTO doctorStatusDTO);

    /**
     * 根据id获取医生信息
     * @param id
     * @return
     */
    DoctorVO getById(Long id);

    /**
     * 修改医生信息
     * @param doctorUpdateDTO
     * @return
     */
    boolean update(DoctorUpdateDTO doctorUpdateDTO);

    /**
     * 分页获取医生信息
     * @param pageRequest
     * @return
     */
    PageResponse list(DoctorPageRequestDTO pageRequest);

    /**
     * 医生注册
     * @param doctorRegisterDTO
     * @return
     */
    boolean register(DoctorRegisterDTO doctorRegisterDTO);

    /**
     * 医生登录
     * @param doctorLoginDTO
     * @return
     */
    DoctorLoginVO login(DoctorLoginDTO doctorLoginDTO);

    /**
     * 医生修改个人资料
     * @param profileUpdateDTO
     * @return
     */
    boolean updateProfile(DoctorProfileUpdateDTO profileUpdateDTO);

    /**
     * 医生退出
     * @return
     */
    boolean logout();
}
