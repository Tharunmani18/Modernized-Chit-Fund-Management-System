package com.chit.api.dao.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("user")
public class UserDBModel {

  @Transient
  public static String USER_SEQUENCE = "user";

  @Id
  private long id;

  private String number;

  private String password;

  private String lastname;

  private String firstname;

  private String usertype;

  private boolean isDefault;

}
