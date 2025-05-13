package com.ozhegov.cloudstorage.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class WrongCredentials extends Exception{
    public WrongCredentials(String message) {
        super(message);
    }
}
