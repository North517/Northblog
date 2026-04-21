package com.quanxiaoha.weblog.web.service;

import com.quanxiaoha.weblog.common.utils.Response;

/**
 * @Author: North001
 * @Date: 2026-04-21
 * @Version: v1.0.0
 * @Description: TODO
 **/
public interface StatisticsService {
    /**
     * 获取文章总数、分类总数、标签总数、总访问量统计信息
     * @return
     */
    Response findInfo();
}
