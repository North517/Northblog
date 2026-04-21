package com.quanxiaoha.weblog.web.service;

import com.quanxiaoha.weblog.common.utils.Response;
import com.quanxiaoha.weblog.web.model.vo.tag.FindTagArticlePageListReqVO;
import com.quanxiaoha.weblog.web.model.vo.tag.FindTagListReqVO;

/**
 * @author: North001
 * @url: blog.arnasoft.site
 * @date: 2026-04-21
 * @description: 分类
 **/
public interface TagService {
    /**
     * 获取标签列表
     * @return
     */
    Response findTagList(FindTagListReqVO findTagListReqVO);

    /**
     * 获取标签下文章分页列表
     * @param findTagArticlePageListReqVO
     * @return
     */
    Response findTagPageList(FindTagArticlePageListReqVO findTagArticlePageListReqVO);
}
