package com.itermit.core.model.dto;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Relation(itemRelation = "article", collectionRelation = "articles")
public class ArticleDto extends RepresentationModel<ArticleDto> {

    private Long id;
    private String title;
    private String logo;
    private String description;
    private String content;
    private Boolean visible;
    private UserDto author;
    private Instant publishDate;
    private Instant createdDate;
    private Instant lastUpdateDate;
}
