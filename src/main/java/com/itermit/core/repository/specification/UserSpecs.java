package com.itermit.core.repository.specification;

import com.itermit.core.model.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;

public class UserSpecs {

    public static Specification<User> containsTextInName(String text) {
        String finalText = "%" + text + "%";

        Specification<User> userSpecification = (root, query, builder) -> builder.or(
                builder.like(root.get("username"), finalText),
                builder.like(root.get("profile").get("name"), finalText),
                builder.like(root.get("profile").get("surname"), finalText)
        );

        if (text.matches("\\d+")) {
            userSpecification = userSpecification.or((root, query, builder) ->
                    builder.in(root.get("id")).value(Collections.singleton(text))
            );
        }

        return userSpecification;
    }
}
