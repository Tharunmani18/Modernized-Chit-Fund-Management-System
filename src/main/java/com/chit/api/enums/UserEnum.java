package com.chit.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserEnum {

  USER_NUMBER("number cannot be empty in path variable"),
  USER_NUMBER_PASSWORD("Username and password cannot be empty"),
  USER_EMPTY("User Empty"),
  USER_NUMBER_PASSWORD_EMPTY("Number or password cannot be empty"),
  USER_NOT_FOUND("User not found with the provided number"),
  USER_INVALID("Invalid password"),
  USER_NUMBER_EMPTY("Number cannot be empty"),
  USER_DETAILS("Number, firstname, lastname, and usertype are required."),
  USER_EXISTS("Number already exists"),
  USER_CREATED("User created successfully"),
  USER_DELETED("Deleted Successfully"),
  USER_DEFAULT_PASSWORD("chit1234"),
  USER_INCORRECT_PASSWORD("Incorrect password"),
  USER_PASSWORD_UPDATED("Password Updated successfully"),
  USER_MAIL_SENT("Mail Sent Successfully"),
  USER_EMAIL_EMPTY("name, emailid, message, phoneno and selectedrequest connect be empty"),
  USER_EMAIL_INVALID("Invalid email address format.");


  private final String EnumUserConstant;
}
