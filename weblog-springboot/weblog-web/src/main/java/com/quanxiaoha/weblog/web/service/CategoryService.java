package com.quanxiaoha.weblog.web.service;

import com.quanxiaoha.weblog.common.utils.Response;
import com.quanxiaoha.weblog.web.model.vo.category.FindCategoryArticlePageListReqVO;
import com.quanxiaoha.weblog.web.model.vo.category.FindCategoryListReqVO;

/**
 * @author: North001
 * @url: blog.arnasoft.site
 * @date: 2026-04-21
 * @description: 分类
 **/
public interface CategoryService {
    /**
     * 获取分类列表
     * @return
     */
    Response findCategoryList(FindCategoryListReqVO findCategoryListReqVO);

    /**
     * 获取分类下文章分页数据
     * @param findCategoryArticlePageListReqVO
     * @return
     */
    Response findCategoryArticlePageList(FindCategoryArticlePageListReqVO findCategoryArticlePageListReqVO);
}
