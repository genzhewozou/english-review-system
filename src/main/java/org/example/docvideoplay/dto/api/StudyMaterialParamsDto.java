package org.example.docvideoplay.dto.api;

import org.example.docvideoplay.enums.MaterialType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class StudyMaterialParamsDto {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotNull(message = "Material type is required")
    private MaterialType type;
    
    // Constructors
    public StudyMaterialParamsDto() {}
    
    public StudyMaterialParamsDto(String title, MaterialType type) {
        this.title = title;
        this.type = type;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public MaterialType getType() {
        return type;
    }
    
    public void setType(MaterialType type) {
        this.type = type;
    }
}