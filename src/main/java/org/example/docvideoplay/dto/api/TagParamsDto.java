package org.example.docvideoplay.dto.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class TagParamsDto {
    
    @NotBlank(message = "Tag name is required")
    @Size(max = 100, message = "Tag name must be at most 100 characters")
    private String name;
    
    @Size(max = 500, message = "Tag description must be at most 500 characters")
    private String description;
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
