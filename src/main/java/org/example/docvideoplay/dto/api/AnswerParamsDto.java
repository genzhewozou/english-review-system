package org.example.docvideoplay.dto.api;

import org.example.docvideoplay.enums.AnswerQuality;

import javax.validation.constraints.NotNull;

public class AnswerParamsDto {
    
    @NotNull(message = "Highlight ID is required")
    private Long highlightId;
    
    @NotNull(message = "Answer quality is required")
    private AnswerQuality quality;
    
    private Integer responseTimeSeconds;
    
    // Constructors
    public AnswerParamsDto() {}
    
    public AnswerParamsDto(Long highlightId, AnswerQuality quality, Integer responseTimeSeconds) {
        this.highlightId = highlightId;
        this.quality = quality;
        this.responseTimeSeconds = responseTimeSeconds;
    }
    
    // Getters and Setters
    public Long getHighlightId() {
        return highlightId;
    }
    
    public void setHighlightId(Long highlightId) {
        this.highlightId = highlightId;
    }
    
    public AnswerQuality getQuality() {
        return quality;
    }
    
    public void setQuality(AnswerQuality quality) {
        this.quality = quality;
    }
    
    public Integer getResponseTimeSeconds() {
        return responseTimeSeconds;
    }
    
    public void setResponseTimeSeconds(Integer responseTimeSeconds) {
        this.responseTimeSeconds = responseTimeSeconds;
    }
}