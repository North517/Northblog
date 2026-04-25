package com.quanxiaoha.weblog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: North001
 * @Date: 2026-04-21
 * @Version: v1.0.0
 * @Description: 文章状态/类型
 **/
@Getter
@AllArgsConstructor
public enum ArticleTypeEnum {

    DRAFT(0, "草稿"),
    NORMAL(1, "已发布"),
    WIKI(2, "收录于知识库");

    private Integer value;
    private String description;

}