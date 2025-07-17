package com.chit.api.response.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

  private long id;
  private String token;
  private String number;
  private String firstname;
  private String lastname;
  private String usertype;
}
