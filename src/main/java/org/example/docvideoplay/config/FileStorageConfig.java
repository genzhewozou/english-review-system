package org.example.docvideoplay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageConfig {
    
    private String basePath = "./uploads/english-learning";
    private String documentsPath = "./uploads/english-learning/documents";
    private String videosPath = "./uploads/english-learning/videos";
    private String articlesPath = "./uploads/english-learning/articles";
    private String maxFileSize = "100MB";
    private List<String> allowedDocumentTypes = Arrays.asList("pdf", "doc", "docx", "txt", "rtf");
    private List<String> allowedVideoTypes = Arrays.asList("mp4", "avi", "mov", "wmv", "flv");
    private List<String> allowedArticleTypes = Arrays.asList("html", "htm", "md");
    
    // Getters and Setters
    public String getBasePath() {
        return basePath;
    }
    
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    
    public String getDocumentsPath() {
        return documentsPath;
    }
    
    public void setDocumentsPath(String documentsPath) {
        this.documentsPath = documentsPath;
    }
    
    public String getVideosPath() {
        return videosPath;
    }
    
    public void setVideosPath(String videosPath) {
        this.videosPath = videosPath;
    }
    
    public String getArticlesPath() {
        return articlesPath;
    }
    
    public void setArticlesPath(String articlesPath) {
        this.articlesPath = articlesPath;
    }
    
    public String getMaxFileSize() {
        return maxFileSize;
    }
    
    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
    
    public List<String> getAllowedDocumentTypes() {
        return allowedDocumentTypes;
    }
    
    public void setAllowedDocumentTypes(List<String> allowedDocumentTypes) {
        this.allowedDocumentTypes = allowedDocumentTypes;
    }
    
    public List<String> getAllowedVideoTypes() {
        return allowedVideoTypes;
    }
    
    public void setAllowedVideoTypes(List<String> allowedVideoTypes) {
        this.allowedVideoTypes = allowedVideoTypes;
    }
    
    public List<String> getAllowedArticleTypes() {
        return allowedArticleTypes;
    }
    
    public void setAllowedArticleTypes(List<String> allowedArticleTypes) {
        this.allowedArticleTypes = allowedArticleTypes;
    }
}