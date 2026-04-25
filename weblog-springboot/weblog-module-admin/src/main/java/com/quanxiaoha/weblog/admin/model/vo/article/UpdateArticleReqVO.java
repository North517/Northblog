package com.quanxiaoha.weblog.admin.model.vo.article;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: North001
 * @url: blog.arnasoft.site
 * @date: 2026-04-21
 * @description: 更新文章
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(value = "更新文章 VO")
public class UpdateArticleReqVO {

    @NotNull(message = "文章 ID 不能为空")
    private Long id;

    @NotBlank(message = "文章标题不能为空")
    @Length(min = 1, max = 40, message = "文章标题字数需大于 1 小于 40")
    private String title;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    private String cover;

    private String summary;

    private Boolean isPublish = true;

    private Boolean isPrivate = false;

    private Boolean isTop = false;

    private Long categoryId;

    private List<String> tags;
}
