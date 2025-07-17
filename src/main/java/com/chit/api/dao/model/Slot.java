package com.chit.api.dao.model;

import com.chit.api.dto.SubSlot;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Slot {

  private int slotId;
  private String remainingAmounts;
  private Boolean split;
  private String slotAmount;
  private String user;

  private List<SubSlot> subSlots;

  public Slot(int slotId, String remainingAmounts, String slotAmount) {
    this.slotId = slotId;
    this.remainingAmounts = remainingAmounts;
    this.slotAmount = slotAmount;
  }
}
