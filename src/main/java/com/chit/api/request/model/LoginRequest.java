package com.chit.api.request.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

  private String number;

  private String password;

  private String newpassword;

}
