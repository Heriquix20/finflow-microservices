package com.finflow.auth.repository;

import com.finflow.auth.model.AuthUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AuthUserRepository extends MongoRepository<AuthUser, String> {

    Optional<AuthUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
