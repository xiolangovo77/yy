package com.ecnu.controller;


import com.ecnu.common.BaseResponse;
import com.ecnu.common.ResultUtils;
import com.ecnu.exception.ErrorCode;
import com.ecnu.exception.ThrowUtils;
import com.ecnu.utils.AliOssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@Tag(name = "通用接口")
@RequestMapping("/common")
public class CommonController {

    @Resource
    private AliOssUtil aliOssUtil;

    /**
     * 上传MP4视频文件
     * @param file
     * @return
     */
    @PostMapping("/uploadMP4")
    @Operation(summary = "上传MP4视频")
    public BaseResponse<?> uploadMP4(MultipartFile file) {
        return doUpload(file, ".mp4", 500 * 1024 * 1024); // 500MB
    }

    /**
     * 上传MD报告文件
     * @param file
     * @return
     */
    @PostMapping("/uploadMD")
    @Operation(summary = "上传MD报告")
    public BaseResponse<?> uploadMD(MultipartFile file) {
        return doUpload(file, ".md", 10 * 1024 * 1024); // 10MB
    }

    /**
     * 通用上传逻辑
     */
    private BaseResponse<?> doUpload(MultipartFile file, String allowedExtension, long maxSize) {
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "文件不能为空");

        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文件类型不支持，仅允许上传 " + allowedExtension + " 文件");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!allowedExtension.equals(extension)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文件类型不支持，仅允许上传 " + allowedExtension + " 文件");
        }

        // 校验文件大小
        long fileSize = file.getSize();
        log.info("文件上传：{}, 大小：{} bytes", originalFilename, fileSize);
        if (fileSize > maxSize) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文件大小不能超过" + (maxSize / 1024 / 1024) + "MB");
        }

        try {
            String objectName = getObjectName(file);
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return ResultUtils.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e);
        }
        return ResultUtils.error(ErrorCode.OPERATION_ERROR, "文件上传失败");
    }

    private String getObjectName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }
}
