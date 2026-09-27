package com.ecnu.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectIntroduction {
    private Integer id;
    private String title;
    private String content;
    private LocalDateTime updateTime;
}
