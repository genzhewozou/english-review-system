package org.example.docvideoplay.dto.api;

public class DeckUpdateDto {
    
    private String name;
    private String description;
    private Boolean isPublic;
    
    // Constructors
    public DeckUpdateDto() {}
    
    public DeckUpdateDto(String name, String description, Boolean isPublic) {
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
    }
    
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
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
}