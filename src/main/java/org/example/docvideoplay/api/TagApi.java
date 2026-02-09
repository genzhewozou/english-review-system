package org.example.docvideoplay.api;

import org.example.docvideoplay.dto.api.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/tags")
public interface TagApi {
    
    /**
     * Create a new tag
     * @param params Tag creation parameters
     * @return Created tag
     */
    @PostMapping
    ResponseEntity<TagResultDto> createTag(@RequestBody TagParamsDto params);
    
    /**
     * Get all tags for current user
     * @return List of tags
     */
    @GetMapping
    ResponseEntity<List<TagResultDto>> getAllTags();
    
    /**
     * Get a tag by ID
     * @param id Tag ID
     * @return Tag
     */
    @GetMapping("/{id}")
    ResponseEntity<TagResultDto> getTagById(@PathVariable Long id);
    
    /**
     * Update a tag
     * @param id Tag ID
     * @param params Tag update parameters
     * @return Updated tag
     */
    @PutMapping("/{id}")
    ResponseEntity<TagResultDto> updateTag(@PathVariable Long id, @RequestBody TagUpdateDto params);
    
    /**
     * Delete a tag
     * @param id Tag ID
     * @return Void
     */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteTag(@PathVariable Long id);
    
    /**
     * Search tags
     * @param q Search query
     * @return List of matching tags
     */
    @GetMapping("/search")
    ResponseEntity<List<TagResultDto>> searchTags(@RequestParam String q);
    
    /**
     * Count tags for current user
     * @return Tag count
     */
    @GetMapping("/count")
    ResponseEntity<Long> countTags();
}
