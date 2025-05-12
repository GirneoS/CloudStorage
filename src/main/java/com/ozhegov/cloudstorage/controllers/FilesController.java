package com.ozhegov.cloudstorage.controllers;

import com.google.gson.Gson;
import com.ozhegov.cloudstorage.exception.FileIsAlreadyExistsException;
import com.ozhegov.cloudstorage.exception.NoSuchFileException;
import com.ozhegov.cloudstorage.dto.Blob;
import com.ozhegov.cloudstorage.dto.ErrorMessage;
import com.ozhegov.cloudstorage.model.StorageUser;
import com.ozhegov.cloudstorage.repository.UserRepository;
import com.ozhegov.cloudstorage.service.FileService;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class FilesController {
    @Autowired
    private FileService service;
    @Autowired
    private UserRepository userRepository;
    @PostMapping("/directory")
    public ResponseEntity<String> createDirectory(@RequestParam String path){
        try {
            Blob blob = service.createDirectory(path);
            String json = (new Gson()).toJson(blob);
            return ResponseEntity.status(201).body(json);
        }catch(NoSuchFileException e){
            return ResponseEntity.status(404).body("Parent directory doesn't exists");
        }catch(FileIsAlreadyExistsException e){
            return ResponseEntity.status(409).body("Such directory is already exists");
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/directory")
    public ResponseEntity<String> getFilesInDir(@RequestParam String path){
        try {
            Long userId = getCurrentUserId();
            if(userId == null)
                return ResponseEntity.status(401).body("Сессия пользователя истекла");
            path = "user-" + userId + "-files/" + path;
            List<Blob> files = service.findAllInDir(path);
            String json = (new Gson()).toJson(files);
            return ResponseEntity.status(200).body(json);
        }catch(NoSuchFileException e){
            return ResponseEntity.status(404).body("Папки по такому пути не существует");
        } catch (ErrorResponseException | ServerException | InsufficientDataException | IOException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
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
            String json = (new Gson()).toJson(new ErrorMessage("Invalid or empty path"));
            return ResponseEntity.status(HttpStatus.valueOf(400)).body(json);
        }catch(NoSuchFileException e){
            String json = (new Gson()).toJson(new ErrorMessage("Resource not found"));
            return ResponseEntity.status(404).body(json);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/resource/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path){
        Long userId = getCurrentUserId();
        if(userId == null) {
            ByteArrayResource empty = new ByteArrayResource(new byte[0]);
            return ResponseEntity.status(401).body(empty);
        }
        path = "user-" + userId + "-files/" + path;
        try{
            Resource resource = service.downloadFile(path);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,"inline; filename=\"" + resource.getFile() + "\"");

            return ResponseEntity.status(HttpStatusCode.valueOf(201)).headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException();
        }
    }
    @PostMapping("/resource")
    public ResponseEntity<String> uploadFile(@RequestParam String path, @RequestParam MultipartFile file){
        Long userId = getCurrentUserId();
        if(userId == null)
            return ResponseEntity.status(401).body("Сессия пользователя истекла");
        path = "user-" + userId + "-files/" + path;
        try{
            Blob blob = service.uploadFile(file, path);
            String json = (new Gson()).toJson(blob);
            return ResponseEntity.status(HttpStatus.valueOf(201)).body(json);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        } catch (FileIsAlreadyExistsException e) {
            String json = (new Gson()).toJson(new ErrorMessage("File already exists"));
            return ResponseEntity.status(409).body(json);
        }
    }
    @GetMapping("/resource/move")
    public ResponseEntity<String> replaceResource(@RequestParam String from, @RequestParam String to){
        Long userId = getCurrentUserId();
        if(userId == null)
            return ResponseEntity.status(401).body("Сессия пользователя истекла");
        to = "user-" + userId + "-files/" + to;
        from = "user-" + userId + "-files/" + from;
        try{
            Blob blob = service.replaceResource(from, to);
            String json = (new Gson()).toJson(blob);
            return ResponseEntity.status(200).body(json);
        } catch(NoSuchFileException e) {
            String json = (new Gson()).toJson(new ErrorMessage("File to replace hasn't been found"));
            return ResponseEntity.status(404).body(json);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        }
    }
    @DeleteMapping("/resource")
    public ResponseEntity<String> deleteFileByPath(@RequestParam String path) {
        Long userId = getCurrentUserId();
        if(userId == null)
            return ResponseEntity.status(401).body("Сессия пользователя истекла");
        path = "user-" + userId + "-files/" + path;
        try {
            service.deleteFile(path);
            return ResponseEntity.status(204).body("Ресурс успешно удален");
        }catch(NoSuchFileException e){
            return ResponseEntity.status(404).body("Указанный ресурс не найден");
        }catch (ServerException | InsufficientDataException | IOException |
                NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                InternalException | ErrorResponseException e) {
            return ResponseEntity.status(500).body("Ошибка сервера");
        }
    }

    private Long getCurrentUserId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<StorageUser> user = userRepository.findByName(username);
        return user.map(StorageUser::getId).orElse(null);
    }
}
