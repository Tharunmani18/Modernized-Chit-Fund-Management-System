package com.chit.api.dao.model;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("email")
public class EmailDBModel {

  @Transient
  public static String EMAIL_SEQUENCE = "email";

  @Id
  private long id;
  private String emailid;
  private String name;
  private String message;
  private String phoneno;
  private String selectedRequirenment;

}
