package com.chit.api.request.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChitRequest {

  private String chitname;
  private String amount;
  private String tenure;
  private String installment;
  private String startDate;
  private String endDate;
}
