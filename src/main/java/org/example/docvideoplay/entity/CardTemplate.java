package org.example.docvideoplay.entity;

import javax.persistence.*;

/**
 * Entity for card templates
 * Card templates define the structure and layout of different card types
 */
@Entity
@Table(name = "card_templates")
public class CardTemplate extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false, length = 50)
    private String templateType;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String frontTemplate;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String backTemplate;
    
    @Column(nullable = false)
    private Boolean isSystemTemplate = false;
    
    // Constructors
    public CardTemplate() {}
    
    public CardTemplate(Long userId, String name, String description, String templateType,
                      String frontTemplate, String backTemplate, Boolean isSystemTemplate) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.templateType = templateType;
        this.frontTemplate = frontTemplate;
        this.backTemplate = backTemplate;
        this.isSystemTemplate = isSystemTemplate != null ? isSystemTemplate : false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
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
    
    public String getTemplateType() {
        return templateType;
    }
    
    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }
    
    public String getFrontTemplate() {
        return frontTemplate;
    }
    
    public void setFrontTemplate(String frontTemplate) {
        this.frontTemplate = frontTemplate;
    }
    
    public String getBackTemplate() {
        return backTemplate;
    }
    
    public void setBackTemplate(String backTemplate) {
        this.backTemplate = backTemplate;
    }
    
    public Boolean getIsSystemTemplate() {
        return isSystemTemplate;
    }
    
    public void setIsSystemTemplate(Boolean isSystemTemplate) {
        this.isSystemTemplate = isSystemTemplate;
    }
}
