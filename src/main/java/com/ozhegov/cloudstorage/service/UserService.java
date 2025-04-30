package com.ozhegov.cloudstorage.service;

import com.ozhegov.cloudstorage.dto.AuthRequest;
import com.ozhegov.cloudstorage.model.StorageUser;
import com.ozhegov.cloudstorage.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository repository;
    public void registerUser(AuthRequest request) {
        StorageUser user = new StorageUser();
        user.setName(request.getName());
        user.setPassword(request.getPassword());
        repository.save(user);
    }
}
