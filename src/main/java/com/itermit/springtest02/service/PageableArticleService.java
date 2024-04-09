package com.itermit.springtest02.service;

import com.itermit.springtest02.model.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PageableArticleService {

    Page<Article> findAll(Pageable pageable, String search);
}
