package com.chit.api.controller;

import com.chit.api.enums.FileEnum;
import com.chit.api.response.model.FileResponse;
import com.chit.api.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.DeleteMapping;


@CrossOrigin
@RestController
@RequestMapping("/file")
@Slf4j
public class FileContoller {

  @Autowired
  private FileService fileService;

  @Operation(
      summary = "Upload File Service",
      description = "uploading files",
      tags = {"Uploading File API"},
      responses = {
          @ApiResponse(responseCode = "200", description = "file uploaded successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid file or empty")
      }

  )
  @PostMapping(value = "/uploadFiles", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<FileResponse> fileUpload(@RequestParam("file") MultipartFile file,
      @RequestParam(value = "encryption", defaultValue = "false") boolean encryption) {
    log.info("Received file upload request for file: {}", file.getOriginalFilename());
    FileResponse fileResponse = new FileResponse();
    try {
      fileService.uploadFile(file, encryption);
      log.info("File uploaded successfully: {}", file.getOriginalFilename());
      fileResponse.setMessage(FileEnum.FILE_SUCCESS.getEnumFileConstant());
      return new ResponseEntity<>(fileResponse, HttpStatus.OK);
    } catch (Exception e) {
      log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
      fileResponse.setMessage("File upload failed: " + e.getMessage());
      return new ResponseEntity<>(fileResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(
      summary = "Download File Service",
      description = "Download files",
      tags = {"Download File API"},
      responses = {
          @ApiResponse(responseCode = "200", description = "file Downloaded successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid filename or empty")
      }

  )
  @GetMapping(value = "/download/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + filename + "\"")
        .body(fileService.getDownloadFile(filename));
  }

  @Operation(
    summary = "Delete File Service",
    description = "Delete file by filename",
    tags = {"Delete File API"},
    responses = {
        @ApiResponse(responseCode = "200", description = "File deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid filename or file not found")
    }
)
@DeleteMapping(value = "/delete/{filename}", produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<FileResponse> deleteFile(@PathVariable String filename) {
    log.info("Received file delete request for filename: {}", filename);
    FileResponse fileResponse = new FileResponse();
    try {
        fileService.deleteFileByFilename(filename);
        log.info("File deleted successfully: {}", filename);
        fileResponse.setMessage(FileEnum.FILE_SUCCESS.getEnumFileConstant());
        return new ResponseEntity<>(fileResponse, HttpStatus.OK);
    } catch (Exception e) {
        log.error("Failed to delete file: {}", filename, e);
        fileResponse.setMessage("File deletion failed: " + e.getMessage());
        return new ResponseEntity<>(fileResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

  @Operation(
    summary = "Delete All Files Service",
    description = "Delete all files from the database and filesystem",
    tags = {"Delete All Files API"},
    responses = {
        @ApiResponse(responseCode = "200", description = "All files deleted successfully"),
        @ApiResponse(responseCode = "500", description = "Error occurred during deletion")
    }
)
@DeleteMapping(value = "/delete/all", produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<FileResponse> deleteAllFiles() {
    log.info("Received request to delete all files");
    FileResponse fileResponse = new FileResponse();
    try {
        fileService.deleteAllFiles();
        log.info("All files deleted successfully");
        fileResponse.setMessage(FileEnum.FILE_SUCCESS.getEnumFileConstant());
        return new ResponseEntity<>(fileResponse, HttpStatus.OK);
    } catch (Exception e) {
        log.error("Failed to delete all files", e);
        fileResponse.setMessage("File deletion failed: " + e.getMessage());
        return new ResponseEntity<>(fileResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


}
