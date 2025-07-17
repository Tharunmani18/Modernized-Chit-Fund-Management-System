package com.chit.api.globalexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

  @ExceptionHandler(value = {ResourceNotFoundException.class})
  public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException resourceNotFound) {
    Exception resourceException = new Exception(
        resourceNotFound.getMessage()
    );

    return new ResponseEntity<>(resourceException, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(value = {BadRequestException.class})
  public ResponseEntity<Object> handleBadRequest(BadRequestException badRequest) {
    Exception badRequestException = new Exception(
        badRequest.getMessage()
    );

    return new ResponseEntity<>(badRequestException, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(value = {ResourceExistsException.class})
  public ResponseEntity<Object> handleBadRequest(ResourceExistsException resourceExists) {
    Exception resourceExistsException = new Exception(
        resourceExists.getMessage()
    );

    return new ResponseEntity<>(resourceExistsException, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(value = {ChitApiException.class})
  public ResponseEntity<Object> handleBadRequest(ChitApiException ChitApiExists) {
    Exception chitApiException = new Exception(
        ChitApiExists.getMessage()
    );

    return new ResponseEntity<>(chitApiException, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
