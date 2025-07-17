package com.chit.api.controller;

import com.chit.api.dao.model.UserDBModel;
import com.chit.api.enums.UserEnum;
import com.chit.api.globalexceptions.BadRequestException;
import com.chit.api.request.model.LoginRequest;
import com.chit.api.request.model.UserDetailsRequest;
import com.chit.api.response.model.CountResponse;
import com.chit.api.response.model.LoginResponse;
import com.chit.api.response.model.PasswordResponse;
import com.chit.api.response.model.UserResponse;
import com.chit.api.service.UserService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

  @Autowired
  private UserService userService;

  @Operation(
      summary = "User login",
      description = "Authenticate a user with a phone number and password",
      tags = {"User API"},
      responses = {
          @ApiResponse(responseCode = "200", description = "Login successful"),
          @ApiResponse(responseCode = "400", description = "Invalid credentials")
      }
  )
  @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
    log.info("Entered Login Controller method...");
    LoginResponse loginResponse = userService.login(loginRequest.getNumber(),
        loginRequest.getPassword());
    log.info("Logged in successfully with user number:{}", loginResponse.getNumber());
    return ResponseEntity.ok(loginResponse);
  }

  @Operation(
      summary = "Add a new user",
      description = "Register a new user with the provided details",
      tags = {"User API"},
      responses = {
          @ApiResponse(responseCode = "201", description = "User created successfully"),
          @ApiResponse(responseCode = "400", description = "User already exists")
      }
  )
  @PostMapping(path = "/addUser", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserResponse> addUser(@RequestBody UserDetailsRequest userDetailsRequest) {
    log.info("Entered add user controller method...");
    userService.getUserByNumber(userDetailsRequest.getNumber());
    UserResponse userResponse = new UserResponse();
    long userDBModelSaved = userService.addUser(userDetailsRequest);
    userResponse.setId(userDBModelSaved);
    userResponse.setMessage(UserEnum.USER_CREATED.getEnumUserConstant());
    log.info("User created successfully with number:{}", userDetailsRequest.getNumber());
    return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
  }

  @Operation(
      summary = "Get all users",
      description = "Retrieve a list of all registered users",
      tags = {"User API"},
      responses = {
          @ApiResponse(responseCode = "200", description = "Successfully retrieved users")
      }
  )
  @GetMapping(path = "/getUsers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<UserDBModel>> getUsers() {
    log.info("Entered get users controller method...");
    List<UserDBModel> userDBModel = userService.getUsers();
    log.info("Retrieved users successfully");
    return new ResponseEntity<>(userDBModel, HttpStatus.OK);
  }

  @Operation(
      summary = "Get user count",
      description = "Get the total number of registered users",
      tags = {"User API"},
      responses = {
          @ApiResponse(responseCode = "200", description = "Successfully retrieved user count")
      }
  )
  @GetMapping(path = "/getCount", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<CountResponse> getCount() {
    log.info("Entered get count controller method...");
    CountResponse countResponse = new CountResponse();
    long userCount = userService.count();
    countResponse.setCount(userCount);
    log.info("Retrieved user count successfully");
    return new ResponseEntity<>(countResponse, HttpStatus.OK);
  }

  @Operation(
      summary = "Delete a user",
      description = "Delete a specific user by their phone number",
      tags = {"User API"},
      responses = {
          @ApiResponse(responseCode = "200", description = "User deleted successfully"),
          @ApiResponse(responseCode = "404", description = "User not found")
      }
  )
  @DeleteMapping(path = "/deleteUser", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserResponse> deleteUser(
      @RequestBody UserDetailsRequest userDetailsRequest) {
    log.info("Entered delete user controller method...");
    userService.delete(userDetailsRequest.getNumber());
    UserResponse userResponse = new UserResponse();
    userResponse.setMessage(UserEnum.USER_DELETED.getEnumUserConstant());
    log.info("User with number {} deleted successfully", userDetailsRequest.getNumber());
    return new ResponseEntity<>(userResponse, HttpStatus.OK);
  }

  @Operation(
      summary = "Delete all users",
      description = "Delete all registered users",
      tags = {"User API"},
      responses = {
          @ApiResponse(responseCode = "200", description = "All users deleted successfully")
      }
  )
  @DeleteMapping(path = "/deleteUsers", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserResponse> deleteAllUsers() {
    log.info("Entered delete all users controller method...");
    userService.deleteUser();
    UserResponse userResponse = new UserResponse();
    userResponse.setMessage(UserEnum.USER_DELETED.getEnumUserConstant());
    log.info("All users deleted successfully");
    return new ResponseEntity<>(userResponse, HttpStatus.OK);
  }

  @Operation(
      summary = "Update user password",
      description = "Update the password of a user identified by their phone number",
      tags = {"User API"},
      responses = {
          @ApiResponse(responseCode = "200", description = "Password updated successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid phone number or password")
      }

  )
  @PostMapping(path = "/updatePassword/{number}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PasswordResponse> updatePassword(@PathVariable String number,
      @RequestBody LoginRequest loginRequest) {
    log.info("Entered update password controller method...");
    if (number == null || number.trim().isEmpty()) {
      log.error("User number not provided");
      throw new BadRequestException(UserEnum.USER_NUMBER.getEnumUserConstant());
    }
    String updatedNumber = userService.updatePassword(number, loginRequest.getPassword(),
        loginRequest.getNewpassword());
    PasswordResponse passwordResponse = new PasswordResponse();
    if (updatedNumber == null || updatedNumber.isEmpty()) {
      passwordResponse.setMessage(UserEnum.USER_INVALID.getEnumUserConstant());
      log.error("Invalid phone number or password");
      return new ResponseEntity<>(passwordResponse, HttpStatus.BAD_REQUEST);
    } else {
      passwordResponse.setMessage(UserEnum.USER_PASSWORD_UPDATED.getEnumUserConstant());
      log.info("Password updated successfully for user number:{}", updatedNumber);
      return new ResponseEntity<>(passwordResponse, HttpStatus.OK);
    }
  }
}
