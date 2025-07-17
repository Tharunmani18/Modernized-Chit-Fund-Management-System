package com.chit.api.dao;

import com.chit.api.dao.model.EmailDBModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepo extends MongoRepository<EmailDBModel, Long> {

}
