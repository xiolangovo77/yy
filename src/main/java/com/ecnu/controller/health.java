package com.ecnu.controller;

import com.ecnu.common.BaseResponse;
import com.ecnu.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class health {
    @GetMapping
    public BaseResponse<String> health(String message){
        return ResultUtils.success(message);
    }
}
