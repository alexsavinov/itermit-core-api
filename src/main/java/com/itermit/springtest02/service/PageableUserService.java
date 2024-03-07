package com.itermit.springtest02.service;

import com.itermit.springtest02.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PageableUserService {

    Page<User> findAll(Pageable pageable, String search);
}
