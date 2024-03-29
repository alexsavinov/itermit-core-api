package com.itermit.springtest02.repository.specification;

import com.itermit.springtest02.model.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.Set;

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

//    public static Specification<User> containsTextInAttributes(String text, List<String> attributes) {
//        if (!text.contains("%")) {
//            text = "%" + text + "%";
//        }
//        String finalText = text;
//        return (root, query, builder) -> builder.or(
//                root.getModel().getDeclaredSingularAttributes().stream()
//                        .filter(a -> attributes.contains(a.getName()))
//                        .map(a -> builder.like(root.get(a.getName()), finalText))
//                        .toArray(Predicate[]::new)
//        );
//    }

}
