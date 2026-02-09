package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.config.FileStorageConfig;
import org.example.docvideoplay.dao.jpa.CardRepository;
import org.example.docvideoplay.dao.jpa.StudyMaterialRepository;
import org.example.docvideoplay.dao.jpa.TodoItemRepository;
import org.example.docvideoplay.entity.Card;
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
import java.util.stream.Collectors;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;

/**
 * Implementation of StudyMaterialService for managing study materials.
 * Handles file upload, validation, storage, and retrieval operations.
 */
@Service
@Transactional
public class StudyMaterialServiceImpl implements StudyMaterialService {
    
    private static final Logger logger = LoggerFactory.getLogger(StudyMaterialServiceImpl.class);
    
    private final StudyMaterialRepository studyMaterialRepository;
    private final CardRepository cardRepository;
    private final TodoItemRepository todoItemRepository;
    private final FileStorageConfig fileStorageConfig;
    
    @Autowired
    public StudyMaterialServiceImpl(StudyMaterialRepository studyMaterialRepository,
                                   CardRepository cardRepository,
                                   TodoItemRepository todoItemRepository,
                                   FileStorageConfig fileStorageConfig) {
        this.studyMaterialRepository = studyMaterialRepository;
        this.cardRepository = cardRepository;
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
    public StudyMaterial uploadMaterial(MultipartFile file, String title, MaterialType type, Long userId) throws IOException {
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
        
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
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
            studyMaterial.setUserId(userId);
            
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
    public List<StudyMaterial> getMaterialsWithCards() {
        return studyMaterialRepository.findMaterialsWithCards();
    }
    
    @Override
    public void deleteMaterial(Long id) throws IOException {
        if (id == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        
        StudyMaterial material = getMaterialById(id);
        
        try {
            // First, delete all todo items related to cards of this material
            List<Card> cards = cardRepository.findByMaterialIdOrderByStartPositionAsc(id);
            for (Card card : cards) {
                // Delete todo items related to this card
                List<TodoItem> todoItems = todoItemRepository.findByRelatedCardIdOrderByDueDateAsc(card.getId());
                for (TodoItem todoItem : todoItems) {
                    todoItemRepository.delete(todoItem);
                    logger.debug("Deleted todo item with ID: {} related to card: {}", todoItem.getId(), card.getId());
                }
            }
            
            // Delete the physical file
            Path filePath = Paths.get(material.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("File deleted: {}", filePath);
            }
            
            // Delete the database record (this will cascade delete cards due to CascadeType.ALL)
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
    
    @Override
    @Transactional(readOnly = true)
    public List<StudyMaterial> getMaterialsByUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return studyMaterialRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudyMaterial> getMaterialsByUserAndType(Long userId, MaterialType type) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Material type cannot be null");
        }
        return studyMaterialRepository.findByUserId(userId)
                .stream()
                .filter(material -> material.getType() == type)
                .sorted((m1, m2) -> m2.getCreatedDate().compareTo(m1.getCreatedDate()))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public StudyMaterial getMaterialById(Long id, Long userId) {
        if (id == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        Optional<StudyMaterial> material = studyMaterialRepository.findById(id);
        if (!material.isPresent()) {
            throw new IllegalArgumentException("Study material not found with ID: " + id);
        }
        
        StudyMaterial studyMaterial = material.get();
        if (!studyMaterial.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Study material not found with ID: " + id);
        }
        
        return studyMaterial;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudyMaterial> searchMaterialsByUserAndTitle(Long userId, String title) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (!StringUtils.hasText(title)) {
            return getMaterialsByUser(userId);
        }
        return studyMaterialRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudyMaterial> getMaterialsWithCardsByUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return studyMaterialRepository.findMaterialsWithCardsByUserId(userId);
    }
    
    @Override
    public void deleteMaterial(Long id, Long userId) throws IOException {
        if (id == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        StudyMaterial material = getMaterialById(id, userId);
        
        try {
            // First, delete all todo items related to cards of this material
            List<Card> cards = cardRepository.findByMaterialIdOrderByStartPositionAsc(id);
            for (Card card : cards) {
                // Delete todo items related to this card
                List<TodoItem> todoItems = todoItemRepository.findByRelatedCardIdOrderByDueDateAsc(card.getId());
                for (TodoItem todoItem : todoItems) {
                    todoItemRepository.delete(todoItem);
                    logger.debug("Deleted todo item with ID: {} related to card: {}", todoItem.getId(), card.getId());
                }
            }
            
            // Delete the physical file
            Path filePath = Paths.get(material.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("File deleted: {}", filePath);
            }
            
            // Delete the database record (this will cascade delete cards due to CascadeType.ALL)
            studyMaterialRepository.delete(material);
            logger.info("StudyMaterial deleted with ID: {}", id);
            
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", material.getFilePath(), e);
            throw new IOException("Failed to delete file: " + material.getFilePath(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getCountByUserAndType(Long userId, MaterialType type) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Material type cannot be null");
        }
        return studyMaterialRepository.findByUserId(userId)
                .stream()
                .filter(material -> material.getType() == type)
                .count();
    }
    
    @Override
    @Transactional(readOnly = true)
    public String readTextContent(Long materialId, Long userId) throws IOException {
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        StudyMaterial material = getMaterialById(materialId, userId);
        
        // Only allow reading text content for document materials
        if (material.getType() != MaterialType.DOCUMENT) {
            throw new IllegalArgumentException("Text content can only be read from DOCUMENT materials");
        }
        
        // Check if it's a text-based file
        String fileName = material.getFileName().toLowerCase();
        if (!isTextFile(fileName)) {
            throw new IllegalArgumentException("Material is not a text-based document: " + fileName);
        }
        
        return readTextContentFromFile(material);
    }
    
    /**
     * Read text content from a document file
     */
    private String readTextContentFromFile(StudyMaterial material) throws IOException {
        try {
            Path filePath = Paths.get(material.getFilePath());
            if (!Files.exists(filePath)) {
                throw new IOException("File not found: " + material.getFilePath());
            }
            
            String extension = getFileExtension(material.getFileName().toLowerCase()).toLowerCase();
            String content;
            
            // Handle different document types
            if (extension.equals("docx")) {
                // Read DOCX file using POI
                try (XWPFDocument doc = new XWPFDocument(Files.newInputStream(filePath))) {
                    StringBuilder textBuilder = new StringBuilder();
                    for (XWPFParagraph paragraph : doc.getParagraphs()) {
                        textBuilder.append(paragraph.getText());
                        textBuilder.append("\n");
                    }
                    content = textBuilder.toString();
                }
            } else if (extension.equals("doc")) {
                // Read DOC file using POI
                try (HWPFDocument doc = new HWPFDocument(Files.newInputStream(filePath));
                     WordExtractor extractor = new WordExtractor(doc)) {
                    content = String.join("\n", extractor.getParagraphText());
                }
            } else if (extension.equals("pdf")) {
                // Read PDF file using PDFBox
                try (PDDocument doc = PDDocument.load(Files.newInputStream(filePath))) {
                    org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
                    content = stripper.getText(doc);
                }
            } else {
                // Use Tika for other document formats (rtf, odt, ods, odp, xls, xlsx, ppt, pptx)
                try (java.io.InputStream is = Files.newInputStream(filePath)) {
                    Tika tika = new Tika();
                    content = tika.parseToString(is);
                } catch (TikaException te) {
                    logger.warn("Tika failed to parse file, falling back to plain text: {}", te.getMessage());
                    // Fallback to plain text if Tika fails
                    byte[] bytes = Files.readAllBytes(filePath);
                    content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                }
            }
            
            logger.debug("Read text content for material {}: {} characters", material.getId(), content.length());
            
            return content;
            
        } catch (IOException e) {
            logger.error("Failed to read text content for material {}: {}", material.getId(), e.getMessage(), e);
            throw new IOException("Failed to read text content: " + e.getMessage(), e);
        }
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
            
            String extension = getFileExtension(fileName).toLowerCase();
            String content;
            
            // Handle different document types
            if (extension.equals("docx")) {
                // Read DOCX file using POI
                try (XWPFDocument doc = new XWPFDocument(Files.newInputStream(filePath))) {
                    StringBuilder textBuilder = new StringBuilder();
                    for (XWPFParagraph paragraph : doc.getParagraphs()) {
                        textBuilder.append(paragraph.getText());
                        textBuilder.append("\n");
                    }
                    content = textBuilder.toString();
                }
            } else if (extension.equals("doc")) {
                // Read DOC file using POI
                try (HWPFDocument doc = new HWPFDocument(Files.newInputStream(filePath));
                     WordExtractor extractor = new WordExtractor(doc)) {
                    content = String.join("\n", extractor.getParagraphText());
                }
            } else if (extension.equals("pdf")) {
                // Read PDF file using PDFBox
                try (PDDocument doc = PDDocument.load(Files.newInputStream(filePath))) {
                    org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
                    content = stripper.getText(doc);
                }
            } else {
                // Use Tika for other document formats (rtf, odt, ods, odp, xls, xlsx, ppt, pptx)
                try (java.io.InputStream is = Files.newInputStream(filePath)) {
                    Tika tika = new Tika();
                    content = tika.parseToString(is);
                } catch (TikaException te) {
                    logger.warn("Tika failed to parse file, falling back to plain text: {}", te.getMessage());
                    // Fallback to plain text if Tika fails
                    byte[] bytes = Files.readAllBytes(filePath);
                    content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                }
            }
            
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
               extension.equals("htm") ||
               extension.equals("doc") ||
               extension.equals("docx") ||
               extension.equals("pdf") ||
               extension.equals("rtf") ||
               extension.equals("odt") ||
               extension.equals("ods") ||
               extension.equals("odp") ||
               extension.equals("xls") ||
               extension.equals("xlsx") ||
               extension.equals("ppt") ||
               extension.equals("pptx");
    }
}