package com.chit.api.dao.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Setter
@Document("sequence")
public class Sequence {

  @Id
  private String id;
  private long seq;

}
