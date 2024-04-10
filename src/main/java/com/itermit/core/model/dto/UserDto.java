package com.itermit.core.model.dto;

import com.itermit.core.model.entity.Profile;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Relation(itemRelation = "user", collectionRelation = "users")
public class UserDto extends RepresentationModel<UserDto> {

    private Long id;
    private String username;
    private String createdDate;
    private String lastUpdateDate;
    private List<String> roles;
    private Profile profile;
}
