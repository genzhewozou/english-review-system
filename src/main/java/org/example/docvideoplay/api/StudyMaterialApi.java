package org.example.docvideoplay.api;

import org.example.docvideoplay.dto.api.StudyMaterialParamsDto;
import org.example.docvideoplay.dto.api.StudyMaterialResultDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

/**
 * API interface for study material management operations
 * Handles file upload, retrieval, and listing of study materials
 */
@RequestMapping("/api/materials")
public interface StudyMaterialApi {
    
    /**
     * Upload a new study material file
     * 
     * @param file The multipart file to upload
     * @param params Study material parameters (title, type)
     * @return ResponseEntity containing the uploaded material details
     */
    @PostMapping(consumes = "multipart/form-data")
    ResponseEntity<StudyMaterialResultDto> uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute StudyMaterialParamsDto params
    );
    
    /**
     * Get all study materials
     * 
     * @return ResponseEntity containing list of all materials
     */
    @GetMapping
    ResponseEntity<List<StudyMaterialResultDto>> getAllMaterials();
    
    /**
     * Get a specific study material by ID
     * 
     * @param id The material ID
     * @return ResponseEntity containing the material details
     */
    @GetMapping("/{id}")
    ResponseEntity<StudyMaterialResultDto> getMaterial(@PathVariable Long id);
    
    /**
     * Delete a study material by ID
     * 
     * @param id The material ID to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteMaterial(@PathVariable Long id);
    
    /**
     * Download a material file
     * 
     * @param id The material ID
     * @return ResponseEntity with file content
     */
    @GetMapping("/{id}/download")
    ResponseEntity<org.springframework.core.io.Resource> downloadMaterial(@PathVariable Long id);
    
    /**
     * Get material content for viewing
     * 
     * @param id The material ID
     * @return ResponseEntity with file content URL or data
     */
    @GetMapping("/{id}/content")
    ResponseEntity<String> getMaterialContent(@PathVariable Long id);
    
    /**
     * Get text content of a document material for display and highlighting
     * 
     * @param id The material ID
     * @return ResponseEntity with the text content of the document
     */
    @GetMapping("/{id}/text")
    ResponseEntity<String> getMaterialTextContent(@PathVariable Long id);
}