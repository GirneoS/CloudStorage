package com.ozhegov.cloudstorage.model.exception;

public class NoSuchFileException extends RuntimeException{
    public NoSuchFileException(String message) {
        super(message);
    }
}
