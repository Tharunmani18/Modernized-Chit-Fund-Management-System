package com.chit.api.service;

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
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserService implements UserDetailsService {

  private final UserRepo userRepo;
  @Autowired
  private SequenceService sequenceService;
  @Autowired
  private JwtUtil jwtUtil;
  @Autowired
  private AuthenticationManager authenticationManager;
  @Autowired
  private PasswordEncoder passwordEncoder;

  public UserService(UserRepo userRepo) {
    this.userRepo = userRepo;
  }

  public LoginResponse login(String number, String password) {
    log.info("Entered Login service method...");
    if (number == null || number.isEmpty() || password == null || password.isEmpty()) {
      log.error("Number or password is null or empty");
      throw new BadRequestException(UserEnum.USER_NUMBER_PASSWORD.getEnumUserConstant());
    }
    Optional<UserDBModel> optionalUserDBModel = userRepo.findByNumber(number);
    if (!optionalUserDBModel.isPresent()) {
      log.error("User does not exist");
      throw new ResourceNotFoundException(UserEnum.USER_NOT_FOUND.getEnumUserConstant());
    }
    UserDBModel userDBModel = optionalUserDBModel.get();
    if (!passwordEncoder.matches(password, userDBModel.getPassword())) {
      log.error("Invalid password");
      throw new BadRequestException(UserEnum.USER_INVALID.getEnumUserConstant());
    }
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(number, password));
    final UserDetails userDetails = loadUserByUsername(number);
    LoginResponse loginResponse = new LoginResponse();
    loginResponse.setId(userDBModel.getId());
    loginResponse.setFirstname(userDBModel.getFirstname());
    loginResponse.setLastname(userDBModel.getLastname());
    loginResponse.setNumber(userDBModel.getNumber());
    loginResponse.setUsertype(userDBModel.getUsertype());
    loginResponse.setToken(jwtUtil.generateToken(userDetails.getUsername()));
    log.info("Exited Login service method[login with number:{}]", loginResponse.getNumber());
    return loginResponse;
  }


  @Override
  public UserDetails loadUserByUsername(String number) throws UsernameNotFoundException {
    log.info("Entered load User By Username service method...");
    UserDBModel userDBModel = userRepo.findByNumber(number)
        .orElseThrow(
            () -> new UsernameNotFoundException(UserEnum.USER_NOT_FOUND.getEnumUserConstant()));

    log.info("Exited load User By Username service method...");
    return org.springframework.security.core.userdetails.User
        .withUsername(userDBModel.getNumber())
        .password(userDBModel.getPassword())
        .authorities("USER")
        .build();
  }

  public boolean getUserByNumber(String number) {
    log.info("Entered get User By Number service method...");
    Optional<UserDBModel> userDBModel = userRepo.findByNumber(number);
    if (userDBModel.isPresent()) {
      log.info("User with number {} Exists", number);
      throw new ResourceExistsException(UserEnum.USER_EXISTS.getEnumUserConstant());
    }
    return false;
  }

  public long addUser(UserDetailsRequest userDetailsRequest) {
    log.info("Entered add User service method...");
    if (userDetailsRequest.getNumber() == null ||
        userDetailsRequest.getFirstname() == null ||
        userDetailsRequest.getLastname() == null ||
        userDetailsRequest.getUsertype() == null) {
      log.error("Invalid user details");
      throw new BadRequestException(UserEnum.USER_DETAILS.getEnumUserConstant());
    }
    UserDBModel userDBModel = new UserDBModel();
    String defaultPassword = UserEnum.USER_DEFAULT_PASSWORD.getEnumUserConstant();
    userDBModel.setId(sequenceService.generateSequence(UserDBModel.USER_SEQUENCE));
    userDBModel.setFirstname(userDetailsRequest.getFirstname());
    userDBModel.setLastname(userDetailsRequest.getLastname());
    userDBModel.setNumber(userDetailsRequest.getNumber());
    userDBModel.setDefault(true);
    String encodedPassword = passwordEncoder.encode(defaultPassword);
    userDBModel.setPassword(encodedPassword);
    userDBModel.setUsertype(userDetailsRequest.getUsertype());
    UserDBModel userDBModelSaved = userRepo.save(userDBModel);
    log.info("User added successfully with number: {} ", userDBModelSaved.getNumber());
    return userDBModelSaved.getId();
  }

  public List<UserDBModel> getUsers() {
    log.info("Entered get Users service method...");
    if (userRepo.findAll().isEmpty()) {
      log.info("No users found");
      throw new ResourceNotFoundException(UserEnum.USER_EMPTY.getEnumUserConstant());
    }
    log.info("Retrieved all the users");
    return userRepo.findAll();
  }

  public long count() {
    log.info("Entered count Users service method...");
    if (userRepo.count() == 0) {
      log.error("No users found in the database");
      throw new ResourceExistsException(UserEnum.USER_EMPTY.getEnumUserConstant());
    }
    log.info("Retrieved the count of users");
    return userRepo.count();
  }

  public void delete(String number) {
    log.info("Entered Delete service method...");
    if (number.isEmpty()) {
      log.error("User number is empty");
      throw new BadRequestException(UserEnum.USER_NUMBER_EMPTY.getEnumUserConstant());
    }
    Optional<UserDBModel> userDBModel = userRepo.findByNumber(number);
    if (userDBModel.isEmpty()) {
      log.error("User not found with the number:{}", number);
      throw new ResourceNotFoundException(UserEnum.USER_NOT_FOUND.getEnumUserConstant());
    } else {
      log.info("User deleted with the number:{}", number);
      userRepo.deleteByNumber(number);
    }
  }

  public void deleteUser() {
    log.info("Entered Delete All user service method...");
    userRepo.deleteAll();
    log.info("All users deleted");
  }

  public String updatePassword(String number, String password, String newPassword) {
    log.info("Entered Update Password service method...");
    if (number == null || number.isEmpty()) {
      log.error("User number is null or empty");
      throw new BadRequestException(UserEnum.USER_NUMBER.getEnumUserConstant());
    }
    if (password == null || password.isEmpty() || newPassword == null || newPassword.isEmpty()) {
      log.error("password or new password is null or empty");
      throw new BadRequestException(UserEnum.USER_NUMBER_PASSWORD_EMPTY.getEnumUserConstant());
    }
    Optional<UserDBModel> optionalUserDBModel = userRepo.findByNumber(number);
    if (optionalUserDBModel.isEmpty()) {
      log.error("User not found with number: {}", number);
      throw new ResourceNotFoundException(UserEnum.USER_NOT_FOUND.getEnumUserConstant());
    }

    UserDBModel userDBModel = optionalUserDBModel.get();
    if (!passwordEncoder.matches(password, userDBModel.getPassword())) {
      log.error("Invalid password");
      throw new BadRequestException(UserEnum.USER_INVALID.getEnumUserConstant());
    }
    String encodedPassword = passwordEncoder.encode(newPassword);
    userDBModel.setPassword(encodedPassword);
    userDBModel.setDefault(false);
    UserDBModel userDBModelSaved = userRepo.save(userDBModel);
    log.info("Password updated successfully for number: {}", userDBModel.getNumber());
    return userDBModelSaved.getNumber();
  }
}