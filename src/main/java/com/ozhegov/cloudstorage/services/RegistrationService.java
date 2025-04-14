package com.ozhegov.cloudstorage.services;

import com.ozhegov.cloudstorage.entity.StorageUser;
import com.ozhegov.cloudstorage.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RegistrationService {
    private UserRepository repository;
    private PasswordEncoder encoder;
    public void addUser(StorageUser user){
        user.setPassword(encoder.encode(user.getPassword()));
        repository.save(user);
    }
}
