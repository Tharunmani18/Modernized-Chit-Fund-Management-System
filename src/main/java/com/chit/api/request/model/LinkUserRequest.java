package com.chit.api.request.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkUserRequest {

  private String userNumber;
  private String chitName;
  private Boolean split;
  private int slot;
  private int requiredAmount;
}
