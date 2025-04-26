package com.ozhegov.cloudstorage.controllers;

import com.ozhegov.cloudstorage.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/storage")
public class FilesController {
    @Autowired
    private FileService fileService;
    @GetMapping
    public String getFiles(){
        return "All files";
    }
    @PostMapping("file/{id}")
    public String deleteFileById(@PathVariable long id){
        return "File with id=" + id + " has been deleted";
    }
    @DeleteMapping("resource/{path}")
    public HttpStatus deleteFileByPath(@PathVariable String path){
        try {
            fileService.deleteFile(path);
            return HttpStatus.NO_CONTENT;
        }catch() {
            return
        }

    }
    @GetMapping("/resource/{path}")
    public ResponseEntity<?> getFileByPath(@PathVariable String path){
        try {
            String json = fileService.getFile(path);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(json);
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.valueOf(400)).body("");
        }
    }
}
