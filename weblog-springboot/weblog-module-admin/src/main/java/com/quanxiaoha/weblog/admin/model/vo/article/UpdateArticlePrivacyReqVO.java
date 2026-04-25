package com.quanxiaoha.weblog.admin.model.vo.article;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @Author: North001
 * @Date: 2026-04-25
 * @Version: v1.0.0
 * @Description: 更新文章私密状态
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(value = "更新文章私密状态 VO")
public class UpdateArticlePrivacyReqVO {

    @NotNull(message = "文章 ID 不能为空")
    private Long id;

    @NotNull(message = "文章私密状态不能为空")
    private Boolean isPrivate;
}
