package com.assistia.exception;

public class SpeechRecognitionServiceException extends RuntimeException{
  public SpeechRecognitionServiceException(String message){
    super(message);
  }
}
