package com.itermit.core.repository;

import com.itermit.core.model.entity.Article;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageableArticleRepository
        extends PagingAndSortingRepository<Article, Long>, JpaSpecificationExecutor<Article> {
}
