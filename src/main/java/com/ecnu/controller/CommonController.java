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
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public BaseResponse<?> upload(MultipartFile file){
        ThrowUtils.throwIf(file == null,ErrorCode.PARAMS_ERROR,"文件不能为空");
        // 获取文件大小（字节）
        long fileSize = file.getSize();
        log.info("文件上传：{}, 大小：{} bytes", file.getOriginalFilename(), fileSize);
        // 如果超过业务限制（100MB），直接拒绝
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (fileSize > maxSize) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文件大小不能超过100MB");
        }
        try {
            String objectName = GetObjectName(file);
            //文件的请求路径
            String filePath=aliOssUtil.upload(file.getBytes(),objectName);
            return ResultUtils.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败：{}",e);
        }
        return ResultUtils.error(ErrorCode.OPERATION_ERROR,"文件上传失败");
    }

    private String GetObjectName(MultipartFile file){
        //原始文件名
        String originalFilename = file.getOriginalFilename();
        //截取原始文件的后缀
        String extension=originalFilename.substring(originalFilename.lastIndexOf("."));
        //构造新文件名称
        String objectName= UUID.randomUUID().toString()+extension;
        return objectName;
    }
}
