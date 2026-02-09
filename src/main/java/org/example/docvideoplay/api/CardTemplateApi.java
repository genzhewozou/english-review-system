package org.example.docvideoplay.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.example.docvideoplay.entity.CardTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * API interface for card template management operations
 */
@RequestMapping("/api/templates")
public interface CardTemplateApi {
    
    /**
     * Create a new card template
     * @param template The template to create
     * @return The created template
     */
    @Operation(
            summary = "Create card template",
            description = "Create a new card template",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Template created successfully",
                            content = @Content(schema = @Schema(implementation = CardTemplate.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid template data"
                    )
            }
    )
    @PostMapping
    ResponseEntity<CardTemplate> createTemplate(@Valid @RequestBody CardTemplate template);
    
    /**
     * Get all templates for the current user
     * @return List of templates
     */
    @Operation(
            summary = "Get all templates",
            description = "Get all templates for the current user, including system templates",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Templates retrieved successfully",
                            content = @Content(schema = @Schema(implementation = List.class))
                    )
            }
    )
    @GetMapping
    ResponseEntity<List<CardTemplate>> getTemplates();
    
    /**
     * Get a template by ID
     * @param id The template ID
     * @return The template
     */
    @Operation(
            summary = "Get template",
            description = "Get a template by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Template retrieved successfully",
                            content = @Content(schema = @Schema(implementation = CardTemplate.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Template not found"
                    )
            }
    )
    @GetMapping("/{id}")
    ResponseEntity<CardTemplate> getTemplate(@PathVariable Long id);
    
    /**
     * Update a template
     * @param id The template ID
     * @param template The updated template data
     * @return The updated template
     */
    @Operation(
            summary = "Update template",
            description = "Update an existing template",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Template updated successfully",
                            content = @Content(schema = @Schema(implementation = CardTemplate.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Template not found"
                    )
            }
    )
    @PutMapping("/{id}")
    ResponseEntity<CardTemplate> updateTemplate(@PathVariable Long id, @Valid @RequestBody CardTemplate template);
    
    /**
     * Delete a template
     * @param id The template ID
     */
    @Operation(
            summary = "Delete template",
            description = "Delete a template",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Template deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Template not found"
                    )
            }
    )
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteTemplate(@PathVariable Long id);
    
    /**
     * Get system templates
     * @return List of system templates
     */
    @Operation(
            summary = "Get system templates",
            description = "Get all system templates",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "System templates retrieved successfully",
                            content = @Content(schema = @Schema(implementation = List.class))
                    )
            }
    )
    @GetMapping("/system")
    ResponseEntity<List<CardTemplate>> getSystemTemplates();
    
    /**
     * Get templates by type
     * @param type The template type
     * @return List of templates
     */
    @Operation(
            summary = "Get templates by type",
            description = "Get templates by type for the current user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Templates retrieved successfully",
                            content = @Content(schema = @Schema(implementation = List.class))
                    )
            }
    )
    @GetMapping("/type/{type}")
    ResponseEntity<List<CardTemplate>> getTemplatesByType(@PathVariable String type);
    
    /**
     * Search templates
     * @param q The search query
     * @return List of matching templates
     */
    @Operation(
            summary = "Search templates",
            description = "Search templates by name",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Templates retrieved successfully",
                            content = @Content(schema = @Schema(implementation = List.class))
                    )
            }
    )
    @GetMapping("/search")
    ResponseEntity<List<CardTemplate>> searchTemplates(@RequestParam String q);
}
