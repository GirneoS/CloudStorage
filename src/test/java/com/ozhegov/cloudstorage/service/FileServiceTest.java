package com.ozhegov.cloudstorage.service;

import com.ozhegov.cloudstorage.config.MinioConfig;
import com.ozhegov.cloudstorage.model.exception.NoSuchFileException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {
    private final String mainBucket = "main-bucket";
    private final String dirName = UUID.randomUUID().toString();
    private MinioClient getClient(){
        String rootPass = System.getenv("MINIO_PASS");
        String root = "root";
        String url = "http://localhost:9001";

        return MinioClient.builder().endpoint(url).credentials(root, rootPass).build();
    }
    @Test
    void checkDirExisting() throws ServerException, InsufficientDataException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        MinioClient client = getClient();
        try {
            client.statObject(StatObjectArgs.builder().bucket(mainBucket).object("test-user/opi_3.jpg").build());
            System.out.println("true");
        }catch(ErrorResponseException e){
            if (e.errorResponse().code().equals("NoSuchKey")){
                System.out.println("false");
            }else{
                System.out.println(e.errorResponse().code());
                throw new RuntimeException();
            }
        }
    }
    @Test
    void downloadFile() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        MinioClient client = getClient();

//        StatObjectResponse stats = client.statObject(StatObjectArgs.builder().bucket(mainBucket).object("user-10-files/лаб6_Ожегов.pdf").build());
        InputStream isFile = client.getObject(GetObjectArgs.builder().bucket(mainBucket).object("user-10-files/лаб6_Ожегов.pdf").build());
        byte[] fileData = isFile.readAllBytes();
        System.out.println(fileData.length);

        StatObjectResponse stats = client.statObject(StatObjectArgs.builder().bucket(mainBucket).object("user-11-files/lab1_Ozhegov.pdf").build());
        System.out.println(stats.contentType());
    }

}