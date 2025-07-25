package com.ozhegov.cloudstorage.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Message {
    @JsonProperty("message")
    private String message;
}
