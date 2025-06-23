package com.ozhegov.cloudstorage;

import com.ozhegov.cloudstorage.model.dto.Blob;

public class Utils {
    public static Blob convertToBlob(String fullPath, long size, boolean isDir){
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
