package com.quanxiaoha.weblog.web.service;

import com.quanxiaoha.weblog.common.utils.Response;
import com.quanxiaoha.weblog.web.model.vo.search.SearchArticlePageListReqVO;

/**
 * @Author: North001
 * @Date: 2026-04-21
 * @Version: v1.0.0
 * @Description: TODO
 **/
public interface SearchService {

    /**
     * 关键词分页搜索
     * @param searchArticlePageListReqVO
     * @return
     */
    Response searchArticlePageList(SearchArticlePageListReqVO searchArticlePageListReqVO);
}
