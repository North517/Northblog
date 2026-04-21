package com.quanxiaoha.weblog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: North001
 * @Date: 2026-04-21
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Getter
@AllArgsConstructor
public enum ArticleTypeEnum {

    NORMAL(1, "普通"),
    WIKI(2, "收录于知识库");

    private Integer value;
    private String description;

}