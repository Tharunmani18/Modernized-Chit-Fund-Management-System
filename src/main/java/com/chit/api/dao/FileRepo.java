package com.chit.api.dao;

import com.chit.api.dao.model.FileDBModel;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepo extends MongoRepository<FileDBModel, String> {

  Optional<FileDBModel> findByFilename(String filename);
}