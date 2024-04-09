package com.itermit.springtest02.repository;

import com.itermit.springtest02.model.entity.Article;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageableArticleRepository
        extends PagingAndSortingRepository<Article, Long>, JpaSpecificationExecutor<Article> {
}
