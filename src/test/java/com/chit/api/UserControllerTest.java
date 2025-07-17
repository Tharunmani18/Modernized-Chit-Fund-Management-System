package com.chit.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chit.api.controller.UserController;
import com.chit.api.dao.model.UserDBModel;
import com.chit.api.enums.UserEnum;
import com.chit.api.request.model.LoginRequest;
import com.chit.api.request.model.UserDetailsRequest;
import com.chit.api.response.model.CountResponse;
import com.chit.api.response.model.LoginResponse;
import com.chit.api.response.model.PasswordResponse;
import com.chit.api.response.model.UserResponse;
import com.chit.api.service.UserService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(classes = UserController.class)
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Autowired
  private UserController userController;

  @MockBean
  private UserService userService;

  @Test
  void userLoginTest() {
    LoginResponse loginResponse = new LoginResponse();
    loginResponse.setId(Long.parseLong("1"));
    loginResponse.setToken("qweruijnbvdsaszxcgv");
    loginResponse.setFirstname("John");
    loginResponse.setLastname("Doe");
    loginResponse.setNumber("1234567890");
    loginResponse.setUsertype("admin");

    when(userService.login(anyString(), anyString())).thenReturn(loginResponse);

    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setNumber("1234567890");
    loginRequest.setPassword("password");
    ResponseEntity<?> responseEntity = userController.login(loginRequest);

    assertNotNull(responseEntity);
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertEquals(loginResponse, responseEntity.getBody());
  }

  @Test
  void addUserTest() {
    UserDetailsRequest userDetailsRequest = new UserDetailsRequest();
    userDetailsRequest.setNumber("1234567890");
    userDetailsRequest.setFirstname("John");
    userDetailsRequest.setLastname("Doe");
    userDetailsRequest.setUsertype("admin");
    when(userService.getUserByNumber(anyString())).thenReturn(false);
    when(userService.addUser(any(UserDetailsRequest.class))).thenReturn(1L);

    ResponseEntity<?> responseEntity = userController.addUser(userDetailsRequest);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    UserResponse userResponse = (UserResponse) responseEntity.getBody();
    assertThat(userResponse.getMessage()).isEqualTo(
        UserEnum.USER_CREATED.getEnumUserConstant().toString());
    assertThat(userResponse.getId()).isEqualTo(1L);

  }

  @Test
  void getUsersTest() {
    List<UserDBModel> userDBModels = new ArrayList<>();
    UserDBModel user1 = new UserDBModel();
    user1.setId(1L);
    user1.setNumber("1234567890");
    user1.setFirstname("John");
    user1.setLastname("Doe");
    user1.setUsertype("admin");

    UserDBModel user2 = new UserDBModel();
    user2.setId(2L);
    user2.setNumber("9876543210");
    user2.setFirstname("Jane");
    user2.setLastname("Smith");
    user2.setUsertype("user");

    userDBModels.add(user1);
    userDBModels.add(user2);
    when(userService.getUsers()).thenReturn(userDBModels);

    ResponseEntity<?> responseEntity = userController.getUsers();

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertEquals(userDBModels, responseEntity.getBody());
  }

  @Test
  void getCountTests() {
    when(userService.count()).thenReturn(5L);

    ResponseEntity<CountResponse> responseEntity = userController.getCount();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody().getCount()).isEqualTo(5L);
  }

  @Test
  void deleteUserTest() {
    UserDetailsRequest userDetailsRequest = new UserDetailsRequest();
    userDetailsRequest.setNumber("1234567890");

    ResponseEntity<UserResponse> responseEntity = userController.deleteUser(userDetailsRequest);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    UserResponse userResponse = responseEntity.getBody();
    assertEquals(UserEnum.USER_DELETED.getEnumUserConstant(), userResponse.getMessage());
    verify(userService).delete(userDetailsRequest.getNumber());
  }

  @Test
  void deleteAllUsersTest() {

    ResponseEntity<UserResponse> responseEntity = userController.deleteAllUsers();

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody().getMessage()).isEqualTo(
        UserEnum.USER_DELETED.getEnumUserConstant());
  }

  @Test
  void updatePasswordTest() {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setNumber("1234567890");
    loginRequest.setPassword("password");
    loginRequest.setNewpassword("newPassword");

    when(
        userService.updatePassword(eq("1234567890"), eq("password"), eq("newPassword"))).thenReturn(
        "1234567890");

    ResponseEntity<PasswordResponse> responseEntity = userController.updatePassword("1234567890",
        loginRequest);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertEquals(UserEnum.USER_PASSWORD_UPDATED.getEnumUserConstant(),
        responseEntity.getBody().getMessage());

  }
}
