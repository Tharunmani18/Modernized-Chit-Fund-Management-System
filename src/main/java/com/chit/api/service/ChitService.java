package com.chit.api.service;

import com.chit.api.dao.ChitRepo;
import com.chit.api.dao.UserRepo;
import com.chit.api.dao.model.ChitDBModel;
import com.chit.api.dao.model.Slot;
import com.chit.api.dto.SubSlot;
import com.chit.api.enums.ChitEnum;
import com.chit.api.enums.UserEnum;
import com.chit.api.globalexceptions.BadRequestException;
import com.chit.api.globalexceptions.ResourceExistsException;
import com.chit.api.globalexceptions.ResourceNotFoundException;
import com.chit.api.request.model.ChitRequest;
import com.chit.api.request.model.LinkUserRequest;
import com.chit.api.sequence.SequenceService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChitService {

  @Autowired
  private ChitRepo chitRepo;

  @Autowired
  private SequenceService sequenceService;

  @Autowired
  private UserRepo userRepo;

  public long addChit(ChitRequest chitRequest) {
    log.info("Entered add chit service method...");
    if (chitRequest.getChitname() == null ||
        chitRequest.getTenure() == null ||
        chitRequest.getInstallment() == null ||
        chitRequest.getAmount() == null ||
        chitRequest.getStartDate() == null ||
        chitRequest.getEndDate() == null
    ) {
      log.error("ChitRequest validation failed: one or more required fields are missing or null");
      throw new BadRequestException(ChitEnum.CHIT_DETAILS.getEnumChitConstant());
    }
    ChitDBModel chitDBModel = new ChitDBModel();
    chitDBModel.setId(sequenceService.generateSequence(ChitDBModel.CHIT_SEQUENCE));
    chitDBModel.setChitname(chitRequest.getChitname());
    chitDBModel.setAmount(chitRequest.getAmount());
    chitDBModel.setTenure(chitRequest.getTenure());
    chitDBModel.setInstallment(chitRequest.getInstallment());
    chitDBModel.setStartDate(chitRequest.getStartDate());
    chitDBModel.setEndDate(chitRequest.getEndDate());
    chitDBModel.setBalanceAmount(chitRequest.getAmount());

    int maxSlots =
        Integer.parseInt(chitRequest.getAmount()) / Integer.parseInt(chitRequest.getInstallment());
    List<Slot> slotList = IntStream.range(1, maxSlots + 1)
        .mapToObj(i -> new Slot(i, chitRequest.getInstallment(), chitRequest.getInstallment()))
        .collect(Collectors.toCollection(ArrayList::new));
    chitDBModel.setSlots(slotList);
    ChitDBModel chitDBModelSaved = chitRepo.save(chitDBModel);
    log.info("chit added successfully with id:{}", chitDBModelSaved.getId());
    return chitDBModelSaved.getId();
  }

  public List<ChitDBModel> getAllChits() {
    log.info("Entered get all chits service method...");
    return chitRepo.findAll();
  }

  public long count() {
    log.info("Entered count chits service method...");
    return chitRepo.count();
  }

  public void getChitByName(String name) {
    log.info("Entered Get Chit By Name service method...");
    if (name == null || name.isEmpty()) {
      log.error("User name is empty or null");
      throw new BadRequestException(ChitEnum.CHIT_NAME_REQUIRED.getEnumChitConstant());
    }
    ChitDBModel chitDBModel = chitRepo.findByChitname(name);
    if (chitDBModel != null && chitDBModel.getChitname().equals(name)) {
      log.error("Chit with the given name already exists.");
      throw new BadRequestException(ChitEnum.CHIT_EXISTS.getEnumChitConstant());
    }
  }


  public void deleteChit() {
    log.info("Entered delete all chits service method...");
    chitRepo.deleteAll();
    log.info("Deleted all chits service method");
  }

  public void delete(String chitname) {
    log.info("Entered delete chit by name service method...");
    if (chitname.isEmpty()) {
      log.error("Chit name is empty");
      throw new BadRequestException(ChitEnum.CHIT_NAME_REQUIRED.getEnumChitConstant());
    }
    ChitDBModel chitDBModel = chitRepo.findByChitname(chitname);
    if (chitDBModel == null) {
      log.error("Chit not found with the provided name: {}", chitname);
      throw new ResourceExistsException(ChitEnum.CHIT_NAME_NOT_FOUND.getEnumChitConstant());
    } else {
      log.info("Deleted chit with name : {}", chitname);
      chitRepo.deleteByChitname(chitname);
    }
  }

  public Long linkUserToChit(LinkUserRequest linkUserRequest) {
    log.info("Entered link user to chit service method...");
    // Validate user number
    if (linkUserRequest.getUserNumber().isEmpty()) {
      log.error("User number is empty");
      throw new BadRequestException(UserEnum.USER_NUMBER.getEnumUserConstant());
    }
    if (linkUserRequest.getChitName().isEmpty()) {
      log.error("Chit name is empty");
      throw new BadRequestException(ChitEnum.CHIT_NAME_REQUIRED.getEnumChitConstant());
    }
    if (linkUserRequest.getSlot() < 0) {
      log.error("Given ChitID is negative");
      throw new BadRequestException(ChitEnum.CHIT_SLOT_ID.getEnumChitConstant());
    }
    if (linkUserRequest.getRequiredAmount() <= 0) {
      log.error("Required amount is less than or equal to zero");
      throw new BadRequestException(ChitEnum.CHIT_REMAINING_AMOUNT.getEnumChitConstant());
    }
    userRepo.findByNumber(linkUserRequest.getUserNumber())
        .orElseThrow(
            () -> new ResourceNotFoundException(UserEnum.USER_NOT_FOUND.getEnumUserConstant()));
    ChitDBModel chitDBModel = chitRepo.findByChitname(linkUserRequest.getChitName());
    if (chitDBModel == null) {
      log.error("Chit with the given name does not exist:{}", linkUserRequest.getChitName());
      throw new ResourceNotFoundException(ChitEnum.CHIT_NAME_NOT_FOUND.getEnumChitConstant());
    }
    List<Slot> slots = chitDBModel.getSlots();
    Slot slotToUpdate = slots.stream()
        .filter(slot -> slot.getSlotId() == linkUserRequest.getSlot())
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException(
            ChitEnum.CHIT_SLOT_NOT_FOUND.getEnumChitConstant()));
    int remainingAmount = Integer.parseInt(slotToUpdate.getRemainingAmounts());
    String newRemainingAmount = String.valueOf(
        remainingAmount - linkUserRequest.getRequiredAmount());
    if (!linkUserRequest.getSplit() && (Integer.parseInt(newRemainingAmount) < 0
        || linkUserRequest.getRequiredAmount() != Integer.parseInt(slotToUpdate.getSlotAmount()))) {
      log.error("Required amount invalid");
      throw new BadRequestException(ChitEnum.CHIT_REQUIRED_AMOUNT.getEnumChitConstant());
    }

    slotToUpdate.setRemainingAmounts(newRemainingAmount);
    chitDBModel.setBalanceAmount(String.valueOf(
        Integer.parseInt(chitDBModel.getBalanceAmount()) - linkUserRequest.getRequiredAmount()));
    slotToUpdate.setUser(linkUserRequest.getUserNumber());
    slotToUpdate.setSplit(linkUserRequest.getSplit());

    if (linkUserRequest.getSplit()) {
      List<SubSlot> subSlots = Optional.ofNullable(slotToUpdate.getSubSlots())
          .orElse(new ArrayList<>());

      int slotAmount = Integer.parseInt(slotToUpdate.getSlotAmount());
      int requiredAmount = linkUserRequest.getRequiredAmount();

      if (requiredAmount <= 0 || slotAmount <= 0 || requiredAmount % slotAmount != 0) {
        log.error("Invalid values for required amount or slot amount");
        throw new BadRequestException(ChitEnum.CHIT_REQUIRED_AMOUNT.getEnumChitConstant());
      }

      int numberOfSlots = requiredAmount / slotAmount;

      List<SubSlot> newSubSlots = IntStream.range(0, numberOfSlots + 1)
          .mapToObj(i -> {
            SubSlot subSlot = new SubSlot();
            subSlot.setSubSlotId(subSlots.size() + i + 1);
            subSlot.setSlotAmount(String.valueOf(linkUserRequest.getRequiredAmount()));
            subSlot.setUserNumber(linkUserRequest.getUserNumber());
            return subSlot;
          })
          .toList();

      subSlots.addAll(newSubSlots);
      slotToUpdate.setSubSlots(subSlots);
    }

    ChitDBModel chitDBModelSaved = chitRepo.save(chitDBModel);
    log.info("User linked to chit with id : {}", chitDBModelSaved.getId());
    return chitDBModelSaved.getId();
  }


}
