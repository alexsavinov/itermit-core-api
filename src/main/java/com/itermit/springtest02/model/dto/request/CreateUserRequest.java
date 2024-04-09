package com.itermit.springtest02.model.dto.request;

import com.itermit.springtest02.model.entity.Profile;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest {

    private String username;
    private String password;
    private Set<String> role;
    private Profile profile;
}
