package com.ecnu.mapper;

import com.ecnu.annotation.AutoFill;
import com.ecnu.model.dto.VideoResourcePageRequestDTO;
import com.ecnu.model.entity.VideoResource;
import com.ecnu.model.enums.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VideoResourceMapper {

    /**
     * 添加视频资源
     * @param videoResource
     * @return
     */
    @AutoFill(OperationType.INSERT)
    boolean add(VideoResource videoResource);

    /**
     * 根据id删除视频资源（软删除）
     * @param id
     * @return
     */
    boolean deleteById(Long id);

    /**
     * 根据id获取视频资源
     * @param id
     * @return
     */
    VideoResource getById(Long id);

    /**
     * 修改视频资源
     * @param videoResource
     * @return
     */
    @AutoFill(OperationType.UPDATE)
    boolean update(VideoResource videoResource);

    /**
     * 分页获取视频资源列表（支持按儿童ID筛选）
     */
    List<VideoResource> list(VideoResourcePageRequestDTO pageRequest);
}
