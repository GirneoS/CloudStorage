package com.ozhegov.cloudstorage.model.exception;

public class FileIsAlreadyExistsException extends RuntimeException{
    public FileIsAlreadyExistsException(String message) {
        super(message);
    }
}
