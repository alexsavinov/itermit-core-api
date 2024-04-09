package com.itermit.springtest02.controller;

import com.itermit.springtest02.model.dto.ArticleDto;
import com.itermit.springtest02.model.dto.ArticlesDto;
import com.itermit.springtest02.model.dto.request.CreateArticleRequest;
import com.itermit.springtest02.model.dto.request.UpdateArticleRequest;
import com.itermit.springtest02.model.entity.Article;
import com.itermit.springtest02.service.ArticleService;
import com.itermit.springtest02.service.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequiredArgsConstructor
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleMapper articleMapper;
    private final ArticlesDto articlesDto;
    private final PagedResourcesAssembler<Article> pagedResourcesAssembler;

    @GetMapping("/{id}")
    public ArticleDto getArticleById(@PathVariable Long id) {
        Article foundArticle = articleService.findById(id);
        ArticleDto articleDto = articleMapper.toDto(foundArticle);

        articleDto.add(linkTo(methodOn(ArticleController.class).getArticleById(articleDto.getId())).withSelfRel());
        articleDto.add(linkTo(methodOn(ArticleController.class).getArticles(null, null)).withRel("collection"));
        return articleDto;
    }

    @GetMapping
    public PagedModel<ArticleDto> getArticles(
            Pageable pageable,
            @RequestParam(required = false) String search) {
        Page<Article> foundArticles = articleService.findAll(pageable, search);

        return pagedResourcesAssembler.toModel(foundArticles, articlesDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleDto addArticle(@RequestBody CreateArticleRequest createRequest) {
        Article createdArticle = articleService.create(createRequest);
        ArticleDto articleDto = articleMapper.toDto(createdArticle);

        articleDto.add(linkTo(methodOn(ArticleController.class).addArticle(createRequest)).withSelfRel());
        articleDto.add(linkTo(methodOn(ArticleController.class).getArticles(null, null)).withRel("collection"));
        return articleDto;
    }

    @PatchMapping
    public ArticleDto updateArticle(@RequestBody UpdateArticleRequest updateRequest) {
        Article updatedArticle = articleService.update(updateRequest);
        ArticleDto articleDto = articleMapper.toDto(updatedArticle);

        articleDto.add(linkTo(methodOn(ArticleController.class).updateArticle(updateRequest)).withSelfRel());
        articleDto.add(linkTo(methodOn(ArticleController.class).getArticles(null, null)).withRel("collection"));
        return articleDto;
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArticleById(@PathVariable Long id) {
        articleService.delete(id);
    }

    @PostMapping("/saveImage")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map saveImage(MultipartFile file) {
        String fileName = articleService.saveImage(file);
        return Collections.singletonMap("location", fileName);
    }

}
