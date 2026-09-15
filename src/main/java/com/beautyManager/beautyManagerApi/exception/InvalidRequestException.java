package com.beautyManager.beautyManagerApi.exception;

public class InvalidRequestException extends RuntimeException{
        public InvalidRequestException(String message){
            super(message);
        }

}
