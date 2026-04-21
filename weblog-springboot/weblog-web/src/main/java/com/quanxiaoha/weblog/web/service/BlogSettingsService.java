package com.quanxiaoha.weblog.web.service;

import com.quanxiaoha.weblog.common.utils.Response;

/**
 * @author: North001
 * @url: blog.arnasoft.site
 * @date: 2026-04-21
 * @description: 博客设置
 **/
public interface BlogSettingsService {
    /**
     * 获取博客设置信息
     * @return
     */
    Response findDetail();
}
