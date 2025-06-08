package com.nguner.yeditepecardshop.repository;

import com.nguner.yeditepecardshop.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);
}

