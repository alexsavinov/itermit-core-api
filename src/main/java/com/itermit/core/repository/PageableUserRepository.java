package com.itermit.core.repository;

import com.itermit.core.model.entity.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageableUserRepository
        extends PagingAndSortingRepository<User, Long>, JpaSpecificationExecutor<User> {
}
