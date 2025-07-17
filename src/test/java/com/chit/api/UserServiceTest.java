package com.chit.api;

import static com.chit.api.enums.UserEnum.USER_NUMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chit.api.dao.UserRepo;
import com.chit.api.dao.model.UserDBModel;
import com.chit.api.enums.UserEnum;
import com.chit.api.globalexceptions.BadRequestException;
import com.chit.api.globalexceptions.ResourceExistsException;
import com.chit.api.globalexceptions.ResourceNotFoundException;
import com.chit.api.jwtsecurity.JwtUtil;
import com.chit.api.request.model.UserDetailsRequest;
import com.chit.api.response.model.LoginResponse;
import com.chit.api.sequence.SequenceService;
import com.chit.api.service.UserService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(classes = UserService.class)
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @MockBean
  private SequenceService sequenceService;

  @MockBean
  private JwtUtil jwtUtil;

  @MockBean
  private AuthenticationManager authenticationManager;

  @MockBean
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UserService userService;

  @MockBean
  private UserRepo userRepo;

  @MockBean
  private UserDetails userDetails;

  @Captor
  private ArgumentCaptor<UserDBModel> userDBModelArgumentCaptor;

  @Test
  void ReturnUserDetailsWhenNumberIsFound() {
    UserDBModel userDBModel = new UserDBModel();
    userDBModel.setId(1L);
    userDBModel.setNumber("testNumber");
    userDBModel.setPassword("testPassword");
    userDBModel.setFirstname("testFirstname");
    userDBModel.setLastname("testLastname");
    userDBModel.setUsertype("USER");

    given(userRepo.findByNumber(anyString())).willReturn(Optional.of(userDBModel));

    UserDetails userDetails = userService.loadUserByUsername("testNumber");

    assertEquals("testNumber", userDetails.getUsername());
    assertEquals("testPassword", userDetails.getPassword());
    assertEquals("USER", userDetails.getAuthorities().iterator().next().getAuthority());
  }


  @Test
  void shouldReturnUserNotFoundTest() {
    String nonExistentUserNumber = "nonExistentUserNumber";
    when(userRepo.findByNumber(anyString())).thenReturn(Optional.empty());

    UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
        () -> userService.loadUserByUsername(nonExistentUserNumber));

    String expectedMessage = UserEnum.USER_NOT_FOUND.getEnumUserConstant();
    String actualMessage = exception.getMessage();
//        Removed library name from the below line
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  public void shouldReturnValidTokenWhenLoginIsSuccessful() {
    UserDBModel userDBModel = new UserDBModel();
    userDBModel.setId(1L);
    userDBModel.setFirstname("John");
    userDBModel.setLastname("Doe");
    userDBModel.setNumber("1234567890");
    userDBModel.setUsertype("USER");
    String encodedPassword = "encodedPassword";
    userDBModel.setPassword(encodedPassword);

    String number = "1234567890";
    String password = "password";
    when(userRepo.findByNumber(number)).thenReturn(Optional.of(userDBModel));
    when(passwordEncoder.matches(password, userDBModel.getPassword())).thenReturn(true);
    when(authenticationManager.authenticate(any())).thenReturn(null);
    when(jwtUtil.generateToken(number)).thenReturn("validToken");

    LoginResponse loginResponse = userService.login(number, password);

    assertEquals(1L, loginResponse.getId());
    assertEquals("John", loginResponse.getFirstname());
    assertEquals("Doe", loginResponse.getLastname());
    assertEquals("1234567890", loginResponse.getNumber());
    assertEquals("USER", loginResponse.getUsertype());
    assertEquals("validToken", loginResponse.getToken());
  }

  @Test
  public void shouldThrowBadRequestExceptionWhenNumberIsEmpty() {
    String number = "";
    String password = "password";

    assertThrows(BadRequestException.class, () -> userService.login(number, password));
  }

  @Test
  public void shouldThrowBadRequestExceptionWhenPasswordIsEmpty() {
    String number = "1234567890";
    String password = "";

    assertThrows(BadRequestException.class, () -> userService.login(number, password));
  }

  @Test
  public void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
    String number = "1234567890";
    String password = "password";
    when(userRepo.findByNumber(number)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> userService.login(number, password));
  }

  @Test
  public void shouldThrowBadRequestExceptionWhenPasswordIsInvalid() {
    UserDBModel userDBModel = new UserDBModel();
    userDBModel.setId(1L);
    userDBModel.setFirstname("John");
    userDBModel.setLastname("Doe");
    userDBModel.setNumber("1234567890");
    userDBModel.setUsertype("USER");
    String encodedPassword = "encodedPassword";
    userDBModel.setPassword(encodedPassword);

    String number = "1234567890";
    String password = "password";
    when(userRepo.findByNumber(number)).thenReturn(Optional.of(userDBModel));
    when(passwordEncoder.matches(password, userDBModel.getPassword())).thenReturn(false);

    assertThrows(BadRequestException.class, () -> userService.login(number, password));
  }

  @Test
  void shouldReturnUserExistIfNumberExists() {
    String existingNumber = "existingUserNumber";
    UserDBModel existingUser = new UserDBModel();
    existingUser.setNumber(existingNumber);
    when(userRepo.findByNumber(existingNumber)).thenReturn(Optional.of(existingUser));

    Throwable exception = assertThrows(ResourceExistsException.class,
        () -> userService.getUserByNumber(existingNumber));

    assertEquals(UserEnum.USER_EXISTS.getEnumUserConstant(), exception.getMessage());

  }

  @Test
  void addUserTest() {
    UserDetailsRequest userDetailsRequest = new UserDetailsRequest();
    userDetailsRequest.setNumber("testNumber");
    userDetailsRequest.setFirstname("testFirstname");
    userDetailsRequest.setLastname("testLastname");
    userDetailsRequest.setUsertype("USER");

    UserDBModel mockUserDBModel = new UserDBModel();
    mockUserDBModel.setId(1L);
    mockUserDBModel.setNumber("testNumber");
    mockUserDBModel.setFirstname("testFirstname");
    mockUserDBModel.setLastname("testLastname");
    mockUserDBModel.setUsertype("USER");
    mockUserDBModel.setPassword(
        passwordEncoder.encode(UserEnum.USER_DEFAULT_PASSWORD.getEnumUserConstant()));

    when(userRepo.save(any(UserDBModel.class))).thenReturn(mockUserDBModel);
    when(userRepo.findById(String.valueOf(1L))).thenReturn(Optional.of(mockUserDBModel));

    long userId = userService.addUser(userDetailsRequest);

    Optional<UserDBModel> userDBModelOptional = userRepo.findById(String.valueOf(userId));
    assertThat(userDBModelOptional).isPresent();
    UserDBModel userDBModel = userDBModelOptional.get();

    assertThat(userDBModel.getNumber()).isEqualTo("testNumber");
    assertThat(userDBModel.getFirstname()).isEqualTo("testFirstname");
    assertThat(userDBModel.getLastname()).isEqualTo("testLastname");
    assertThat(userDBModel.getUsertype()).isEqualTo("USER");
    assertThat(passwordEncoder.matches(UserEnum.USER_DEFAULT_PASSWORD.getEnumUserConstant(),
        userDBModel.getPassword())).isFalse();
  }

  @Test
  public void shouldReturnEmptyListWhenUserRepositoryIsEmpty() {
    when(userRepo.findAll()).thenReturn(Collections.emptyList());

    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
        () -> userService.getUsers());

    String expectedMessage = UserEnum.USER_EMPTY.getEnumUserConstant();
    String actualMessage = exception.getMessage();

    Assertions.assertEquals(expectedMessage, actualMessage);
  }

  @Test
  public void shouldReturnUserListWhenUserRepositoryContainsData() {
    UserDBModel user1 = new UserDBModel();
    user1.setId(1L);
    user1.setFirstname("John");
    user1.setLastname("Doe");
    user1.setNumber("123456");
    user1.setUsertype("ADMIN");

    UserDBModel user2 = new UserDBModel();
    user1.setId(2L);
    user1.setFirstname("John");
    user1.setLastname("Doe");
    user1.setNumber("123456");
    user1.setUsertype("ADMIN");

    List<UserDBModel> expectedUserList = List.of(user1, user2);
    when(userRepo.findAll()).thenReturn(expectedUserList);

    List<UserDBModel> actualUserList = userService.getUsers();

    assertThat(actualUserList).isEqualTo(expectedUserList);
  }

  @Test
  void count_shouldThrowResourceExistsException_whenUserRepoIsEmpty() {
    when(userRepo.count()).thenReturn(0L);

    Throwable exception = assertThrows(ResourceExistsException.class, () -> userService.count());

    assertThat(exception.getMessage()).isEqualTo(UserEnum.USER_EMPTY.getEnumUserConstant());
  }

  @Test
  public void shouldThrowResourceExistsExceptionWhenUserRepositoryIsEmpty() {
    when(userRepo.count()).thenReturn(0L);

    ResourceExistsException exception = assertThrows(ResourceExistsException.class,
        () -> userService.count());

    assertEquals(UserEnum.USER_EMPTY.getEnumUserConstant(), exception.getMessage());
  }

  @Test
  public void shouldDeleteUserWhenNumberExists() {
    String number = "1234567890";
    UserDBModel userDBModel = new UserDBModel();
    userDBModel.setNumber(number);

    when(userRepo.findByNumber(number)).thenReturn(Optional.of(userDBModel));

    userService.delete(number);

    verify(userRepo).deleteByNumber(number);
  }

  @Test
  public void shouldThrowBadRequestExceptionWhenInputNumberIsEmpty() {
    String emptyNumber = "";

    assertThrows(BadRequestException.class, () -> userService.delete(emptyNumber),
        UserEnum.USER_NUMBER_EMPTY.getEnumUserConstant());
  }

  @Test
  void shouldThrowResourceNotFoundExceptionWhenInputNumberDoesNotExist() {
    String nonExistingNumber = "non-existing-number";
    when(userRepo.findByNumber(nonExistingNumber)).thenReturn(Optional.empty());

    Throwable exception = assertThrows(ResourceNotFoundException.class,
        () -> userService.delete(nonExistingNumber));

    assertThat(exception.getMessage()).isEqualTo(UserEnum.USER_NOT_FOUND.getEnumUserConstant());
  }

  @Test
  void shouldRemoveAllUsers() {
    UserDBModel user1 = new UserDBModel();
    user1.setId(1L);
    user1.setFirstname("John");
    user1.setLastname("Doe");
    user1.setNumber("123456");
    user1.setUsertype("ADMIN");

    UserDBModel user2 = new UserDBModel();
    user1.setId(2L);
    user1.setFirstname("John");
    user1.setLastname("Doe");
    user1.setNumber("123456");
    user1.setUsertype("ADMIN");
    userRepo.save(user1);
    userRepo.save(user2);
    userService.deleteUser();

    long userCount = userRepo.count();
    assertThat(userCount).isEqualTo(0);
  }

  @Test
  void shouldUpdateUserPasswordWhenInputIsValid() {
    UserDBModel userDBModel = new UserDBModel();
    userDBModel.setId(1L);
    userDBModel.setNumber("1234567890");
    userDBModel.setPassword("password");
    userDBModel.setFirstname("John");
    userDBModel.setLastname("Doe");
    userDBModel.setUsertype("USER");

    String newPassword = "newPassword";
    String encodedNewPassword = "encodedNewPassword";

    when(userRepo.findByNumber(userDBModel.getNumber())).thenReturn(Optional.of(userDBModel));
    when(passwordEncoder.matches("password", userDBModel.getPassword())).thenReturn(true);
    when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

    when(userRepo.save(any(UserDBModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

    String updatedNumber = userService.updatePassword(userDBModel.getNumber(), "password",
        newPassword);

    assertEquals(userDBModel.getNumber(), updatedNumber);
    verify(userRepo).save(userDBModelArgumentCaptor.capture());
    UserDBModel capturedUserDBModel = userDBModelArgumentCaptor.getValue();
    assertEquals(encodedNewPassword, capturedUserDBModel.getPassword());
  }

  @Test
  public void shouldThrowBadRequestExceptionWhenNumberIsNull() {
    String number = null;
    String password = "password";
    String newPassword = "newPassword";

    assertThrows(BadRequestException.class,
        () -> userService.updatePassword(number, password, newPassword),
        USER_NUMBER.getEnumUserConstant());
  }

  @Test
  void shouldThrowExceptionWhenProvidedPasswordDoesNotMatchCurrentPassword() {

    final String USER_NUMBER = "1234567890";
    final String CURRENT_PASSWORD = "currentPassword";
    final String NEW_PASSWORD = "newPassword";
    UserDBModel userDBModel = new UserDBModel();
    userDBModel.setNumber(USER_NUMBER);
    userDBModel.setPassword(passwordEncoder.encode(CURRENT_PASSWORD));

    when(userRepo.findByNumber(USER_NUMBER)).thenReturn(Optional.of(userDBModel));
    when(passwordEncoder.matches(CURRENT_PASSWORD, userDBModel.getPassword())).thenReturn(false);

    Throwable exception = assertThrows(BadRequestException.class,
        () -> userService.updatePassword(USER_NUMBER, CURRENT_PASSWORD, NEW_PASSWORD));

    assertThat(exception.getMessage()).isEqualTo(UserEnum.USER_INVALID.getEnumUserConstant());
  }

  @Test
  void shouldThrowExceptionWhenNewPasswordIsEmpty() {
    String number = "testNumber";
    String password = "testPassword";
    String newPassword = "";
    UserDBModel userDBModel = new UserDBModel();
    userDBModel.setNumber(number);
    userDBModel.setPassword(passwordEncoder.encode(password));

    Optional<UserDBModel> optionalUserDBModel = Optional.of(userDBModel);
    when(userRepo.findByNumber(number)).thenReturn(optionalUserDBModel);

    Exception exception = assertThrows(BadRequestException.class, () -> {
      userService.updatePassword(number, password, newPassword);
    });

    String expectedMessage = UserEnum.USER_NUMBER_PASSWORD_EMPTY.getEnumUserConstant();
    assertEquals(expectedMessage, exception.getMessage());
  }


}
