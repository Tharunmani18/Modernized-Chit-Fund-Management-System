package com.chit.api.globalexceptions;

public class ChitApiException extends RuntimeException {

  public ChitApiException(String message) {
    super(message);
  }

  public ChitApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
