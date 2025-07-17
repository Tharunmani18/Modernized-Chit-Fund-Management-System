package com.chit.api.service;

import com.chit.api.config.FileConfig;
import com.chit.api.dao.FileRepo;
import com.chit.api.dao.model.FileDBModel;
import com.chit.api.enums.FileEnum;
import com.chit.api.globalexceptions.BadRequestException;
import com.chit.api.globalexceptions.ChitApiException;
import com.chit.api.sequence.SequenceService;
import com.chit.api.utilities.EncryptionUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


@Service
public class FileService {

  private static final Logger log = LoggerFactory.getLogger(FileService.class);
  private final EncryptionUtils encryptionUtils;
  private final FileRepo fileRepo;
  private final FileConfig fileConfig;
  @Autowired
  private SequenceService sequenceService;

  public FileService(EncryptionUtils encryptionUtils, FileRepo fileRepo, FileConfig fileConfig) {
    this.encryptionUtils = encryptionUtils;
    this.fileRepo = fileRepo;
    this.fileConfig = fileConfig;
  }

  public void uploadFile(MultipartFile file, boolean encryption) {
    try {
      if (file == null || file.isEmpty()) {
        log.error("File cannot be empty");
        throw new BadRequestException(FileEnum.FILE_EMPTY.getEnumFileConstant());
      }

      String filename = file.getOriginalFilename();
      log.info("Received file upload request for file: {}", filename);

      String dirPath = fileConfig.getFilepath();
      log.info("File will be saved to directory: {}", dirPath);

      if (!filename.endsWith(FileEnum.FILE_ZIP.getEnumFileConstant())) {
        log.error("Invalid file type: Only .zip files are allowed. Received file: {}", filename);
        throw new BadRequestException(FileEnum.FILE_TYPE.getEnumFileConstant());
      }
      String filePath = dirPath + File.separator + filename;

      Files.copy(file.getInputStream(), Paths.get(filePath));
      log.info("File saved successfully: {}", filePath);

      String finalFilename = encryption ? encryptionUtils.encrypt(filename) : filename;
      String finalFilePath = encryption ? encryptionUtils.encrypt(filePath) : filePath;

      log.info("File and path processed: {} -> {}, {} -> {}", filename, finalFilename, filePath,
          finalFilePath);

      FileDBModel fileDBModel = new FileDBModel();
      fileDBModel.setId(sequenceService.generateSequence(FileDBModel.FILE_SEQUENCE));
      fileDBModel.setFilename(finalFilename);
      fileDBModel.setFilepath(finalFilePath);
      fileRepo.save(fileDBModel);
      log.info("File saved to database: Filename: {}, Filepath: {}", finalFilename, finalFilePath);

    } catch (BadRequestException e) {
      log.error("Bad request: {}", e.getMessage());
      throw new BadRequestException(FileEnum.FILE_TYPE.getEnumFileConstant());
    } catch (Exception e) {
      log.error("Unexpected error during file upload: {}", e.getMessage(), e);
      throw new ChitApiException(e.toString());
    }
  }

  public Resource getDownloadFile(String filename) {
    if (filename == null || filename.isEmpty()) {
      log.error("Filename cannot be empty");
      throw new BadRequestException(FileEnum.FILE_NAME_EMPTY.getEnumFileConstant());
    }

    FileDBModel fileDBModel;
    try {
      fileDBModel = fileRepo.findByFilename(filename)
          .orElseThrow(() -> {
            log.error("File not found: {}", filename);
            return new BadRequestException(FileEnum.FILE_NOT_FOUND.getEnumFileConstant());
          });

      Resource resource = new FileSystemResource(fileDBModel.getFilepath());
      if (!resource.exists()) {
        log.error("File not found at path: {}", fileDBModel.getFilepath());
        throw new BadRequestException(FileEnum.FILE_RESOURCE_NOT_FOUND.getEnumFileConstant());
      }

      return resource;

    } catch (BadRequestException badRequestException) {
      log.error("Bad request: {}", badRequestException.getMessage());
      throw badRequestException;
    } catch (Exception e) {
      log.error("Error occurred while trying to download file: {}", filename, e);
      throw new ChitApiException(e.toString());
    }
  }

  public void deleteFileByFilename(String filename) {
    if (filename == null || filename.isEmpty()) {
        log.error("Filename cannot be empty");
        throw new BadRequestException(FileEnum.FILE_NAME_EMPTY.getEnumFileConstant());
    }

    try {
        // Find the file in the database
        FileDBModel fileDBModel = fileRepo.findByFilename(filename)
            .orElseThrow(() -> {
                log.error("File not found: {}", filename);
                return new BadRequestException(FileEnum.FILE_NOT_FOUND.getEnumFileConstant());
            });

        // Delete the file from the filesystem
        File file = new File(fileDBModel.getFilepath());
        if (file.exists() && file.delete()) {
            log.info("File deleted from filesystem: {}", fileDBModel.getFilepath());
        } else {
            log.error("Failed to delete file from filesystem: {}", fileDBModel.getFilepath());
            throw new ChitApiException("Failed to delete file from filesystem.");
        }

        // Remove the file record from the database
        fileRepo.delete(fileDBModel);
        log.info("File record deleted from database: {}", filename);

    } catch (BadRequestException badRequestException) {
        log.error("Bad request: {}", badRequestException.getMessage());
        throw badRequestException;
    } catch (Exception e) {
        log.error("Error occurred while trying to delete file: {}", filename, e);
        throw new ChitApiException(e.toString());
    }
}

  public void deleteAllFiles() {
    try {
        // Retrieve all files from the database
        List<FileDBModel> allFiles = fileRepo.findAll();

        if (allFiles.isEmpty()) {
            log.info("No files to delete");
            return;
        }

        // Delete each file from the filesystem
        for (FileDBModel fileDBModel : allFiles) {
            File file = new File(fileDBModel.getFilepath());
            if (file.exists() && file.delete()) {
                log.info("Deleted file from filesystem: {}", file.getPath());
            } else {
                log.warn("Failed to delete file or file not found on filesystem: {}", file.getPath());
            }
        }

        // Delete all records from the database
        fileRepo.deleteAll();
        log.info("All file records deleted from database");

    } catch (Exception e) {
        log.error("Error occurred while trying to delete all files", e);
        throw new ChitApiException("Failed to delete all files: " + e.getMessage());
    }
}


}
