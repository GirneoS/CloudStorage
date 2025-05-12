package com.ozhegov.cloudstorage.service;

import com.ozhegov.cloudstorage.dto.AuthRequest;
import com.ozhegov.cloudstorage.dto.UserDTO;
import com.ozhegov.cloudstorage.exception.WrongCredentials;
import com.ozhegov.cloudstorage.model.StorageUser;
import com.ozhegov.cloudstorage.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private UserRepository repository;
    private PasswordEncoder encoder;
    public UserDTO signUpUser(AuthRequest request){
        if(repository.existsByName(request.getUsername()))
            throw new IllegalArgumentException("User already exists");
        StorageUser user = new StorageUser();
        user.setName(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRoles("user");
        repository.save(user);
        return new UserDTO(user.getName());
    }
    public UserDTO signInUser(AuthRequest req) throws WrongCredentials {
        UserDTO dto = new UserDTO(req.getUsername());
        req.setPassword(encoder.encode(req.getPassword()));
        String passDB = repository.findByName(dto.getUsername()).orElseThrow(WrongCredentials::new).getPassword();
        if(!passDB.equals(req.getPassword()))
            throw new WrongCredentials();
        return new UserDTO(req.getUsername());
    }
}
