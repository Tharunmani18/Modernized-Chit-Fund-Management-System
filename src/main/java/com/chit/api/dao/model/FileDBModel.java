package com.chit.api.dao.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("files")
public class FileDBModel {

  @Transient
  public static String FILE_SEQUENCE = "files";

  @Id
  private long id;

  private String filename;

  private String filepath;

}