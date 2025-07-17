package com.chit.api.request.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailsRequest {

  private String number;
  private String firstname;
  private String lastname;
  private String usertype;

}
