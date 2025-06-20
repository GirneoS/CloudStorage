package com.ozhegov.cloudstorage.repository;

import com.ozhegov.cloudstorage.model.entity.StorageUser;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends Repository<StorageUser, Long> {
    Optional<StorageUser> findByName(String name);
    boolean existsByName(String name);
    StorageUser save(StorageUser user);
    List<StorageUser> findAll();
    void deleteAll();
}
