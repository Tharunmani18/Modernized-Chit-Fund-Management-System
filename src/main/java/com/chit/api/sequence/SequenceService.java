package com.chit.api.sequence;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.chit.api.dao.model.Sequence;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SequenceService {

  public MongoOperations mongoOperations;

  @Autowired
  public SequenceService(MongoOperations mongoOperations) {
    this.mongoOperations = mongoOperations;
  }

  public long generateSequence(String seqName) {
    log.info("Entered generate sequence method...");
    Sequence counter = mongoOperations.findAndModify(query(where("id").is(seqName)),
        new Update().inc("seq", 1),
        options().returnNew(true).upsert(true), Sequence.class);
    return !Objects.isNull(counter) ? counter.getSeq() : 1;

  }
}
