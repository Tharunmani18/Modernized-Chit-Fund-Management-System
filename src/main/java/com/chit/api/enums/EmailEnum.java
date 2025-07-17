package com.chit.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailEnum {

  MAIL_SENT("Mail Sent Successfully"),
  EMAIL_EMPTY("name, emailId, message, phoneNo and selected request cannot be empty"),
  EMAIL_INVALID("Invalid email address format."),
  EMAIL_NOT_FOUND("Record not found");

  private final String EnumEmailConstant;
}
