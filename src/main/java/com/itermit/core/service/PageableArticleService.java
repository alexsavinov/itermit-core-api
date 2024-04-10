package com.itermit.core.service;

import com.itermit.core.model.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PageableArticleService {

    Page<Article> findAll(Pageable pageable, String search);
}
