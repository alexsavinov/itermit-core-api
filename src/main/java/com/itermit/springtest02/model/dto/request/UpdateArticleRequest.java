package com.itermit.springtest02.model.dto.request;

import lombok.*;

import java.time.Instant;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateArticleRequest {

    private Long id;
    private String title;
    private String logo;
    private String description;
    private String content;
    private Boolean visible;
    private Instant publishDate;
    private Long authorId;
}
