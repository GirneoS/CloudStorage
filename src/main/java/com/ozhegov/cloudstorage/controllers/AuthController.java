package com.ozhegov.cloudstorage.controllers;

import com.google.gson.Gson;
import com.ozhegov.cloudstorage.config.CustomUserDetails;
import com.ozhegov.cloudstorage.dto.ErrorMessage;
import com.ozhegov.cloudstorage.dto.AuthRequest;
import com.ozhegov.cloudstorage.dto.UserDTO;
import com.ozhegov.cloudstorage.service.AuthService;
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

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private AuthService authService;
    private AuthenticationManager authManager;

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@Validated @RequestBody AuthRequest request, HttpSession session){
        try {
            UserDTO dto = authService.signUpUser(request);
            String json = new Gson().toJson(dto);

            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getName(),request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            SecurityContext context = SecurityContextHolder.getContext();

            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            return ResponseEntity.status(201).body(json);
        }catch(IllegalArgumentException e){
            String json = (new Gson()).toJson(new ErrorMessage("User with this name already exists"));
            return ResponseEntity.status(409).body(json);
        }
    }
    //здесь мы возьмем пользователя из бд и добавим его в редис
    @PostMapping("/sign-in")
    public ResponseEntity<String> signIn(@Validated @RequestBody AuthRequest req, HttpSession session) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getName(), req.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserDTO dto = new UserDTO(userDetails.getUsername());
        String json = new Gson().toJson(dto);

        return ResponseEntity.ok(new Gson().toJson(json));
    }

}
