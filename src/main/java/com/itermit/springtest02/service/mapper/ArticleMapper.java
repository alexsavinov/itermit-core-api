package com.itermit.springtest02.service.mapper;

import com.itermit.springtest02.model.dto.ArticleDto;
import com.itermit.springtest02.model.dto.UserDto;
import com.itermit.springtest02.model.dto.request.CreateArticleRequest;
import com.itermit.springtest02.model.dto.request.UpdateArticleRequest;
import com.itermit.springtest02.model.entity.Article;
import com.itermit.springtest02.model.entity.User;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ArticleMapper {

    private final ModelMapper modelMapper;
    private final UserMapper userMapper;

    public ArticleDto toDto(Article article) {
        ArticleDto articleDto = modelMapper.map(article, ArticleDto.class);
        UserDto userDto = userMapper.toDto(article.getAuthor());
        articleDto.setAuthor(userDto);

        return articleDto;
    }

    public Article toArticle(CreateArticleRequest request, User user) {
        Article article = modelMapper.map(request, Article.class);
        article.setId(0L);
        article.setAuthor(user);

        return article;
    }

    public Article toArticle(UpdateArticleRequest request, User user) {
        Article article = modelMapper.map(request, Article.class);
        article.setAuthor(user);

        return article;
    }

    public Article toArticle(UpdateArticleRequest request) {
        Article article = modelMapper.map(request, Article.class);

        return article;
    }
}
