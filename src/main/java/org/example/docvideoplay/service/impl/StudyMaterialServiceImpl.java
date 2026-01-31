package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.config.FileStorageConfig;
import org.example.docvideoplay.dao.jpa.HighlightRepository;
import org.example.docvideoplay.dao.jpa.StudyMaterialRepository;
import org.example.docvideoplay.dao.jpa.TodoItemRepository;
import org.example.docvideoplay.entity.Highlight;
import org.example.docvideoplay.entity.StudyMaterial;
import org.example.docvideoplay.entity.TodoItem;
import org.example.docvideoplay.enums.MaterialType;
import org.example.docvideoplay.service.StudyMaterialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of StudyMaterialService for managing study materials.
 * Handles file upload, validation, storage, and retrieval operations.
 */
@Service
@Transactional
public class StudyMaterialServiceImpl implements StudyMaterialService {
    
    private static final Logger logger = LoggerFactory.getLogger(StudyMaterialServiceImpl.class);
    
    private final StudyMaterialRepository studyMaterialRepository;
    private final HighlightRepository highlightRepository;
    private final TodoItemRepository todoItemRepository;
    private final FileStorageConfig fileStorageConfig;
    
    @Autowired
    public StudyMaterialServiceImpl(StudyMaterialRepository studyMaterialRepository,
                                   HighlightRepository highlightRepository,
                                   TodoItemRepository todoItemRepository,
                                   FileStorageConfig fileStorageConfig) {
        this.studyMaterialRepository = studyMaterialRepository;
        this.highlightRepository = highlightRepository;
        this.todoItemRepository = todoItemRepository;
        this.fileStorageConfig = fileStorageConfig;
        initializeStorageDirectories();
    }
    
    /**
     * Initialize storage directories if they don't exist
     */
    private void initializeStorageDirectories() {
        try {
            Files.createDirectories(Paths.get(fileStorageConfig.getDocumentsPath()));
            Files.createDirectories(Paths.get(fileStorageConfig.getVideosPath()));
            Files.createDirectories(Paths.get(fileStorageConfig.getArticlesPath()));
            logger.info("Storage directories initialized successfully");
        } catch (IOException e) {
            logger.error("Failed to initialize storage directories", e);
            throw new RuntimeException("Failed to initialize storage directories", e);
        }
    }
    
    @Override
    public StudyMaterial uploadMaterial(MultipartFile file, String title, MaterialType type) throws IOException {
        // Validate input parameters
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        
        if (type == null) {
            throw new IllegalArgumentException("Material type cannot be null");
        }
        
        // Validate file type
        if (!isFileTypeAllowed(file, type)) {
            throw new IllegalArgumentException("File type not allowed for " + type + " materials");
        }
        
        // Generate unique file name to avoid conflicts
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String uniqueFileName = generateUniqueFileName(originalFileName, fileExtension);
        
        // Determine storage path based on material type
        String storagePath = getStoragePathForType(type);
        Path targetLocation = Paths.get(storagePath).resolve(uniqueFileName);
        
        try {
            // Copy file to target location
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File uploaded successfully: {}", targetLocation);
            
            // Create and save StudyMaterial entity
            StudyMaterial studyMaterial = new StudyMaterial();
            studyMaterial.setTitle(title);
            studyMaterial.setFileName(originalFileName);
            studyMaterial.setFilePath(targetLocation.toString());
            studyMaterial.setType(type);
            studyMaterial.setMimeType(file.getContentType());
            studyMaterial.setFileSize(file.getSize());
            
            StudyMaterial savedMaterial = studyMaterialRepository.save(studyMaterial);
            logger.info("StudyMaterial saved with ID: {}", savedMaterial.getId());
            
            return savedMaterial;
            
        } catch (IOException e) {
            logger.error("Failed to store file: {}", uniqueFileName, e);
            throw new IOException("Failed to store file: " + uniqueFileName, e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudyMaterial> getAllMaterials() {
        return studyMaterialRepository.findAllByOrderByCreatedDateDesc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudyMaterial> getMaterialsByType(MaterialType type) {
        if (type == null) {
            throw new IllegalArgumentException("Material type cannot be null");
        }
        return studyMaterialRepository.findByTypeOrderByCreatedDateDesc(type);
    }
    
    @Override
    @Transactional(readOnly = true)
    public StudyMaterial getMaterialById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        Optional<StudyMaterial> material = studyMaterialRepository.findById(id);
        if (!material.isPresent()) {
            throw new IllegalArgumentException("Study material not found with ID: " + id);
        }
        
        return material.get();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudyMaterial> searchMaterialsByTitle(String title) {
        if (!StringUtils.hasText(title)) {
            return getAllMaterials();
        }
        return studyMaterialRepository.findByTitleContainingIgnoreCase(title);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudyMaterial> searchMaterialsByFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return getAllMaterials();
        }
        return studyMaterialRepository.findByFileNameContainingIgnoreCase(fileName);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudyMaterial> getMaterialsWithHighlights() {
        return studyMaterialRepository.findMaterialsWithHighlights();
    }
    
    @Override
    public void deleteMaterial(Long id) throws IOException {
        if (id == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        StudyMaterial material = getMaterialById(id);
        
        try {
            // First, delete all todo items related to highlights of this material
            List<Highlight> highlights = highlightRepository.findByMaterialIdOrderByStartPositionAsc(id);
            for (Highlight highlight : highlights) {
                // Delete todo items related to this highlight
                List<TodoItem> todoItems = todoItemRepository.findByRelatedHighlightIdOrderByDueDateAsc(highlight.getId());
                for (TodoItem todoItem : todoItems) {
                    todoItemRepository.delete(todoItem);
                    logger.debug("Deleted todo item with ID: {} related to highlight: {}", todoItem.getId(), highlight.getId());
                }
            }
            
            // Delete the physical file
            Path filePath = Paths.get(material.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("File deleted: {}", filePath);
            }
            
            // Delete the database record (this will cascade delete highlights due to CascadeType.ALL)
            studyMaterialRepository.delete(material);
            logger.info("StudyMaterial deleted with ID: {}", id);
            
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", material.getFilePath(), e);
            throw new IOException("Failed to delete file: " + material.getFilePath(), e);
        }
    }
    
    @Override
    public boolean isFileTypeAllowed(MultipartFile file, MaterialType materialType) {
        if (file == null || materialType == null) {
            return false;
        }
        
        String fileName = file.getOriginalFilename();
        if (!StringUtils.hasText(fileName)) {
            return false;
        }
        
        String fileExtension = getFileExtension(fileName).toLowerCase();
        
        switch (materialType) {
            case DOCUMENT:
                return fileStorageConfig.getAllowedDocumentTypes().contains(fileExtension);
            case VIDEO:
                return fileStorageConfig.getAllowedVideoTypes().contains(fileExtension);
            case ARTICLE:
                return fileStorageConfig.getAllowedArticleTypes().contains(fileExtension);
            default:
                return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getCountByType(MaterialType type) {
        if (type == null) {
            throw new IllegalArgumentException("Material type cannot be null");
        }
        return studyMaterialRepository.countByType(type);
    }
    
    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1);
    }
    
    /**
     * Generate unique filename to avoid conflicts
     */
    private String generateUniqueFileName(String originalFileName, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String baseName = originalFileName;
        
        // Remove extension from base name if present
        if (StringUtils.hasText(extension)) {
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                baseName = originalFileName.substring(0, lastDotIndex);
            }
        }
        
        // Clean base name for file system compatibility
        baseName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        if (StringUtils.hasText(extension)) {
            return String.format("%s_%s_%s.%s", baseName, timestamp, uuid, extension);
        } else {
            return String.format("%s_%s_%s", baseName, timestamp, uuid);
        }
    }
    
    /**
     * Get storage path based on material type
     */
    private String getStoragePathForType(MaterialType type) {
        switch (type) {
            case DOCUMENT:
                return fileStorageConfig.getDocumentsPath();
            case VIDEO:
                return fileStorageConfig.getVideosPath();
            case ARTICLE:
                return fileStorageConfig.getArticlesPath();
            default:
                throw new IllegalArgumentException("Unknown material type: " + type);
        }
    }
    
    @Override
    public org.springframework.core.io.Resource loadFileAsResource(String filePath) throws IOException {
        try {
            Path file = Paths.get(filePath).normalize();
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(file.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IOException("File not found or not readable: " + filePath);
            }
        } catch (Exception e) {
            logger.error("Error loading file as resource: {}", filePath, e);
            throw new IOException("Could not load file: " + filePath, e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public String readTextContent(Long materialId) throws IOException {
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        StudyMaterial material = getMaterialById(materialId);
        
        // Only allow reading text content for document materials
        if (material.getType() != MaterialType.DOCUMENT) {
            throw new IllegalArgumentException("Text content can only be read from DOCUMENT materials");
        }
        
        // Check if it's a text-based file
        String fileName = material.getFileName().toLowerCase();
        if (!isTextFile(fileName)) {
            throw new IllegalArgumentException("Material is not a text-based document: " + fileName);
        }
        
        try {
            Path filePath = Paths.get(material.getFilePath());
            if (!Files.exists(filePath)) {
                throw new IOException("File not found: " + material.getFilePath());
            }
            
            // Read the file content as UTF-8 text
            byte[] bytes = Files.readAllBytes(filePath);
            String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            logger.debug("Read text content for material {}: {} characters", materialId, content.length());
            
            return content;
            
        } catch (IOException e) {
            logger.error("Failed to read text content for material {}: {}", materialId, e.getMessage(), e);
            throw new IOException("Failed to read text content: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if a file is a text-based document that can be displayed
     */
    private boolean isTextFile(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.equals("txt") || 
               extension.equals("md") || 
               extension.equals("text") ||
               extension.equals("log") ||
               extension.equals("csv") ||
               extension.equals("json") ||
               extension.equals("xml") ||
               extension.equals("html") ||
               extension.equals("htm");
    }
}