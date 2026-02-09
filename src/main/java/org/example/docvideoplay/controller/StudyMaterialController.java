package org.example.docvideoplay.controller;

import org.example.docvideoplay.api.StudyMaterialApi;
import org.example.docvideoplay.dto.api.StudyMaterialParamsDto;
import org.example.docvideoplay.dto.api.StudyMaterialResultDto;
import org.example.docvideoplay.entity.StudyMaterial;
import org.example.docvideoplay.service.StudyMaterialService;
import org.example.docvideoplay.service.UserService;
import org.example.docvideoplay.service.VocabularyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for study material management operations.
 * Implements StudyMaterialApi interface for file upload, retrieval, and listing.
 */
@RestController
public class StudyMaterialController implements StudyMaterialApi {
    
    private static final Logger logger = LoggerFactory.getLogger(StudyMaterialController.class);
    
    private final StudyMaterialService studyMaterialService;
    private final UserService userService;
    private final VocabularyService vocabularyService;
    
    @Autowired
    public StudyMaterialController(StudyMaterialService studyMaterialService, UserService userService, VocabularyService vocabularyService) {
        this.studyMaterialService = studyMaterialService;
        this.userService = userService;
        this.vocabularyService = vocabularyService;
    }
    
    /**
     * Get the current authenticated user ID or default user ID
     * 
     * @return The current authenticated user ID or default user ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // If authenticated, get user by username
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            return userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username))
                    .getId();
        }
        
        // Default to user 'leo' if not authenticated
        return userService.findByUsername("leo")
                .orElseThrow(() -> new IllegalArgumentException("Default user not found"))
                .getId();
    }
    
    @Override
    public ResponseEntity<StudyMaterialResultDto> uploadMaterial(
            MultipartFile file, 
            @Valid StudyMaterialParamsDto params) {
        
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Uploading material: title={}, type={}, fileName={}, userId={}", 
                       params.getTitle(), params.getType(), file.getOriginalFilename(), currentUserId);
            
            // Validate file
            if (file.isEmpty()) {
                logger.warn("Upload failed: File is empty");
                return ResponseEntity.badRequest().build();
            }
            
            if (!studyMaterialService.isFileTypeAllowed(file, params.getType())) {
                logger.warn("Upload failed: File type not allowed for material type {}", params.getType());
                return ResponseEntity.badRequest().build();
            }
            
            // Upload material
            StudyMaterial material = studyMaterialService.uploadMaterial(
                file, params.getTitle(), params.getType(), currentUserId);
            
            StudyMaterialResultDto result = convertToResultDto(material);
            
            logger.info("Material uploaded successfully: id={}, userId={}", material.getId(), currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
            
        } catch (IOException e) {
            logger.error("File upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Upload validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error during upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<List<StudyMaterialResultDto>> getAllMaterials() {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving all study materials for userId: {}", currentUserId);
            
            List<StudyMaterial> materials = studyMaterialService.getMaterialsByUser(currentUserId);
            List<StudyMaterialResultDto> results = materials.stream()
                    .map(this::convertToResultDto)
                    .collect(Collectors.toList());
            
            logger.debug("Retrieved {} study materials for userId: {}", results.size(), currentUserId);
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            logger.error("Error retrieving materials: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<StudyMaterialResultDto> getMaterial(Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Retrieving study material: id={}, userId={}", id, currentUserId);
            
            StudyMaterial material = studyMaterialService.getMaterialById(id, currentUserId);
            StudyMaterialResultDto result = convertToResultDto(material);
            
            logger.debug("Retrieved study material: id={}, title={}, userId={}", id, material.getTitle(), currentUserId);
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Material not found: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving material {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<Void> deleteMaterial(Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.info("Deleting study material: id={}, userId={}", id, currentUserId);
            
            studyMaterialService.deleteMaterial(id, currentUserId);
            
            logger.info("Study material deleted successfully: id={}, userId={}", id, currentUserId);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.warn("Material not found for deletion: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            logger.error("File deletion failed for material {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Error deleting material {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Download a material file
     * 
     * @param id The material ID
     * @return ResponseEntity with file content
     */
    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadMaterial(@PathVariable Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Downloading material: id={}, userId={}", id, currentUserId);
            
            StudyMaterial material = studyMaterialService.getMaterialById(id, currentUserId);
            org.springframework.core.io.Resource resource = studyMaterialService.loadFileAsResource(material.getFilePath());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = material.getMimeType();
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                
                logger.debug("Downloaded material: id={}, filename={}, userId={}", id, material.getFileName(), currentUserId);
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                               "attachment; filename=\"" + material.getFileName() + "\"")
                        .body(resource);
            } else {
                logger.warn("Material file not found or not readable: id={}", id);
                return ResponseEntity.notFound().build();
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("Material not found: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error downloading material {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get material content for viewing
     * 
     * @param id The material ID
     * @return ResponseEntity with file content URL or data
     */
    @Override
    public ResponseEntity<String> getMaterialContent(@PathVariable Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Getting material content: id={}, userId={}", id, currentUserId);
            
            StudyMaterial material = studyMaterialService.getMaterialById(id, currentUserId);
            
            // For now, return the file path or a URL to access the content
            // In a production system, you might want to serve the actual file content
            // or return a signed URL for secure access
            String contentUrl = "/api/materials/" + id + "/download";
            
            logger.debug("Retrieved material content URL: id={}, url={}, userId={}", id, contentUrl, currentUserId);
            return ResponseEntity.ok(contentUrl);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Material not found: id={}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting material content {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get text content of a document material for display and highlighting
     * 
     * @param id The material ID
     * @return ResponseEntity with the text content of the document
     */
    @Override
    public ResponseEntity<String> getMaterialTextContent(@PathVariable Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            logger.debug("Getting material text content: id={}, userId={}", id, currentUserId);
            
            StudyMaterial material = studyMaterialService.getMaterialById(id, currentUserId);
            String textContent = studyMaterialService.readTextContent(id, currentUserId);
            
            logger.debug("Retrieved text content for material {}: {} characters, userId={}", id, textContent.length(), currentUserId);
            return ResponseEntity.ok(textContent);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Material not found or not a text document: id={}, error={}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            logger.error("Error reading text content for material {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Unexpected error getting text content for material {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Convert StudyMaterial entity to StudyMaterialResultDto
     * 
     * @param material The StudyMaterial entity
     * @return The converted DTO
     */
    private StudyMaterialResultDto convertToResultDto(StudyMaterial material) {
        StudyMaterialResultDto dto = new StudyMaterialResultDto();
        dto.setId(material.getId());
        dto.setTitle(material.getTitle());
        dto.setFileName(material.getFileName());
        dto.setType(material.getType());
        dto.setMimeType(material.getMimeType());
        dto.setFileSize(material.getFileSize());
        dto.setCreatedDate(material.getCreatedDate());
        dto.setUpdatedDate(material.getUpdatedDate());
        
        // Set card count using service method to avoid lazy loading issues
        long cardCount = vocabularyService.getCardCountByMaterial(material.getId());
        dto.setCardCount((int) cardCount);
        
        return dto;
    }
}