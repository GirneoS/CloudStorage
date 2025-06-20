package com.ozhegov.cloudstorage.service;

import com.ozhegov.cloudstorage.model.dto.Blob;
import com.ozhegov.cloudstorage.model.exception.FileIsAlreadyExistsException;
import com.ozhegov.cloudstorage.model.exception.NoSuchFileException;
import com.ozhegov.cloudstorage.model.exception.StorageException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FileService {
    private final MinioClient client;
    @Autowired
    public FileService(MinioClient client){
        this.client = client;
    }
    @Value("${main.bucket}")
    private String mainBucket;
    public Blob uploadFile(MultipartFile file, String path) throws FileIsAlreadyExistsException, StorageException, ServerException, InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, ErrorResponseException {
        if(!path.endsWith("/"))
            path += "/";
        String fullPath = path + file.getOriginalFilename();
        try(InputStream is = file.getInputStream()) {
            long size = file.getSize();

            Map<String, String> header = Map.of("If-None-Match", "*");
            try {
                client.putObject(PutObjectArgs.builder()
                        .bucket(mainBucket)
                        .object(fullPath)
                        .stream(is, size, -1)
                        .headers(header)
                        .contentType(file.getContentType())
                        .build());

                deleteBadSideDir(fullPath);
                return convertToBlob(fullPath, size, !file.getResource().isFile());
            } catch(ErrorResponseException e){
                deleteBadSideDir(fullPath);
                if(e.errorResponse().code().equals("PreconditionFailed"))
                    throw new FileIsAlreadyExistsException(e.getMessage());
                throw new StorageException(e.getMessage());
            }
        }
    }
    public Blob getFile(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, NoSuchFileException {
        StatObjectResponse stats = null;
        try {
            stats = client.statObject(StatObjectArgs.builder()
                    .bucket(mainBucket)
                    .object(path)
                    .build());
        }catch(ErrorResponseException e){
            if(e.errorResponse().code().equals("NoSuchKey"))
                throw new NoSuchFileException(e.getMessage());
            throw e;
        }
        return convertToBlob(stats.object(), stats.size(), path.endsWith("/"));
    }
    public byte[] downloadFile(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        try(InputStream isFile = client.getObject(GetObjectArgs.builder().bucket(mainBucket).object(path).build())) {
            return isFile.readAllBytes();
        }
    }
    public Blob replaceResource(String from, String to) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, NoSuchFileException {
        if(from.endsWith("/"))
            return renameDir(from, to);
        return replaceFile(from, to);
    }
    public Blob renameDir(String from, String to) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, NoSuchFileException {
        Iterable<Result<Item>> itemsIterator = client.listObjects(ListObjectsArgs.builder().bucket(mainBucket).prefix(from).build());
        long dirSize = 0;
        int lastSlashId = to.length() - 1;
        for(Result<Item> result : itemsIterator){
            Item item = result.get();
            replaceFile(item.objectName(), to + item.objectName().substring(lastSlashId));
            dirSize += item.size();
        }
        return convertToBlob(to, dirSize, true);
    }
    public Blob replaceFile(String from, String to) throws NoSuchFileException, ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        try {
            client.copyObject(CopyObjectArgs.builder()
                    .bucket(mainBucket)
                    .object(to)
                    .source(CopySource.builder()
                            .bucket(mainBucket)
                            .object(from).build())
                    .build());
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(mainBucket)
                    .object(from)
                    .build());
            GetObjectResponse stats = client.getObject(GetObjectArgs.builder()
                    .bucket(mainBucket)
                    .object(to)
                    .build());

            return convertToBlob(to, stats.headers().size(), false);
        }catch(ErrorResponseException e){
            if(e.errorResponse().code().equals("NoSuchKey"))
                throw new NoSuchFileException(e.getMessage());
            throw e;
        }
    }
    public void createUserDirectory(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        try(InputStream is = new ByteArrayInputStream(new byte[0])) {
            client.putObject(PutObjectArgs.builder().bucket(mainBucket).object(path).stream(is, 0, -1).build());
        }
    }
    public Blob createDirectory(String path) throws NoSuchFileException, ErrorResponseException, ServerException, InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, FileIsAlreadyExistsException {
        System.out.println(path);
        if(doesDirExist(path)){
            throw new FileIsAlreadyExistsException("This directory is already exists");
        }
        try(InputStream is = new ByteArrayInputStream(new byte[0])) {
            client.putObject(PutObjectArgs.builder().bucket(mainBucket).object(path).stream(is, 0, -1).build());
            renameDir(path, path.substring(0,path.length() - 10) + "/");
            return convertToBlob(path, 0, true);
        }
    }
    public List<Blob> findAllInDir(String prefix) throws NoSuchFileException, ErrorResponseException, ServerException, InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<Blob> blobs = new ArrayList<>();
        Iterable<Result<Item>> files = client.listObjects(ListObjectsArgs.builder()
                .bucket(mainBucket)
                .prefix(prefix)
                .build());
        try {
            for (Result<Item> result : files) {
                Item item = result.get();
//                System.out.println("prefix=" + prefix + "; file=" + item.objectName());
                if(item.objectName().equals(prefix))
                    return List.of();
                blobs.add(convertToBlob(item.objectName(), item.size(), item.isDir()));
            }
        }catch(ErrorResponseException e){
            if(e.errorResponse().code().equals("NoSuchKey")){
                throw new NoSuchFileException(e.getMessage());
            }
            throw e;
        }
        return blobs;
    }
    public void deleteObject(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, NoSuchFileException {
        if(path.endsWith("/")){
            Iterable<Result<Item>> files = client.listObjects(ListObjectsArgs.builder().bucket(mainBucket).prefix(path).build());
            for(Result<Item> file: files)
                deleteSingleFile(file.get().objectName());
            return;
        }
        deleteSingleFile(path);
    }
    private void deleteSingleFile(String path) throws ServerException, InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, NoSuchFileException, ErrorResponseException {
        if(doesDirExist(path))
            client.removeObject(RemoveObjectArgs.builder()
                .bucket(mainBucket)
                .object(path)
                .build());
    }
    private boolean doesDirExist(String path){
        try{
            client.statObject(StatObjectArgs.builder().bucket(mainBucket).object(path).build());
            return true;
        }catch(ErrorResponseException e){
            if(e.errorResponse().code().equals("NoSuchKey"))
                return false;

            throw new RuntimeException();
        }catch(InsufficientDataException | InternalException | InvalidKeyException |
               InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e){
            throw new RuntimeException();
        }
    }
    private void deleteBadSideDir(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        int lastSlashId = path.lastIndexOf('/');
        if(lastSlashId == -1)
            return;
        path = path.substring(0,lastSlashId) + "__XLDIR__";

        Iterable<Result<Item>> files = client.listObjects(ListObjectsArgs.builder().bucket(mainBucket).prefix(path).build());
        if(!files.iterator().hasNext())
            return;
        for(Result<Item> result: files)
            client.removeObject(RemoveObjectArgs.builder().bucket(mainBucket).object(result.get().objectName()).build());

    }
    private Blob convertToBlob(String fullPath, long size, boolean isDir){
        if(isDir)
            fullPath = fullPath.substring(0,fullPath.lastIndexOf('/'));

        int lastSlashId = fullPath.lastIndexOf('/');
        String path = (lastSlashId != -1) ? fullPath.substring(0, lastSlashId + 1) : "";
        String name = (lastSlashId != -1) ? fullPath.substring(lastSlashId + 1) : fullPath;
        String file = isDir ? "DIRECTORY" : "FILE";
        if(isDir)
            name+='/';

        return Blob.builder()
                .path(path)
                .name(name)
                .size(size)
                .type(file)
                .build();
    }

}
