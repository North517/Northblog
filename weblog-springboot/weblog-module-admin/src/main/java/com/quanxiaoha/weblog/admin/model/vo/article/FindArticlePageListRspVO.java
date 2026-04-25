package com.quanxiaoha.weblog.admin.model.vo.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author: North001
 * @url: blog.arnasoft.site
 * @date: 2026-04-21
 * @description: 文章分页
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindArticlePageListRspVO  {

    /**
     * 文章 ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章封面
     */
    private String cover;

    /**
     * 发布时间
     */
    private LocalDateTime createTime;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 文章状态：0 草稿，1 已发布
     */
    private Integer type;

    /**
     * 是否私密：false 公开，true 私密
     */
    private Boolean isPrivate;

}
