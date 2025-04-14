package com.ozhegov.cloudstorage.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/storage/files")
public class FilesController {
    @GetMapping
    public String getFiles(){
        return "All files";
    }
}
