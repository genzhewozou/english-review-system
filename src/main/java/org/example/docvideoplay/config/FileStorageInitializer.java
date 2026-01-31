package org.example.docvideoplay.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileStorageInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(FileStorageInitializer.class);
    
    @Autowired
    private FileStorageConfig fileStorageConfig;
    
    @Override
    public void run(String... args) throws Exception {
        createDirectoryIfNotExists(fileStorageConfig.getBasePath());
        createDirectoryIfNotExists(fileStorageConfig.getDocumentsPath());
        createDirectoryIfNotExists(fileStorageConfig.getVideosPath());
        createDirectoryIfNotExists(fileStorageConfig.getArticlesPath());
        
        logger.info("File storage directories initialized successfully");
    }
    
    private void createDirectoryIfNotExists(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            logger.info("Created directory: {}", directoryPath);
        } else {
            logger.debug("Directory already exists: {}", directoryPath);
        }
    }
}