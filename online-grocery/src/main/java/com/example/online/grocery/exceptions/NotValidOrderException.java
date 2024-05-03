package com.example.online.grocery.exceptions;

public class NotValidOrderException extends RuntimeException{
    public NotValidOrderException(String message) {
        super(message);
    }
}
