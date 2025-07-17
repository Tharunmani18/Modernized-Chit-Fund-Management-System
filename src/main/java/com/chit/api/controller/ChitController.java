package com.chit.api.controller;

import com.chit.api.dao.model.ChitDBModel;
import com.chit.api.enums.ChitEnum;
import com.chit.api.globalexceptions.ResourceNotFoundException;
import com.chit.api.request.model.ChitRequest;
import com.chit.api.request.model.LinkUserRequest;
import com.chit.api.response.model.ChitResponse;
import com.chit.api.response.model.CountResponse;
import com.chit.api.service.ChitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/chit")
@Slf4j
public class ChitController {

  @Autowired
  private ChitService chitService;

  @Operation(
      summary = "Add a new chit",
      description = "Create a new chit with the provided details",
      tags = {"Chit API"},
      responses = {
          @ApiResponse(responseCode = "201", description = "Chit created successfully"),
          @ApiResponse(responseCode = "400", description = "Bad request")
      }
  )
  @PostMapping(path = "/addChit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> addChit(@RequestBody ChitRequest chitRequest) {
    log.info("Entered add chits controller method...");
    chitService.getChitByName(chitRequest.getChitname());
    ChitResponse chitResponse = new ChitResponse();
    long chitDBModelSaved = chitService.addChit(chitRequest);
    chitResponse.setId(chitDBModelSaved);
    chitResponse.setMessage(ChitEnum.CHIT_CREATED.getEnumChitConstant());
    log.info("chit created successfully");
    return new ResponseEntity<>(chitResponse, HttpStatus.CREATED);
  }

  @Operation(
      summary = "Get all chits",
      description = "Retrieve a list of all chits",
      tags = {"Chit API"},
      responses = {
          @ApiResponse(responseCode = "200", description = "Success"),
          @ApiResponse(responseCode = "404", description = "No chits found")
      }
  )
  @GetMapping(path = "/getChits", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getChits() {
    log.info("Entered get chits controller method...");
    List<ChitDBModel> chitList = chitService.getAllChits();
    if (chitList.isEmpty()) {
      log.info("No chits found");
      throw new ResourceNotFoundException(ChitEnum.CHIT_EMPTY.getEnumChitConstant());
    }
    log.info("Chits found successfully");
    return new ResponseEntity<>(chitList, HttpStatus.OK);
  }

  @Operation(
      summary = "Get chit count",
      description = "Get the total number of chits",
      tags = {"Chit API"},
      responses = {
          @ApiResponse(responseCode = "200", description = "Success"),
          @ApiResponse(responseCode = "404", description = "No chits found")
      }
  )
  @GetMapping(path = "/getCount", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getCount() {
    log.info("Entered get count controller method...");
    CountResponse countResponse = new CountResponse();
    long chitCount = chitService.count();
    if (chitCount == 0) {
      log.info("No chits found");
      throw new ResourceNotFoundException(ChitEnum.CHIT_EMPTY.getEnumChitConstant());
    }
    countResponse.setCount(chitCount);
    log.info("Chit count found successfully");
    return new ResponseEntity<>(countResponse, HttpStatus.OK);
  }

  @Operation(
      summary = "Delete all chits",
      description = "Delete all chits from the database",
      tags = {"Chit API"},
      responses = {
          @ApiResponse(responseCode = "200", description = "All chits deleted successfully"),
          @ApiResponse(responseCode = "404", description = "No chits found")
      }
  )
  @GetMapping(path = "/deleteChits", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> deleteChits() {
    log.info("Entered delete chits controller method...");
    ChitResponse chitResponse = new ChitResponse();
    try {
      chitService.deleteChit();
      chitResponse.setMessage(ChitEnum.ALL_CHITS_DELETED.getEnumChitConstant());
      log.info("All chits deleted successfully");
    } catch (ResourceNotFoundException e) {
      chitResponse.setMessage(e.getMessage());
      log.info("No chits found");
      return new ResponseEntity<>(chitResponse, HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(chitResponse, HttpStatus.OK);
  }

  @Operation(
      summary = "Delete a chit",
      description = "Delete a specific chit by its name",
      tags = {"Chit API"},
      responses = {
          @ApiResponse(responseCode = "200", description = "Chit deleted successfully"),
          @ApiResponse(responseCode = "404", description = "Chit not found")
      }
  )
  @DeleteMapping(path = "/deleteChit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> deleteUser(@RequestBody ChitRequest chitRequest) {
    log.info("Entered delete user controller method...");
    chitService.delete(chitRequest.getChitname());
    ChitResponse chitResponse = new ChitResponse();
    chitResponse.setMessage(ChitEnum.CHIT_DELETED.getEnumChitConstant());
    log.info("Chit deleted successfully");
    return new ResponseEntity<>(chitResponse, HttpStatus.OK);
  }

  @Operation(
      summary = "Link chit to user",
      description = "linking user to chit slot",
      tags = {"Chit API"},
      responses = {
          @ApiResponse(responseCode = "200", description = "Updated Successfully"),
          @ApiResponse(responseCode = "404", description = "slot not found")
      }
  )
  @PostMapping(path = "/linkUser", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> linkUser(@RequestBody LinkUserRequest linkUserRequest) {
    log.info("Entered link user controller method...");
    long linkUser = chitService.linkUserToChit(linkUserRequest);
    ChitResponse chitResponse = new ChitResponse();
    chitResponse.setId(linkUser);
    chitResponse.setMessage(ChitEnum.CHIT_USER_LINKED.getEnumChitConstant());
    log.info("User linked successfully to chit slot", chitResponse);
    return new ResponseEntity<>(chitResponse, HttpStatus.OK);
  }
}
