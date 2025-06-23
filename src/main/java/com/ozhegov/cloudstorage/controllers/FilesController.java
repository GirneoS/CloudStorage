package com.ozhegov.cloudstorage.controllers;

import com.google.gson.Gson;
import com.ozhegov.cloudstorage.model.exception.FileIsAlreadyExistsException;
import com.ozhegov.cloudstorage.model.exception.NoSuchFileException;
import com.ozhegov.cloudstorage.model.dto.Blob;
import com.ozhegov.cloudstorage.model.dto.Message;
import com.ozhegov.cloudstorage.model.exception.StorageException;
import com.ozhegov.cloudstorage.model.entity.StorageUser;
import com.ozhegov.cloudstorage.repository.UserRepository;
import com.ozhegov.cloudstorage.service.FileService;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
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
        path = "user-" + getCurrentUserId() + "-files/" + path;
        System.out.println(path);
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
            List<Blob> files = service.getAllInDir(path);
            String json = (new Gson()).toJson(files);
//            System.out.println("json на выход: "+json);
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
            String json = (new Gson()).toJson(new Message("Invalid or empty path"));
            return ResponseEntity.status(HttpStatus.valueOf(400)).body(json);
        }catch(NoSuchFileException e){
            String json = (new Gson()).toJson(new Message("Resource not found"));
            return ResponseEntity.status(404).body(json);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/resource/download")
    public ResponseEntity<?> downloadFile(@RequestParam String path){
        try{
            byte[] binaryData = service.downloadFile(path);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return new ResponseEntity<>(binaryData, headers, HttpStatus.OK);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    @PostMapping(value="/resource", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam String path, @RequestParam("object") MultipartFile file){
        Long userId = getCurrentUserId();
        if(userId == null)
            return ResponseEntity.status(401).body("Сессия пользователя истекла");
        path = "user-" + userId + "-files/" + path;
        try{
            Blob blob = service.uploadFile(file, path);
            String json = (new Gson()).toJson(blob);
            return ResponseEntity.status(HttpStatus.valueOf(201)).body(json);
        } catch (StorageException | ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        } catch (FileIsAlreadyExistsException e) {
            String json = (new Gson()).toJson(new Message("File already exists"));
            return ResponseEntity.status(409).body(json);
        }
    }
    @GetMapping("/resource/move")
    public ResponseEntity<String> replaceResource(@RequestParam String from, @RequestParam String to){
        System.out.println("перемещение: " + "from=" + from + "; to=" + to);
        try{
            Blob blob = service.replaceResource(from, to);
            String json = (new Gson()).toJson(blob);
            return ResponseEntity.status(200).body(json);
        } catch(NoSuchFileException e) {
            String json = (new Gson()).toJson(new Message("File to replace hasn't been found"));
            return ResponseEntity.status(404).body(json);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        }
    }
    @DeleteMapping("/resource")
    public ResponseEntity<String> deleteFileByPath(@RequestParam String path) {
        System.out.println("удаление файла, полный путь: " + path);
        try {
            service.deleteObject(path);
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
