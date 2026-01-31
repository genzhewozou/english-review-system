package org.example.docvideoplay.service;

import org.example.docvideoplay.entity.StudyMaterial;
import org.example.docvideoplay.enums.MaterialType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Service for managing study materials including file upload, storage, and retrieval.
 * Handles documents, videos, and articles for the English learning system.
 */
public interface StudyMaterialService {
    
    /**
     * Upload and store a study material file with metadata.
     * Validates file type, size, and stores the file in the appropriate directory.
     * 
     * @param file The multipart file to upload
     * @param title The title for the study material
     * @param type The type of material (DOCUMENT, VIDEO, ARTICLE)
     * @return The saved StudyMaterial entity
     * @throws IOException if file storage fails
     * @throws IllegalArgumentException if file validation fails
     */
    StudyMaterial uploadMaterial(MultipartFile file, String title, MaterialType type) throws IOException;
    
    /**
     * Retrieve all study materials ordered by creation date (newest first).
     * 
     * @return List of all study materials
     */
    List<StudyMaterial> getAllMaterials();
    
    /**
     * Retrieve study materials by type ordered by creation date (newest first).
     * 
     * @param type The material type to filter by
     * @return List of materials of the specified type
     */
    List<StudyMaterial> getMaterialsByType(MaterialType type);
    
    /**
     * Retrieve a specific study material by ID.
     * 
     * @param id The ID of the study material
     * @return The study material entity
     * @throws IllegalArgumentException if material not found
     */
    StudyMaterial getMaterialById(Long id);
    
    /**
     * Search study materials by title (case insensitive).
     * 
     * @param title The search term for title
     * @return List of materials matching the title search
     */
    List<StudyMaterial> searchMaterialsByTitle(String title);
    
    /**
     * Search study materials by file name (case insensitive).
     * 
     * @param fileName The search term for file name
     * @return List of materials matching the file name search
     */
    List<StudyMaterial> searchMaterialsByFileName(String fileName);
    
    /**
     * Get materials that have highlights.
     * 
     * @return List of materials with at least one highlight
     */
    List<StudyMaterial> getMaterialsWithHighlights();
    
    /**
     * Delete a study material and its associated file.
     * 
     * @param id The ID of the study material to delete
     * @throws IllegalArgumentException if material not found
     * @throws IOException if file deletion fails
     */
    void deleteMaterial(Long id) throws IOException;
    
    /**
     * Validate if a file type is allowed for the specified material type.
     * 
     * @param file The file to validate
     * @param materialType The intended material type
     * @return true if file type is allowed, false otherwise
     */
    boolean isFileTypeAllowed(MultipartFile file, MaterialType materialType);
    
    /**
     * Get the count of materials by type.
     * 
     * @param type The material type
     * @return Count of materials of the specified type
     */
    long getCountByType(MaterialType type);
    
    /**
     * Load a file as a Spring Resource for download.
     * 
     * @param filePath The path to the file
     * @return The file as a Resource
     * @throws IOException if file cannot be loaded
     */
    org.springframework.core.io.Resource loadFileAsResource(String filePath) throws IOException;
    
    /**
     * Read the text content of a document material for display and highlighting.
     * Only works with text-based documents (txt, md, etc.).
     * 
     * @param materialId The ID of the study material
     * @return The text content of the document
     * @throws IOException if file cannot be read
     * @throws IllegalArgumentException if material not found or not a text document
     */
    String readTextContent(Long materialId) throws IOException;
}