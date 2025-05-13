package com.ozhegov.cloudstorage.exception;

public class FileIsAlreadyExistsException extends RuntimeException{
    public FileIsAlreadyExistsException(String message) {
        super(message);
    }
}
