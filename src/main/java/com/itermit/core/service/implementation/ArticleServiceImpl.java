package com.itermit.core.service.implementation;

import com.itermit.core.exception.ArticleNotFoundException;
import com.itermit.core.model.dto.request.CreateArticleRequest;
import com.itermit.core.model.dto.request.UpdateArticleRequest;
import com.itermit.core.repository.ArticleRepository;
import com.itermit.core.repository.PageableArticleRepository;
import com.itermit.core.model.dto.request.*;
import com.itermit.core.model.entity.Article;
import com.itermit.core.model.entity.User;
import com.itermit.core.repository.*;
import com.itermit.core.repository.specification.ArticleSpecs;
import com.itermit.core.service.ArticleService;
import com.itermit.core.service.UserService;
import com.itermit.core.service.mapper.ArticleMapper;
import com.itermit.core.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserService userService;
    private final PageableArticleRepository pageableArticleRepository;
    private final ArticleMapper articleMapper;
    private final FileUtils fileUtils;


    @Override
    public Article findById(Long id) {
        log.debug("Looking for an article with id {}", id);

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("Requested resource not found (id = %s)"
                        .formatted(id)
                ));

        log.info("Retrieved an article with id {}", id);
        return article;
    }

    @Override
    public Page<Article> findAll(Pageable pageable, String search) {
        log.debug("Retrieving articles. Page request: {}", pageable);

        Page<Article> articles;
        if (ofNullable(search).isEmpty() || search.equals("")) {
            articles = pageableArticleRepository.findAll(pageable);
        } else {
            articles = pageableArticleRepository.findAll(
                    Specification.where(ArticleSpecs.containsTextInName(search)),
                    pageable);
        }

        log.info("Retrieved {} articles of {} total", articles.getSize(), articles.getTotalElements());
        return articles;
    }

    @Override
    public Article create(CreateArticleRequest createRequest) {
        log.debug("Creating a new article");

        User user = userService.findById(createRequest.getAuthorId());
        Article newArticle = articleMapper.toArticle(createRequest, user);
        Article createdArticle = articleRepository.save(newArticle);

        log.info("Created a new article with id {}", createdArticle.getId());
        return createdArticle;
    }

    @Override
    public Article update(UpdateArticleRequest updateRequest) {
        log.debug("Updating article");

        Article article;
        User user = userService.findById(updateRequest.getAuthorId());
        article = articleMapper.toArticle(updateRequest, user);

        Article updatedArticle = articleRepository.save(article);

        log.info("Updated an article with id {}", updatedArticle.getId());
        return updatedArticle;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting article with id {}", id);

        Article foundArticle = findById(id);

        articleRepository.delete(foundArticle);

        log.info("Article with id {} is deleted", foundArticle.getId());
    }

    @Override
    public String saveImage(MultipartFile image) {
        log.debug("Saving article image");

        String fileName = fileUtils.saveArticleImage(image);

        log.info("Saved article image with filename: {}", fileName);

        return fileName;
    }
}
