package com.itermit.springtest02.model.dto;

import com.itermit.springtest02.controller.ArticleController;
import com.itermit.springtest02.model.entity.Article;
import com.itermit.springtest02.service.mapper.ArticleMapper;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ArticlesDto extends RepresentationModelAssemblerSupport<Article, ArticleDto> {

    private final ArticleMapper articleMapper;

    public ArticlesDto(ArticleMapper articleMapper) {
        super(ArticleController.class, ArticleDto.class);
        this.articleMapper = articleMapper;
    }

    @Override
    public ArticleDto toModel(Article entity) {
        ArticleDto articleDto = articleMapper.toDto(entity);
        articleDto.add(linkTo(methodOn(ArticleController.class).getArticleById(articleDto.getId())).withSelfRel());
        return articleDto;
    }
}
