package com.ecnu.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectIntroductionVO {
    private String title;
    private String content;
    private LocalDateTime updateTime;
}
