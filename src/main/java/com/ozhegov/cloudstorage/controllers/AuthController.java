package com.ozhegov.cloudstorage.controllers;

import com.google.gson.Gson;
import com.ozhegov.cloudstorage.config.CustomUserDetails;
import com.ozhegov.cloudstorage.model.dto.Message;
import com.ozhegov.cloudstorage.model.dto.AuthRequest;
import com.ozhegov.cloudstorage.model.dto.UserDTO;
import com.ozhegov.cloudstorage.model.exception.ResourceIsAlreadyExistsException;
import com.ozhegov.cloudstorage.model.exception.NoSuchFileException;
import com.ozhegov.cloudstorage.repository.UserRepository;
import com.ozhegov.cloudstorage.service.AuthService;
import com.ozhegov.cloudstorage.service.FileService;
import io.minio.errors.*;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private AuthService authService;
    private AuthenticationManager authManager;
    private FileService fileService;
    private UserRepository userRepository;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Validated @RequestBody AuthRequest request, HttpSession session){
        UserDTO dto = authService.signUpUser(request);

        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        SecurityContext context = SecurityContextHolder.getContext();

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        long userId = userRepository.findByName(dto.getUsername()).orElseThrow(RuntimeException::new).getId();
        fileService.createDirectory("user-" + userId + "-files/");
        return ResponseEntity.status(201).body(dto);
    }
    @PostMapping("/sign-in")
    public ResponseEntity<UserDTO> signIn(@Validated @RequestBody AuthRequest req, HttpSession session) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserDTO dto = new UserDTO(userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

}
