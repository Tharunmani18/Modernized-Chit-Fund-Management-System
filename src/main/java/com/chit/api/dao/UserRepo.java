package com.chit.api.dao;

import com.chit.api.dao.model.UserDBModel;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends MongoRepository<UserDBModel, String> {

  UserDBModel findByNumberAndPassword(String number, String Password);

  Optional<UserDBModel> findByNumber(String number);

  void deleteByNumber(String number);
}