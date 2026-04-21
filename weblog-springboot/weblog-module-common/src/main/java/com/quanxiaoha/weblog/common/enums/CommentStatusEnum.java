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
public enum CommentStatusEnum {

    // ----------- 通用异常状态码 -----------
    WAIT_EXAMINE(1, "等待审核"),
    NORMAL(2, "正常"),
    EXAMINE_FAILED(3, "审核不通过"),
    ;

    private Integer code;
    private String description;

}
