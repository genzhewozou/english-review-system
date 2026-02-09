package org.example.docvideoplay.config;

import org.example.docvideoplay.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initializes default user during application startup
 */
@Component
public class UserInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(UserInitializer.class);

    private final UserService userService;

    @Autowired
    public UserInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            logger.info("Initializing default user...");
            userService.initializeDefaultUser();
            logger.info("Default user initialization completed");
        } catch (Exception e) {
            logger.error("Error initializing default user: {}", e.getMessage());
        }
    }
}
