package com.ozhegov.cloudstorage.service;

import com.ozhegov.cloudstorage.model.dto.Blob;
import com.ozhegov.cloudstorage.model.exception.NoSuchFileException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
@SpringBootTest
class FileServiceTest {
    @Autowired
    private FileService fileService;
    private static final String testBucket = "test-bucket";
    private static final MinioClient client = getClient();
    private static MinioClient getClient(){
        String rootPass = System.getenv("MINIO_PASS");
        String root = "root";
        String url = "http://localhost:9001";

        return MinioClient.builder().endpoint(url).credentials(root, rootPass).build();
    }
    @BeforeAll
    static void createTestBucket() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        client.makeBucket(MakeBucketArgs.builder().bucket(testBucket).build());
    }
    @AfterAll
    static void deleteTestBucket() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Iterable<Result<Item>> files = client.listObjects(ListObjectsArgs.builder()
                                                            .bucket(testBucket)
                                                            .recursive(true).build());
        for(Result<Item> result: files){
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(testBucket)
                    .object(result.get().objectName()).build());
        }
        client.removeBucket(RemoveBucketArgs.builder().bucket(testBucket).build());
    }
    @Test
    void checkUploadingFile() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        ClassPathResource textFile = new ClassPathResource("test-files/test-file.txt");
        String textFileName = "test-file.txt";
        String textFileOriginalName = "test-file.txt";
        String textFileContentType = "text/plain";

        ClassPathResource imageFile = new ClassPathResource("test-files/test-image.png");
        String imageFileName = "test-image.png";
        String imageFileOriginalName = "test-image.png";
        String imageFileContentType = "image/png";

        MultipartFile file1 = new MockMultipartFile(textFileName, textFileOriginalName, textFileContentType,textFile.getInputStream());
        MultipartFile file2 = new MockMultipartFile(imageFileName, imageFileOriginalName, imageFileContentType, imageFile.getInputStream());

        fileService.uploadFile(file1, "/");
        fileService.uploadFile(file2, "/");

        StatObjectResponse textFileStats = client.statObject(StatObjectArgs.builder().bucket(testBucket).object(textFileName).build());
        assertEquals(textFileName, textFileStats.object());
        assertEquals(textFileContentType, textFileStats.contentType());

        StatObjectResponse imageFileStats = client.statObject(StatObjectArgs.builder().bucket(testBucket).object(imageFileName).build());
        assertEquals(imageFileName, imageFileStats.object());
        assertEquals(imageFileContentType, imageFileStats.contentType());
    }

    @Test
    void checkCreatingUserDir(){
    }

    @Test
    void checkCreatingEmptyDir() throws ServerException, InsufficientDataException, NoSuchFileException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        fileService.createDirectory("user-files-test/");
        for(Result<Item> result : client.listObjects(ListObjectsArgs.builder().bucket(testBucket).build())){
            Item item = result.get();
            System.out.println(item.objectName());
        }
        List<Blob> objs = fileService.getAllInDir("user-files-test/");
        System.out.println(objs.size());
    }

    @Test
    void checkRenamingDir(){

    }

    @Test
    void checkRenamingFile(){

    }

    @Test
    void checkDeletingFile(){

    }

}