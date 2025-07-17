package com.chit.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileEnum {

  FILE_SUCCESS("File is successfully uploaded"),
  FILE_SAME("same file name cannot be uploaded or file already present in database"),
  FILE_EMPTY("File cannot be empty"),
  FILE_NAME_EMPTY("FileName cannot be empty"),
  FILE_DIRECTORY("user.home"),
  FILE_ZIP(".zip"),
  FILE_TYPE("Invalid file type: Only .zip files are allowed. Received file"),
  FILE_NOT_FOUND("FileName not found..!"),
  FILE_RESOURCE_NOT_FOUND("Resource not found..!");

  private final String EnumFileConstant;
}
