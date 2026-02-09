package org.example.docvideoplay.service.impl;

import org.example.docvideoplay.entity.Tag;
import org.example.docvideoplay.repository.TagRepository;
import org.example.docvideoplay.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TagServiceImpl implements TagService {
    
    @Autowired
    private TagRepository tagRepository;
    
    @Override
    @Transactional
    public Tag createTag(String name, String description, Long userId) {
        // Check if tag name already exists
        if (tagRepository.existsByNameAndUserId(name, userId)) {
            throw new IllegalArgumentException("Tag with this name already exists");
        }
        
        // Create new tag
        Tag tag = new Tag(userId, name, description);
        return tagRepository.save(tag);
    }
    
    @Override
    public List<Tag> getAllTags(Long userId) {
        return tagRepository.findByUserIdAndIsActiveTrue(userId);
    }
    
    @Override
    public Optional<Tag> getTagById(Long id, Long userId) {
        return tagRepository.findById(id)
                .filter(tag -> tag.getUserId().equals(userId) && tag.getIsActive());
    }
    
    @Override
    @Transactional
    public Tag updateTag(Long id, String name, String description, Long userId) {
        // Find tag
        Tag tag = tagRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId) && t.getIsActive())
                .orElseThrow(() -> new IllegalArgumentException("Tag not found or not active"));
        
        // Check if new name already exists (excluding current tag)
        if (!tag.getName().equals(name) && tagRepository.existsByNameAndUserIdAndIdNot(name, userId, id)) {
            throw new IllegalArgumentException("Tag with this name already exists");
        }
        
        // Update tag
        tag.setName(name);
        tag.setDescription(description);
        return tagRepository.save(tag);
    }
    
    @Override
    @Transactional
    public void deleteTag(Long id, Long userId) {
        // Find tag
        Tag tag = tagRepository.findById(id)
                .filter(t -> t.getUserId().equals(userId) && t.getIsActive())
                .orElseThrow(() -> new IllegalArgumentException("Tag not found or not active"));
        
        // Soft delete
        tag.setIsActive(false);
        tagRepository.save(tag);
    }
    
    @Override
    public List<Tag> searchTags(String query, Long userId) {
        return tagRepository.findByNameContainingAndUserIdAndIsActiveTrue(query, userId);
    }
    
    @Override
    public boolean tagNameExists(String name, Long userId, Long excludeId) {
        if (excludeId == null) {
            return tagRepository.existsByNameAndUserId(name, userId);
        } else {
            return tagRepository.existsByNameAndUserIdAndIdNot(name, userId, excludeId);
        }
    }
    
    @Override
    public long countTags(Long userId) {
        return tagRepository.countByUserIdAndIsActiveTrue(userId);
    }
}
