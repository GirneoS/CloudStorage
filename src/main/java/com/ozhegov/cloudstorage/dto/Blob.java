package com.ozhegov.cloudstorage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Blob {
    private String path;
    private String name;
    private long size;
    private String type;
}
