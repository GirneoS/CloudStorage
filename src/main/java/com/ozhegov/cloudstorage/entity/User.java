package com.ozhegov.cloudstorage.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "Users")
@Data
public class User {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private String name;
    @Column
    private String password;
}
