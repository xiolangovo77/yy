package com.ecnu.mapper;

import com.ecnu.annotation.AutoFill;
import com.ecnu.model.dto.DoctorLoginDTO;
import com.ecnu.model.dto.DoctorPageRequestDTO;
import com.ecnu.model.dto.DoctorStatusDTO;
import com.ecnu.model.entity.Doctor;
import com.ecnu.model.enums.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DoctorMapper {

    /**
     * 添加医生
     * @param doctor
     * @return
     */
    @AutoFill(OperationType.INSERT)
    boolean add(Doctor doctor);

    /**
     * 根据id删除医生（软删除）
     * @param id
     * @return
     */
    boolean deleteById(Long id);

    /**
     * 更改医生账号状态
     * @param doctorStatusDTO
     * @return
     */
    boolean updateByStatus(DoctorStatusDTO doctorStatusDTO);

    /**
     * 根据id获取医生信息
     * @param id
     * @return
     */
    Doctor getById(Long id);

    Doctor getByIdForUpdate(Long id);

    /**
     * 修改医生信息
     * @param doctor
     * @return
     */
    @AutoFill(OperationType.UPDATE)
    boolean update(Doctor doctor);

    /**
     * 分页获取医生信息（支持按姓名/账号模糊搜索，角色精确筛选）
     * @param pageRequest
     * @return
     */
    List<Doctor> list(DoctorPageRequestDTO pageRequest);

    /**
     * 根据账号和密码获取医生信息
     * @param doctorLoginDTO
     * @return
     */
    List<Doctor> getByAccountPassword(DoctorLoginDTO doctorLoginDTO);
}
