package com.chit.api.response.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetEmailResponse {

  private String emailid;
  private String name;
  private String message;
  private String phoneno;
  private String selectedRequirenment;
  
}