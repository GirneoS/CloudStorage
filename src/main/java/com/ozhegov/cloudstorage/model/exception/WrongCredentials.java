package com.ozhegov.cloudstorage.model.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class WrongCredentials extends Exception{
    public WrongCredentials(String message) {
        super(message);
    }
}
