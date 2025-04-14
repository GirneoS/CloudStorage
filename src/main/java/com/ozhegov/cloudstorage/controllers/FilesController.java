package com.ozhegov.cloudstorage.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/storage/files")
public class FilesController {
    @GetMapping
    public String getFiles(){
        return "All files";
    }
    @PostMapping("/{id}")
    public String deleteFileById(@PathVariable long id){
        return "File with id=" + id + " has been deleted";
    }
}
