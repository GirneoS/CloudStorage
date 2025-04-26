package com.ozhegov.cloudstorage.services;

import com.google.gson.Gson;
import com.ozhegov.cloudstorage.entity.Blob;
import com.ozhegov.cloudstorage.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileService {
    @Autowired
    FileRepository repository;
    public boolean deleteFile(String path){
    }
    public String getFile(String path) throws IllegalArgumentException{
        Blob blob = repository.getResourceByPath(path);
        Gson gson = new Gson();
        return gson.toJson(blob);
    }
}
