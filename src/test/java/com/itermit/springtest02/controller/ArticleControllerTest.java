package com.itermit.springtest02.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itermit.springtest02.controller.advice.ApplicationControllerAdvice;
import com.itermit.springtest02.exception.ArticleNotFoundException;
import com.itermit.springtest02.model.dto.ArticleDto;
import com.itermit.springtest02.model.dto.request.CreateArticleRequest;
import com.itermit.springtest02.model.dto.request.UpdateArticleRequest;
import com.itermit.springtest02.model.entity.Article;
import com.itermit.springtest02.service.ArticleService;
import com.itermit.springtest02.service.mapper.ArticleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ArticleControllerTest {

    private static final Long ARTICLE_ID = 1L;
    @InjectMocks
    private ArticleController subject;
    @Mock
    private ArticleService articleService;
    @Mock
    private ArticleMapper articleMapper;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    @Mock
    private PagedResourcesAssembler<Article> pagedResourcesAssembler;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(subject)
                .setControllerAdvice(new ApplicationControllerAdvice())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void getArticleById() throws Exception {
        Article expectedArticle = new Article();
        ArticleDto articleDto = ArticleDto.builder().id(ARTICLE_ID).title("Article1").build();

        when(articleService.findById(any(Long.class))).thenReturn(expectedArticle);
        when(articleMapper.toDto(any(Article.class))).thenReturn(articleDto);

        mockMvc.perform(
                        get("/articles/{id}", ARTICLE_ID)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(articleDto.getId().toString()))
                .andExpect(jsonPath("$.title").value(articleDto.getTitle()));

        verify(articleService).findById(ARTICLE_ID);
        verify(articleMapper).toDto(expectedArticle);
        verifyNoMoreInteractions(articleService, articleMapper);
    }

    @Test
    void getArticleById_whenArticleNotFoundExceptionIsThrows_returns404() throws Exception {
        String errorMessage = "Article not found";

        when(articleService.findById(any(Long.class))).thenThrow(new ArticleNotFoundException(errorMessage));

        mockMvc.perform(
                        get("/articles/{id}", ARTICLE_ID)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value(errorMessage));

        verify(articleService).findById(ARTICLE_ID);
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void getArticles() throws Exception {
        Article expectedArticle = Article.builder().id(ARTICLE_ID).title("Article").build();

        List<Article> expectedArticles = List.of(expectedArticle);

        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Page<Article> pageableExpectedArticles = new PageImpl<>(expectedArticles, pageable, expectedArticles.size());

        when(articleService.findAll(any(Pageable.class), any())).thenReturn(pageableExpectedArticles);

        mockMvc.perform(
                        get("/articles")
                                .param("page", "0")
                                .param("size", "5")
                                .param("sort", "name,asc")
                                .param("search", "name=test")
                )
                .andExpect(status().isOk());

        verify(articleService).findAll(pageable, "name=test");
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void addArticle() throws Exception {
        Article createdArticle = new Article();
        ArticleDto articleDto = ArticleDto.builder().id(ARTICLE_ID).title("Article1").build();

        when(articleService.create(any(CreateArticleRequest.class))).thenReturn(createdArticle);
        when(articleMapper.toDto(any(Article.class))).thenReturn(articleDto);

        RequestBuilder requestBuilder = post("/articles")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(createdArticle))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(articleDto.getId().toString()))
                .andExpect(jsonPath("$.title").value(articleDto.getTitle()));

        verify(articleMapper).toDto(createdArticle);
        verifyNoMoreInteractions(articleService, articleMapper);
    }

    @Test
    void updateArticle() throws Exception {
        Article updatedArticle = new Article();
        ArticleDto articleDto = ArticleDto.builder().id(ARTICLE_ID).title("Article1").build();

        when(articleService.update(any(UpdateArticleRequest.class))).thenReturn(updatedArticle);
        when(articleMapper.toDto(any(Article.class))).thenReturn(articleDto);

        RequestBuilder requestBuilder = patch("/articles")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(updatedArticle))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(articleDto.getId().toString()))
                .andExpect(jsonPath("$.title").value(articleDto.getTitle()));

        verify(articleMapper).toDto(updatedArticle);
        verifyNoMoreInteractions(articleService, articleMapper);
    }

    @Test
    void deleteArticleById() throws Exception {
        RequestBuilder requestBuilder = delete("/articles/{id}", ARTICLE_ID);

        mockMvc.perform(requestBuilder).andExpect(status().isNoContent());

        verify(articleService).delete(ARTICLE_ID);
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void updateAvatar() throws Exception {
        MockMultipartFile firstFile = new MockMultipartFile(
                "data",
                "filename.txt",
                "text/plain",
                "some xml".getBytes()
        );

        RequestBuilder requestBuilder = multipart("/articles/saveImage")
                .file(firstFile);

        when(articleService.saveImage(any())).thenReturn("filename.txt");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isAccepted());

        verify(articleService).saveImage(eq(null));
        verifyNoMoreInteractions(articleService);
    }
}