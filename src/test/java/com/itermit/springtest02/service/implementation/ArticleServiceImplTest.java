package com.itermit.core.service.implementation;

import com.itermit.core.exception.ArticleNotFoundException;
import com.itermit.core.model.dto.request.CreateArticleRequest;
import com.itermit.core.model.dto.request.UpdateArticleRequest;
import com.itermit.core.model.entity.User;
import com.itermit.core.service.implementation.ArticleServiceImpl;
import com.itermit.core.service.implementation.UserServiceImpl;
import com.itermit.core.model.entity.Article;
import com.itermit.core.repository.PageableArticleRepository;
import com.itermit.core.repository.ArticleRepository;
import com.itermit.core.service.mapper.ArticleMapper;
import com.itermit.core.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    private static final Long ARTICLE_ID = 1L;
    private static final Long USER_ID = 1L;
    @InjectMocks
    private ArticleServiceImpl subject;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserServiceImpl userService;
    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private PageableArticleRepository pageableArticleRepository;
    @Mock
    private FileUtils fileUtils;

    @Test
    void findById() {
        Article expectedArticle = Article.builder().id(ARTICLE_ID).build();

        when(articleRepository.findById(any(Long.class))).thenReturn(of(expectedArticle));

        Article actualArticle = subject.findById(ARTICLE_ID);

        verify(articleRepository).findById(ARTICLE_ID);
        verifyNoMoreInteractions(articleRepository);

        assertThat(actualArticle).isEqualTo(expectedArticle);
    }

    @Test
    void findById_whenArticleIsNotFoundById_throwsArticleNotFoundException() {
        ArticleNotFoundException exception = assertThrows(ArticleNotFoundException.class,
                () -> subject.findById(ARTICLE_ID));

        verify(articleRepository).findById(ARTICLE_ID);
        verifyNoMoreInteractions(articleRepository);

        String expectedMessage = "Requested resource not found (id = " + ARTICLE_ID + ")";
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void findAll() {
        List<Article> expectedArticles = List.of(new Article());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<Article> pageableExpectedArticles = new PageImpl<>(expectedArticles, pageable, expectedArticles.size());

        when(pageableArticleRepository.findAll(any(Pageable.class))).thenReturn(pageableExpectedArticles);

        Page<Article> actualArticles = subject.findAll(pageable, "");

        verify(pageableArticleRepository).findAll(pageable);
        verifyNoMoreInteractions(pageableArticleRepository);

        assertThat(actualArticles).isEqualTo(pageableExpectedArticles);
    }

    @Test
    void findAll_withSearch() {
        List<Article> expectedArticles = List.of(new Article());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<Article> pageableExpectedArticles = new PageImpl<>(expectedArticles, pageable, expectedArticles.size());

        when(pageableArticleRepository.findAll(any(), any(Pageable.class))).thenReturn(pageableExpectedArticles);

        Page<Article> actualArticles = subject.findAll(pageable, "name=test");

        verifyNoMoreInteractions(pageableArticleRepository);

        assertThat(actualArticles).isEqualTo(pageableExpectedArticles);
    }
    
    @Test
    void create() {
        CreateArticleRequest createRequest = CreateArticleRequest.builder()
                .title("myArticle")
                .authorId(USER_ID)
                .build();
        Article expectedArticle = Article.builder().id(ARTICLE_ID).title("myArticle").build();
        User user = User.builder().id(USER_ID).build();

        when(articleRepository.save(any(Article.class))).thenReturn(expectedArticle);
        when(articleMapper.toArticle(any(CreateArticleRequest.class), any(User.class))).thenReturn(expectedArticle);
        when(userService.findById(any(Long.class))).thenReturn(user);

        Article actualArticle = subject.create(createRequest);

        verify(articleRepository).save(eq(expectedArticle));
        verify(userService).findById(eq(USER_ID));
        verify(articleMapper).toArticle(eq(createRequest), eq(user));
        verifyNoMoreInteractions(articleRepository, userService, articleMapper);

        assertThat(actualArticle).isEqualTo(expectedArticle);
    }

    @Test
    void update() {        
        Article expectedArticle = Article.builder()
                .id(ARTICLE_ID)
                .title("myArticle")
                .build();

        UpdateArticleRequest updateRequest = UpdateArticleRequest.builder()
                .title("myArticle")
                .authorId(USER_ID)
                .build();
        User user = User.builder().id(USER_ID).build();

        when(articleRepository.save(any(Article.class))).thenReturn(expectedArticle);
        when(articleMapper.toArticle(any(UpdateArticleRequest.class), any(User.class))).thenReturn(expectedArticle);
        when(userService.findById(any(Long.class))).thenReturn(user);

        Article actualArticle = subject.update(updateRequest);

        verify(articleRepository).save(eq(expectedArticle));
        verify(userService).findById(eq(USER_ID));
        verify(articleMapper).toArticle(eq(updateRequest), eq(user));
        verifyNoMoreInteractions(articleRepository, userService, articleMapper);

        assertThat(actualArticle).isEqualTo(expectedArticle);
    }

    @Test
    void delete() {
        Article deleteArticle = Article.builder().id(ARTICLE_ID).title("myArticle").build();

        when(articleRepository.findById(any(Long.class))).thenReturn(of(deleteArticle));

        subject.delete(ARTICLE_ID);

        verify(articleRepository).delete(deleteArticle);
        verifyNoMoreInteractions(articleRepository);
    }

    @Test
    void saveImage() {
        String fileName = "filename.txt";
        MockMultipartFile file = new MockMultipartFile(
                "data",
                fileName,
                "text/plain",
                "some xml".getBytes()
        );

        when(fileUtils.saveArticleImage(any(MultipartFile.class))).thenReturn(fileName);

        String actualFileName = subject.saveImage(file);

        assertThat(actualFileName).isEqualTo(fileName);
        verify(fileUtils).saveArticleImage(any());
        verifyNoMoreInteractions(fileUtils);
    }

}