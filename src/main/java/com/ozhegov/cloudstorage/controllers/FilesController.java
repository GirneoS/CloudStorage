package com.ozhegov.cloudstorage.controllers;

import com.google.gson.Gson;
import com.ozhegov.cloudstorage.exception.FileIsAlreadyExistsException;
import com.ozhegov.cloudstorage.exception.NoSuchFileException;
import com.ozhegov.cloudstorage.model.Blob;
import com.ozhegov.cloudstorage.model.ErrorMessage;
import com.ozhegov.cloudstorage.service.FileService;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/storage")
public class FilesController {
    @Autowired
    private FileService service;
    @PostMapping("/directory?path={path}")
    public ResponseEntity<?> createDirectory(@PathVariable String path){
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
    @GetMapping("/directory?path={path}")
    public ResponseEntity<?> getFilesInDir(@PathVariable String path){
        try {
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
    @GetMapping("/resource?path={path}")
    public ResponseEntity<?> getFileByPath(@PathVariable String path){
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
    @GetMapping("/resource/download?path={path}")
    public ResponseEntity<?> downloadFile(@PathVariable String path){
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
    @PostMapping("/resource?path={path}")
    public ResponseEntity<?> uploadFile(@PathVariable String path, @RequestParam MultipartFile file){
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
    @GetMapping("/resource/move?from={from}&to={to}")
    public ResponseEntity<?> replaceResource(@PathVariable String from, @PathVariable String to){
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
    @DeleteMapping("/resource?path={path}")
    public HttpStatus deleteFileByPath(@PathVariable String path) {
        try {
            service.deleteFile(path);
            return HttpStatus.valueOf(204);
        }catch(NoSuchFileException e){
            return HttpStatus.valueOf(404);
        }catch (ServerException | InsufficientDataException | IOException |
                NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                InternalException | ErrorResponseException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
