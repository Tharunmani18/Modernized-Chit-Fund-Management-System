package com.chit.api.request.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailRequest {

  private String name;
  private String emailid;
  private String message;
  private String phoneno;
  private String selectedRequirenment;

}
