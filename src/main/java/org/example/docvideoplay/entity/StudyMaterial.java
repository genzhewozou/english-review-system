package org.example.docvideoplay.entity;

import org.example.docvideoplay.enums.MaterialType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_materials")
public class StudyMaterial extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String filePath;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaterialType type;
    
    @Column
    private String mimeType;
    
    @Column
    private Long fileSize;
    
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Highlight> highlights = new ArrayList<>();
    
    // Constructors
    public StudyMaterial() {}
    
    public StudyMaterial(String title, String fileName, String filePath, MaterialType type) {
        this.title = title;
        this.fileName = fileName;
        this.filePath = filePath;
        this.type = type;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public MaterialType getType() {
        return type;
    }
    
    public void setType(MaterialType type) {
        this.type = type;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public List<Highlight> getHighlights() {
        return highlights;
    }
    
    public void setHighlights(List<Highlight> highlights) {
        this.highlights = highlights;
    }
    
    // Helper methods
    public void addHighlight(Highlight highlight) {
        highlights.add(highlight);
        highlight.setMaterial(this);
    }
    
    public void removeHighlight(Highlight highlight) {
        highlights.remove(highlight);
        highlight.setMaterial(null);
    }
}