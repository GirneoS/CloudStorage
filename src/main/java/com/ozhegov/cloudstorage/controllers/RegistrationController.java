package com.ozhegov.cloudstorage.controllers;

import com.ozhegov.cloudstorage.entity.StorageUser;
import com.ozhegov.cloudstorage.services.RegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/storage/register")
@AllArgsConstructor
public class RegistrationController {
    private RegistrationService registrationService;
    @PostMapping
    public String addUser(@RequestBody StorageUser user){
        registrationService.addUser(user);
        return "User " + user.getName() + " has been saved";
    }
}
