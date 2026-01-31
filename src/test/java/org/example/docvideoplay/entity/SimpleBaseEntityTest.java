package org.example.docvideoplay.entity;

import org.example.docvideoplay.enums.MaterialType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple unit test for BaseEntity functionality to verify the setup works
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class SimpleBaseEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testBasicPersistence() {
        // Create a test material
        StudyMaterial material = new StudyMaterial("Test Title", "test.txt", "/uploads/test.txt", MaterialType.DOCUMENT);
        material.setMimeType("text/plain");
        material.setFileSize(1024L);
        
        // Manually set audit fields
        LocalDateTime now = LocalDateTime.now();
        material.setCreatedDate(now);
        material.setUpdatedDate(now);
        
        // Save and flush
        StudyMaterial saved = entityManager.persistAndFlush(material);
        
        // Verify ID was generated
        assertThat(saved.getId()).isNotNull();
        
        // Clear context
        entityManager.clear();
        
        // Retrieve and verify
        StudyMaterial retrieved = entityManager.find(StudyMaterial.class, saved.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getTitle()).isEqualTo("Test Title");
        assertThat(retrieved.getCreatedDate()).isNotNull();
        assertThat(retrieved.getUpdatedDate()).isNotNull();
    }
}