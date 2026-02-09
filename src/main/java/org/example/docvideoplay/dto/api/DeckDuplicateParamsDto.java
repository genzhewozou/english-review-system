package org.example.docvideoplay.dto.api;

import javax.validation.constraints.NotBlank;

public class DeckDuplicateParamsDto {
    
    @NotBlank(message = "New deck name is required")
    private String newName;
    
    // Constructors
    public DeckDuplicateParamsDto() {}
    
    public DeckDuplicateParamsDto(String newName) {
        this.newName = newName;
    }
    
    // Getters and Setters
    public String getNewName() {
        return newName;
    }
    
    public void setNewName(String newName) {
        this.newName = newName;
    }
}