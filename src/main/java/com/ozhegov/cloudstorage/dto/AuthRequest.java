package com.ozhegov.cloudstorage.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String name;
    private String password;
}
