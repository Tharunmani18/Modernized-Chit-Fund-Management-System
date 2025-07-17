package com.chit.api.dao;

import com.chit.api.dao.model.ChitDBModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChitRepo extends MongoRepository<ChitDBModel, String> {

  ChitDBModel findByChitname(String name);

  void deleteByChitname(String chitname);
}
