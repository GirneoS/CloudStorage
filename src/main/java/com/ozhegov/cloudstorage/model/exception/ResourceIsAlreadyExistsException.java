package com.ozhegov.cloudstorage.model.exception;

public class ResourceIsAlreadyExistsException extends RuntimeException{
    public ResourceIsAlreadyExistsException(String message) {
        super(message);
    }
}
