package com.chit.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResourceEnum {


  USER_NOT_FOUND("Empty chit"),

  ;
  // Getter method
  private final String EnumResourseConstant;
}
