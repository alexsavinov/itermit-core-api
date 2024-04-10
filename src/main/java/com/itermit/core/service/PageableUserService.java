package com.itermit.core.service;

import com.itermit.core.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PageableUserService {

    Page<User> findAll(Pageable pageable, String search);
}
