package com.ozhegov.cloudstorage.repository;

import com.ozhegov.cloudstorage.entity.Blob;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FileRepository {
    private final MinioClient client;
    @Autowired
    public FileRepository(MinioClient client){
        this.client = client;
    }
    public Blob getResourceByPath(String path) {
        try{
            Item item = client.listObjects(ListObjectsArgs.builder()
                    .bucket("main-bucket")
                    .prefix(path)
                    .build())
                    .iterator().next().get();
            return convertToBlob(item.objectName(), item.size(), item.isDir());
        }catch(ServerException | InsufficientDataException | ErrorResponseException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e){
            throw new RuntimeException();
        }
    }
    public Blob updatePathOfResource(String from, String to){
        try {
            ObjectWriteResponse response = client.copyObject(CopyObjectArgs.builder()
                    .bucket("main-bucket")
                    .object(to)
                    .source(CopySource.builder()
                            .bucket("main-bucket")
                            .object(from).build())
                    .build());
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket("main-bucket")
                    .object(from)
                    .build());
            GetObjectResponse stats = client.getObject(GetObjectArgs.builder()
                    .bucket("main-bucket")
                    .matchETag(response.etag())
                    .object(to)
                    .build());

            return convertToBlob(to, stats.headers().size(), to.charAt(to.length()-1) == '/');
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
    }
    public List<Blob> getFilesInDir(String prefix){
        List<Blob> fileList = new ArrayList<>();
        client.listObjects(ListObjectsArgs.builder().bucket("main-bucket").prefix(prefix).build()).forEach(o -> {
            try {
                Item item = o.get();
                fileList.add(convertToBlob(item.objectName(), item.size(), item.isDir()));
            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                     InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                throw new RuntimeException(e);
            }
        });
        return fileList;
    }
    public boolean deleteResource(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        client.removeObject(RemoveObjectArgs.builder()
                .bucket("main-bucket")
                .object(path)
                .build());
        return true;
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
