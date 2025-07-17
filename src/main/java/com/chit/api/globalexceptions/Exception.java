package com.chit.api.globalexceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Exception {

  private final String message;

  public Exception(String message) {
    this.message = message;

  }

}
