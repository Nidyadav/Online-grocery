package com.example.online.grocery.exceptions;

public class ItemNotFoundException extends  RuntimeException{
    public ItemNotFoundException(String message) {
        super(message);
    }
}
