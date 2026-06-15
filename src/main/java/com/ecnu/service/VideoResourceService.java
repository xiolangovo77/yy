package com.ecnu.service;

import com.ecnu.common.DeleteRequest;
import com.ecnu.model.dto.VideoResourceAddDTO;
import com.ecnu.model.dto.VideoResourceUpdateDTO;
import com.ecnu.model.vo.VideoResourceVO;

public interface VideoResourceService {

    /**
     * 添加视频资源
     * @param videoResourceAddDTO
     * @return
     */
    boolean add(VideoResourceAddDTO videoResourceAddDTO);

    /**
     * 删除视频资源（软删除）
     * @param deleteRequest
     * @return
     */
    boolean delete(DeleteRequest deleteRequest);

    /**
     * 根据id获取视频资源
     * @param id
     * @return
     */
    VideoResourceVO getById(Long id);

    /**
     * 修改视频资源
     * @param videoResourceUpdateDTO
     * @return
     */
    boolean update(VideoResourceUpdateDTO videoResourceUpdateDTO);


}
