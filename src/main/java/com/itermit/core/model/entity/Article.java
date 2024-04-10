package com.itermit.core.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "article")
public class Article extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Size(max = 100)
    private String title;

    @Column
    @Size(max = 50)
    private String logo;

    @Column
    @Size(max = 250)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column
    private Boolean visible;

    @Column
    private Instant publishDate;

    @ManyToOne
    @JoinColumn(name = "usr_id")
    public User author;
}
