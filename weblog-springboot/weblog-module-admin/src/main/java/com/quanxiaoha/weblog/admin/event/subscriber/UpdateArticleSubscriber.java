package com.quanxiaoha.weblog.admin.event.subscriber;

import com.quanxiaoha.weblog.admin.event.UpdateArticleEvent;
import com.quanxiaoha.weblog.common.constant.Constants;
import com.quanxiaoha.weblog.common.domain.dos.ArticleContentDO;
import com.quanxiaoha.weblog.common.domain.dos.ArticleDO;
import com.quanxiaoha.weblog.common.domain.mapper.ArticleContentMapper;
import com.quanxiaoha.weblog.common.domain.mapper.ArticleMapper;
import com.quanxiaoha.weblog.common.enums.ArticleTypeEnum;
import com.quanxiaoha.weblog.search.LuceneHelper;
import com.quanxiaoha.weblog.search.index.ArticleIndex;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @Author: North001
 * @Date: 2026-04-21
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Component
@Slf4j
public class UpdateArticleSubscriber implements ApplicationListener<UpdateArticleEvent> {

    @Autowired
    private LuceneHelper luceneHelper;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ArticleContentMapper articleContentMapper;

    @Override
    @Async("threadPoolTaskExecutor")
    public void onApplicationEvent(UpdateArticleEvent event) {
        // 在这里处理收到的事件，可以是任何逻辑操作
        Long articleId = event.getArticleId();

        // 获取当前线程名称
        String threadName = Thread.currentThread().getName();

        log.info("==> threadName: {}", threadName);
        log.info("==> 文章更新事件消费成功，articleId: {}", articleId);

        // 查询文章数据
        ArticleDO articleDO = articleMapper.selectById(articleId);

        // 更新条件（通过文章 ID 来更新）
        Term condition = new Term(ArticleIndex.COLUMN_ID, String.valueOf(articleId));

        if (articleDO == null || !ArticleTypeEnum.NORMAL.getValue().equals(articleDO.getType()) || Boolean.TRUE.equals(articleDO.getIsPrivate())) {
            long count = luceneHelper.deleteDocument(ArticleIndex.NAME, condition);
            log.info("==> 删除草稿或私密文章对应 Lucene 文档结束，articleId: {}，受影响行数: {}", articleId, count);
            return;
        }

        ArticleContentDO articleContentDO = articleContentMapper.selectByArticleId(articleId);

        // 构建文档
        Document document = new Document();
        document.add(new TextField(ArticleIndex.COLUMN_ID, String.valueOf(articleId), Field.Store.YES));
        document.add(new TextField(ArticleIndex.COLUMN_TITLE, articleDO.getTitle(), Field.Store.YES));
        document.add(new TextField(ArticleIndex.COLUMN_COVER, StringUtils.hasText(articleDO.getCover()) ? articleDO.getCover() : "", Field.Store.YES));
        document.add(new TextField(ArticleIndex.COLUMN_SUMMARY, StringUtils.hasText(articleDO.getSummary()) ? articleDO.getSummary() : "", Field.Store.YES));
        document.add(new TextField(ArticleIndex.COLUMN_CONTENT, articleContentDO.getContent(), Field.Store.YES));
        document.add(new TextField(ArticleIndex.COLUMN_CREATE_TIME, Constants.DATE_TIME_FORMATTER.format(articleDO.getCreateTime()), Field.Store.YES));

        long count = luceneHelper.updateDocument(ArticleIndex.NAME, document, condition);

        log.info("==> 更新文章对应 Lucene 文档结束，articleId: {}，受影响行数: {}", articleId, count);
    }
}