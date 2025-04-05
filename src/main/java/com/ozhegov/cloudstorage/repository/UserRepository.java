package com.ozhegov.cloudstorage.repository;

import com.ozhegov.cloudstorage.entity.User;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface UserRepository extends Repository<User, Long> {
    List<User> findAllById(Long id);
    User findByName(String name);
    boolean existsByName(String name);
}
