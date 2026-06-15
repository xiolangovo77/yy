package com.ecnu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ecnu.common.DefaultValue;
import com.ecnu.common.DeleteRequest;
import com.ecnu.common.PageResponse;
import com.ecnu.common.StatusConstant;
import com.ecnu.context.BaseContext;
import com.ecnu.exception.BusinessException;
import com.ecnu.exception.ErrorCode;
import com.ecnu.exception.ThrowUtils;
import com.ecnu.mapper.DoctorMapper;
import com.ecnu.model.dto.DoctorAddDTO;
import com.ecnu.model.dto.DoctorLoginDTO;
import com.ecnu.model.dto.DoctorPageRequestDTO;
import com.ecnu.model.dto.DoctorProfileUpdateDTO;
import com.ecnu.model.dto.DoctorRegisterDTO;
import com.ecnu.model.dto.DoctorStatusDTO;
import com.ecnu.model.dto.DoctorUpdateDTO;
import com.ecnu.model.entity.Doctor;
import com.ecnu.model.enums.RoleEnum;
import com.ecnu.model.vo.DoctorLoginVO;
import com.ecnu.model.vo.DoctorVO;
import com.ecnu.properties.JwtProperties;
import com.ecnu.service.DoctorService;
import com.ecnu.utils.JwtUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 添加医生
     * @param doctorAddDTO
     * @return
     */
    @Override
    public boolean add(DoctorAddDTO doctorAddDTO) {
        // 参数校验
        // account
        String account = doctorAddDTO.getAccount();
        ThrowUtils.throwIf(StrUtil.hasBlank(account), ErrorCode.PARAMS_ERROR, "账号不能为空");
        if (account.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能超过50位");
        }
        // password
        String password = doctorAddDTO.getPassword();
        if (StrUtil.hasBlank(password)) {
            password = DefaultValue.DEFAULT_PASSWORD;
            doctorAddDTO.setPassword(password);
        }
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能少于6位");
        }
        if (password.length() > 15) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能超过15位");
        }
        // doctorName
        String doctorName = doctorAddDTO.getDoctorName();
        ThrowUtils.throwIf(StrUtil.hasBlank(doctorName), ErrorCode.PARAMS_ERROR, "医生姓名不能为空");
        if (doctorName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "医生姓名不能超过20位");
        }
        // hospital
        String hospital = doctorAddDTO.getHospital();
        ThrowUtils.throwIf(StrUtil.hasBlank(hospital), ErrorCode.PARAMS_ERROR, "所在医院不能为空");
        if (hospital.length() > 64) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "医院名称不能超过64位");
        }
        // role
        Long role = doctorAddDTO.getRole();
        ThrowUtils.throwIf(role == null, ErrorCode.PARAMS_ERROR, "角色不能为空");
        if (RoleEnum.getNameByCode(role).isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "传入角色错误");
        }

        Doctor doctor = new Doctor();
        BeanUtil.copyProperties(doctorAddDTO, doctor);

        // 手动生成雪花算法ID
        doctor.setId(IdUtil.getSnowflakeNextId());

        // 密码加密
        String SALT = DefaultValue.SALT;
        String md5Password = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        doctor.setPassword(md5Password);

        // 字段补充
        doctor.setStatus(StatusConstant.ENABLED);

        // 数据库操作
        boolean result = doctorMapper.add(doctor);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 删除医生（软删除）
     * @param deleteRequest
     * @return
     */
    @Override
    public boolean delete(DeleteRequest deleteRequest) {
        // 参数校验
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        // 管理员账号不可删除
        Doctor existingDoctor = doctorMapper.getById(id);
        ThrowUtils.throwIf(existingDoctor == null, ErrorCode.NOT_FOUND_ERROR, "医生不存在");
        if (RoleEnum.ADMIN.getCode().equals(existingDoctor.getRole())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "管理员账号不可删除");
        }
        // 数据库操作
        boolean result = doctorMapper.deleteById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 修改医生状态
     * @param doctorStatusDTO
     * @return
     */
    @Override
    public boolean updateStatus(DoctorStatusDTO doctorStatusDTO) {
        // 参数验证
        Long id = doctorStatusDTO.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        Integer status = doctorStatusDTO.getStatus();
        ThrowUtils.throwIf(status != StatusConstant.ENABLED && status != StatusConstant.DISABLED,
                ErrorCode.PARAMS_ERROR, "传入状态错误");
        // 管理员账号不可修改状态
        Doctor existingDoctor = doctorMapper.getById(id);
        ThrowUtils.throwIf(existingDoctor == null, ErrorCode.NOT_FOUND_ERROR, "医生不存在");
        if (RoleEnum.ADMIN.getCode().equals(existingDoctor.getRole())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "管理员账号不可禁用");
        }
        // 数据库操作
        boolean result = doctorMapper.updateByStatus(doctorStatusDTO);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 根据id获取医生信息
     * @param id
     * @return
     */
    @Override
    public DoctorVO getById(Long id) {
        // 参数校验
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        // 数据库查询
        Doctor doctor = doctorMapper.getById(id);
        ThrowUtils.throwIf(doctor == null, ErrorCode.NOT_FOUND_ERROR, "医生不存在");
        DoctorVO doctorVO = new DoctorVO();
        BeanUtil.copyProperties(doctor, doctorVO);
        return doctorVO;
    }

    /**
     * 修改医生信息
     * @param doctorUpdateDTO
     * @return
     */
    @Override
    public boolean update(DoctorUpdateDTO doctorUpdateDTO) {
        // 校验参数
        // account
        String account = doctorUpdateDTO.getAccount();
        ThrowUtils.throwIf(StrUtil.hasBlank(account), ErrorCode.PARAMS_ERROR, "账号不能为空");
        if (account.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能超过50位");
        }
        // password
        String password = doctorUpdateDTO.getPassword();
        if (!StrUtil.hasBlank(password) && password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能少于6位");
        }
        if (!StrUtil.hasBlank(password) && password.length() > 15) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能超过15位");
        }
        // doctorName
        String doctorName = doctorUpdateDTO.getDoctorName();
        ThrowUtils.throwIf(StrUtil.hasBlank(doctorName), ErrorCode.PARAMS_ERROR, "医生姓名不能为空");
        if (doctorName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "医生姓名不能超过20位");
        }
        // hospital
        String hospital = doctorUpdateDTO.getHospital();
        ThrowUtils.throwIf(StrUtil.hasBlank(hospital), ErrorCode.PARAMS_ERROR, "所在医院不能为空");
        if (hospital.length() > 64) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "医院名称不能超过64位");
        }
        // role
        Long role = doctorUpdateDTO.getRole();
        ThrowUtils.throwIf(role == null, ErrorCode.PARAMS_ERROR, "角色不能为空");
        if (RoleEnum.getNameByCode(role).isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "传入角色错误");
        }

        // 管理员账号不可修改
        Doctor existingDoctor = doctorMapper.getById(doctorUpdateDTO.getId());
        ThrowUtils.throwIf(existingDoctor == null, ErrorCode.NOT_FOUND_ERROR, "医生不存在");
        if (RoleEnum.ADMIN.getCode().equals(existingDoctor.getRole())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "管理员账号不可修改");
        }

        Doctor doctor = new Doctor();
        BeanUtil.copyProperties(doctorUpdateDTO, doctor);

        // 密码加密
        if (!StrUtil.hasBlank(password)) {
            String SALT = DefaultValue.SALT;
            String md5Password = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
            doctor.setPassword(md5Password);
        }

        // 数据库操作
        boolean result = doctorMapper.update(doctor);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 分页获取医生信息
     * @param pageRequest
     * @return
     */
    @Override
    public PageResponse list(DoctorPageRequestDTO pageRequest) {
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
        List<Doctor> list = doctorMapper.list(pageRequest);
        ThrowUtils.throwIf(list == null, ErrorCode.OPERATION_ERROR);

        // 封装结果
        List<DoctorVO> voList = list.stream()
                .map(doctor -> BeanUtil.toBean(doctor, DoctorVO.class))
                .collect(Collectors.toList());
        PageInfo<DoctorVO> page = new PageInfo<>(voList);
        return new PageResponse(page.getTotal(), page.getList());
    }

    /**
     * 医生注册（自动分配普通医生角色）
     * @param doctorRegisterDTO
     * @return
     */
    @Override
    public boolean register(DoctorRegisterDTO doctorRegisterDTO) {
        // 参数校验
        // account
        String account = doctorRegisterDTO.getAccount();
        ThrowUtils.throwIf(StrUtil.hasBlank(account), ErrorCode.PARAMS_ERROR, "账号不能为空");
        if (account.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能超过50位");
        }
        // password
        String password = doctorRegisterDTO.getPassword();
        if (StrUtil.hasBlank(password)) {
            password = DefaultValue.DEFAULT_PASSWORD;
            doctorRegisterDTO.setPassword(password);
        }
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能少于6位");
        }
        if (password.length() > 15) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能超过15位");
        }
        // doctorName
        String doctorName = doctorRegisterDTO.getDoctorName();
        ThrowUtils.throwIf(StrUtil.hasBlank(doctorName), ErrorCode.PARAMS_ERROR, "医生姓名不能为空");
        if (doctorName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "医生姓名不能超过20位");
        }
        // hospital
        String hospital = doctorRegisterDTO.getHospital();
        ThrowUtils.throwIf(StrUtil.hasBlank(hospital), ErrorCode.PARAMS_ERROR, "所在医院不能为空");
        if (hospital.length() > 64) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "医院名称不能超过64位");
        }

        Doctor doctor = new Doctor();
        BeanUtil.copyProperties(doctorRegisterDTO, doctor);

        // 手动生成雪花算法ID
        doctor.setId(IdUtil.getSnowflakeNextId());

        // 密码加密
        String SALT = DefaultValue.SALT;
        String md5Password = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        doctor.setPassword(md5Password);

        // 注册用户自动分配普通医生角色
        doctor.setRole(RoleEnum.DOCTOR.getCode());
        // 默认启用
        doctor.setStatus(StatusConstant.ENABLED);

        // 数据库操作
        boolean result = doctorMapper.add(doctor);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 医生登录
     * @param doctorLoginDTO
     * @return
     */
    @Override
    public DoctorLoginVO login(DoctorLoginDTO doctorLoginDTO) {
        // 参数校验
        // account
        String account = doctorLoginDTO.getAccount();
        ThrowUtils.throwIf(StrUtil.hasBlank(account), ErrorCode.PARAMS_ERROR, "账号不能为空");
        // password
        String password = doctorLoginDTO.getPassword();
        ThrowUtils.throwIf(StrUtil.hasBlank(password), ErrorCode.PARAMS_ERROR, "密码不能为空");

        // 密码加密
        String SALT = DefaultValue.SALT;
        String md5Password = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        doctorLoginDTO.setPassword(md5Password);

        // 数据库操作
        List<Doctor> doctorList = doctorMapper.getByAccountPassword(doctorLoginDTO);
        ThrowUtils.throwIf(doctorList == null || doctorList.isEmpty(), ErrorCode.OPERATION_ERROR, "账号或密码错误");

        // 封装返回结果(token)
        Doctor doctor = doctorList.getFirst();
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", doctor.getId());
        claims.put("roleId", doctor.getRole());
        String token = JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);

        // 构建缓存 key
        String redisKey = DefaultValue.DEFAULT_LOGIN_REDIS_KEY + doctor.getId();
        // 从 Redis 缓存中查询
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        String cachedValue = valueOps.get(redisKey);
        if (cachedValue != null) {
            // 如果缓存命中，删除缓存（踢掉旧登录）
            stringRedisTemplate.delete(redisKey);
        }
        // 存入 Redis 缓存
        String cacheValue = token;
        // 设置过期时间
        int cacheExpireTime = Math.toIntExact(jwtProperties.getTtl());
        valueOps.set(redisKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);

        // 返回结果
        return DoctorLoginVO.builder()
                .id(doctor.getId())
                .doctorName(doctor.getDoctorName())
                .account(doctor.getAccount())
                .role(doctor.getRole())
                .token(token)
                .build();
    }

    /**
     * 医生修改个人资料
     * @param profileUpdateDTO
     * @return
     */
    @Override
    public boolean updateProfile(DoctorProfileUpdateDTO profileUpdateDTO) {
        // 获取当前登录医生ID
        Long currentId = BaseContext.getCurrentId();
        ThrowUtils.throwIf(currentId == null, ErrorCode.NOT_LOGIN_ERROR, "未登录");

        // 参数校验
        // doctorName
        String doctorName = profileUpdateDTO.getDoctorName();
        ThrowUtils.throwIf(StrUtil.hasBlank(doctorName), ErrorCode.PARAMS_ERROR, "医生姓名不能为空");
        if (doctorName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "医生姓名不能超过20位");
        }
        // hospital
        String hospital = profileUpdateDTO.getHospital();
        ThrowUtils.throwIf(StrUtil.hasBlank(hospital), ErrorCode.PARAMS_ERROR, "所在医院不能为空");
        if (hospital.length() > 64) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "医院名称不能超过64位");
        }
        // password（选填）
        String password = profileUpdateDTO.getPassword();
        if (!StrUtil.hasBlank(password) && password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能少于6位");
        }
        if (!StrUtil.hasBlank(password) && password.length() > 15) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能超过15位");
        }

        // 构建更新对象
        Doctor doctor = new Doctor();
        BeanUtil.copyProperties(profileUpdateDTO, doctor);
        doctor.setId(currentId);

        // 密码加密
        if (!StrUtil.hasBlank(password)) {
            String SALT = DefaultValue.SALT;
            String md5Password = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
            doctor.setPassword(md5Password);
        }

        // 数据库操作
        boolean result = doctorMapper.update(doctor);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 医生退出
     * @return
     */
    @Override
    public boolean logout() {
        // 构建缓存 key
        String redisKey = DefaultValue.DEFAULT_LOGIN_REDIS_KEY + BaseContext.getCurrentId();
        // 删除缓存
        stringRedisTemplate.delete(redisKey);
        return true;
    }
}
