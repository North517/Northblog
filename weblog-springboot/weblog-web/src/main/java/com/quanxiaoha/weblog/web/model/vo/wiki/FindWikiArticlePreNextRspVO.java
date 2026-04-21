package com.quanxiaoha.weblog.web.model.vo.wiki;

import com.quanxiaoha.weblog.web.model.vo.article.FindPreNextArticleRspVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: North001
 * @Date: 2026-04-21
 * @Version: v1.0.0
 * @Description: TODO
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindWikiArticlePreNextRspVO {
    /**
     * 上一篇文章
     */
    private FindPreNextArticleRspVO preArticle;
    /**
     * 下一篇文章
     */
    private FindPreNextArticleRspVO nextArticle;

}