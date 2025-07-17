package com.chit.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChitEnum {

  CHIT_EMPTY("Empty chit"),
  CHIT_DETAILS("chit name, amount, tenure, installment, startDate, and endDate are required."),
  CHIT_EXISTS("chit name already exits"),
  CHIT_CREATED("Chit created successfully"),
  ALL_CHITS_DELETED("Deleted all Chit records"),
  CHIT_DELETED("Deleted Successfully"),
  CHIT_NAME_NOT_FOUND("Chit name not found"),
  CHIT_NAME_REQUIRED("chit name required"),
  CHIT_USER_LINKED("User linked Successfully"),
  CHIT_SLOTS_NOT_FOUND("slots are not found"),
  CHIT_SPLIT_FALSE("split should be false"),
  CHIT_SLOT_ID("slotId cannot be negative"),
  CHIT_SLOT_NOT_FOUND("slot not found"),
  CHIT_REQUIRED_AMOUNT("required amount invalid"),
  CHIT_REQUIRED("required amount cannot be greater then remaining amount"),
  CHIT_REMAINING_AMOUNT("required amount cannot be zero or negative"),
  CHIT_VALID("enter a valid required amount");

  private final String EnumChitConstant;
}
