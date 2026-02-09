package org.example.docvideoplay.controller;

import org.example.docvideoplay.api.TagApi;
import org.example.docvideoplay.dto.api.TagParamsDto;
import org.example.docvideoplay.dto.api.TagResultDto;
import org.example.docvideoplay.dto.api.TagUpdateDto;
import org.example.docvideoplay.entity.Tag;
import org.example.docvideoplay.entity.User;
import org.example.docvideoplay.service.TagService;
import org.example.docvideoplay.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
public class TagController implements TagApi {
    
    @Autowired
    private TagService tagService;
    
    private TagResultDto mapToDto(Tag tag) {
        TagResultDto dto = new TagResultDto();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setDescription(tag.getDescription());
        dto.setIsActive(tag.getIsActive());
        dto.setCreatedAt(tag.getCreatedDate());
        dto.setUpdatedAt(tag.getUpdatedDate());
        return dto;
    }
    
    private Long getCurrentUserId() {
        User user = SecurityUtils.getCurrentUser();
        return user.getId();
    }
    
    @Override
    public ResponseEntity<TagResultDto> createTag(TagParamsDto params) {
        try {
            Long userId = getCurrentUserId();
            Tag tag = tagService.createTag(params.getName(), params.getDescription(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToDto(tag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @Override
    public ResponseEntity<List<TagResultDto>> getAllTags() {
        try {
            Long userId = getCurrentUserId();
            List<Tag> tags = tagService.getAllTags(userId);
            List<TagResultDto> dtos = tags.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @Override
    public ResponseEntity<TagResultDto> getTagById(Long id) {
        try {
            Long userId = getCurrentUserId();
            return tagService.getTagById(id, userId)
                    .map(tag -> ResponseEntity.ok(mapToDto(tag)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @Override
    public ResponseEntity<TagResultDto> updateTag(Long id, TagUpdateDto params) {
        try {
            Long userId = getCurrentUserId();
            Tag tag = tagService.updateTag(id, params.getName(), params.getDescription(), userId);
            return ResponseEntity.ok(mapToDto(tag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @Override
    public ResponseEntity<Void> deleteTag(Long id) {
        try {
            Long userId = getCurrentUserId();
            tagService.deleteTag(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<List<TagResultDto>> searchTags(String q) {
        try {
            Long userId = getCurrentUserId();
            List<Tag> tags = tagService.searchTags(q, userId);
            List<TagResultDto> dtos = tags.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @Override
    public ResponseEntity<Long> countTags() {
        try {
            Long userId = getCurrentUserId();
            long count = tagService.countTags(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
