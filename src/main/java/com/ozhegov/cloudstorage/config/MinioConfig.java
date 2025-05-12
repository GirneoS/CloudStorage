package com.ozhegov.cloudstorage.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Value("${minio.url}")
    private String url;
    @Value("${minio.root-user}")
    private String rootUser;
    @Bean
    public MinioClient minioClient(){
        String rootPassword = System.getenv("MINIO_PASS");
        return MinioClient.builder()
                .endpoint(url)
                .credentials(rootUser,rootPassword)
                .build();
    }
}
