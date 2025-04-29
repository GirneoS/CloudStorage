package com.ozhegov.cloudstorage.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "Users")
@Data
public class StorageUser {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String roles;
}
