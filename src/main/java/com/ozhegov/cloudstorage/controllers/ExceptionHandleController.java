package com.ozhegov.cloudstorage.controllers;

import com.ozhegov.cloudstorage.model.dto.ErrorResponseDTO;
import com.ozhegov.cloudstorage.model.exception.NoSuchFileException;
import com.ozhegov.cloudstorage.model.exception.ResourceIsAlreadyExistsException;
import com.ozhegov.cloudstorage.model.exception.WrapperException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandleController {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDTO handleUserConflictException(RuntimeException e){
        return new ErrorResponseDTO("This name was taken", e.getMessage());
    }
    @ExceptionHandler(ResourceIsAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDTO handleResourceConflictException(RuntimeException e){
        return new ErrorResponseDTO("Resource is already exists", e.getMessage());
    }
    @ExceptionHandler(NoSuchFileException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNotFoundResource(RuntimeException e){
        return new ErrorResponseDTO("Resource was found", e.getMessage());
    }
    @ExceptionHandler(WrapperException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDTO handleServerError(RuntimeException e){
        return new ErrorResponseDTO("Server error", e.getMessage());
    }
}
