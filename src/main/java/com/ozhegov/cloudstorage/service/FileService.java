package com.ozhegov.cloudstorage.service;

import com.ozhegov.cloudstorage.dto.Blob;
import com.ozhegov.cloudstorage.exception.FileIsAlreadyExistsException;
import com.ozhegov.cloudstorage.exception.NoSuchFileException;
import com.ozhegov.cloudstorage.exception.StorageException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class FileService {
    private final MinioClient client;
    @Autowired
    public FileService(MinioClient client){
        this.client = client;
    }
    @Value("${main.bucket}")
    private  String mainBucket;
    public Blob uploadFile(MultipartFile file, String path) throws FileIsAlreadyExistsException, StorageException {
        if(!path.endsWith("/"))
            path += "/";
        String fullPath = path + file.getOriginalFilename();
        System.out.println(fullPath);
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
                return convertToBlob(fullPath, size, !file.getResource().isFile());
            } catch(ErrorResponseException e){
                if(e.errorResponse().code().equals("PreconditionFailed"))
                    throw new FileIsAlreadyExistsException(e.getMessage());
                throw new StorageException(e.getMessage());
            }
        } catch (IOException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new StorageException(e.getMessage());
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
    public Resource downloadFile(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        InputStream is =  client.getObject(GetObjectArgs.builder()
                .bucket(mainBucket)
                .object(path)
                .build());
        byte[] bytes = is.readAllBytes();
        return new ByteArrayResource(bytes);
    }
    public Blob replaceResource(String from, String to) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, NoSuchFileException {
        try {
            ObjectWriteResponse response = client.copyObject(CopyObjectArgs.builder()
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
                    .matchETag(response.etag())
                    .object(to)
                    .build());

            return convertToBlob(to, stats.headers().size(), to.endsWith("/"));
        }catch(ErrorResponseException e){
            if(e.errorResponse().code().equals("NoSuchKey"))
                throw new NoSuchFileException(e.getMessage());
            throw e;
        }
    }
    public Blob createDirectory(String path) throws NoSuchFileException, ErrorResponseException, ServerException, InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, FileIsAlreadyExistsException {
        int parentDirIndex = path.substring(0, path.length() - 1).lastIndexOf('/');
        String parentDirPath = path.substring(0, parentDirIndex + 1);
        try {
            client.statObject(StatObjectArgs.builder().bucket(mainBucket).object(parentDirPath).build());
        }catch(ErrorResponseException e){
            if(e.errorResponse().code().equals("NoSuchKey"))
                throw new NoSuchFileException(e.getMessage());
            throw e;
        }
        try {
            client.statObject(StatObjectArgs.builder().bucket(mainBucket).build());
            throw new FileIsAlreadyExistsException("This file is already exists");
        }catch(ErrorResponseException ignored){}
        InputStream is = new ByteArrayInputStream(new byte[0]);
        client.putObject(PutObjectArgs.builder().bucket(mainBucket).object(path).stream(is, 0, -1).build());
        return convertToBlob(path,0,true);
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
    public void deleteFile(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, NoSuchFileException {
        try {
            client.statObject(StatObjectArgs.builder()
                    .bucket(mainBucket)
                    .object(path)
                    .build());
        }catch (ErrorResponseException e){
            if(e.errorResponse().code().equals("NoSuchKey"))
                throw new NoSuchFileException(e.getMessage());
            throw e;
        }

        client.removeObject(RemoveObjectArgs.builder()
                .bucket(mainBucket)
                .object(path)
                .build());
    }
    private Blob convertToBlob(String fullPath, long size, boolean isDir){
        int lastSlash = fullPath.lastIndexOf('/');
        String path = (lastSlash != -1) ? fullPath.substring(0, lastSlash) : "";
        String name = (lastSlash != -1) ? fullPath.substring(lastSlash + 1) : fullPath;
        String file = isDir ? "DIRECTORY" : "FILE";

        return Blob.builder()
                .path(path)
                .name(name)
                .size(size)
                .type(file)
                .build();
    }

}
