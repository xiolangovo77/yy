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
import com.ecnu.mapper.VideoResourceMapper;
import com.ecnu.model.dto.VideoResourceAddDTO;
import com.ecnu.model.dto.VideoResourcePageRequestDTO;
import com.ecnu.model.dto.VideoResourceUpdateDTO;
import com.ecnu.model.entity.VideoResource;
import com.ecnu.model.vo.VideoResourceVO;
import com.ecnu.service.VideoResourceService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoResourceServiceImpl implements VideoResourceService {

    @Resource
    private VideoResourceMapper videoResourceMapper;

    /**
     * 添加视频资源
     * @param videoResourceAddDTO
     * @return
     */
    @Override
    public boolean add(VideoResourceAddDTO videoResourceAddDTO) {
        // 参数校验
        // childId（必填）
        Long childId = videoResourceAddDTO.getChildId();
        ThrowUtils.throwIf(childId == null, ErrorCode.PARAMS_ERROR, "关联儿童ID不能为空");
        // videoUrl（必填）
        String videoUrl = videoResourceAddDTO.getVideoUrl();
        ThrowUtils.throwIf(StrUtil.hasBlank(videoUrl), ErrorCode.PARAMS_ERROR, "视频存储地址不能为空");
        if (videoUrl.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "视频存储地址不能超过512位");
        }
        // videoName（默认值：诊断视频）
        String videoName = videoResourceAddDTO.getVideoName();
        if (StrUtil.hasBlank(videoName)) {
            videoName = DefaultValue.DEFAULT_VIDEO_NAME;
            videoResourceAddDTO.setVideoName(videoName);
        }

        VideoResource videoResource = new VideoResource();
        BeanUtil.copyProperties(videoResourceAddDTO, videoResource);

        // 手动生成雪花算法ID
        videoResource.setId(IdUtil.getSnowflakeNextId());

        // 自动获取当前登录医生ID作为上传医生
        videoResource.setUploadDoctorId(BaseContext.getCurrentId());

        // 数据库操作
        boolean result = videoResourceMapper.add(videoResource);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 删除视频资源（软删除）
     * @param deleteRequest
     * @return
     */
    @Override
    public boolean delete(DeleteRequest deleteRequest) {
        // 参数校验
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        // 数据库操作
        boolean result = videoResourceMapper.deleteById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }

    /**
     * 根据id获取视频资源
     * @param id
     * @return
     */
    @Override
    public VideoResourceVO getById(Long id) {
        // 参数校验
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        // 数据库查询
        VideoResource videoResource = videoResourceMapper.getById(id);
        ThrowUtils.throwIf(videoResource == null, ErrorCode.NOT_FOUND_ERROR, "视频资源不存在");
        VideoResourceVO videoResourceVO = new VideoResourceVO();
        BeanUtil.copyProperties(videoResource, videoResourceVO);
        return videoResourceVO;
    }

    /**
     * 分页获取视频资源列表
     */
    @Override
    public PageResponse list(VideoResourcePageRequestDTO pageRequest) {
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
        List<VideoResource> list = videoResourceMapper.list(pageRequest);
        ThrowUtils.throwIf(list == null, ErrorCode.OPERATION_ERROR);

        List<VideoResourceVO> voList = list.stream()
                .map(v -> BeanUtil.toBean(v, VideoResourceVO.class))
                .collect(Collectors.toList());
        PageInfo<VideoResourceVO> page = new PageInfo<>(voList);
        return new PageResponse(page.getTotal(), page.getList());
    }

    /**
     * 修改视频资源（不修改上传医生）
     * @param videoResourceUpdateDTO
     * @return
     */
    @Override
    public boolean update(VideoResourceUpdateDTO videoResourceUpdateDTO) {
        // 校验参数
        Long id = videoResourceUpdateDTO.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "id不能为空");
        // childId（必填）
        Long childId = videoResourceUpdateDTO.getChildId();
        ThrowUtils.throwIf(childId == null, ErrorCode.PARAMS_ERROR, "关联儿童ID不能为空");
        // videoUrl（必填）
        String videoUrl = videoResourceUpdateDTO.getVideoUrl();
        ThrowUtils.throwIf(StrUtil.hasBlank(videoUrl), ErrorCode.PARAMS_ERROR, "视频存储地址不能为空");
        if (videoUrl.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "视频存储地址不能超过512位");
        }
        // videoName（默认值：诊断视频）
        String videoName = videoResourceUpdateDTO.getVideoName();
        if (StrUtil.hasBlank(videoName)) {
            videoName = DefaultValue.DEFAULT_VIDEO_NAME;
            videoResourceUpdateDTO.setVideoName(videoName);
        }

        VideoResource videoResource = new VideoResource();
        BeanUtil.copyProperties(videoResourceUpdateDTO, videoResource);

        // 数据库操作（不修改 upload_doctor_id）
        boolean result = videoResourceMapper.update(videoResource);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return result;
    }


}
