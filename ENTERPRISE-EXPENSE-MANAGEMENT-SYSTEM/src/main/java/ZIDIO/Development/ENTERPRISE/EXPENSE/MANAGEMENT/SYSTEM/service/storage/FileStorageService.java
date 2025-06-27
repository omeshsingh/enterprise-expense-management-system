package ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.service.storage;

import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.config.FileStorageProperties;
import ZIDIO.Development.ENTERPRISE.EXPENSE.MANAGEMENT.SYSTEM.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        logger.info("File storage location initialized to: {}", this.fileStorageLocation);

        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("Created file storage directory: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            logger.error("Could not create the directory where the uploaded files will be stored.", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not create the directory for uploaded files: " + ex.getMessage());
        }
    }

    public String storeFile(MultipartFile file, String subDirectory) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        logger.debug("Attempting to store file: {} in subdirectory: {}", originalFileName, subDirectory);

        try {
            if (originalFileName.contains("..")) {
                logger.warn("Filename contains invalid path sequence: {}", originalFileName);
                throw new ApiException(HttpStatus.BAD_REQUEST, "Filename contains invalid path sequence " + originalFileName);
            }

            String fileExtension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                fileExtension = originalFileName.substring(i);
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            Path targetDirectory = this.fileStorageLocation.resolve(subDirectory).normalize();
            if (!Files.exists(targetDirectory)) {
                Files.createDirectories(targetDirectory);
                logger.debug("Created target subdirectory: {}", targetDirectory);
            }


            Path targetLocation = targetDirectory.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Stored file {} as {}", originalFileName, targetLocation);

            // Return the relative path from the base upload directory
            return Paths.get(subDirectory).resolve(uniqueFileName).toString().replace("\\", "/");

        } catch (IOException ex) {
            logger.error("Could not store file {}. Please try again!", originalFileName, ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file " + originalFileName + ". Error: " + ex.getMessage());
        }
    }

    public Resource loadFileAsResource(String relativeFilePath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(relativeFilePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                logger.warn("File not found or not readable: {}", filePath);
                throw new ApiException(HttpStatus.NOT_FOUND, "File not found " + relativeFilePath);
            }
        } catch (MalformedURLException ex) {
            logger.error("File path malformed for: {}", relativeFilePath, ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "File path error for " + relativeFilePath + ": " + ex.getMessage());
        }
    }

    public void deleteFile(String relativeFilePath) {
        try {
            Path filePath = this.fileStorageLocation.resolve(relativeFilePath).normalize();
            Files.deleteIfExists(filePath);
            logger.info("Deleted file: {}", filePath);
        } catch (IOException ex) {
            logger.warn("Could not delete file: {} due to: {}", relativeFilePath, ex.getMessage());
            // Depending on requirements, you might not want to throw a critical error here
            // if the main operation (e.g., deleting an expense entity) was successful.
        }
    }
}