package com.ozhegov.cloudstorage.controllers;

import com.google.gson.Gson;
import com.ozhegov.cloudstorage.model.dto.Blob;
import com.ozhegov.cloudstorage.model.dto.Message;
import com.ozhegov.cloudstorage.model.entity.StorageUser;
import com.ozhegov.cloudstorage.repository.UserRepository;
import com.ozhegov.cloudstorage.service.FileService;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class FilesController {
    private FileService service;
    private UserRepository userRepository;
    @PostMapping("/directory")
    public ResponseEntity<String> createDirectory(@RequestParam String path){
        path = "user-" + getCurrentUserId() + "-files/" + path;
        Blob blob = service.createDirectory(path);
        String json = (new Gson()).toJson(blob);
        return ResponseEntity.status(201).body(json);

    }
    @GetMapping("/directory")
    public ResponseEntity<String> getFilesInDir(@RequestParam String path){

        Long userId = getCurrentUserId();
        if(userId == null)
            return ResponseEntity.status(401).body("Сессия пользователя истекла");
        path = "user-" + userId + "-files/" + path;
        List<Blob> files = service.getAllInDir(path);
        String json = (new Gson()).toJson(files);
        return ResponseEntity.status(200).body(json);

    }
    @GetMapping("/resource")
    public ResponseEntity<String> getFileByPath(@RequestParam String path){
        Long userId = getCurrentUserId();
        if(userId == null)
            return ResponseEntity.status(401).body("Сессия пользователя истекла");
        path = "user-" + userId + "-files/" + path;
        try {
            Blob blob = service.getFile(path);
            String json = (new Gson()).toJson(blob);
            return ResponseEntity.status(200).body(json);
        }catch(IllegalArgumentException e) {
            String json = (new Gson()).toJson(new Message("Invalid or empty path"));
            return ResponseEntity.status(HttpStatus.valueOf(400)).body(json);
        }
    }
    @GetMapping("/resource/download")
    public ResponseEntity<?> downloadFile(@RequestParam String path){
        byte[] binaryData = service.downloadFile(path);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return new ResponseEntity<>(binaryData, headers, HttpStatus.OK);
    }
    @PostMapping(value="/resource", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam String path, @RequestParam("object") MultipartFile file){
        Long userId = getCurrentUserId();
        if(userId == null)
            return ResponseEntity.status(401).body("Сессия пользователя истекла");
        path = "user-" + userId + "-files/" + path;
        Blob blob = service.uploadFile(file, path);
        String json = (new Gson()).toJson(blob);
        return ResponseEntity.status(HttpStatus.valueOf(201)).body(json);
    }
    @GetMapping("/resource/move")
    public ResponseEntity<String> replaceResource(@RequestParam String from, @RequestParam String to){
        Blob blob = service.replaceResource(from, to);
        String json = (new Gson()).toJson(blob);
        return ResponseEntity.status(200).body(json);
    }
    @DeleteMapping("/resource")
    public ResponseEntity<String> deleteFileByPath(@RequestParam String path) {
        service.deleteObject(path);
        return ResponseEntity.status(204).body("Ресурс успешно удален");

    }

    private Long getCurrentUserId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<StorageUser> user = userRepository.findByName(username);
        return user.map(StorageUser::getId).orElse(null);
    }
}
