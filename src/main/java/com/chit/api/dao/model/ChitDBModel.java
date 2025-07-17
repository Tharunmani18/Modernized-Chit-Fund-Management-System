package com.chit.api.dao.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("chit")
@Getter
@Setter
public class ChitDBModel {

  public static String CHIT_SEQUENCE = "chit";
  private Long id;
  private String chitname;
  private String amount;
  private String tenure;
  private String installment;
  private String startDate;
  private String endDate;
  private String balanceAmount;
  private List<Slot> slots;

}
